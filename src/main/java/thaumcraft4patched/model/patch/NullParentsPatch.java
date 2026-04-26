package thaumcraft4patched.model.patch;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.world.WorldEvent;
import thaumcraft.api.research.ResearchCategoryList;
import thaumcraft.api.research.ResearchItem;

import static thaumcraft.api.research.ResearchCategories.researchCategories;
import static thaumcraft4patched.Thaumcraft4Patched.logger;

public class NullParentsPatch implements IPatch {

    protected boolean isPatched = false;
    protected int cpt = 0;

    public NullParentsPatch() {}

    @SubscribeEvent
    public void apply(WorldEvent.Load event) {
        if (event.world instanceof WorldServer && !isPatched) {
            // Iterating on all researchs
            for (ResearchCategoryList rl : researchCategories.values())
                for (ResearchItem ri : rl.research.values()) {
                    if (ri.parents == null) {
                        ri.parents = new String[0];
                        cpt++;
                    }
                    if (ri.parentsHidden == null) {
                        ri.parentsHidden = new String[0];
                        cpt++;
                    }
                }
            logger.info("Patched", cpt, "parents that were Null (world loading)");
            isPatched = true;
            cpt = 0;
        }
    }

    @SubscribeEvent
    public void reset(WorldEvent.Unload event) {
        isPatched = false;
    }
}
