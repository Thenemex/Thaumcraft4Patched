package thaumcraft4patched.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import static thaumcraft4patched.Thaumcraft4Patched.logger;
import static thaumcraft4patched.asm.ASMUtils.nextRealInstruction;
import static thaumcraft4patched.asm.ASMUtils.previousRealInstruction;
import static thaumcraft4patched.asm.ASMUtils.readClass;
import static thaumcraft4patched.asm.ASMUtils.writeClass;

@SuppressWarnings("unused")
public class HarvestLevelConfigTransformer
        implements IClassTransformer {

    private static final String TARGET =
            "com.awesomehippo.harvestlevelconfig.HarvestLevelConfig";

    private static final String PATCH_OWNER =
            "thaumcraft4patched/model/patch/"
                    + "HlcHandledDropsPatch";

    private static final String BREAK_EVENT_DESCRIPTOR =
            "(Lnet/minecraftforge/event/world/BlockEvent$BreakEvent;)V";

    private static final String HARVEST_DROPS_EVENT_DESCRIPTOR =
            "(Lnet/minecraftforge/event/world/"
                    + "BlockEvent$HarvestDropsEvent;)V";

    private static final String SHOULD_CLEAR_DROPS_DESCRIPTOR =
            "(Lnet/minecraftforge/event/world/"
                    + "BlockEvent$HarvestDropsEvent;)Z";

    private static final String QUARTZ_PATCH_OWNER =
            "thaumcraft4patched/model/patch/HlcNetherQuartzOverridePatch";
    private static final String SKIP_QUARTZ_DESCRIPTOR =
            "(Lnet/minecraftforge/event/world/BlockEvent$BreakEvent;)Z";

    private static final String RESOLVE_QUARTZ_NAME_DESCRIPTOR =
            "(Ljava/lang/String;Lnet/minecraftforge/event/world/"
                    + "BlockEvent$HarvestDropsEvent;)Ljava/lang/String;";

    @Override
    public byte[] transform(
            String name,
            String transformedName,
            byte[] basicClass) {

        if (basicClass == null) {
            return null;
        }

        if (!TARGET.equals(transformedName)) {
            return basicClass;
        }

        ClassNode classNode = readClass(basicClass);
        MethodNode breakMethod = null;
        MethodNode dropsMethod = null;

        for (MethodNode method : classNode.methods) {
            if ("onBlockBreak".equals(method.name)
                    && BREAK_EVENT_DESCRIPTOR.equals(method.desc)) {

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
                    && HARVEST_DROPS_EVENT_DESCRIPTOR.equals(method.desc)) {

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

            FieldInsnNode field =
                    (FieldInsnNode) instruction;

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

            FieldInsnNode field =
                    (FieldInsnNode) instruction;

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
                            PATCH_OWNER,
                            "rememberBreak",
                            BREAK_EVENT_DESCRIPTOR
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
                        PATCH_OWNER,
                        "shouldClearDrops",
                        SHOULD_CLEAR_DROPS_DESCRIPTOR
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

        patchQuartzHarvestRule(dropsMethod);

        JumpInsnNode quartzExit = null;
        int quartzCheckCount = 0;

        for (AbstractInsnNode instruction : breakMethod.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (call.getOpcode() != Opcodes.INVOKEINTERFACE
                    || !"java/util/Map".equals(call.owner)
                    || !"containsKey".equals(call.name)
                    || !"(Ljava/lang/Object;)Z".equals(call.desc)) {
                continue;
            }
            AbstractInsnNode next = nextRealInstruction(call);
            if (!(next instanceof JumpInsnNode)
                    || next.getOpcode() != Opcodes.IFEQ) {
                continue;
            }
            JumpInsnNode jump = (JumpInsnNode) next;
            AbstractInsnNode exit = nextRealInstruction(jump.label);
            if (exit == null || exit.getOpcode() != Opcodes.RETURN) {
                continue;
            }
            quartzExit = jump;
            quartzCheckCount++;
        }

        if (quartzCheckCount != 1) {
            logger.error("Unexpected Harvest Level Config break handler. "
                    + "Nether Quartz compatibility guard was not installed.");
            return basicClass;
        }

        InsnList quartzGuard = new InsnList();
        quartzGuard.add(new VarInsnNode(Opcodes.ALOAD, 1));
        quartzGuard.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                QUARTZ_PATCH_OWNER, "shouldSkipManualDrops", SKIP_QUARTZ_DESCRIPTOR));
        quartzGuard.add(new JumpInsnNode(Opcodes.IFNE, quartzExit.label));
        breakMethod.instructions.insert(quartzExit, quartzGuard);
        logger.info(
                "Successfully patched Harvest Level Config "
                        + "drop handling!"
        );

        return writeClass(classNode);
    }

    private static void patchQuartzHarvestRule(MethodNode dropsMethod) {
        MethodInsnNode registryLookup = null;
        for (AbstractInsnNode instruction : dropsMethod.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (call.getOpcode() != Opcodes.INVOKEVIRTUAL
                    || !"net/minecraft/util/RegistryNamespaced".equals(call.owner)
                    || !"(Ljava/lang/Object;)Ljava/lang/String;".equals(call.desc)
                    || !("getNameForObject".equals(call.name)
                        || "func_148750_c".equals(call.name))) {
                continue;
            }
            if (registryLookup != null) {
                logger.error("Found multiple Harvest Level Config block name lookups. "
                        + "Nether Quartz harvest rule patch was not installed.");
                return;
            }
            registryLookup = call;
        }

        if (registryLookup == null) {
            logger.error("Could not find Harvest Level Config block name lookup. "
                    + "Nether Quartz harvest rule patch was not installed.");
            return;
        }

        InsnList resolveName = new InsnList();
        resolveName.add(new VarInsnNode(Opcodes.ALOAD, 1));
        resolveName.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                QUARTZ_PATCH_OWNER, "resolveHarvestBlockName", RESOLVE_QUARTZ_NAME_DESCRIPTOR));
        dropsMethod.instructions.insert(registryLookup, resolveName);
        logger.info("Successfully patched Harvest Level Config Nether Quartz harvest validation.");
    }
}
