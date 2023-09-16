package io.github.thatsmusic99.hitwtracker.mixin;

import io.github.thatsmusic99.hitwtracker.game.GameTracker;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.regex.Pattern;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    private static final Logger logger = LoggerFactory.getLogger(ClientPlayNetworkHandlerMixin.class.getName());
    private static final Pattern MAP = Pattern.compile(".*MAP: (\\w+).*");
    private static final Pattern HITW_TITLE = Pattern.compile("MCCI: HOLE IN THE WALL ");

    @ModifyVariable(at = @At("TAIL"), method = "onScoreboardObjectiveUpdate", argsOnly = true)
    private ScoreboardObjectiveUpdateS2CPacket onUpdate(ScoreboardObjectiveUpdateS2CPacket packet) {

        // Ensure it's updating the title and that the result isn't empty
        if (packet.getMode() != 2) return packet;
        if (packet.getDisplayName().getString().isEmpty()) return packet;

        if (HITW_TITLE.matcher(packet.getDisplayName().getString()).matches()) {
            GameTracker.confirm();
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


}
