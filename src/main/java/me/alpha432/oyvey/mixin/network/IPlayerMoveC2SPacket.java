package me.alpha432.oyvey.mixin.network;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerboundMovePlayerPacket.class)
public interface IPlayerMoveC2SPacket {
    @Accessor("onGround")
    void setOnGround(boolean onGround);

    @Accessor("y")
    void setY(double y);

    @Accessor("x")
    void setX(double x);

    @Accessor("z")
    void setZ(double z);
}
