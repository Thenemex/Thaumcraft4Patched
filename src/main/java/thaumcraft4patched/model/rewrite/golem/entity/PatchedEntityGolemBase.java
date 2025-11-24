package thaumcraft4patched.model.rewrite.golem.entity;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import thaumcraft.common.entities.ai.combat.*;
import thaumcraft.common.entities.ai.fluid.*;
import thaumcraft.common.entities.ai.interact.AIFish;
import thaumcraft.common.entities.ai.interact.AIHarvestCrops;
import thaumcraft.common.entities.ai.interact.AIUseItem;
import thaumcraft.common.entities.ai.inventory.*;
import thaumcraft.common.entities.ai.misc.AIOpenDoor;
import thaumcraft.common.entities.ai.misc.AIReturnHome;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.entities.golems.EnumGolemType;
import thaumcraft4patched.model.rewrite.golem.task.PatchedAIHarvestLogs;

@SuppressWarnings("unused")
public class PatchedEntityGolemBase extends EntityGolemBase {

    public PatchedEntityGolemBase(World par1World) {
        super(par1World);
    }

    public PatchedEntityGolemBase(World par0World, EnumGolemType type, boolean adv) {
        super(par0World, type, adv);
    }

    @Override
    public boolean setupGolem() {
        if (!this.worldObj.isRemote) this.dataWatcher.updateObject(19, (byte)this.golemType.ordinal());

        this.getNavigator().setAvoidsWater(this.getGolemType() != EnumGolemType.STONE && this.getGolemType() != EnumGolemType.IRON && this.getGolemType() != EnumGolemType.THAUMIUM);

        if (this.getGolemType().fireResist) this.isImmuneToFire = true;

        int bonus = 0;
        try {
            bonus = this.getGolemDecoration().contains("H") ? 5 : 0;
        } catch (Exception ignored) {}

        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(this.getGolemType().health + bonus);
        int damage = 2 + this.getGolemStrength() + this.getUpgradeAmount(1);

        try {
            if (this.getGolemDecoration().contains("M")) damage += 2;
        } catch (Exception ignored) {}

        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(damage);
        this.tasks.taskEntries.clear();
        if (this.getCore() > -1) this.tasks.addTask(0, new AIAvoidCreeperSwell(this));

        switch (this.getCore()) {
            case 0:
                this.tasks.addTask(0, new AIHomeReplace(this));
                this.tasks.addTask(1, new AIHomePlace(this));
                this.tasks.addTask(2, new AIHomeDrop(this));
                this.tasks.addTask(3, new AIFillTake(this));
                this.tasks.addTask(4, new AIFillGoto(this));
                break;
            case 1:
                this.tasks.addTask(0, new AIHomeReplace(this));
                this.tasks.addTask(1, new AIEmptyPlace(this));
                this.tasks.addTask(2, new AIEmptyDrop(this));
                this.tasks.addTask(3, new AIEmptyGoto(this));
                this.tasks.addTask(4, new AIHomeTake(this));
                break;
            case 2:
                this.tasks.addTask(0, new AIHomeReplace(this));
                this.tasks.addTask(1, new AIHomePlace(this));
                this.tasks.addTask(2, new AIItemPickup(this));
                break;
            case 3:
                this.tasks.addTask(2, new AIHarvestCrops(this));
                break;
            case 4:
                if (this.decoration.contains("R"))
                    this.tasks.addTask(2, new AIDartAttack(this));
                this.tasks.addTask(3, new AIGolemAttackOnCollide(this));
                this.targetTasks.addTask(1, new AIHurtByTarget(this, false));
                this.targetTasks.addTask(2, new AINearestAttackableTarget(this, 0, true));
                break;
            case 5:
                this.tasks.addTask(1, new AILiquidEmpty(this));
                this.tasks.addTask(2, new AILiquidGather(this));
                this.tasks.addTask(3, new AILiquidGoto(this));
                break;
            case 6:
                this.tasks.addTask(1, new AIEssentiaEmpty(this));
                this.tasks.addTask(2, new AIEssentiaGather(this));
                this.tasks.addTask(3, new AIEssentiaGoto(this));
                break;
            case 7:
                // Replaced AIHarvestLogs with PatchedAIHarvestLogs
                this.tasks.addTask(2, new PatchedAIHarvestLogs(this));
                break;
            case 8:
                this.tasks.addTask(0, new AIHomeReplace(this));
                this.tasks.addTask(0, new AIUseItem(this));
                this.tasks.addTask(4, new AIHomeTake(this));
                break;
            case 9:
                if (this.decoration.contains("R"))
                    this.tasks.addTask(2, new AIDartAttack(this));
                this.tasks.addTask(3, new AIGolemAttackOnCollide(this));
                this.targetTasks.addTask(1, new AINearestButcherTarget(this, 0, true));
                break;
            case 10:
                this.tasks.addTask(0, new AIHomeReplace(this));
                this.tasks.addTask(1, new AISortingPlace(this));
                this.tasks.addTask(3, new AISortingGoto(this));
                this.tasks.addTask(4, new AIHomeTakeSorting(this));
                break;
            case 11:
                this.tasks.addTask(2, new AIFish(this));
        }

        if (this.getCore() > -1) {
            this.tasks.addTask(5, new AIOpenDoor(this, true));
            this.tasks.addTask(6, new AIReturnHome(this));
            this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
            this.tasks.addTask(8, new EntityAILookIdle(this));
        }

        return true;
    }
}
