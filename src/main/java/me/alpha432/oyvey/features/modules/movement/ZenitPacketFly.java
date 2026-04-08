package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;

public class ZenitPacketFly extends Module {
    public ZenitPacketFly() {
        super("ZenitPacketFly", "Safe fly to prevent fall damage", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            mc.player.fallDistance = 0.0f; // Resetear daño al encender
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;

        // Congelar el movimiento normal y resetear la caída en el cliente
        mc.player.setDeltaMovement(Vec3.ZERO);
        mc.player.fallDistance = 0.0f;

        double speed = 0.2;
        Vec3 direction = calculateDirection(speed);

        double targetX = mc.player.getX() + direction.x;
        double targetY = mc.player.getY();
        double targetZ = mc.player.getZ() + direction.z;

        if (mc.options.keyJump.isDown()) targetY += speed;
        if (mc.options.keyShift.isDown()) targetY -= speed;

        // Enviar nuestra posición exacta marcando 'true' (onGround)
        // Esto le dice al servidor que estamos apoyados en un bloque imaginario, anulando la caída.
        if (mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(targetX, targetY, targetZ, true, false));
        }

        // Mover al jugador físicamente en nuestra pantalla
        mc.player.setPos(targetX, targetY, targetZ);
    }

    private Vec3 calculateDirection(double speed) {
        float yaw = mc.player.getYRot();
        
        double forward = 0;
        double strafe = 0;

        if (mc.options.keyUp.isDown()) forward++;
        if (mc.options.keyDown.isDown()) forward--;
        if (mc.options.keyLeft.isDown()) strafe++;
        if (mc.options.keyRight.isDown()) strafe--;
        
        if (forward == 0 && strafe == 0) return Vec3.ZERO;
        
        double rad = Math.toRadians(yaw);
        double x = (forward * -Math.sin(rad) + strafe * Math.cos(rad)) * speed;
        double z = (forward * Math.cos(rad) - strafe * -Math.sin(rad)) * speed;
        
        return new Vec3(x, 0, z);
    }
}
