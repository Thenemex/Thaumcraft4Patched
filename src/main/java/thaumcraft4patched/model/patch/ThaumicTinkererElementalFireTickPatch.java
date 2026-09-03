package thaumcraft4patched.model.patch;

import net.minecraft.world.GameRules;
import thaumcraft4patched.config.Config;

/**
 * Lets Thaumic Tinkerer's six elemental fires tick when doFireTick is off.
 *
 * BlockFireBase.updateTick returns at once when that game rule is off, so the
 * six fires never rain-extinguish or transmute neighbor blocks on servers that
 * disable vanilla fire spread. Vanilla fire lives in a different class and is
 * not touched.
 *
 * The transformer sends the doFireTick game rule read of that method to this
 * class. The read reports the rule as on when the patch is enabled, so the
 * transmutation logic still runs.
 */
public final class ThaumicTinkererElementalFireTickPatch implements IPatch {

    private ThaumicTinkererElementalFireTickPatch() {}

    public static boolean getGameRuleBooleanValue(GameRules rules, String rule) {
        if ("doFireTick".equals(rule)
                && Config.ttEnabled
                && Config.elementalFireIgnoresDoFireTickPatchEnabled) {
            return true;
        }
        return rules.getGameRuleBooleanValue(rule);
    }
}
