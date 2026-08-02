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

        int intensity = Math.max(
                0,
                Math.min(10, Config.magicCookiesEntropyFogIntensity)
        );

        float density = ENTROPY_FOG_DENSITIES[intensity];

        /*
         * Preserve the intended lingering fog, but fade it safely instead
         * of allowing Magic Cookies' original 0.095 density to return.
         */
        if (!entropyActive
                && remainingEntropyFogFrames <= ENTROPY_FADE_START) {

            density *= remainingEntropyFogFrames / (float) ENTROPY_FADE_START;
        }

        applyFog(
                extendedPlayer,
                lastRed,
                lastGreen,
                lastBlue,
                density
        );
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

        FloatBuffer colourBuffer = BufferUtils.createFloatBuffer(4);
        colourBuffer.put(red);
        colourBuffer.put(green);
        colourBuffer.put(blue);
        colourBuffer.put(1.0F);
        colourBuffer.flip();

        GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
        GL11.glFog(GL11.GL_FOG_COLOR, colourBuffer);
        GL11.glFogf(
                GL11.GL_FOG_DENSITY,
                Math.max(0.0F, density)
        );
    }

    private static float clampColour(float value) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            return 0.0F;
        }

        return Math.max(0.0F, Math.min(1.0F, value));
    }
}