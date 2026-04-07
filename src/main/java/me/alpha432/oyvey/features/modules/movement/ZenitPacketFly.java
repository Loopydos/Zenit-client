package zenit.module.modules.movement;

import zenit.module.Module;
import zenit.module.Category;
import zenit.event.events.PacketEvent;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.world.phys.Vec3;

public class ZenitPacketFly extends Module {
    private int teleportId = 0;

    public ZenitPacketFly() {
        super("ZenitPacketFly", "Fly por desincronización de paquetes (Bypass Grim/Vulcan)", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            // Guardamos la posición inicial limpia
            teleportId = 0;
        }
    }

    @Override
    public void onUpdate() {
        if (nullCheck()) return;

        // 1. Congelamos la velocidad física del cliente local
        mc.player.setVelocity(0, 0, 0);

        // 2. Calculamos el vector hacia donde queremos ir basándonos en WASD
        Vec3 direction = calculateDirection(0.2); // 0.2 es la velocidad (ajustable)

        double targetX = mc.player.getX() + direction.x;
        double targetY = mc.player.getY();
        double targetZ = mc.player.getZ() + direction.z;

        // Altura (Espacio para subir, Shift para bajar)
        if (mc.options.jumpKey.isPressed()) targetY += 0.2;
        if (mc.options.sneakKey.isPressed()) targetY -= 0.2;

        // 3. Enviamos el paquete de posición "legítimo"
        sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(targetX, targetY, targetZ, true));

        // 4. Enviamos un paquete "Fuera de Límites" (Out of Bounds) para romper la predicción
        sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(targetX, targetY - 1337.0, targetZ, false));

        // 5. Actualizamos la posición local para que no te veas trabado
        mc.player.setPos(targetX, targetY, targetZ);
    }

    // Interceptamos los paquetes que RECIBIMOS del servidor (Rubberbands)
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket packet) {
            // El servidor intenta devolvernos a la posición original
            teleportId = packet.getTeleportId();
            
            // Confirmamos el teleport para que el servidor no nos expulse
            sendPacket(new TeleportConfirmC2SPacket(teleportId));
            
            // Cancelamos el evento para que nuestro cliente ignore la corrección visualmente
            event.cancel();
        }
    }

    // Interceptamos los paquetes que ENVIAMOS
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof PlayerMoveC2SPacket) {
            // Cancelamos los paquetes de movimiento estándar de Minecraft, 
            // ya que los estamos gestionando manualmente en onUpdate()
            event.cancel();
        }
    }

    // --- MÉTODOS UTILITARIOS ---

    private void sendPacket(net.minecraft.network.packet.Packet<?> packet) {
        if (mc.getConnection() != null) {
            mc.getConnection().send(packet);
        }
    }

    private Vec3 calculateDirection(double speed) {
        float yaw = mc.player.getYaw();
        double forward = mc.player.input.forwardImpulse;
        double strafe = mc.player.input.leftImpulse;
        
        if (forward == 0 && strafe == 0) return Vec3.ZERO;
        
        double cos = Math.cos(Math.toRadians(yaw + 90));
        double sin = Math.sin(Math.toRadians(yaw + 90));
        
        double x = (forward * cos + strafe * sin) * speed;
        double z = (forward * sin - strafe * cos) * speed;
        
        return new Vec3(x, 0, z);
    }
}
