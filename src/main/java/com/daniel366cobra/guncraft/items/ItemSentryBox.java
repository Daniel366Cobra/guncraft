package com.daniel366cobra.guncraft.items;

import com.daniel366cobra.guncraft.GunCraft;
import com.daniel366cobra.guncraft.Reference;
import com.daniel366cobra.guncraft.entities.EntitySentryGun;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemSentryBox extends Item {
	public ItemSentryBox()
	{
		setUnlocalizedName("sentrybox");
		setRegistryName(Reference.MODID, "itemsentrybox");
		setMaxStackSize(1);
		setCreativeTab(GunCraft.guncrafttab);
	}
	
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        ItemStack stack = player.getHeldItem(hand);

        if (world.isRemote)
        {
            return EnumActionResult.SUCCESS;
        }
        else if (!player.canPlayerEdit(pos.offset(facing), facing, stack))
        {
            return EnumActionResult.FAIL;
        }
        else
        {                     
            BlockPos sentryPos = pos.offset(facing);
            EntitySentryGun sentry = new EntitySentryGun(world, player);
            sentry.setLocationAndAngles((double)sentryPos.getX() + 0.5D, (double)sentryPos.getY(), (double)sentryPos.getZ() + 0.5D, player.rotationYaw, 0.0F);
            world.spawnEntity(sentry);
            stack.shrink(1);
            return EnumActionResult.SUCCESS;
        }
    }
	
	@SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
