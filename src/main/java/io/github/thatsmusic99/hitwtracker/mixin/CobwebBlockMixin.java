package io.github.thatsmusic99.hitwtracker.mixin;

import io.github.thatsmusic99.hitwtracker.util.interfaces.VelocityTracking;
import net.minecraft.block.BlockState;
import net.minecraft.block.CobwebBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CobwebBlock.class)
public class CobwebBlockMixin {

    @Inject(at = @At("TAIL"), method = "onEntityCollision")
    private void trackCollision(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (entity != MinecraftClient.getInstance().player) return;

        ((VelocityTracking) MinecraftClient.getInstance().player).onWeb();
    }
}
