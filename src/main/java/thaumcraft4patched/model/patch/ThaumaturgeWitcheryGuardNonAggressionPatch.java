package thaumcraft4patched.model.patch;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static thaumcraft4patched.Thaumcraft4Patched.logger;

public class ThaumaturgeWitcheryGuardNonAggressionPatch
        implements IPatch {

    private static final String THAUMATURGE_CLASS =
            "com.ilya3point999k.thaumicconcilium.common.entities."
                    + "mobs.thaumaturge.Thaumaturge";

    private static final String WITCHERY_GUARD_CLASS =
            "com.emoniph.witchery.entity.EntityVillageGuard";

    private static Method thaumaturgeSetAngerMethod;
    private static boolean angerMethodLookupAttempted;
    private static boolean angerResetFailureLogged;

    /**
     * Immediately rejects a protected target when either entity attempts
     * to select the other.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onTargetSelected(
            LivingSetAttackTargetEvent event) {

        if (event.target == null
                || !isProtectedPair(
                event.entityLiving,
                event.target)) {
            return;
        }

        clearCombatState(
                event.entityLiving,
                event.target
        );
    }

    /**
     * Prevents any remaining melee, ranged, or indirect damage between
     * Thaumaturges and Witchery Village Guards.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLivingAttack(LivingAttackEvent event) {
        Entity attacker = event.source.getEntity();

        if (!(attacker instanceof EntityLivingBase)) {
            return;
        }

        EntityLivingBase attackerLiving =
                (EntityLivingBase) attacker;

        if (!isProtectedPair(
                attackerLiving,
                event.entityLiving)) {
            return;
        }

        event.setCanceled(true);

        clearCombatState(
                attackerLiving,
                event.entityLiving
        );
    }

    /**
     * Cleans up targets that were already active before the patch loaded
     * or were assigned by AI code that bypassed the normal target event.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingUpdate(
            LivingEvent.LivingUpdateEvent event) {

        EntityLivingBase entity = event.entityLiving;

        if (!(entity instanceof EntityLiving)) {
            return;
        }

        EntityLiving living = (EntityLiving) entity;
        EntityLivingBase attackTarget =
                living.getAttackTarget();

        if (attackTarget != null
                && isProtectedPair(entity, attackTarget)) {

            clearCombatState(entity, attackTarget);
            return;
        }

        EntityLivingBase revengeTarget =
                entity.getAITarget();

        if (revengeTarget != null
                && isProtectedPair(entity, revengeTarget)) {

            clearCombatState(entity, revengeTarget);
        }
    }

    private static void clearCombatState(
            EntityLivingBase first,
            EntityLivingBase second) {

        clearTarget(first, second);
        clearTarget(second, first);

        resetThaumaturgeAnger(first);
        resetThaumaturgeAnger(second);
    }

    private static void clearTarget(
            EntityLivingBase entity,
            EntityLivingBase protectedTarget) {

        if (entity instanceof EntityLiving) {
            EntityLiving living = (EntityLiving) entity;

            if (living.getAttackTarget() == protectedTarget) {
                living.setAttackTarget(null);
            }
        }

        if (entity.getAITarget() == protectedTarget) {
            entity.setRevengeTarget(null);
        }

        if (entity instanceof EntityCreature) {
            ((EntityCreature) entity)
                    .getNavigator()
                    .clearPathEntity();
        }
    }

    private static boolean isProtectedPair(
            Entity first,
            Entity second) {

        return isThaumaturge(first)
                && isWitcheryGuard(second)
                || isWitcheryGuard(first)
                && isThaumaturge(second);
    }

    private static boolean isThaumaturge(Entity entity) {
        return hasClassName(entity, THAUMATURGE_CLASS);
    }

    private static boolean isWitcheryGuard(Entity entity) {
        return hasClassName(entity, WITCHERY_GUARD_CLASS);
    }

    private static boolean hasClassName(
            Entity entity,
            String expectedClassName) {

        if (entity == null) {
            return false;
        }

        Class<?> currentClass = entity.getClass();

        while (currentClass != null) {
            if (expectedClassName.equals(
                    currentClass.getName())) {
                return true;
            }

            currentClass = currentClass.getSuperclass();
        }

        return false;
    }

    private static void resetThaumaturgeAnger(
            EntityLivingBase entity) {

        if (!isThaumaturge(entity)) {
            return;
        }

        Method setAngerMethod =
                getThaumaturgeSetAngerMethod(entity);

        if (setAngerMethod == null) {
            return;
        }

        try {
            setAngerMethod.invoke(entity, 0);
        } catch (IllegalAccessException
                 | InvocationTargetException exception) {

            if (!angerResetFailureLogged) {
                angerResetFailureLogged = true;

                logger.error(
                        "Could not reset Thaumaturge anger while "
                                + "preventing combat with Witchery Guards. "
                                + "Further errors will be suppressed.",
                        exception
                );
            }
        }
    }

    private static Method getThaumaturgeSetAngerMethod(
            EntityLivingBase entity) {

        if (angerMethodLookupAttempted) {
            return thaumaturgeSetAngerMethod;
        }

        angerMethodLookupAttempted = true;

        try {
            thaumaturgeSetAngerMethod =
                    entity.getClass().getMethod(
                            "setAnger",
                            int.class
                    );
        } catch (NoSuchMethodException exception) {
            logger.error(
                    "Could not locate Thaumic Concilium's "
                            + "Thaumaturge#setAnger method. Existing anger "
                            + "may take time to expire naturally.",
                    exception
            );
        }

        return thaumaturgeSetAngerMethod;
    }
}