package thaumcraft4patched.model.patch;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import nemexlib.api.thaumcraft.API;
import net.minecraftforge.event.world.WorldEvent;

public class WitchingGadgetsResearchPatch implements IPatch {

    protected final String tab = "WITCHGADG", tag = "WGBAUBLES";

    @SubscribeEvent
    public void apply(WorldEvent.Load event) {
        // Thaumium, Infusion, Primal Arrows
        API.addParents(tab, tag, false, (String) null);
        API.addParents(tab, tag, true, "THAUMIUM", "INFUSION", "PRIMALARROW");
    }
}
