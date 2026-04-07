package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;

public class ZenitPacketFly extends Module {
    public ZenitPacketFly() {
        super("ZenitPacketFly", "Packet-based fly for desyncing movement", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;

        mc.player.setDeltaMovement(Vec3.ZERO);

        double speed = 0.2;
        Vec3 direction = calculateDirection(speed);

        double targetX = mc.player.getX() + direction.x;
        double targetY = mc.player.getY();
        double targetZ = mc.player.getZ() + direction.z;

        if (mc.options.keyJump.isDown()) targetY += speed;
        if (mc.options.keyShift.isDown()) targetY -= speed;

        if (mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(targetX, targetY, targetZ, true, false));
            mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(targetX, targetY - 1337.0, targetZ, false, false));
        }

        mc.player.setPos(targetX, targetY, targetZ);
    }

    private Vec3 calculateDirection(double speed) {
        float yaw = mc.player.getYRot();
        
        double forward = 0;
        double strafe = 0;

        if (mc.player.input.up) forward++;
        if (mc.player.input.down) forward--;
        if (mc.player.input.left) strafe++;
        if (mc.player.input.right) strafe--;
        
        if (forward == 0 && strafe == 0) return Vec3.ZERO;
        
        double rad = Math.toRadians(yaw);
        double x = (forward * -Math.sin(rad) + strafe * Math.cos(rad)) * speed;
        double z = (forward * Math.cos(rad) - strafe * -Math.sin(rad)) * speed;
        
        return new Vec3(x, 0, z);
    }
}
