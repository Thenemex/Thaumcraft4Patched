package thaumcraft4patched.model.patch;

import net.minecraft.util.IIcon;
import thaumcraft4patched.config.Config;

/**
 * Protects Magic Cookies' Golem Decoration icon lookup from invalid metadata.
 *
 * Valid decoration metadata is left completely unchanged. Invalid values use
 * the first registered decoration icon as a safe rendering fallback.
 */
public final class MagicCookiesGolemDecorationIconPatch {

    private MagicCookiesGolemDecorationIconPatch() {}

    public static IIcon getIcon(
            IIcon[] icons,
            int metadata) {

        if (!Config.mgckEnabled
                || !Config.golemDecorationIconBoundsPatchEnabled) {

            return icons[metadata];
        }

        if (icons == null || icons.length == 0) {
            return null;
        }

        if (metadata < 0 || metadata >= icons.length) {
            return icons[0];
        }

        return icons[metadata];
    }
}