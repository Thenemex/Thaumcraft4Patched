package thaumcraft4patched.model.patch;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import tschallacka.magiccookies.entities.living.ExtendedPlayer;
import tschallacka.magiccookies.items.StuffLoader;
import tschallacka.magiccookies.potions.MagicPotionHandler;
import thaumcraft4patched.config.Config;

import java.nio.FloatBuffer;

@SideOnly(Side.CLIENT)
public class MagicCookiesFogPatch implements IPatch {

    private static final float[] ENTROPY_FOG_DENSITIES = {
            0.010F,
            0.016F,
            0.023F,
            0.031F,
            0.040F,
            0.045F,
            0.050F,
            0.055F,
            0.060F,
            0.065F,
            0.070F
    };

    /*
     * Exponential fog does not have a separate start/end distance.
     * Closeness therefore adjusts the fog density around the selected
     * intensity while keeping setting 5 identical to the normal patched
     * behavior.
     *
     * Lower values push the fog farther away from the player, while higher
     * values make it close in much more aggressively.
     *
     * 0  = 0.50x density
     * 5  = 1.00x density (default / normal patched distance)
     * 8  = 2.00x density
     * 9  = 3.50x density
     * 10 = 4.50x density (extremely close)
     *
     * The final density is capped separately to prevent extreme intensity
     * and closeness combinations from becoming uncontrolled.
     */
    private static final float[] ENTROPY_FOG_CLOSENESS_MULTIPLIERS = {
            0.50F,
            0.60F,
            0.70F,
            0.80F,
            0.90F,
            1.00F,
            1.25F,
            1.50F,
            2.00F,
            3.50F,
            4.50F
    };
    /*
     * Prevent extreme combinations of intensity and closeness from
     * approaching Magic Cookies' old problematic opaque fog density.
     */
    private static final float MAX_SAFE_FOG_DENSITY = 0.180F;

    private static final int ENTROPY_FOG_DURATION = 400;
    private static final int ENTROPY_FADE_START = 180;

    private int remainingEntropyFogFrames = 0;

    private float lastRed = 0.0F;
    private float lastGreen = 0.0F;
    private float lastBlue = 0.0F;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void apply(EntityViewRenderEvent.RenderFogEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayer player = minecraft.thePlayer;

        if (player == null) {
            return;
        }

        ExtendedPlayer extendedPlayer = ExtendedPlayer.get(player);

        if (extendedPlayer == null) {
            return;
        }

        int[] entropyEffect = MagicPotionHandler.getPotionEffect(
                extendedPlayer,
                StuffLoader.MCpotionEntropyID
        );

        boolean entropyActive = entropyEffect != null;

        if (entropyActive) {
            float[] originalColour = extendedPlayer.getFogColour();

            if (originalColour == null || originalColour.length < 3) {
                return;
            }

            lastRed = clampColour(originalColour[0]);
            lastGreen = clampColour(originalColour[1]);
            lastBlue = clampColour(originalColour[2]);

            remainingEntropyFogFrames = ENTROPY_FOG_DURATION;
        } else {
            /*
             * Do not override another Magic Cookies fog effect that may
             * have replaced the entropy fog.
             */
            if (hasAnotherMagicCookiesFog(extendedPlayer)) {
                remainingEntropyFogFrames = 0;
                return;
            }

            if (remainingEntropyFogFrames <= 0) {
                return;
            }

            remainingEntropyFogFrames--;
        }

        int intensity = clampConfigValue(
                Config.mgckEntropyFogIntensity
        );

        int closeness = clampConfigValue(
                Config.mgckEntropyFogCloseness
        );

        float density = getDensity(closeness, intensity, entropyActive);

        applyFog(
                extendedPlayer,
                lastRed,
                lastGreen,
                lastBlue,
                density
        );
    }

    private float getDensity(int closeness, int intensity, boolean entropyActive) {
        float closenessMultiplier =
                ENTROPY_FOG_CLOSENESS_MULTIPLIERS[closeness];

        float density =
                ENTROPY_FOG_DENSITIES[intensity]
                        * closenessMultiplier;

        density = Math.min(
                MAX_SAFE_FOG_DENSITY,
                density
        );

        /*
         * Preserve the intended lingering fog, but fade it safely instead
         * of allowing Magic Cookies' original 0.095 density to return.
         */
        if (!entropyActive
                && remainingEntropyFogFrames <= ENTROPY_FADE_START) {

            density *= remainingEntropyFogFrames
                    / (float) ENTROPY_FADE_START;
        }
        return density;
    }

    private static boolean hasAnotherMagicCookiesFog(
            ExtendedPlayer extendedPlayer) {

        return MagicPotionHandler.getPotionEffect(
                extendedPlayer,
                StuffLoader.MCpotionSlicknessID
        ) != null
                || MagicPotionHandler.getPotionEffect(
                extendedPlayer,
                StuffLoader.MCpotionDrunkID
        ) != null
                || MagicPotionHandler.getPotionEffect(
                extendedPlayer,
                StuffLoader.MCpotionDarkThunderID
        ) != null;
    }

    private static void applyFog(
            ExtendedPlayer extendedPlayer,
            float red,
            float green,
            float blue,
            float density) {

        extendedPlayer.setFogColorA(1.0F);

        FloatBuffer colourBuffer =
                BufferUtils.createFloatBuffer(4);

        colourBuffer.put(red);
        colourBuffer.put(green);
        colourBuffer.put(blue);
        colourBuffer.put(1.0F);
        colourBuffer.flip();

        GL11.glFogi(
                GL11.GL_FOG_MODE,
                GL11.GL_EXP
        );

        GL11.glFog(
                GL11.GL_FOG_COLOR,
                colourBuffer
        );

        GL11.glFogf(
                GL11.GL_FOG_DENSITY,
                Math.max(0.0F, density)
        );
    }

    private static int clampConfigValue(int value) {
        return Math.max(
                0,
                Math.min(10, value)
        );
    }

    private static float clampColour(float value) {
        if (Float.isNaN(value)
                || Float.isInfinite(value)) {
            return 0.0F;
        }

        return Math.max(
                0.0F,
                Math.min(1.0F, value)
        );
    }
}