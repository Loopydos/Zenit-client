package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.event.events.PacketEvent;
import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent; // Cambiar si tu base usa @EventHandler

public class ZenitPacketFly extends Module {
    private int teleportId = 0;

    public ZenitPacketFly() {
        // Ajustado al constructor de Oyvey: Nombre, Desc, Categoría, se activa al click, persistent, enabledByDefault
        super("ZenitPacketFly", "Fly por desincronización de paquetes", Category.MOVEMENT, true, false, false);
    }

    @Override
    public void onEnable() {
        teleportId = 0;
    }

    @Override
    public void onUpdate() {
        if (nullCheck()) return;

        // 1. Detener movimiento físico
        mc.player.setVelocity(0, 0, 0);

        // 2. Calcular dirección
        double speed = 0.2;
        Vec3 direction = calculateDirection(speed);

        double targetX = mc.player.getX() + direction.x;
        double targetY = mc.player.getY();
        double targetZ = mc.player.getZ() + direction.z;

        if (mc.options.jumpKey.isPressed()) targetY += speed;
        if (mc.options.sneakKey.isPressed()) targetY -= speed;

        // 3. Envío manual de paquetes (Usando la conexión del networkHandler)
        if (mc.getNetworkHandler() != null) {
            // Paquete real
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(targetX, targetY, targetZ, true));
            // Paquete de desincronización (Bounds)
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(targetX, targetY - 1337.0, targetZ, false));
        }

        // 4. Mover al jugador localmente
        mc.player.setPos(targetX, targetY, targetZ);
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket packet) {
            teleportId = packet.getTeleportId();
            if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().sendPacket(new TeleportConfirmC2SPacket(teleportId));
            }
            event.setCanceled(true); // Cancela la corrección del servidor (rubberband)
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof PlayerMoveC2SPacket) {
            // Cancelamos los paquetes automáticos para que solo se envíen los nuestros del onUpdate
            event.setCanceled(true);
        }
    }

    private Vec3 calculateDirection(double speed) {
        float yaw = mc.player.getYaw();
        // En 1.21.1, los inputs se manejan a través de player.input
        double forward = mc.player.input.forwardImpulse;
        double strafe = mc.player.input.leftImpulse;
        
        if (forward == 0 && strafe == 0) return Vec3.ZERO;
        
        double rad = Math.toRadians(yaw);
        double x = (forward * -Math.sin(rad) + strafe * Math.cos(rad)) * speed;
        double z = (forward * Math.cos(rad) - strafe * -Math.sin(rad)) * speed;
        
        return new Vec3(x, 0, z);
    }
}
