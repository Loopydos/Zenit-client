package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;

public class ZenitPacketFly extends Module {
    // Note: teleportId is initialized but not currently used in the logic below.
    // To fully handle rubberbands, a packet listener for ClientboundPlayerPositionPacket is required.
    private int teleportId = 0;

    public ZenitPacketFly() {
        super("ZenitPacketFly", "Packet-based fly for desyncing movement", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        teleportId = 0;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;

        // Stops natural gravity and movement momentum
        mc.player.setDeltaMovement(Vec3.ZERO);

        double speed = 0.2;
        Vec3 direction = calculateDirection(speed);

        double targetX = mc.player.getX() + direction.x;
        double targetY = mc.player.getY();
        double targetZ = mc.player.getZ() + direction.z;

        // Vertical movement handling
        if (mc.options.keyJump.isDown()) targetY += speed;
        if (mc.options.keyShift.isDown()) targetY -= speed;

        if (mc.getConnection() != null) {
            // Sends the main movement packet
            mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(targetX, targetY, targetZ, true));
            
            // Sends an out-of-bounds packet to confuse the anticheat (standard packet-fly technique)
            mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(targetX, targetY - 1337.0, targetZ, false));
        }

        // Sets client-side position to prevent stuttering
        mc.player.setPos(targetX, targetY, targetZ);
    }

    private Vec3 calculateDirection(double speed) {
        float yaw = mc.player.getYRot(); // Mojmap: getYRot() is used for yaw
        
        // In 1.21.1 Mojmaps, forward and left impulses are stored in forwardImpulse and leftImpulse
        double forward = mc.player.input.forwardImpulse;
        double strafe = mc.player.input.leftImpulse;
        
        if (forward == 0 && strafe == 0) return Vec3.ZERO;
        
        double rad = Math.toRadians(yaw);
        double x = (forward * -Math.sin(rad) + strafe * Math.cos(rad)) * speed;
        double z = (forward * Math.cos(rad) - strafe * -Math.sin(rad)) * speed;
        
        return new Vec3(x, 0, z);
    }
}
