package thaumcraft4patched.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.TypeInsnNode;

import static thaumcraft4patched.Thaumcraft4Patched.logger;

@SuppressWarnings("unused")
public class TCPatchTransformer implements IClassTransformer {

    private static final String GOLEM_LUMBER_TARGET =
            "thaumcraft.common.entities.ai.interact.AIHarvestLogs";

    private static final String EXCAVATION_FOCUS_TARGET =
            "thaumcraft.common.items.wands.foci.ItemFocusExcavation";

    private static final String ENDER_ZOO_SPAWN_HANDLER_TARGET =
            "crazypants.enderzoo.spawn.MobSpawnEventHandler";

    private static final String WITCHERY_RAISE_LAND_TARGET =
            "com.emoniph.witchery.brewing.action.effect.BrewActionRaiseLand";

    private static final String WITCHERY_RAISE_LAND_METHOD =
            "doApplyToBlock";

    private static final String WITCHERY_RAISE_LAND_METHOD_DESCRIPTOR =
            "(Lnet/minecraft/world/World;"
                    + "IIILnet/minecraftforge/common/util/ForgeDirection;"
                    + "ILcom/emoniph/witchery/brewing/ModifiersEffect;"
                    + "Lnet/minecraft/item/ItemStack;)V";

    private static final String WORLD_OWNER =
            "net/minecraft/world/World";

    private static final String SET_BLOCK_TO_AIR_DESCRIPTOR =
            "(III)Z";

    private static final String SET_BLOCK_DESCRIPTOR =
            "(IIILnet/minecraft/block/Block;II)Z";

    private static final String RAISE_LAND_PATCH_OWNER =
            "thaumcraft4patched/model/patch/"
                    + "WitcheryRaiseLandProtectionPatch";

    private static final String PATCH_SET_BLOCK_TO_AIR_DESCRIPTOR =
            "(Lnet/minecraft/world/World;III)Z";

    private static final String PATCH_SET_BLOCK_DESCRIPTOR =
            "(Lnet/minecraft/world/World;"
                    + "IIILnet/minecraft/block/Block;II)Z";

    private static final String OLD_GOLEM_FAKE_PLAYER_NAME =
            "FakeThaumcraftGolem";

    private static final String NEW_GOLEM_FAKE_PLAYER_NAME =
            "FakeThaumcraftGolemLumber";

    private static final String EXCAVATE_METHOD_NAME =
            "excavate";

    private static final String EXCAVATE_METHOD_DESCRIPTOR =
            "(Lnet/minecraft/world/World;"
                    + "Lnet/minecraft/item/ItemStack;"
                    + "Lnet/minecraft/entity/player/EntityPlayer;"
                    + "Lnet/minecraft/block/Block;IIII)Z";

    private static final String HARVEST_CONTEXT_OWNER =
            "thaumcraft4patched/model/patch/"
                    + "ExcavationFocusHarvestContext";

    private static final String FORGE_EVENT_FACTORY_OWNER =
            "net/minecraftforge/event/ForgeEventFactory";

    private static final String FIRE_BLOCK_HARVESTING_NAME =
            "fireBlockHarvesting";

    private static final String FIRE_BLOCK_HARVESTING_DESCRIPTOR =
            "(Ljava/util/ArrayList;"
                    + "Lnet/minecraft/world/World;"
                    + "Lnet/minecraft/block/Block;"
                    + "IIIIIFZ"
                    + "Lnet/minecraft/entity/player/EntityPlayer;)F";

    private static final String THAUMCRAFT_BLOCK_UTILS_OWNER =
            "thaumcraft/common/lib/utils/BlockUtils";

    private static final String DROP_BLOCK_WITH_CHANCE_NAME =
            "dropBlockAsItemWithChance";

    private static final String DROP_BLOCK_WITH_CHANCE_DESCRIPTOR =
            "(Lnet/minecraft/world/World;"
                    + "Lnet/minecraft/block/Block;"
                    + "IIIIFI"
                    + "Lnet/minecraft/entity/player/EntityPlayer;)V";

    private static final String FORGE_HOOKS_OWNER =
            "net/minecraftforge/common/ForgeHooks";

    private static final String FORGE_TOOL_CHECK_NAME =
            "isToolEffective";

    private static final String TOOL_CHECK_DESCRIPTOR =
            "(Lnet/minecraft/item/ItemStack;"
                    + "Lnet/minecraft/block/Block;I)Z";

    private static final String EXCAVATION_TOOL_PATCH_OWNER =
            "thaumcraft4patched/model/patch/"
                    + "HlcExcavationFocusToolPatch";

    private static final String HARVEST_LEVEL_CONFIG_TARGET =
            "com.awesomehippo.harvestlevelconfig.HarvestLevelConfig";

    private static final String HLC_HANDLED_DROPS_PATCH_OWNER =
            "thaumcraft4patched/model/patch/"
                    + "HlcHandledDropsPatch";

    private static final String HLC_BREAK_EVENT_DESCRIPTOR =
            "(Lnet/minecraftforge/event/world/BlockEvent$BreakEvent;)V";

    private static final String HLC_HARVEST_DROPS_EVENT_DESCRIPTOR =
            "(Lnet/minecraftforge/event/world/BlockEvent$HarvestDropsEvent;)V";

    private static final String HLC_SHOULD_CLEAR_DROPS_DESCRIPTOR =
            "(Lnet/minecraftforge/event/world/BlockEvent$HarvestDropsEvent;)Z";

    private static final String THAUMONOMICON_RECIPE_TARGET =
            "thaumcraft.client.gui.GuiResearchRecipe";

    private static final String THAUMONOMICON_RECIPE_LIGHTING_PATCH_OWNER =
            "thaumcraft4patched/model/patch/"
                    + "ThaumonomiconRecipeLightingPatch";

    private static final String RENDER_ITEM_OWNER =
            "net/minecraft/client/renderer/entity/RenderItem";

    private static final String RENDER_ITEM_GUI_NAME =
            "func_82406_b";

    private static final String RENDER_ITEM_GUI_DESCRIPTOR =
            "(Lnet/minecraft/client/gui/FontRenderer;"
                    + "Lnet/minecraft/client/renderer/texture/TextureManager;"
                    + "Lnet/minecraft/item/ItemStack;II)V";

    private static final String PATCH_RENDER_ITEM_GUI_DESCRIPTOR =
            "(Lnet/minecraft/client/renderer/entity/RenderItem;"
                    + "Lnet/minecraft/client/gui/FontRenderer;"
                    + "Lnet/minecraft/client/renderer/texture/TextureManager;"
                    + "Lnet/minecraft/item/ItemStack;II)V";


    private static final String ANGELICA_MODEL_MESHES_TARGET =
            "com.gtnewhorizons.angelica.rendering.tesr.VanillaModelMeshes";

    private static final String ANGELICA_SIGN_PATCH_OWNER =
            "thaumcraft4patched/model/patch/"
                    + "AngelicaSignEditorCompatibilityPatch";

    private static final String ANGELICA_RENDER_SIGN_NAME =
            "renderSign";

    private static final String ANGELICA_RENDER_SIGN_CACHED_NAME =
            "tc4patched$renderSignCached";

    private static final String ANGELICA_RENDER_SIGN_DESCRIPTOR =
            "(Lnet/minecraft/client/model/ModelSign;)V";

    private static final String MAGIC_COOKIES_DARK_SHRINE_TARGET =
            "tschallacka.magiccookies.worldgen.WorldGenDarkShrine";

    private static final String DARK_SHRINE_GENERATE_NAME =
            "generateDungeonAt";

    private static final String GET_BLOCK_DESCRIPTOR =
            "(III)Lnet/minecraft/block/Block;";

    private static final String BLOCKS_OWNER =
            "net/minecraft/init/Blocks";

    private static final String BLOCK_FIELD_DESCRIPTOR =
            "Lnet/minecraft/block/Block;";

    private static final String DARK_SHRINE_PATCH_OWNER =
            "thaumcraft4patched/model/patch/"
                    + "MagicCookiesDarkShrineFillPatch";

    private static final String PATCH_GET_BLOCK_DESCRIPTOR =
            "(Lnet/minecraft/world/World;IIII)"
                    + "Lnet/minecraft/block/Block;";

    private static final String GRAVESTONE_DEATH_EVENTS_TARGET =
            "de.maxhenkel.gravestone.events.DeathEvents";

    private static final String GRAVESTONE_GIVE_NOTE_NAME =
            "givePlayerNote";

    private static final String GRAVESTONE_GIVE_NOTE_DESCRIPTOR =
            "(Lnet/minecraft/entity/player/EntityPlayer;)V";

    private static final String INVENTORY_PLAYER_OWNER =
            "net/minecraft/entity/player/InventoryPlayer";

    private static final String ADD_ITEM_STACK_DESCRIPTOR =
            "(Lnet/minecraft/item/ItemStack;)Z";

    private static final String GRAVESTONE_PATCH_OWNER =
            "thaumcraft4patched/model/patch/"
                    + "GravestoneDeathNotePatch";

    private static final String PATCH_ADD_DEATH_NOTE_NAME =
            "addDeathNote";

    private static final String PATCH_ADD_DEATH_NOTE_DESCRIPTOR =
            "(Lnet/minecraft/entity/player/InventoryPlayer;"
                    + "Lnet/minecraft/item/ItemStack;)Z";

    private static final String MAGIC_COOKIES_GOLEM_DECORATION_TARGET =
            "tschallacka.magiccookies.entities.living.golem.ItemGolemDecoration";

    private static final String GOLEM_DECORATION_ICON_DESCRIPTOR =
            "(I)Lnet/minecraft/util/IIcon;";

    private static final String GOLEM_DECORATION_ICON_FIELD_DESCRIPTOR =
            "[Lnet/minecraft/util/IIcon;";

    private static final String GOLEM_DECORATION_PATCH_OWNER =
            "thaumcraft4patched/model/patch/"
                    + "MagicCookiesGolemDecorationIconPatch";

    private static final String GOLEM_DECORATION_PATCH_DESCRIPTOR =
            "([Lnet/minecraft/util/IIcon;I)Lnet/minecraft/util/IIcon;";

    private static final String MINETWEAKER_MC_SERVER_TARGET =
            "minetweaker.mc1710.server.MCServer";

    private static final String MINETWEAKER_REMOVE_COMMAND_ACTION_TARGET =
            "minetweaker.mc1710.server.MCServer$RemoveCommandAction";

    private static final String MINETWEAKER_COMMAND_ROLLBACK_PATCH_OWNER =
            "thaumcraft4patched/model/patch/"
                    + "MineTweakerServerCommandRollbackPatch";

    private static final String MINETWEAKER_COMMAND_ROLLBACK_GUARD_NAME =
            "shouldSkipCommandRollback";

    private static final String MINETWEAKER_COMMAND_ROLLBACK_GUARD_DESCRIPTOR =
            "()Z";

    private static final String MINETWEAKER_REMOVE_COMMAND_DESCRIPTOR =
            "(Lnet/minecraft/command/ICommand;)V";

    private static final String THAUMIC_EXPLORATION_OBLIVION_JAR_TARGET =
            "flaxbeard.thaumicexploration.block.BlockTrashJar";

    private static final String OBLIVION_JAR_GET_DROPS_NAME =
            "getDrops";

    private static final String OBLIVION_JAR_GET_DROPS_DESCRIPTOR =
            "(Lnet/minecraft/world/World;IIIII)Ljava/util/ArrayList;";

    private static final String OBLIVION_JAR_PATCH_OWNER =
            "thaumcraft4patched/model/patch/"
                    + "ThaumicExplorationOblivionJarHarvestPatch";

    private static final String OBLIVION_JAR_PATCH_DESCRIPTOR =
            "(Lnet/minecraft/block/Block;I)Ljava/util/ArrayList;";
    private static final String THAUMCRAFT_BLOCK_JAR_TARGET =
            "thaumcraft.common.blocks.BlockJar";

    private static final String BLOCK_JAR_HARVEST_DESCRIPTOR =
            "(Lnet/minecraft/world/World;IIIILnet/minecraft/entity/player/EntityPlayer;)V";

    private static final String BLOCK_DROP_AS_ITEM_DESCRIPTOR =
            "(Lnet/minecraft/world/World;IIIII)V";

    private static final String PATCH_DROP_AS_ITEM_DESCRIPTOR =
            "(Lnet/minecraft/block/Block;"
                    + "Lnet/minecraft/world/World;IIIII)V";

    private static final String ELEMENTAL_FIRE_TARGET =
            "thaumic.tinkerer.common.block.fire.BlockFireBase";

    private static final String ELEMENTAL_FIRE_UPDATE_TICK_DESCRIPTOR =
            "(Lnet/minecraft/world/World;IIILjava/util/Random;)V";

    private static final String GAME_RULES_OWNER =
            "net/minecraft/world/GameRules";

    private static final String GET_GAME_RULE_BOOLEAN_DESCRIPTOR =
            "(Ljava/lang/String;)Z";

    private static final String ELEMENTAL_FIRE_PATCH_OWNER =
            "thaumcraft4patched/model/patch/"
                    + "ThaumicTinkererElementalFireTickPatch";

    private static final String PATCH_GET_GAME_RULE_BOOLEAN_DESCRIPTOR =
            "(Lnet/minecraft/world/GameRules;Ljava/lang/String;)Z";

    @Override
    public byte[] transform(
            String name,
            String transformedName,
            byte[] basicClass) {

        if (basicClass == null) {
            return null;
        }

        if (GOLEM_LUMBER_TARGET.equals(transformedName)) {
            return transformGolemLumberClass(basicClass);
        }

        if (EXCAVATION_FOCUS_TARGET.equals(transformedName)) {
            return transformExcavationFocusClass(basicClass);
        }

        if (THAUMONOMICON_RECIPE_TARGET.equals(transformedName)) {
            return transformThaumonomiconRecipe(basicClass);
        }

        if (HARVEST_LEVEL_CONFIG_TARGET.equals(transformedName)) {
            return transformHarvestLevelConfig(basicClass);
        }

        if (ENDER_ZOO_SPAWN_HANDLER_TARGET.equals(transformedName)) {
            return transformEnderZooSpawnHandler(basicClass);
        }

        if (WITCHERY_RAISE_LAND_TARGET.equals(transformedName)) {
            return transformWitcheryRaiseLandClass(basicClass);
        }

        if (ANGELICA_MODEL_MESHES_TARGET.equals(transformedName)) {
            return transformAngelicaModelMeshes(basicClass);
        }

        if (MAGIC_COOKIES_DARK_SHRINE_TARGET.equals(transformedName)) {
            return transformMagicCookiesDarkShrine(basicClass);
        }

        if (MAGIC_COOKIES_GOLEM_DECORATION_TARGET.equals(transformedName)) {
            return transformMagicCookiesGolemDecoration(basicClass);
        }

        if (THAUMCRAFT_BLOCK_JAR_TARGET.equals(transformedName)) {
            return transformThaumcraftBlockJar(basicClass);
        }

        if (MINETWEAKER_MC_SERVER_TARGET.equals(transformedName)) {
            return transformMineTweakerMcServer(basicClass);
        }

        if (MINETWEAKER_REMOVE_COMMAND_ACTION_TARGET.equals(transformedName)) {
            return transformMineTweakerRemoveCommandAction(basicClass);
        }


        if (THAUMIC_EXPLORATION_OBLIVION_JAR_TARGET.equals(transformedName)) {
            return transformThaumicExplorationOblivionJar(basicClass);
        }

        if (GRAVESTONE_DEATH_EVENTS_TARGET.equals(transformedName)) {
            return transformGravestoneDeathEvents(basicClass);
        }

        if (ELEMENTAL_FIRE_TARGET.equals(transformedName)) {
            return transformThaumicTinkererElementalFire(basicClass);
        }
        return basicClass;
    }

    /**
     * Preserves the existing FakeThaumcraftGolem lumber patch.
     */
    private byte[] transformGolemLumberClass(byte[] basicClass) {
        ClassNode classNode = readClass(basicClass);
        boolean patched = false;

        for (MethodNode method : classNode.methods) {
            if ("<init>".equals(method.name)
                    && method.desc.startsWith(
                    "(Lthaumcraft/common/entities/golems/"
                            + "EntityGolemBase;)")) {

                patched = patchGolemConstructor(method);
                break;
            }
        }

        if (!patched) {
            logger.error(
                    "FakeThaumcraftGolem cannot be found in "
                            + "AIHarvestLogs constructor. Report to author!"
            );
        }

        return writeClass(classNode);
    }

    private boolean patchGolemConstructor(MethodNode method) {
        for (AbstractInsnNode instruction
                : method.instructions.toArray()) {

            if (!(instruction instanceof LdcInsnNode)) {
                continue;
            }

            LdcInsnNode constantInstruction =
                    (LdcInsnNode) instruction;

            if (OLD_GOLEM_FAKE_PLAYER_NAME.equals(
                    constantInstruction.cst)) {

                constantInstruction.cst =
                        NEW_GOLEM_FAKE_PLAYER_NAME;

                logger.info(
                        "Transforming AIHarvestLogs constructor ..."
                );

                return true;
            }
        }

        return false;
    }

    /**
     * Wraps only the harvest calls inside the Excavation Focus's private
     * excavate method.
     * <p>
     * Physically hitting a block with the wand does not use this method and
     * therefore does not activate the focus-magic harvest context.
     */
    private byte[] transformExcavationFocusClass(byte[] basicClass) {
        ClassNode classNode = readClass(basicClass);
        boolean foundExcavateMethod = false;
        boolean patched = false;

        for (MethodNode method : classNode.methods) {
            if (!EXCAVATE_METHOD_NAME.equals(method.name)
                    || !EXCAVATE_METHOD_DESCRIPTOR.equals(method.desc)) {
                continue;
            }

            foundExcavateMethod = true;
            patched = patchExcavationHarvestCalls(method);
            break;
        }

        if (!foundExcavateMethod) {
            logger.error(
                    "Could not find ItemFocusExcavation.excavate. "
                            + "The Excavation Focus harvest context "
                            + "was not installed!"
            );
        } else if (!patched) {
            logger.error(
                    "Could not find both Excavation Focus harvest calls. "
                            + "The Excavation Focus harvest context "
                            + "was not installed!"
            );
        } else {
            logger.info(
                    "Successfully transformed Excavation Focus harvest "
                            + "calls to track active focus magic!"
            );
        }

        return writeClass(classNode);
    }

    private boolean patchExcavationHarvestCalls(MethodNode method) {
        MethodInsnNode silkHarvestCall = null;
        MethodInsnNode normalHarvestCall = null;

        for (AbstractInsnNode instruction
                : method.instructions.toArray()) {

            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodCall =
                    (MethodInsnNode) instruction;

            if (methodCall.getOpcode() != Opcodes.INVOKESTATIC) {
                continue;
            }

            if (FORGE_EVENT_FACTORY_OWNER.equals(methodCall.owner)
                    && FIRE_BLOCK_HARVESTING_NAME.equals(methodCall.name)
                    && FIRE_BLOCK_HARVESTING_DESCRIPTOR.equals(
                    methodCall.desc)) {

                silkHarvestCall = methodCall;
                continue;
            }

            if (THAUMCRAFT_BLOCK_UTILS_OWNER.equals(methodCall.owner)
                    && DROP_BLOCK_WITH_CHANCE_NAME.equals(methodCall.name)
                    && DROP_BLOCK_WITH_CHANCE_DESCRIPTOR.equals(
                    methodCall.desc)) {

                normalHarvestCall = methodCall;
            }
        }

        /*
         * Do not partially patch the method. Both harvest paths must be found
         * before either call is redirected.
         */
        if (silkHarvestCall == null || normalHarvestCall == null) {
            return false;
        }

        silkHarvestCall.owner = HARVEST_CONTEXT_OWNER;
        silkHarvestCall.name = FIRE_BLOCK_HARVESTING_NAME;
        silkHarvestCall.desc = FIRE_BLOCK_HARVESTING_DESCRIPTOR;

        normalHarvestCall.owner = HARVEST_CONTEXT_OWNER;
        normalHarvestCall.name = DROP_BLOCK_WITH_CHANCE_NAME;
        normalHarvestCall.desc = DROP_BLOCK_WITH_CHANCE_DESCRIPTOR;

        return true;
    }

    private byte[] transformThaumonomiconRecipe(byte[] basicClass) {
        ClassNode classNode = readClass(basicClass);
        MethodInsnNode[] calls = new MethodInsnNode[16];
        int count = 0;

        for (MethodNode method : classNode.methods) {
            for (AbstractInsnNode instruction
                    : method.instructions.toArray()) {

                if (!(instruction instanceof MethodInsnNode)) {
                    continue;
                }

                MethodInsnNode call =
                        (MethodInsnNode) instruction;

                if (call.getOpcode() != Opcodes.INVOKEVIRTUAL
                        || !RENDER_ITEM_OWNER.equals(call.owner)
                        || !RENDER_ITEM_GUI_NAME.equals(call.name)
                        || !RENDER_ITEM_GUI_DESCRIPTOR.equals(call.desc)) {

                    continue;
                }

                if (count < calls.length) {
                    calls[count] = call;
                }

                count++;
            }
        }

        if (count != calls.length) {
            logger.error(
                    "GuiResearchRecipe has an unexpected number of "
                            + "item render calls. Thaumonomicon recipe "
                            + "lighting patch was not installed!"
            );

            return basicClass;
        }

        for (MethodInsnNode call : calls) {
            call.setOpcode(Opcodes.INVOKESTATIC);
            call.owner =
                    THAUMONOMICON_RECIPE_LIGHTING_PATCH_OWNER;
            call.name = "renderItem";
            call.desc =
                    PATCH_RENDER_ITEM_GUI_DESCRIPTOR;
        }

        logger.info(
                "Successfully patched Thaumonomicon recipe item lighting!"
        );

        return writeClass(classNode);
    }

    private byte[] transformHarvestLevelConfig(byte[] basicClass) {
        ClassNode classNode = readClass(basicClass);
        MethodNode breakMethod = null;
        MethodNode dropsMethod = null;

        for (MethodNode method : classNode.methods) {
            if ("onBlockBreak".equals(method.name)
                    && HLC_BREAK_EVENT_DESCRIPTOR.equals(method.desc)) {

                if (breakMethod != null) {
                    logger.error(
                            "Found multiple Harvest Level Config "
                                    + "onBlockBreak methods. Drop handling patch "
                                    + "was not installed!"
                    );
                    return basicClass;
                }

                breakMethod = method;
            }

            if ("onHarvestDrops".equals(method.name)
                    && HLC_HARVEST_DROPS_EVENT_DESCRIPTOR.equals(method.desc)) {

                if (dropsMethod != null) {
                    logger.error(
                            "Found multiple Harvest Level Config "
                                    + "onHarvestDrops methods. Drop handling patch "
                                    + "was not installed!"
                    );
                    return basicClass;
                }

                dropsMethod = method;
            }
        }

        if (breakMethod == null || dropsMethod == null) {
            logger.error(
                    "Could not find Harvest Level Config drop handlers. "
                            + "Drop handling patch was not installed!"
            );
            return basicClass;
        }

        FieldInsnNode[] handledDropsWrites =
                new FieldInsnNode[2];

        int writeCount = 0;

        for (AbstractInsnNode instruction
                : breakMethod.instructions.toArray()) {

            if (!(instruction instanceof FieldInsnNode)) {
                continue;
            }

            FieldInsnNode field = (FieldInsnNode) instruction;

            if (field.getOpcode() != Opcodes.PUTFIELD
                    || !classNode.name.equals(field.owner)
                    || !"handledDrops".equals(field.name)
                    || !"Z".equals(field.desc)) {

                continue;
            }

            AbstractInsnNode value =
                    previousRealInstruction(field);

            if (value == null
                    || value.getOpcode() != Opcodes.ICONST_1) {

                continue;
            }

            if (writeCount < handledDropsWrites.length) {
                handledDropsWrites[writeCount] = field;
            }

            writeCount++;
        }

        JumpInsnNode handledDropsJump = null;
        int checkCount = 0;

        for (AbstractInsnNode instruction
                : dropsMethod.instructions.toArray()) {

            if (!(instruction instanceof FieldInsnNode)) {
                continue;
            }

            FieldInsnNode field = (FieldInsnNode) instruction;

            if (field.getOpcode() != Opcodes.GETFIELD
                    || !classNode.name.equals(field.owner)
                    || !"handledDrops".equals(field.name)
                    || !"Z".equals(field.desc)) {

                continue;
            }

            AbstractInsnNode next =
                    nextRealInstruction(field);

            if (next instanceof JumpInsnNode
                    && next.getOpcode() == Opcodes.IFEQ) {

                handledDropsJump = (JumpInsnNode) next;
                checkCount++;
            }
        }

        if (writeCount != 2 || checkCount != 1) {
            logger.error(
                    "Harvest Level Config handledDrops no longer "
                            + "matches the expected implementation. "
                            + "Drop handling patch was not installed!"
            );
            return basicClass;
        }

        for (FieldInsnNode write : handledDropsWrites) {
            InsnList remember = new InsnList();

            remember.add(
                    new VarInsnNode(Opcodes.ALOAD, 1)
            );

            //noinspection deprecation
            remember.add(
                    new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            HLC_HANDLED_DROPS_PATCH_OWNER,
                            "rememberBreak",
                            HLC_BREAK_EVENT_DESCRIPTOR
                    )
            );

            breakMethod.instructions.insert(write, remember);
        }

        InsnList guard = new InsnList();

        guard.add(
                new VarInsnNode(Opcodes.ALOAD, 1)
        );

        //noinspection deprecation
        guard.add(
                new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        HLC_HANDLED_DROPS_PATCH_OWNER,
                        "shouldClearDrops",
                        HLC_SHOULD_CLEAR_DROPS_DESCRIPTOR
                )
        );

        guard.add(
                new JumpInsnNode(
                        Opcodes.IFEQ,
                        handledDropsJump.label
                )
        );

        dropsMethod.instructions.insert(
                handledDropsJump,
                guard
        );

        logger.info(
                "Successfully patched Harvest Level Config "
                        + "drop handling!"
        );

        return writeClass(classNode);
    }

    /**
     * Redirects only Ender Zoo's dirt/grass tool-effectiveness check.
     */
    private byte[] transformEnderZooSpawnHandler(byte[] basicClass) {
        ClassNode classNode = readClass(basicClass);
        boolean patched = false;

        for (MethodNode method : classNode.methods) {
            if (!"onBlockHarvest".equals(method.name)
                    || !("(Lnet/minecraftforge/event/world/"
                    + "BlockEvent$HarvestDropsEvent;)V")
                    .equals(method.desc)) {

                continue;
            }

            patched = patchEnderZooToolCheck(method);
            break;
        }

        if (patched) {
            logger.info(
                    "Successfully transformed Ender Zoo's harvest "
                            + "tool check for Excavation Focus "
                            + "compatibility!"
            );
        } else {
            logger.error(
                    "Could not find Ender Zoo's ForgeHooks."
                            + "isToolEffective call in onBlockHarvest. "
                            + "The compatibility patch was not applied!"
            );
        }

        return writeClass(classNode);
    }

    private boolean patchEnderZooToolCheck(MethodNode method) {
        for (AbstractInsnNode instruction
                : method.instructions.toArray()) {

            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodCall =
                    (MethodInsnNode) instruction;

            if (methodCall.getOpcode() != Opcodes.INVOKESTATIC
                    || !FORGE_HOOKS_OWNER.equals(methodCall.owner)
                    || !FORGE_TOOL_CHECK_NAME.equals(methodCall.name)
                    || !TOOL_CHECK_DESCRIPTOR.equals(methodCall.desc)) {

                continue;
            }

            methodCall.owner = EXCAVATION_TOOL_PATCH_OWNER;
            methodCall.name = FORGE_TOOL_CHECK_NAME;
            methodCall.desc = TOOL_CHECK_DESCRIPTOR;

            return true;
        }

        return false;
    }

    /**
     * Redirects only Witchery Raise Land's source removal and destination
     * placement calls through our protection helper.
     * <p>
     * Both calls must be found before either one is modified. This prevents a
     * partial transformation from creating block loss or duplication.
     */
    private byte[] transformWitcheryRaiseLandClass(byte[] basicClass) {
        ClassNode classNode = readClass(basicClass);
        MethodNode targetMethod = null;

        for (MethodNode method : classNode.methods) {
            if (WITCHERY_RAISE_LAND_METHOD.equals(method.name)
                    && WITCHERY_RAISE_LAND_METHOD_DESCRIPTOR.equals(
                    method.desc)) {

                targetMethod = method;
                break;
            }
        }

        if (targetMethod == null) {
            logger.error(
                    "Could not find Witchery BrewActionRaiseLand."
                            + "doApplyToBlock. Raise Land protection "
                            + "was not installed!"
            );

            return basicClass;
        }

        if (!patchWitcheryRaiseLandBlockMoves(targetMethod)) {
            logger.error(
                    "Could not find both Witchery Raise Land block "
                            + "movement calls. Raise Land protection "
                            + "was not installed!"
            );

            return basicClass;
        }

        logger.info(
                "Successfully transformed Witchery Raise Land block "
                        + "movement calls for protected-block handling!"
        );

        return writeClass(classNode);
    }

    private boolean patchWitcheryRaiseLandBlockMoves(
            MethodNode method) {

        MethodInsnNode removeBlockCall = null;
        MethodInsnNode placeBlockCall = null;

        for (AbstractInsnNode instruction
                : method.instructions.toArray()) {

            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodCall =
                    (MethodInsnNode) instruction;

            if (methodCall.getOpcode() != Opcodes.INVOKEVIRTUAL
                    || !WORLD_OWNER.equals(methodCall.owner)) {
                continue;
            }

            if (SET_BLOCK_TO_AIR_DESCRIPTOR.equals(methodCall.desc)
                    && ("func_147468_f".equals(methodCall.name)
                    || "setBlockToAir".equals(methodCall.name))) {

                removeBlockCall = methodCall;
                continue;
            }

            if (SET_BLOCK_DESCRIPTOR.equals(methodCall.desc)
                    && ("func_147465_d".equals(methodCall.name)
                    || "setBlock".equals(methodCall.name))) {

                placeBlockCall = methodCall;
            }
        }

        /*
         * Never partially transform Raise Land. Preventing only one half of
         * the move could destroy blocks or duplicate protected blocks.
         */
        if (removeBlockCall == null || placeBlockCall == null) {
            return false;
        }

        removeBlockCall.setOpcode(Opcodes.INVOKESTATIC);
        removeBlockCall.owner = RAISE_LAND_PATCH_OWNER;
        removeBlockCall.name = "setBlockToAir";
        removeBlockCall.desc =
                PATCH_SET_BLOCK_TO_AIR_DESCRIPTOR;

        placeBlockCall.setOpcode(Opcodes.INVOKESTATIC);
        placeBlockCall.owner = RAISE_LAND_PATCH_OWNER;
        placeBlockCall.name = "setBlock";
        placeBlockCall.desc =
                PATCH_SET_BLOCK_DESCRIPTOR;

        return true;
    }

    /**
     * Wraps Angelica's cached vanilla sign renderer with our compatibility
     * helper while preserving the original cached implementation.
     * <p>
     * The original renderSign(ModelSign) method is renamed and a same-signature
     * wrapper is installed in its place. Normal rendering therefore continues
     * to use Angelica's cached implementation, while the helper can selectively
     * fall back to vanilla rendering inside the sign editor.
     */
    private byte[] transformAngelicaModelMeshes(byte[] basicClass) {
        ClassNode classNode = readClass(basicClass);
        MethodNode originalRenderSign = null;

        for (MethodNode method : classNode.methods) {
            if (ANGELICA_RENDER_SIGN_CACHED_NAME.equals(method.name)
                    && ANGELICA_RENDER_SIGN_DESCRIPTOR.equals(method.desc)) {

                /*
                 * Already transformed. Avoid ever wrapping the class twice.
                 */
                return basicClass;
            }

            if (!ANGELICA_RENDER_SIGN_NAME.equals(method.name)
                    || !ANGELICA_RENDER_SIGN_DESCRIPTOR.equals(method.desc)) {

                continue;
            }

            if (originalRenderSign != null) {
                logger.error(
                        "Found multiple Angelica renderSign(ModelSign) methods. "
                                + "Sign editor compatibility patch "
                                + "was not installed!"
                );

                return basicClass;
            }

            originalRenderSign = method;
        }

        if (originalRenderSign == null) {
            logger.error(
                    "Could not find Angelica VanillaModelMeshes.renderSign"
                            + "(ModelSign). Sign editor compatibility patch "
                            + "was not installed!"
            );

            return basicClass;
        }

        int originalAccess = originalRenderSign.access;
        String originalSignature = originalRenderSign.signature;
        String[] originalExceptions =
                originalRenderSign.exceptions == null
                        ? null
                        : originalRenderSign.exceptions.toArray(
                        new String[0]
                );

        /*
         * Preserve Angelica's complete original cached implementation under
         * another name. The compatibility helper calls this method whenever
         * the vanilla sign-editor fallback is not required.
         */
        originalRenderSign.name =
                ANGELICA_RENDER_SIGN_CACHED_NAME;

        MethodNode wrapper = new MethodNode(
                originalAccess,
                ANGELICA_RENDER_SIGN_NAME,
                ANGELICA_RENDER_SIGN_DESCRIPTOR,
                originalSignature,
                originalExceptions
        );

        wrapper.instructions.add(
                new VarInsnNode(Opcodes.ALOAD, 0)
        );

        //noinspection deprecation
        wrapper.instructions.add(
                new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        ANGELICA_SIGN_PATCH_OWNER,
                        ANGELICA_RENDER_SIGN_NAME,
                        ANGELICA_RENDER_SIGN_DESCRIPTOR
                )
        );

        wrapper.instructions.add(
                new InsnNode(Opcodes.RETURN)
        );

        /*
         * ClassWriter(0) does not calculate these values.
         * The wrapper only holds one ModelSign reference.
         */
        wrapper.maxStack = 1;
        wrapper.maxLocals = 1;

        classNode.methods.add(wrapper);

        logger.info(
                "Successfully wrapped Angelica cached sign rendering "
                        + "for sign editor compatibility!"
        );

        return writeClass(classNode);
    }

    /**
     * Sends the block read of the Dark Shrine foundation loop through our
     * helper, so the loop gets a floor.
     * <p>
     * The loop keeps the vertical offset in a local variable that starts at -8
     * and goes down by one for each layer. The helper needs that offset, so the
     * offset is pushed as an extra argument and the read becomes a static call.
     * <p>
     * Only a read that feeds a comparison with air, and that uses a counter the
     * method lowers by one, is treated as the foundation loop. A method with
     * more than one read of that shape is left alone.
     */
    private byte[] transformMagicCookiesDarkShrine(byte[] basicClass) {
        ClassNode classNode = readClass(basicClass);
        MethodNode targetMethod = null;

        for (MethodNode method : classNode.methods) {
            if (DARK_SHRINE_GENERATE_NAME.equals(method.name)) {
                targetMethod = method;
                break;
            }
        }

        if (targetMethod == null) {
            logger.error(
                    "Could not find Magic Cookies WorldGenDarkShrine."
                            + "generateDungeonAt. The endless foundation "
                            + "patch was not installed!"
            );

            return basicClass;
        }

        if (!patchDarkShrineFoundationLoop(targetMethod)) {
            logger.error(
                    "Could not find the Magic Cookies Dark Shrine "
                            + "foundation loop. The endless foundation "
                            + "patch was not installed!"
            );

            return basicClass;
        }

        logger.info(
                "Successfully transformed the Magic Cookies Dark Shrine "
                        + "foundation loop to stop at a set depth!"
        );

        return writeClass(classNode);
    }

    /**
     * Replaces Magic Cookies' unsafe Golem Decoration icon-array lookup with a
     * bounds-safe helper.
     *
     * The original method is:
     *
     * return this.icon[metadata];
     *
     * Only that exact known implementation is transformed. If Magic Cookies
     * changes the method in another version, the class is left untouched.
     */
    private byte[] transformMagicCookiesGolemDecoration(
            byte[] basicClass) {

        ClassNode classNode = readClass(basicClass);
        MethodNode targetMethod = null;

        for (MethodNode method : classNode.methods) {
            if (GOLEM_DECORATION_ICON_DESCRIPTOR.equals(method.desc)
                    && ("func_77617_a".equals(method.name)
                    || "getIconFromDamage".equals(method.name))) {

                if (targetMethod != null) {
                    logger.error(
                            "Found multiple Magic Cookies Golem Decoration "
                                    + "icon lookup methods. The icon bounds "
                                    + "patch was not installed!"
                    );

                    return basicClass;
                }

                targetMethod = method;
            }
        }

        if (targetMethod == null) {
            logger.error(
                    "Could not find Magic Cookies ItemGolemDecoration "
                            + "icon lookup. The icon bounds patch "
                            + "was not installed!"
            );

            return basicClass;
        }

        AbstractInsnNode[] instructions =
                targetMethod.instructions.toArray();

        AbstractInsnNode[] realInstructions =
                new AbstractInsnNode[5];

        int realCount = 0;

        for (AbstractInsnNode instruction : instructions) {
            if (instruction.getOpcode() < 0) {
                continue;
            }

            if (realCount >= realInstructions.length) {
                logger.error(
                        "Magic Cookies ItemGolemDecoration icon lookup "
                                + "has an unexpected bytecode shape. "
                                + "The icon bounds patch was not installed!"
                );

                return basicClass;
            }

            realInstructions[realCount++] = instruction;
        }

        if (realCount != 5
                || realInstructions[0].getOpcode() != Opcodes.ALOAD
                || realInstructions[1].getOpcode() != Opcodes.GETFIELD
                || realInstructions[2].getOpcode() != Opcodes.ILOAD
                || realInstructions[3].getOpcode() != Opcodes.AALOAD
                || realInstructions[4].getOpcode() != Opcodes.ARETURN) {

            logger.error(
                    "Magic Cookies ItemGolemDecoration icon lookup "
                            + "no longer matches the expected implementation. "
                            + "The icon bounds patch was not installed!"
            );

            return basicClass;
        }

        VarInsnNode loadThis =
                (VarInsnNode) realInstructions[0];

        FieldInsnNode iconField =
                (FieldInsnNode) realInstructions[1];

        VarInsnNode loadMetadata =
                (VarInsnNode) realInstructions[2];

        if (loadThis.var != 0
                || loadMetadata.var != 1
                || !"icon".equals(iconField.name)
                || !GOLEM_DECORATION_ICON_FIELD_DESCRIPTOR.equals(
                iconField.desc)) {

            logger.error(
                    "Magic Cookies ItemGolemDecoration icon lookup "
                            + "uses an unexpected field or argument layout. "
                            + "The icon bounds patch was not installed!"
            );

            return basicClass;
        }

        //noinspection deprecation
        targetMethod.instructions.set(
                realInstructions[3],
                new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        GOLEM_DECORATION_PATCH_OWNER,
                        "getIcon",
                        GOLEM_DECORATION_PATCH_DESCRIPTOR
                )
        );

        /*
         * The original AALOAD consumed an icon array and integer index and
         * returned an IIcon. The helper has the same stack effect, so maxStack
         * and frames remain valid.
         */
        logger.info(
                "Successfully transformed Magic Cookies Golem Decoration "
                        + "icon lookup with metadata bounds protection!"
        );

        return writeClass(classNode);
    }

    private boolean patchDarkShrineFoundationLoop(MethodNode method) {
        MethodInsnNode foundationBlockRead = null;
        int foundationCounter = -1;

        for (AbstractInsnNode instruction
                : method.instructions.toArray()) {

            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodCall =
                    (MethodInsnNode) instruction;

            if (methodCall.getOpcode() != Opcodes.INVOKEVIRTUAL
                    || !WORLD_OWNER.equals(methodCall.owner)
                    || !GET_BLOCK_DESCRIPTOR.equals(methodCall.desc)
                    || !("func_147439_a".equals(methodCall.name)
                    || "getBlock".equals(methodCall.name))) {

                continue;
            }

            if (!isAirLoopTest(methodCall)) {
                continue;
            }

            int counter = findFoundationCounter(methodCall);

            if (counter < 0
                    || !hasDecrement(method, counter)) {

                continue;
            }

            /*
             * A second loop of the same shape would make the counter
             * unsafe to guess. Leave the class as it is.
             */
            if (foundationBlockRead != null) {
                return false;
            }

            foundationBlockRead = methodCall;
            foundationCounter = counter;
        }

        if (foundationBlockRead == null) {
            return false;
        }

        method.instructions.insertBefore(
                foundationBlockRead,
                new VarInsnNode(Opcodes.ILOAD, foundationCounter)
        );

        foundationBlockRead.setOpcode(Opcodes.INVOKESTATIC);
        foundationBlockRead.owner = DARK_SHRINE_PATCH_OWNER;
        foundationBlockRead.name = "getBlock";
        foundationBlockRead.desc = PATCH_GET_BLOCK_DESCRIPTOR;

        /*
         * ClassWriter(0) keeps the stored maxStack, and the extra
         * argument holds one more slot.
         */
        method.maxStack = method.maxStack + 1;

        return true;
    }

    /**
     * Tells if this block read feeds the air test of the foundation loop.
     */
    private static boolean isAirLoopTest(MethodInsnNode blockRead) {
        AbstractInsnNode airRead = nextRealInstruction(blockRead);

        if (!(airRead instanceof FieldInsnNode)) {
            return false;
        }

        FieldInsnNode airField = (FieldInsnNode) airRead;

        if (airField.getOpcode() != Opcodes.GETSTATIC
                || !BLOCKS_OWNER.equals(airField.owner)
                || !BLOCK_FIELD_DESCRIPTOR.equals(airField.desc)
                || !("field_150350_a".equals(airField.name)
                || "air".equals(airField.name))) {

            return false;
        }

        AbstractInsnNode comparison =
                nextRealInstruction(airRead);

        return comparison != null
                && comparison.getOpcode() == Opcodes.IF_ACMPNE;
    }

    /**
     * Reads back the arguments of the block read and gives the local variable
     * that holds the vertical offset of the loop.
     * <p>
     * The expected shape, in normal order, is the world, then three sums of two
     * local variables. The second variable of the middle sum is the offset.
     */
    private static int findFoundationCounter(MethodInsnNode blockRead) {
        int[] expectedOpcodes = {
                Opcodes.IADD, Opcodes.ILOAD, Opcodes.ILOAD,
                Opcodes.IADD, Opcodes.ILOAD, Opcodes.ILOAD,
                Opcodes.IADD, Opcodes.ILOAD, Opcodes.ILOAD,
                Opcodes.ALOAD
        };

        AbstractInsnNode current = blockRead;
        int counter = -1;

        for (int index = 0; index < expectedOpcodes.length; index++) {
            current = previousRealInstruction(current);

            if (current == null
                    || current.getOpcode() != expectedOpcodes[index]) {

                return -1;
            }

            if (index == 4) {
                counter = ((VarInsnNode) current).var;
            }
        }

        return counter;
    }

    private static boolean hasDecrement(
            MethodNode method,
            int variable) {

        for (AbstractInsnNode instruction
                : method.instructions.toArray()) {

            if (!(instruction instanceof IincInsnNode)) {
                continue;
            }

            IincInsnNode increment =
                    (IincInsnNode) instruction;

            if (increment.var == variable
                    && increment.incr < 0) {

                return true;
            }
        }

        return false;
    }

    private static AbstractInsnNode nextRealInstruction(
            AbstractInsnNode instruction) {

        AbstractInsnNode current = instruction.getNext();

        while (current != null
                && current.getOpcode() < 0) {

            current = current.getNext();
        }

        return current;
    }

    private static AbstractInsnNode previousRealInstruction(
            AbstractInsnNode instruction) {

        AbstractInsnNode current = instruction.getPrevious();

        while (current != null
                && current.getOpcode() < 0) {

            current = current.getPrevious();
        }

        return current;
    }

    /**
     * Routes Thaumcraft BlockJar's explicit early drop through the Oblivion Jar
     * harvest helper.
     *
     * The helper suppresses that first drop only for Thaumic Exploration's
     * Oblivion Jar while forwarding the call unchanged for every other jar.
     */
    private byte[] transformThaumcraftBlockJar(
            byte[] basicClass) {

        ClassNode classNode = readClass(basicClass);
        MethodNode harvestMethod = null;

        for (MethodNode method : classNode.methods) {
            if (BLOCK_JAR_HARVEST_DESCRIPTOR.equals(method.desc)
                    && ("func_149681_a".equals(method.name)
                    || "harvestBlock".equals(method.name))) {

                harvestMethod = method;
                break;
            }
        }

        if (harvestMethod == null) {
            logger.error(
                    "Could not find Thaumcraft BlockJar.harvestBlock. "
                            + "Oblivion Jar duplicate-drop protection "
                            + "was not installed!"
            );

            return basicClass;
        }

        MethodInsnNode dropCall = null;

        for (AbstractInsnNode instruction
                : harvestMethod.instructions.toArray()) {

            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodCall =
                    (MethodInsnNode) instruction;

            if (methodCall.getOpcode() != Opcodes.INVOKEVIRTUAL
                    || !BLOCK_DROP_AS_ITEM_DESCRIPTOR.equals(methodCall.desc)
                    || !("func_149697_b".equals(methodCall.name)
                    || "dropBlockAsItem".equals(methodCall.name))) {

                continue;
            }

            /*
             * There should be exactly one explicit drop in BlockJar.harvestBlock.
             * Refuse to guess if the implementation changes.
             */
            if (dropCall != null) {
                logger.error(
                        "Found multiple BlockJar dropBlockAsItem calls. "
                                + "Oblivion Jar duplicate-drop protection "
                                + "was not installed!"
                );

                return basicClass;
            }

            dropCall = methodCall;
        }

        if (dropCall == null) {
            logger.error(
                    "Could not find BlockJar's early dropBlockAsItem call. "
                            + "Oblivion Jar duplicate-drop protection "
                            + "was not installed!"
            );

            return basicClass;
        }

        dropCall.setOpcode(Opcodes.INVOKESTATIC);
        dropCall.owner = OBLIVION_JAR_PATCH_OWNER;
        dropCall.name = "dropBlockAsItem";
        dropCall.desc = PATCH_DROP_AS_ITEM_DESCRIPTOR;

        logger.info(
                "Successfully wrapped Thaumcraft BlockJar's early drop "
                        + "for Oblivion Jar compatibility!"
        );

        return writeClass(classNode);
    }

    /**
     * Restores a recoverable drop for Thaumic Exploration's Oblivion Jar.
     * The original BlockTrashJar.getDrops implementation intentionally returns
     * an empty ArrayList. We only replace it if its bytecode still matches that
     * known implementation, preventing an unsafe patch if Thaumic Exploration
     * changes the method in another version.
     */
    private byte[] transformThaumicExplorationOblivionJar(
            byte[] basicClass) {

        ClassNode classNode = readClass(basicClass);
        MethodNode getDropsMethod = null;

        for (MethodNode method : classNode.methods) {
            if (OBLIVION_JAR_GET_DROPS_NAME.equals(method.name)
                    && OBLIVION_JAR_GET_DROPS_DESCRIPTOR.equals(method.desc)) {

                getDropsMethod = method;
                break;
            }
        }

        if (getDropsMethod == null) {
            logger.error(
                    "Could not find Thaumic Exploration "
                            + "BlockTrashJar.getDrops. Oblivion Jar harvest "
                            + "patch was not installed!"
            );

            return basicClass;
        }

        if (!isOriginalOblivionJarEmptyDropsMethod(getDropsMethod)) {
            logger.error(
                    "Thaumic Exploration BlockTrashJar.getDrops no longer "
                            + "matches the expected empty-drop implementation. "
                            + "Oblivion Jar harvest patch was not installed!"
            );

            return basicClass;
        }

        getDropsMethod.instructions.clear();
        getDropsMethod.tryCatchBlocks.clear();

        if (getDropsMethod.localVariables != null) {
            getDropsMethod.localVariables.clear();
        }

        /*
         * Argument layout:
         *
         * 0 = this BlockTrashJar
         * 1 = World
         * 2 = x
         * 3 = y
         * 4 = z
         * 5 = metadata
         * 6 = fortune
         */

        getDropsMethod.instructions.add(
                new VarInsnNode(Opcodes.ALOAD, 0)
        );

        getDropsMethod.instructions.add(
                new VarInsnNode(Opcodes.ILOAD, 5)
        );

        //noinspection deprecation
        getDropsMethod.instructions.add(
                new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        OBLIVION_JAR_PATCH_OWNER,
                        OBLIVION_JAR_GET_DROPS_NAME,
                        OBLIVION_JAR_PATCH_DESCRIPTOR
                )
        );

        getDropsMethod.instructions.add(
                new InsnNode(Opcodes.ARETURN)
        );

        getDropsMethod.maxStack = 2;
        getDropsMethod.maxLocals = Math.max(
                getDropsMethod.maxLocals,
                7
        );

        logger.info(
                "Successfully transformed Thaumic Exploration's "
                        + "Oblivion Jar drop handling! yippeee"
        );

        return writeClass(classNode);
    }

    /**
     * Verifies the known Thaumic Exploration 1.1-53 implementation :D :
     */
    private static boolean isOriginalOblivionJarEmptyDropsMethod(
            MethodNode method) {

        int[] expectedOpcodes = {
                Opcodes.NEW,
                Opcodes.DUP,
                Opcodes.INVOKESPECIAL,
                Opcodes.ASTORE,
                Opcodes.ALOAD,
                Opcodes.ARETURN
        };

        int opcodeIndex = 0;

        for (AbstractInsnNode instruction
                : method.instructions.toArray()) {

            int opcode = instruction.getOpcode();

            /*
             * Ignore labels, line numbers and frames.
             */
            if (opcode < 0) {
                continue;
            }

            if (opcodeIndex >= expectedOpcodes.length
                    || opcode != expectedOpcodes[opcodeIndex]) {

                return false;
            }

            if (opcode == Opcodes.INVOKESPECIAL) {
                if (!(instruction instanceof MethodInsnNode)) {
                    return false;
                }

                MethodInsnNode constructorCall =
                        (MethodInsnNode) instruction;

                if (!"java/util/ArrayList".equals(constructorCall.owner)
                        || !"<init>".equals(constructorCall.name)
                        || !"()V".equals(constructorCall.desc)) {

                    return false;
                }
            }

            opcodeIndex++;
        }

        return opcodeIndex == expectedOpcodes.length;
    }

    private byte[] transformMineTweakerMcServer(byte[] basicClass) {
        ClassNode classNode = readClass(basicClass);
        MethodNode targetMethod = null;

        for (MethodNode method : classNode.methods) {
            if ("removeCommand".equals(method.name)
                    && MINETWEAKER_REMOVE_COMMAND_DESCRIPTOR.equals(method.desc)
                    && (method.access & Opcodes.ACC_STATIC) != 0) {

                if (targetMethod != null) {
                    logger.error(
                            "Found multiple MineTweaker MCServer command-removal "
                                    + "methods. The server command rollback patch "
                                    + "was not installed!"
                    );

                    return basicClass;
                }

                targetMethod = method;
            }
        }

        if (targetMethod == null) {
            logger.error(
                    "Could not find MineTweaker MCServer.removeCommand(ICommand). "
                            + "The server command rollback patch was not installed!"
            );

            return basicClass;
        }

        if (!hasMineTweakerServerLookupPrefix(targetMethod)) {
            logger.error(
                    "MineTweaker MCServer.removeCommand(ICommand) no longer "
                            + "matches the expected implementation. "
                            + "The server command rollback patch was not installed!"
            );

            return basicClass;
        }

        insertMineTweakerCommandRollbackGuard(targetMethod);

        logger.info(
                "Installed MineTweaker server command rollback guard."
        );

        return writeClass(classNode);
    }

    private byte[] transformMineTweakerRemoveCommandAction(
            byte[] basicClass) {

        ClassNode classNode = readClass(basicClass);
        MethodNode targetMethod = null;

        for (MethodNode method : classNode.methods) {
            if ("undo".equals(method.name)
                    && "()V".equals(method.desc)) {

                if (targetMethod != null) {
                    logger.error(
                            "Found multiple MineTweaker RemoveCommandAction undo "
                                    + "methods. The server command rollback patch "
                                    + "was not installed!"
                    );

                    return basicClass;
                }

                targetMethod = method;
            }
        }

        if (targetMethod == null) {
            logger.error(
                    "Could not find MineTweaker RemoveCommandAction.undo(). "
                            + "The server command rollback patch was not installed!"
            );

            return basicClass;
        }

        if (!hasMineTweakerServerLookupPrefix(targetMethod)) {
            logger.error(
                    "MineTweaker RemoveCommandAction.undo() no longer matches "
                            + "the expected implementation. "
                            + "The server command rollback patch was not installed!"
            );

            return basicClass;
        }

        insertMineTweakerCommandRollbackGuard(targetMethod);

        logger.info(
                "Installed MineTweaker removed-command undo guard."
        );

        return writeClass(classNode);
    }

    private boolean hasMineTweakerServerLookupPrefix(
            MethodNode method) {

        AbstractInsnNode first = null;
        AbstractInsnNode second = null;
        AbstractInsnNode third = null;
        int found = 0;

        for (AbstractInsnNode instruction
                : method.instructions.toArray()) {

            if (instruction.getOpcode() < 0) {
                continue;
            }

            if (found == 0) {
                first = instruction;
            } else if (found == 1) {
                second = instruction;
            } else if (found == 2) {
                third = instruction;
                break;
            }

            found++;
        }

        if (!(first instanceof MethodInsnNode)
                || !(second instanceof MethodInsnNode)
                || !(third instanceof TypeInsnNode)) {

            return false;
        }

        MethodInsnNode getServer =
                (MethodInsnNode) first;

        MethodInsnNode getCommandManager =
                (MethodInsnNode) second;

        TypeInsnNode commandHandlerCast =
                (TypeInsnNode) third;

        boolean validGetServer =
                getServer.getOpcode() == Opcodes.INVOKESTATIC
                        && "net/minecraft/server/MinecraftServer".equals(
                        getServer.owner)
                        && ("func_71276_C".equals(getServer.name)
                        || "getServer".equals(getServer.name))
                        && "()Lnet/minecraft/server/MinecraftServer;".equals(
                        getServer.desc);

        boolean validGetCommandManager =
                getCommandManager.getOpcode() == Opcodes.INVOKEVIRTUAL
                        && "net/minecraft/server/MinecraftServer".equals(
                        getCommandManager.owner)
                        && ("func_71187_D".equals(getCommandManager.name)
                        || "getCommandManager".equals(
                        getCommandManager.name))
                        && "()Lnet/minecraft/command/ICommandManager;".equals(
                        getCommandManager.desc);

        boolean validCommandHandlerCast =
                commandHandlerCast.getOpcode() == Opcodes.CHECKCAST
                        && "net/minecraft/command/CommandHandler".equals(
                        commandHandlerCast.desc);

        return validGetServer
                && validGetCommandManager
                && validCommandHandlerCast;
    }

    private void insertMineTweakerCommandRollbackGuard(
            MethodNode method) {

        LabelNode continueOriginal = new LabelNode();
        InsnList guard = new InsnList();

        //noinspection deprecation
        guard.add(
                new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        MINETWEAKER_COMMAND_ROLLBACK_PATCH_OWNER,
                        MINETWEAKER_COMMAND_ROLLBACK_GUARD_NAME,
                        MINETWEAKER_COMMAND_ROLLBACK_GUARD_DESCRIPTOR
                )
        );

        guard.add(
                new JumpInsnNode(
                        Opcodes.IFEQ,
                        continueOriginal
                )
        );

        guard.add(new InsnNode(Opcodes.RETURN));
        guard.add(continueOriginal);

        method.instructions.insertBefore(
                method.instructions.getFirst(),
                guard
        );
    }

    /**
     * Makes the Gravestone death note obey "enable_death_note".
     *
     * Gravestone gives a note from a death handler that runs when the
     * keepInventory game rule is on. That handler never reads the config entry
     * of its own mod, so a note arrives after every death. The inventory call
     * of that handler now goes to the patch, which drops the note when the
     * entry is off.
     */
    private byte[] transformGravestoneDeathEvents(byte[] basicClass) {
        ClassNode classNode = readClass(basicClass);
        MethodNode targetMethod = null;

        for (MethodNode method : classNode.methods) {
            if (GRAVESTONE_GIVE_NOTE_NAME.equals(method.name)
                    && GRAVESTONE_GIVE_NOTE_DESCRIPTOR.equals(method.desc)
                    && (method.access & Opcodes.ACC_STATIC) != 0) {

                targetMethod = method;
                break;
            }
        }

        if (targetMethod == null) {
            logger.error(
                    "Could not find Gravestone DeathEvents.givePlayerNote. "
                            + "The death note config patch was not installed!"
            );

            return basicClass;
        }

        if (!patchDeathNoteInventoryCall(targetMethod)) {
            logger.error(
                    "Could not find the Gravestone death note inventory "
                            + "call. The death note config patch was not "
                            + "installed!"
            );

            return basicClass;
        }

        logger.info(
                "Successfully transformed the Gravestone death note to "
                        + "follow the enable_death_note config entry!"
        );

        return writeClass(classNode);
    }

    private boolean patchDeathNoteInventoryCall(MethodNode method) {
        MethodInsnNode inventoryCall = null;

        for (AbstractInsnNode instruction
                : method.instructions.toArray()) {

            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodCall =
                    (MethodInsnNode) instruction;

            if (methodCall.getOpcode() != Opcodes.INVOKEVIRTUAL
                    || !INVENTORY_PLAYER_OWNER.equals(methodCall.owner)
                    || !ADD_ITEM_STACK_DESCRIPTOR.equals(methodCall.desc)
                    || !("func_70441_a".equals(methodCall.name)
                    || "addItemStackToInventory".equals(methodCall.name))) {

                continue;
            }

            /*
             * A second call of the same shape would leave the target
             * unclear. Leave the class as it is.
             */
            if (inventoryCall != null) {
                return false;
            }

            inventoryCall = methodCall;
        }

        if (inventoryCall == null) {
            return false;
        }

        inventoryCall.setOpcode(Opcodes.INVOKESTATIC);
        inventoryCall.owner = GRAVESTONE_PATCH_OWNER;
        inventoryCall.name = PATCH_ADD_DEATH_NOTE_NAME;
        inventoryCall.desc = PATCH_ADD_DEATH_NOTE_DESCRIPTOR;

        return true;
    }

    /**
     * Sends the doFireTick game rule read of Thaumic Tinkerer elemental fire
     * through our helper.
     *
     * BlockFireBase.updateTick returns at once when that rule is off, so the
     * six elemental fires never transmute on servers that disable vanilla fire
     * spread. Vanilla fire is a different class and is not transformed.
     */
    private byte[] transformThaumicTinkererElementalFire(byte[] basicClass) {
        ClassNode classNode = readClass(basicClass);
        MethodNode targetMethod = null;

        for (MethodNode method : classNode.methods) {
            if (ELEMENTAL_FIRE_UPDATE_TICK_DESCRIPTOR.equals(method.desc)
                    && ("func_149674_a".equals(method.name)
                    || "updateTick".equals(method.name))) {

                if (targetMethod != null) {
                    logger.error(
                            "Found multiple Thaumic Tinkerer BlockFireBase "
                                    + "updateTick methods. The elemental fire "
                                    + "tick patch was not installed!"
                    );

                    return basicClass;
                }

                targetMethod = method;
            }
        }

        if (targetMethod == null) {
            logger.error(
                    "Could not find Thaumic Tinkerer BlockFireBase.updateTick. "
                            + "The elemental fire tick patch was not installed!"
            );

            return basicClass;
        }

        if (!patchElementalFireDoFireTickCall(targetMethod)) {
            logger.error(
                    "Could not find the unique Thaumic Tinkerer BlockFireBase "
                            + "doFireTick game rule call. The elemental fire "
                            + "tick patch was not installed!"
            );

            return basicClass;
        }

        logger.info(
                "Successfully transformed Thaumic Tinkerer elemental fire "
                        + "to tick when doFireTick is off!"
        );

        return writeClass(classNode);
    }

    private boolean patchElementalFireDoFireTickCall(MethodNode method) {
        MethodInsnNode gameRuleCall = null;

        for (AbstractInsnNode instruction
                : method.instructions.toArray()) {

            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodCall =
                    (MethodInsnNode) instruction;

            if (methodCall.getOpcode() != Opcodes.INVOKEVIRTUAL
                    || !GAME_RULES_OWNER.equals(methodCall.owner)
                    || !GET_GAME_RULE_BOOLEAN_DESCRIPTOR.equals(
                    methodCall.desc)
                    || !("func_82766_b".equals(methodCall.name)
                    || "getGameRuleBooleanValue".equals(
                    methodCall.name))) {

                continue;
            }

            AbstractInsnNode previous =
                    previousRealInstruction(methodCall);

            if (!(previous instanceof LdcInsnNode)
                    || !"doFireTick".equals(
                    ((LdcInsnNode) previous).cst)) {

                continue;
            }

            if (gameRuleCall != null) {
                return false;
            }

            gameRuleCall = methodCall;
        }

        if (gameRuleCall == null) {
            return false;
        }

        gameRuleCall.setOpcode(Opcodes.INVOKESTATIC);
        gameRuleCall.owner = ELEMENTAL_FIRE_PATCH_OWNER;
        gameRuleCall.name = "getGameRuleBooleanValue";
        gameRuleCall.desc = PATCH_GET_GAME_RULE_BOOLEAN_DESCRIPTOR;

        return true;
    }

    private static ClassNode readClass(byte[] basicClass) {
        ClassNode classNode = new ClassNode();
        new ClassReader(basicClass).accept(classNode, 0);
        return classNode;
    }

    private static byte[] writeClass(ClassNode classNode) {
        /*
         * These transformations replace constants or same-signature static
         * method calls, so the existing stack frames remain valid.
         */
        ClassWriter writer = new ClassWriter(0);
        classNode.accept(writer);
        return writer.toByteArray();
    }
}