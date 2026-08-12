package thaumcraft4patched.model.patch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.model.ModelSign;
import thaumcraft4patched.config.Config;

import static thaumcraft4patched.Thaumcraft4Patched.logger;

/**
 * Keeps Angelica's cached vanilla sign rendering enabled normally, while
 * falling back to Minecraft's original ModelSign renderer inside the vanilla
 * sign editor where Angelica's cached path can corrupt the preview.
 *
 * Angelica is accessed reflectively so Thaumcraft4Patched does not gain a
 * hard runtime dependency on it.
 */
public final class AngelicaSignEditorCompatibilityPatch {

    private static final String ANGELICA_MODEL_MESHES =
            "com.gtnewhorizons.angelica.rendering.tesr.VanillaModelMeshes";

    private static Method angelicaRenderSignMethod;
    private static boolean lookupAttempted;
    private static boolean lookupWarningLogged;

    private AngelicaSignEditorCompatibilityPatch() {}

    public static void renderSign(ModelSign model) {
        if (shouldUseVanillaRenderer()) {
            model.renderSign();
            return;
        }

        if (!renderWithAngelica(model)) {
            /*
             * Fail safely. If Angelica changes its internal API in a future
             * version, signs should still render instead of crashing.
             */
            model.renderSign();
        }
    }

    private static boolean shouldUseVanillaRenderer() {
        Minecraft minecraft = Minecraft.getMinecraft();

        return Config.angelicaEnabled
                && Config.angelicaSignEditorCompatibilityPatchEnabled
                && minecraft != null
                && minecraft.currentScreen instanceof GuiEditSign;
    }

    private static boolean renderWithAngelica(ModelSign model) {
        Method renderMethod = getAngelicaRenderSignMethod();

        if (renderMethod == null) {
            return false;
        }

        try {
            renderMethod.invoke(null, model);
            return true;
        } catch (IllegalAccessException exception) {
            logLookupWarning();
            return false;
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();

            /*
             * Do not hide a real exception thrown by Angelica's renderer.
             * Only reflection/linkage failures should fall back silently.
             */
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }

            if (cause instanceof Error) {
                throw (Error) cause;
            }

            throw new RuntimeException(
                    "Angelica sign renderer failed",
                    cause
            );
        }
    }

    private static synchronized Method getAngelicaRenderSignMethod() {
        if (lookupAttempted) {
            return angelicaRenderSignMethod;
        }

        lookupAttempted = true;

        try {
            Class<?> vanillaModelMeshes =
                    Class.forName(ANGELICA_MODEL_MESHES);

            angelicaRenderSignMethod =
                    vanillaModelMeshes.getDeclaredMethod(
                            "tc4patched$renderSignCached",
                            ModelSign.class
                    );

            angelicaRenderSignMethod.setAccessible(true);
        } catch (ReflectiveOperationException exception) {
            logLookupWarning();
        } catch (LinkageError error) {
            logLookupWarning();
        }

        return angelicaRenderSignMethod;
    }

    private static void logLookupWarning() {
        if (lookupWarningLogged) {
            return;
        }

        lookupWarningLogged = true;

        logger.error(
                "Could not access Angelica's cached sign renderer. "
                        + "Falling back to vanilla sign rendering."
        );
    }
}