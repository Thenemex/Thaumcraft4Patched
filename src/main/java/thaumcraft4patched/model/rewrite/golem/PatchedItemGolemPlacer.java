package thaumcraft4patched.model.rewrite.golem;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import thaumcraft.common.entities.golems.EnumGolemType;
import thaumcraft.common.entities.golems.ItemGolemBell;
import thaumcraft.common.entities.golems.ItemGolemPlacer;
import thaumcraft4patched.model.rewrite.golem.entity.PatchedEntityGolemBase;

import java.util.Arrays;

public class PatchedItemGolemPlacer extends ItemGolemPlacer {

    public PatchedItemGolemPlacer() {
        super();
    }

    @Override
    public boolean spawnCreature(World par0World, double par2, double par4, double par6, int side, ItemStack stack, EntityPlayer player) {
        boolean adv = stack.hasTagCompound() && stack.stackTagCompound.hasKey("advanced");
        // Replaced EntityGolemBase with PatchedEntityGolemBase
        PatchedEntityGolemBase golem = new PatchedEntityGolemBase(par0World, EnumGolemType.getType(stack.getItemDamage()), adv);
        golem.setLocationAndAngles(par2, par4, par6, par0World.rand.nextFloat() * 360.0F, 0.0F);
        golem.playLivingSound();
        golem.setHomeArea(MathHelper.floor_double(par2), MathHelper.floor_double(par4), MathHelper.floor_double(par6), 32);

        if (stack.hasTagCompound() && stack.stackTagCompound.hasKey("core"))
            golem.setCore(stack.stackTagCompound.getByte("core"));

        if (stack.hasTagCompound() && stack.stackTagCompound.hasKey("upgrades")) {
            int ul = golem.upgrades.length;
            golem.upgrades = stack.stackTagCompound.getByteArray("upgrades");
            if (ul != golem.upgrades.length) {
                byte[] tt = new byte[ul];

                Arrays.fill(tt, (byte) -1);

                for(int a = 0; a < golem.upgrades.length; ++a)
                    if (a < ul)
                        tt[a] = golem.upgrades[a];

                golem.upgrades = tt;
            }
        }

        String deco = "";
        if (stack.hasTagCompound() && stack.stackTagCompound.hasKey("deco")) {
            deco = stack.stackTagCompound.getString("deco");
            golem.decoration = deco;
        }

        golem.setup(side);
        par0World.spawnEntityInWorld(golem);
        golem.setGolemDecoration(deco);
        golem.setOwner(player.getCommandSenderName());
        golem.setMarkers(ItemGolemBell.getMarkers(stack));
        int a = 0;

        for(byte b : golem.upgrades) {
            golem.setUpgrade(a, b);
            ++a;
        }

        if (stack.hasDisplayName()) {
            golem.setCustomNameTag(stack.getDisplayName());
            golem.func_110163_bv();
        }

        if (stack.hasTagCompound() && stack.stackTagCompound.hasKey("Inventory")) {
            NBTTagList nbttaglist2 = stack.stackTagCompound.getTagList("Inventory", 10);
            golem.inventory.readFromNBT(nbttaglist2);
        }

        return true;
    }
}
