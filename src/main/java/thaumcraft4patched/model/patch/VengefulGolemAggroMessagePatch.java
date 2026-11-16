package thaumcraft4patched.model.patch;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IChatComponent;
import thaumcraft4patched.config.Config;

public final class VengefulGolemAggroMessagePatch {

    private VengefulGolemAggroMessagePatch() {}

    public static void sendTaunt(EntityPlayer player, IChatComponent message) {
        if (!Config.tcclmEnabled || !Config.hideVengefulGolemAggroMessage) {
            player.addChatMessage(message);
        }
    }
}
