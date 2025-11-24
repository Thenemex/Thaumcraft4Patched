package thaumcraft4patched.model.config;

import com.google.common.collect.BiMap;
import com.google.common.collect.ListMultimap;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.EntityRegistry;
import nemexlib.api.thaumcraft.API;
import net.minecraft.entity.Entity;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft4patched.model.rewrite.golem.PatchedItemGolemPlacer;
import thaumcraft4patched.model.rewrite.golem.entity.PatchedEntityGolemBase;
import thaumcraft4patched.utils.exceptions.BugPatchIsIncomplete;

import java.lang.reflect.Field;

import static net.minecraft.entity.EntityList.classToStringMapping;
import static net.minecraft.entity.EntityList.stringToClassMapping;
import static thaumcraft.api.aspects.Aspect.WEAPON;
import static thaumcraft4patched.Thaumcraft4Patched.logger;
import static thaumcraft4patched.config.Config.*;

@SuppressWarnings("unchecked")
public class ConfigBugPatches {

    public static void initTC4() {
        if (boneBowResearchPatchEnabled) patchHiddenBoneBowResearch();
        if (golemLumberCoreWoodHardnessPatchEnabled) patchGolemLumberCoreWoodHardness();
    }

    public static void initTX() {
        if (removeNecroInfusionRecipe) removeNecroInfusionRecipe();
    }

    protected static void patchHiddenBoneBowResearch() {
        ResearchItem research = API.getResearch("ARTIFICE", "BONEBOW");
        research.setItemTriggers(); // Cleared all item triggers
        research.setAspectTriggers(WEAPON);
    }

    protected static void patchGolemLumberCoreWoodHardness() {
        // Entity Registries switches
        ModContainer mc = FMLCommonHandler.instance().findContainerFor(Thaumcraft.instance);
        EntityRegistry.EntityRegistration golem = null;
        int cpt = 0;
        try { // Switching the class of the Entity in EntityRegistry.entityRegistrations (ListMultimap)
            Field field = EntityRegistry.class.getDeclaredField("entityRegistrations");
            field.setAccessible(true);
            ListMultimap<ModContainer, EntityRegistry.EntityRegistration> entityRegistrations = (ListMultimap<ModContainer, EntityRegistry.EntityRegistration>) field.get(EntityRegistry.instance());
            golem = entityRegistrations.get(mc).get(17);
            field = EntityRegistry.EntityRegistration.class.getDeclaredField("entityClass");
            field.setAccessible(true);
            field.set(golem, PatchedEntityGolemBase.class);
            if (golem.getEntityClass().equals(PatchedEntityGolemBase.class)) cpt++;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.info("Exception caught on switching entityRegistrations:", e.getClass().getSimpleName(), e.getLocalizedMessage());
        }

        if (golem == null) throw new NullPointerException("EntityRegistration of TC4-Golem is null -> Report to mod author !");

        try { // Switching the class of the Entity in EntityRegistry.entityClassRegistrations (BiMap)
            Field field = EntityRegistry.class.getDeclaredField("entityClassRegistrations");
            field.setAccessible(true);
            BiMap<Class<? extends Entity>, EntityRegistry.EntityRegistration> entityClassRegistrations = (BiMap<Class<? extends Entity>, EntityRegistry.EntityRegistration>) field.get(EntityRegistry.instance());
            entityClassRegistrations.remove(EntityGolemBase.class);
            entityClassRegistrations.put(PatchedEntityGolemBase.class, golem);
            if (!entityClassRegistrations.containsKey(EntityGolemBase.class) && entityClassRegistrations.containsKey(PatchedEntityGolemBase.class)) cpt++;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.info("Exception caught on switching entityRegistrations:", e.getClass().getSimpleName(), e.getLocalizedMessage());
        }

        String entityModName = String.format("%s.%s", mc.getModId(), "Golem");
            // Switching the class of the Entity in EntityList.classToStringMapping (HashMap)
        classToStringMapping.remove(EntityGolemBase.class);
        classToStringMapping.put(PatchedEntityGolemBase.class, entityModName);
        if (!classToStringMapping.containsKey(EntityGolemBase.class) && classToStringMapping.containsKey(PatchedEntityGolemBase.class) && classToStringMapping.containsValue(entityModName)) cpt++;

            // Switching the class of the Entity in EntityList.stringToClassMapping (HashMap)
        stringToClassMapping.remove(entityModName);
        stringToClassMapping.put(entityModName, PatchedEntityGolemBase.class);
        if (stringToClassMapping.containsKey(entityModName) && !stringToClassMapping.containsValue(EntityGolemBase.class) && stringToClassMapping.containsValue(PatchedEntityGolemBase.class)) cpt++;

        // Item Registries switch
        ConfigItems.itemGolemPlacer = (new PatchedItemGolemPlacer().setUnlocalizedName("ItemGolemPlacer"));
        if (ConfigItems.itemGolemPlacer instanceof PatchedItemGolemPlacer) cpt++;

        if (cpt != 5) throw new BugPatchIsIncomplete("GolemLumberWoodHardness", cpt, 5);
        else logger.info("Successfully patch bug for Golem Lumber Wood Hardness !");
    }

    protected static void removeNecroInfusionRecipe() {
        InfusionRecipe recipe, toRemove = null;
        for (Object o : ThaumcraftApi.getCraftingRecipes())
            if (o instanceof InfusionRecipe) {
                recipe = (InfusionRecipe) o;
                if (recipe.getResearch().equals("NECROINFUSION"))
                    toRemove = recipe;
            }
        if (toRemove != null) {
            ThaumcraftApi.getCraftingRecipes().remove(toRemove);
            logger.info("Successfully removed infusion recipe linked to \"NECROINFUSION\" tag !");
        } else
            logger.info("Could not remove of find infusion recipe linked to \"NECROINFUSION\" tag ...");
    }
}
