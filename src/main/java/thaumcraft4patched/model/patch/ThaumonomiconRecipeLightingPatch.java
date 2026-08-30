package thaumcraft4patched.model.patch;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import thaumcraft4patched.config.Config;


//This too wayyyy longer than expected, the cause was GL_RESCALE_NORMAL was set disabled through Angelica
// on some Thaumonomicon recipe renders, making 3D models light incorrectly and appear too dark.
// So this took me about 3 days because of how long it took to pinpoint the exact issue lol.


public final class ThaumonomiconRecipeLightingPatch {

    private ThaumonomiconRecipeLightingPatch() {
    }

    public static void renderItem(
            RenderItem renderer,
            FontRenderer fontRenderer,
            TextureManager textureManager,
            ItemStack stack,
            int x,
            int y) {

        if (!Config.thaumonomiconRecipeLightingPatchEnabled) {
            renderer.renderItemAndEffectIntoGUI(
                    fontRenderer,
                    textureManager,
                    stack,
                    x,
                    y
            );
            return;
        }

        boolean rescaleNormal =
                GL11.glIsEnabled(GL12.GL_RESCALE_NORMAL);

        GL11.glEnable(GL12.GL_RESCALE_NORMAL);

        try {
            renderer.renderItemAndEffectIntoGUI(
                    fontRenderer,
                    textureManager,
                    stack,
                    x,
                    y
            );
        } finally {
            if (rescaleNormal) {
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            } else {
                GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            }
        }
    }
}