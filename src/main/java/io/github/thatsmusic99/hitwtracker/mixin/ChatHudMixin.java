package io.github.thatsmusic99.hitwtracker.mixin;

import io.github.thatsmusic99.hitwtracker.listener.ServerMessageListener;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatHud.class)
public class ChatHudMixin {

    @ModifyVariable(at = @At("TAIL"), method = "logChatMessage", argsOnly = true)
    public Text onChatMessage(Text message) {
        ServerMessageListener.checkMessage(message.getString());
        return message;
    }
}
