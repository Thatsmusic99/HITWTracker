package io.github.thatsmusic99.hitwtracker.mixin;

import io.github.thatsmusic99.hitwtracker.util.interfaces.VelocityTracking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class EntityMixin {

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;onGround:Z", opcode = Opcodes.PUTFIELD), method = "move")
    public void onSetGround(Entity entity, boolean onGround) {
        entity.setOnGround(onGround);
        if (MinecraftClient.getInstance().player == null) return;
        if (((Entity) (Object) this).getId() == MinecraftClient.getInstance().player.getId()) {
            ((VelocityTracking) MinecraftClient.getInstance().player).gameTracker$onGround(onGround);
        }
    }
}
