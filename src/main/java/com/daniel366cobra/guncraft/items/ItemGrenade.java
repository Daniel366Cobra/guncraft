package com.daniel366cobra.guncraft.items;

import com.daniel366cobra.guncraft.GunCraft;
import com.daniel366cobra.guncraft.Reference;
import com.daniel366cobra.guncraft.entities.EntityThrownGrenade;
import com.daniel366cobra.guncraft.init.ModSounds;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemGrenade extends Item
{
	public ItemGrenade()
	{
		setUnlocalizedName("grenade");
		setRegistryName(Reference.MODID, "itemgrenade");
		this.maxStackSize = 16;
		this.setCreativeTab(GunCraft.guncrafttab);
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack)
	{
		return 72000;
	}

	//Primes the grenade
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		ItemStack itemstack = player.getHeldItem(hand);
		if (!world.isRemote)
		{
			world.playSound(null, player.posX, player.posY, player.posZ, ModSounds.grenadefuse, SoundCategory.PLAYERS, 1.0F, 1.0F);
		}
		player.setActiveHand(hand);
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase entityLiving, int timeLeft)
	{
		if (entityLiving instanceof EntityPlayer)
		{
			EntityPlayer entityplayer = (EntityPlayer)entityLiving;
			boolean creative = entityplayer.capabilities.isCreativeMode;

			int timeCooking = this.getMaxItemUseDuration(stack) - timeLeft;
			if (timeCooking < 0) return;

			if (!world.isRemote)
			{
				//Create and spawn the grenade
				EntityThrownGrenade grenade = new EntityThrownGrenade(world, entityplayer, 80 - timeCooking);
				grenade.shoot(entityplayer, entityplayer.rotationPitch, entityplayer.rotationYaw, 0.0F, 1.5F, 1.0F);
				world.spawnEntity(grenade);
				world.playSound(null, entityplayer.posX,  entityplayer.posY, entityplayer.posZ, ModSounds.grenadetoss, SoundCategory.PLAYERS, 1.0F, 1.0F);
			}
			if (!creative) {stack.shrink(1);}
			entityplayer.getCooldownTracker().setCooldown(this, 20);
		}
	}

	@SideOnly(Side.CLIENT)
	public void initModel() {
		ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
	}
}