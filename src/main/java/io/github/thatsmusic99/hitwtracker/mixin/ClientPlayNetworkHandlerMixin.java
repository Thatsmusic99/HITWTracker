package io.github.thatsmusic99.hitwtracker.mixin;

import io.github.thatsmusic99.hitwtracker.game.GameTracker;
import io.github.thatsmusic99.hitwtracker.util.interfaces.CobwebTracking;
import io.github.thatsmusic99.hitwtracker.util.interfaces.HotPotatoTracking;
import io.github.thatsmusic99.hitwtracker.util.interfaces.VelocityTracking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.regex.Pattern;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientPlayNetworkHandlerMixin.class.getName());
    private static final Pattern MAP = Pattern.compile(".*MAP: (\\w+).*");
    private static final Pattern HITW_TITLE = Pattern.compile("MCCI: HOLE IN THE WALL ");
    private static final Pattern HITW_PLOBBY_TITLE = Pattern.compile("MCCI: HOLE IN THE WALL {2}\\(Plobby\\)");
    private static final byte FIREWORK_EXPLODE_UPDATE = 17;

    @ModifyVariable(at = @At("TAIL"), method = "onScoreboardObjectiveUpdate", argsOnly = true)
    private ScoreboardObjectiveUpdateS2CPacket onUpdate(ScoreboardObjectiveUpdateS2CPacket packet) {

        // Ensure it's updating the title and that the result isn't empty
        if (packet.getMode() != 2) return packet;
        if (packet.getDisplayName().getString().isEmpty()) return packet;

        LOGGER.debug("Detected title: " + packet.getDisplayName().getString());

        if (HITW_TITLE.matcher(packet.getDisplayName().getString()).matches()) {
            GameTracker.confirm(false);
        } else if (HITW_PLOBBY_TITLE.matcher(packet.getDisplayName().getString()).matches()) {
            GameTracker.confirm(true);
        } else {
            GameTracker.reject();
        }
        return packet;
    }

    @ModifyVariable(at = @At("TAIL"), method = "onTeam", argsOnly = true)
    private TeamS2CPacket onTeam(TeamS2CPacket packet) {

        if (packet.getTeam().isPresent()) {
            var prefix = packet.getTeam().get().getPrefix();
            var matcher = MAP.matcher(prefix.getString());
            if (matcher.matches()) {
                GameTracker.setMap(matcher.group(1));
            }
        }

        return packet;
    }

    @ModifyVariable(at = @At("TAIL"), method = "onEntityVelocityUpdate", argsOnly = true)
    private EntityVelocityUpdateS2CPacket onVelocityUpdate(EntityVelocityUpdateS2CPacket packet) {

        if (MinecraftClient.getInstance().player.getId() == packet.getId()) {
            ((HotPotatoTracking) MinecraftClient.getInstance().player).onExplosionLaunch();
        }
        return packet;
    }

    @ModifyVariable(at = @At("TAIL"), method = "onExplosion", argsOnly = true)
    private ExplosionS2CPacket onExplosion(ExplosionS2CPacket packet) {
        if (!packet.getAffectedBlocks().isEmpty()) return packet;
        ((HotPotatoTracking) MinecraftClient.getInstance().player).onExplosionSound();
        return packet;
    }

    @ModifyVariable(at = @At("HEAD"), method = "onWorldBorderInitialize", argsOnly = true)
    private WorldBorderInitializeS2CPacket onBorderUpdate(WorldBorderInitializeS2CPacket packet) {

        if (MinecraftClient.getInstance().player == null) return packet;

        final PlayerEntity player = MinecraftClient.getInstance().player;
        final HotPotatoTracking playerPotato = ((HotPotatoTracking) player);

        // If there's considerably more warning blocks, then
        if (packet.getWarningTime() == 0) {
            playerPotato.onPotatoWarning();
        } else {
            playerPotato.coolPotatoWarning();
        }
        return packet;
    }

    @ModifyVariable(at = @At("TAIL"), method = "onEntityStatus", argsOnly = true)
    private EntityStatusS2CPacket onEntityUpdate(EntityStatusS2CPacket packet) {

        if (packet.getStatus() != FIREWORK_EXPLODE_UPDATE) return packet;
        if (MinecraftClient.getInstance().player == null) return packet;

        // Check the distance between the player and the firework
        final BlockPos playerPos = MinecraftClient.getInstance().player.getBlockPos();
        final Entity fireworkEntity = packet.getEntity(MinecraftClient.getInstance().player.getEntityWorld());
        if (!(fireworkEntity instanceof FireworkRocketEntity firework)) return packet;

        if (firework.getOwner() != MinecraftClient.getInstance().player && firework.getPos().distanceTo(playerPos.toCenterPos()) < 5) {
            ((VelocityTracking) MinecraftClient.getInstance().player).onBlastOff();
        }

        return packet;
    }

    @ModifyVariable(at = @At("TAIL"), method = "onScreenHandlerSlotUpdate", argsOnly = true)
    private ScreenHandlerSlotUpdateS2CPacket onSlotUpdate(ScreenHandlerSlotUpdateS2CPacket packet) {

        if (MinecraftClient.getInstance().player == null) return packet;

        if (packet.getStack().getItem().getTranslationKey().equals(Items.COBWEB.getTranslationKey())) {
            ((CobwebTracking) MinecraftClient.getInstance().player).onCobwebProvide();
        }

        return packet;
    }
}
