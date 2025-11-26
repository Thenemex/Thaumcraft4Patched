package thaumcraft4patched.api.golem;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;

public class FakePlayerGolem extends FakePlayer {

    public FakePlayerGolem(WorldServer world, GameProfile name) {
        super(world, name);
    }

    @Override
    public boolean canHarvestBlock(Block block) {
        return true;
    }

}
