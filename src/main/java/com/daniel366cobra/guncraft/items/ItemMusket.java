package com.daniel366cobra.guncraft.items;

import java.util.List;

import javax.annotation.Nullable;

import com.daniel366cobra.guncraft.GunCraft;
import com.daniel366cobra.guncraft.Reference;
import com.daniel366cobra.guncraft.entities.EntityGenericBullet;
import com.daniel366cobra.guncraft.init.ModItems;
import com.daniel366cobra.guncraft.init.ModSounds;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMusket extends Item {


	public ItemMusket(){
		setUnlocalizedName("musket");
		setRegistryName(Reference.MODID, "itemmusket");
		setMaxStackSize(1);
		setCreativeTab(GunCraft.guncrafttab);
		setMaxDamage(384);

	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack)
	{
		return EnumAction.BOW;

	}

	@Override
	public boolean getIsRepairable(ItemStack tool, ItemStack material)
	{
		return false;
	}

	//Maximum item usage time.
	@Override
	public int getMaxItemUseDuration(ItemStack stack)
	{
		return 72000;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flagin)
	{
		super.addInformation(stack, world, tooltip, flagin);
		tooltip.add(I18n.format("musket.ammo.type", TextFormatting.BOLD, TextFormatting.RESET));

	}

	//Returns the first available ItemStack of ammunition.
	private ItemStack findAmmo(EntityPlayer player)
	{
		if (this.isAmmo(player.getHeldItem(EnumHand.OFF_HAND)))
		{
			return player.getHeldItem(EnumHand.OFF_HAND);
		}
		else if (this.isAmmo(player.getHeldItem(EnumHand.MAIN_HAND)))
		{
			return player.getHeldItem(EnumHand.MAIN_HAND);
		}
		else
		{
			for (int i = 0; i < player.inventory.getSizeInventory(); ++i)
			{
				ItemStack itemstack = player.inventory.getStackInSlot(i);

				if (this.isAmmo(itemstack))
				{
					return itemstack;
				}
			}
			//field_190927_a is a null itemstack
			return ItemStack.EMPTY;
		}
	}

	//Is the item usable as ammunition?
	private boolean isAmmo(ItemStack stack) {
		return stack.getItem() == ModItems.musketball;
	}

	//Sets musket in use (aiming).
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		ItemStack itemstack = player.getHeldItem(hand);
		if (!world.isRemote)
		{
			world.playSound(null, player.posX, player.posY, player.posZ, ModSounds.flintlockcock, SoundCategory.PLAYERS, 1.0F, 1.0F);
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
			boolean infinity = EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, stack) > 0;

			ItemStack ammostack = this.findAmmo(entityplayer);

			int timeAiming = this.getMaxItemUseDuration(stack) - timeLeft;
			if (timeAiming < 0) return;


			//if player has ammo or is in creative mode or has infinity enchantment
			if ((!ammostack.isEmpty() || creative || infinity) && timeAiming >= 20 && !entityplayer.isWet())
			{

				if (!world.isRemote)
				{
					//Create and spawn the bullet
					EntityGenericBullet bullet = new EntityGenericBullet(world, entityplayer, 18.0D, 1.0D, false);
					bullet.shoot(entityplayer, entityplayer.rotationPitch, entityplayer.rotationYaw, 6.5F, 0.0F);
					world.spawnEntity(bullet);

					//Shot sound
					world.playSound(null, entityplayer.posX, entityplayer.posY, entityplayer.posZ, ModSounds.musketshot, SoundCategory.PLAYERS, 2.0F, 1.0F);

					//recoil
					entityplayer.setLocationAndAngles(entityplayer.posX, entityplayer.posY, entityplayer.posZ, entityplayer.rotationYaw, entityplayer.rotationPitch - (itemRand.nextFloat() * 10.0F + 10.0F));

					//Degrade the durability
					stack.damageItem(1, entityplayer);
					//additionally have a chance to explode if very low
					DamageSource gunexplosiondamage;
					int remainingLife = stack.getMaxDamage() - stack.getItemDamage();
					if (remainingLife <= 10)
					{
						if (itemRand.nextFloat()<(1.0F/(remainingLife + 1)))
						{

							entityplayer.inventory.deleteStack(stack);
							gunexplosiondamage = new EntityDamageSource("gunexplosion",entityplayer);
							world.newExplosion(entityplayer, entityplayer.posX, entityplayer.posY + entityplayer.eyeHeight - 0.1D, entityplayer.posZ, 1.5F, false, true);
							entityplayer.attackEntityFrom(gunexplosiondamage, 18.0F);

						}
					}


				}

				//Consume 1 unit of ammo if not in creative mode or enchanted for infinity
				if (!creative && !infinity)
				{
					//Detracts 1 from stack size, sets a bool value when stack becomes null
					ammostack.shrink(1);
					//Checks the bool field (true = null stack)
					if (ammostack.isEmpty())
					{
						entityplayer.inventory.deleteStack(ammostack);
					}
				}
			}
			else if ((ammostack.isEmpty() && !creative) || entityplayer.isWet())
			{	//Misfire in water and rain and on empty gun not in creative
				if (!world.isRemote)
				{
					world.playSound(null, entityplayer.posX, entityplayer.posY, entityplayer.posZ, ModSounds.flintlockmisfire, SoundCategory.PLAYERS, 2.0F, 1.0F);
				}
			}

		}
	}

	@SideOnly(Side.CLIENT)
	public void initModel() {
		ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
	}

}
