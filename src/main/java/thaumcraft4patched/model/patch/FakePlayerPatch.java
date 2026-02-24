package thaumcraft4patched.model.patch;

import com.mojang.authlib.GameProfile;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.world.WorldEvent;
import thaumcraft.common.config.ConfigItems;
import thaumcraft4patched.api.golem.FakePlayerGolem;

import java.lang.reflect.Field;
import java.util.Map;

import static thaumcraft4patched.Thaumcraft4Patched.logger;

@SuppressWarnings("unchecked")
public class FakePlayerPatch implements IPatch {

    public static final GameProfile username = new GameProfile(null, "FakeThaumcraftGolem");
    protected static FakePlayer golem;

    protected Map<GameProfile, FakePlayer> fakePlayers;

    public FakePlayerPatch() {
        try {
            Field field = FakePlayerFactory.class.getDeclaredField("fakePlayers");
            field.setAccessible(true);
            fakePlayers = (Map<GameProfile, FakePlayer>) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {}
    }

    @SubscribeEvent
    public void apply(WorldEvent.Load event) {
        if (event.world instanceof WorldServer)
            if (!fakePlayers.containsKey(username)) {
                fakePlayers.put(username, new FakePlayerGolem((WorldServer) event.world, username));
                golem = fakePlayers.get(username);
                golem.inventory.addItemStackToInventory(new ItemStack(ConfigItems.itemAxeThaumium));
                if (golem.getHeldItem().getItem().equals(ConfigItems.itemAxeThaumium))
                    logger.info("Successfully patched golemLumberCoreWoodHardness bug !");
            } else if (!(fakePlayers.get(username) instanceof FakePlayerGolem))
                logger.info("Cannot apply FakePlayerGolem patch -> the fake player already exists");
    }
}
