package thaumcraft4patched.model.patch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import thaumcraft4patched.config.Config;

public final class ThaumicBasesEntityDeconstructorPatch implements IPatch {

    private static volatile Method addWarpToPlayerMethod;

    private ThaumicBasesEntityDeconstructorPatch() {}

    public static void addWarpIfPlayer(
            Entity entity,
            int permanentWarp,
            int temporaryWarp) {


        if (!Config.tbEnabled
                || !Config.entityDeconstructorPlayerCastPatchEnabled) {

            EntityPlayerMP player =
                    EntityPlayerMP.class.cast(entity);

            invokeAddWarp(
                    player,
                    permanentWarp,
                    temporaryWarp
            );

            return;
        }


        if (entity instanceof EntityPlayerMP) {

            invokeAddWarp(
                    (EntityPlayerMP) entity,
                    permanentWarp,
                    temporaryWarp
            );
        }
    }

    private static void invokeAddWarp(
            EntityPlayer player,
            int permanentWarp,
            int temporaryWarp) {

        try {

            getAddWarpToPlayerMethod().invoke(
                    null,
                    player,
                    permanentWarp,
                    temporaryWarp
            );

        } catch (InvocationTargetException exception) {

            Throwable cause = exception.getCause();

            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }

            if (cause instanceof Error) {
                throw (Error) cause;
            }

            throw new RuntimeException(
                    "Thaumic Bases addWarpToPlayer failed",
                    cause
            );

        } catch (ReflectiveOperationException exception) {

            throw new RuntimeException(
                    "Could not access Thaumic Bases TBUtils.addWarpToPlayer",
                    exception
            );
        }
    }

    private static Method getAddWarpToPlayerMethod()
            throws ReflectiveOperationException {

        Method method = addWarpToPlayerMethod;

        if (method != null) {
            return method;
        }

        synchronized (ThaumicBasesEntityDeconstructorPatch.class) {

            method = addWarpToPlayerMethod;

            if (method == null) {

                Class<?> utils =
                        Class.forName("tb.utils.TBUtils");

                method = utils.getMethod(
                        "addWarpToPlayer",
                        EntityPlayer.class,
                        int.class,
                        int.class
                );

                addWarpToPlayerMethod = method;
            }
        }

        return method;
    }
}
