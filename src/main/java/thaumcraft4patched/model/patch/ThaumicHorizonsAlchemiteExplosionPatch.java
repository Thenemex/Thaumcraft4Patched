package thaumcraft4patched.model.patch;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import thaumcraft4patched.config.Config;

public final class ThaumicHorizonsAlchemiteExplosionPatch implements IPatch {

    private ThaumicHorizonsAlchemiteExplosionPatch() {}

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void onDetonate(
            World world,
            Explosion explosion,
            List<Entity> affectedEntities,
            List affectedBlocks,
            double diameter) {

        if (!Config.thEnabled
                || !Config.alchemiteExplosionClaimProtectionPatchEnabled) {

            return;
        }


        explosion.affectedBlockPositions = affectedBlocks;

        ForgeEventFactory.onExplosionDetonate(
                world,
                explosion,
                affectedEntities,
                diameter
        );
    }
}