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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


//UPDATED 1.12
public class ItemLeverActionRifle extends Item {

	public ItemLeverActionRifle(){
		setUnlocalizedName("leveractionrifle");
		setRegistryName(Reference.MODID, "itemleveractionrifle");
		setMaxStackSize(1);
		setCreativeTab(GunCraft.guncrafttab);
		setMaxDamage(7);

	}	

	public EnumAction getItemUseAction(ItemStack stack)
	{
		return EnumAction.BOW;

	}

	public boolean getIsRepairable(ItemStack tool, ItemStack material)
	{
		return false;
	}

	//Maximum item usage time.
	public int getMaxItemUseDuration(ItemStack stack)
	{
		return 72000;
	}

	@SideOnly(Side.CLIENT)
	public void renderHelmetOverlay(ItemStack stack, EntityPlayer player, net.minecraft.client.gui.ScaledResolution resolution, float partialTicks)
	{
		
	}
	
	


	//Shows ammunition type in tooltip
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flagin)
	{
		super.addInformation(stack, world, tooltip, flagin);
		tooltip.add(I18n.format("leveractionrifle.ammo.type", TextFormatting.BOLD, TextFormatting.RESET));
		if (stack.hasTagCompound())
		{			
			if (stack.getTagCompound().hasKey("magazine"))
			{
				String displayAmmoTypes = "";
				NBTTagList magazine = stack.getTagCompound().getTagList("magazine", NBT.TAG_STRING);
				for (int i = 0; i < magazine.tagCount(); i++)
				{
					displayAmmoTypes += " " + I18n.format(magazine.getStringTagAt(i));
				}
				tooltip.add(I18n.format("weapon.mag.contents") + displayAmmoTypes);
			}
		}
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

			return ItemStack.EMPTY;
		}
	}

	//Is the item usable as ammunition?
	private boolean isAmmo(ItemStack stack) {
		return (stack.getItem() == ModItems.leveractioncartridge || stack.getItem() == ModItems.leveractioncartridgeincendiary);
	}

	//Give the rifle an NBT magazine.
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean held)
	{
		if (stack.getTagCompound()==null && held)
		{
			stack.setTagCompound(new NBTTagCompound());			
			stack.getTagCompound().setTag("magazine", new NBTTagList());
		}
	}

	//Sets rifle in use (aiming).
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{	
		ItemStack stack = player.getHeldItem(hand);
		player.setActiveHand(hand);
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);

	}
	
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
		return false;
		
	}

	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase entityLiving, int timeLeft)
	{
		if (entityLiving instanceof EntityPlayer)
		{
			EntityPlayer entityplayer = (EntityPlayer)entityLiving;
			boolean creative = entityplayer.capabilities.isCreativeMode;

			ItemStack ammostack = this.findAmmo(entityplayer);
			int timeAiming = this.getMaxItemUseDuration(stack) - timeLeft;

			if (timeAiming < 0) return;

			//Get magazine contents as a list of strings
			NBTTagList magazine = stack.getTagCompound().getTagList("magazine", NBT.TAG_STRING);

			//Long right click -> finished aiming
			if (timeAiming > 10)
			{				
				if (magazine.tagCount() > 0)
				{	

					if (!world.isRemote)
					{
						//Create and spawn the bullet

						EntityGenericBullet bullet = new EntityGenericBullet(world, entityplayer, 20.0D, 0.5D, magazine.getStringTagAt(magazine.tagCount() - 1).equals("ammo.inc"));
						bullet.shoot(entityplayer, entityplayer.rotationPitch, entityplayer.rotationYaw, 6.5F, 0.0F);
						world.spawnEntity(bullet);						
						//Deplete magazine from the back - fire the last loaded
						magazine.removeTag(magazine.tagCount() - 1);
						//Expend 1 unit of ammo, update the damage value 
						stack.setItemDamage(7 - magazine.tagCount());

						//Shot sound and recoil
						world.playSound(null, entityplayer.posX, entityplayer.posY, entityplayer.posZ, ModSounds.leveractionshot_reload, SoundCategory.PLAYERS, 1.0F, 1.0F);

					}
					entityplayer.setLocationAndAngles(entityplayer.posX, entityplayer.posY, entityplayer.posZ, entityplayer.rotationYaw, entityplayer.rotationPitch - (itemRand.nextFloat() * 5.0F + 5.0F));
				}
				else
				{
					if (!world.isRemote)
					{
						//Dry-fire empty gun
						world.playSound(null, entityplayer.posX, entityplayer.posY, entityplayer.posZ, ModSounds.leveractiondryfire, SoundCategory.PLAYERS, 1.0F, 1.0F);
					}
				}


			} //Short right click -> reload 1 unit of ammo
			else if (timeAiming <= 10 && magazine.tagCount() < 7)
			{
				if (ammostack.isEmpty() && creative)
				{//If in creative while out of ammo, load full metal jackets
					if (!world.isRemote)
					{
						magazine.appendTag(new NBTTagString("ammo.fmj"));
						//Load 1 unit of ammo and update the damage value						
						stack.setItemDamage(7 - magazine.tagCount());

						//Reload sound
						world.playSound(null, entityplayer.posX, entityplayer.posY, entityplayer.posZ, ModSounds.leveractionload, SoundCategory.PLAYERS, 1.0F, 1.0F);

					}
				}
				else if (!ammostack.isEmpty())
				{

					if (!world.isRemote)
					{						
						//Replenish magazine from the back
						if (ammostack.getItem() == ModItems.leveractioncartridge)
						{
							magazine.appendTag(new NBTTagString("ammo.fmj"));
						}
						if (ammostack.getItem() == ModItems.leveractioncartridgeincendiary)
						{
							magazine.appendTag(new NBTTagString("ammo.inc"));
						}
						//Load 1 unit of ammo and update the damage value						
						stack.setItemDamage(7 - magazine.tagCount());
						//Reload sound
						world.playSound(null, entityplayer.posX, entityplayer.posY, entityplayer.posZ, ModSounds.leveractionload, SoundCategory.PLAYERS, 1.0F, 1.0F);

					}
					if (!creative)
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
			}

		}
	}

	@SideOnly(Side.CLIENT)
	public void initModel() {
		ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
	}

}