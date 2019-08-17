package com.daniel366cobra.guncraft.items;

import java.util.List;

import javax.annotation.Nullable;

import com.daniel366cobra.guncraft.GuncraftMod;
import com.daniel366cobra.guncraft.Reference;
import com.daniel366cobra.guncraft.entities.EntityGenericBullet;
import com.daniel366cobra.guncraft.init.ModItems;
import com.daniel366cobra.guncraft.init.ModSounds;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemMusket extends Item {

	private byte cockStage = 0;

	public static Item makeDefaultItem(String registryname, int durability) {
		Item.Properties props = new Item.Properties()
				.maxStackSize(1)
				.maxDamage(durability)
				.group(GuncraftMod.GUNCRAFT_WEAPONS_ITEMGROUP);
		Item item = new ItemMusket(props)
				.setRegistryName(Reference.MODID, registryname);
		return item;
	}

	public ItemMusket(Properties builder){
		super(builder);		

		this.addPropertyOverride(new ResourceLocation(Reference.MODID, "load_progress"), (itemstack, world, player) -> {
			if (player != null && itemstack.getItem() == this) {
				return isLoaded(itemstack) ? 0.0F : (float)(itemstack.getUseDuration() - player.getItemInUseCount()) / (float)getLoadTime();
			} else {
				return 0.0F;
			}
		});
		this.addPropertyOverride(new ResourceLocation(Reference.MODID, "loading"), (itemstack, world, player) -> {
			return player != null && player.isHandActive() && player.getActiveItemStack() == itemstack && !isLoaded(itemstack) ? 1.0F : 0.0F;
		});
		this.addPropertyOverride(new ResourceLocation(Reference.MODID, "loaded"), (itemstack, world, player) -> {
			return player != null && isLoaded(itemstack) ? 1.0F : 0.0F;
		});	      
	}	


	@Override
	public UseAction getUseAction(ItemStack stack)
	{
		return UseAction.BOW;

	}

	@Override
	public boolean getIsRepairable(ItemStack tool, ItemStack material)
	{
		return false;
	}

	//Maximum item usage time.
	@Override
	public int getUseDuration(ItemStack stack)
	{
		return 72000;
	}	

	/**
	 * allows items to add custom lines of information to the mouseover description
	 */
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);	     
		tooltip.add(new TranslationTextComponent("musket.ammo.type"));
		if (isLoaded(stack))
		{
			tooltip.add(new TranslationTextComponent("musket.loaded"));
		}

	}

	//Give the musket the necessary NBT flags on first handling.
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {

		if (isSelected && (stack.getTag() == null))
		{
			setLoaded(stack, false);
		}
	}


	public static boolean isLoaded(ItemStack stack) {
		CompoundNBT stackTag = stack.getTag();
		return stackTag != null && stackTag.getBoolean("loaded");
	}

	public static void setLoaded(ItemStack stack, boolean loadedIn) {
		CompoundNBT stackTag = stack.getOrCreateTag();
		stackTag.putBoolean("loaded", loadedIn);
	}

	//How much time it takes to load the musket
	public static int getLoadTime() {	      
		return 20;
	}

	//Get musket loading progress. 1.0 means finished loading
	private static float getLoadProgress(int useTime) {
		float f = (float)useTime / (float)getLoadTime();
		if (f > 1.0F) {
			f = 1.0F;
		}

		return f;
	}

	//Returns the first available ItemStack of ammunition. Returns empty ItemStack if no ammo.
	private static ItemStack findAmmo(PlayerEntity player)
	{
		boolean creative = player.abilities.isCreativeMode;
		ItemStack ammostack;

		if (isAmmo(player.getHeldItem(Hand.OFF_HAND)))
		{
			ammostack = player.getHeldItem(Hand.OFF_HAND);
		}
		else if (isAmmo(player.getHeldItem(Hand.MAIN_HAND)))
		{
			ammostack =  player.getHeldItem(Hand.MAIN_HAND);
		}
		else
		{
			for (int i = 0; i < player.inventory.getSizeInventory(); ++i)
			{
				ItemStack itemstack = player.inventory.getStackInSlot(i);

				if (isAmmo(itemstack))
				{
					ammostack = itemstack;
					return ammostack;
				}
			}

			ammostack = ItemStack.EMPTY;
		}
		if (ammostack.isEmpty() && creative)
		{
			ammostack = new ItemStack(ModItems.musket_ball);			
		}
		return ammostack;
	}

	//Is the item usable as ammunition?
	private static boolean isAmmo(ItemStack stack) {
		return stack.getItem() == ModItems.musket_ball;
	}

	//Returns true if the player is able to load the weapon (has ammo or is in creative mode).
	//Consumes 1 unit of ammo if not in creative
	private static boolean checkAndConsumeAmmo(PlayerEntity player) {

		boolean creative = player.abilities.isCreativeMode;

		ItemStack ammoStack = findAmmo(player);

		if (ammoStack.isEmpty() && creative) {
			ammoStack = new ItemStack(ModItems.musket_ball);	            
		}

		if (!attemptConsumeAmmo(player, ammoStack, creative)) {
			return false;
		}


		return true;
	}

	private static boolean attemptConsumeAmmo(PlayerEntity player, ItemStack ammoStack, boolean creative)
	{
		if (!creative) {
			if (!ammoStack.isEmpty()) //If found ammo, consume 1 unit, remove stack from inventory if empty
			{
				ammoStack.shrink(1);
				if (ammoStack.isEmpty())
				{
					player.inventory.deleteStack(ammoStack);
				}
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return true;
		}
	}

	//Sets musket in use (loading/shooting).
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
	{
		ItemStack heldStack = player.getHeldItem(hand);
		if (isLoaded(heldStack))
		{
			fire(world, player, hand, heldStack);
			setLoaded(heldStack, false);
			return new ActionResult<ItemStack>(ActionResultType.SUCCESS, heldStack);
		}
		else		
			if (!findAmmo(player).isEmpty())
			{
				if(!isLoaded(heldStack))
				{
					this.cockStage = 0;
					player.setActiveHand(hand);
				}
				return new ActionResult<ItemStack>(ActionResultType.SUCCESS, heldStack);
			}
			else
			{
				return new ActionResult<ItemStack>(ActionResultType.FAIL, heldStack);
			}
	}

	public static void fire(World world, PlayerEntity shooter, Hand hand, ItemStack stack)
	{
		boolean creative = shooter.abilities.isCreativeMode;

		if (!shooter.isInWaterRainOrBubbleColumn())
		{
			Vec3d shooterLook = shooter.getLookVec();
			Vec3d firePosition = new Vec3d(shooter.posX, shooter.posY + shooter.getEyeHeight(), shooter.posZ);
			for (int i = 0; i < 30; i++)
			{
				float velMagnModifier = 1.0F - 0.25F + random.nextFloat() * 0.5F;			

				float particleVelX = (float)shooterLook.x * velMagnModifier + (-0.15F + random.nextFloat() * 0.3F);
				float particleVelY = (float)shooterLook.y * velMagnModifier + (-0.15F + random.nextFloat() * 0.3F);
				float particleVelZ = (float)shooterLook.z * velMagnModifier + (-0.15F + random.nextFloat() * 0.3F);


				world.addParticle(ParticleTypes.CLOUD, firePosition.x, firePosition.y, firePosition.z, particleVelX, particleVelY, particleVelZ);
			}
			if (!world.isRemote)
			{
				//Create and spawn the bullet
				EntityGenericBullet bullet = new EntityGenericBullet(world, shooter, 3.0D, 1.0D, false, false);
				bullet.shoot(shooter, shooter.rotationPitch, shooter.rotationYaw, 6F, 1.0F);
				world.addEntity(bullet);

				//Shot sound
				world.playSound(null, shooter.posX, shooter.posY, shooter.posZ, ModSounds.musketshot, SoundCategory.PLAYERS, 2.0F, 1.0F);


				//Degrade the durability
				if (!creative)
				{
					stack.damageItem(1, shooter, (entity) -> {
						entity.sendBreakAnimation(shooter.getActiveHand());
					});

					//additionally have a chance to explode if very low
					DamageSource gunexplosiondamage;
					int remainingLife = stack.getMaxDamage() - stack.getDamage();
					if (remainingLife <= 10)
					{
						if (random.nextFloat()<(1.0F/(remainingLife + 1)))
						{

							shooter.inventory.deleteStack(stack);
							gunexplosiondamage = new EntityDamageSource("gunexplosion", shooter);
							world.createExplosion(shooter, shooter.posX, shooter.posY + shooter.getEyeHeight() - 0.1D, shooter.posZ, 1.5F, false, Explosion.Mode.NONE);
							shooter.attackEntityFrom(gunexplosiondamage, 18.0F);

						}
					}
				}


			}
			//recoil
			shooter.setLocationAndAngles(shooter.posX, shooter.posY, shooter.posZ, shooter.rotationYaw, shooter.rotationPitch - (random.nextFloat() * 10.0F + 10.0F));		

		}
		else
		{
			world.playSound(null, shooter.posX, shooter.posY, shooter.posZ, ModSounds.flintlockmisfire, SoundCategory.PLAYERS, 2.0F, 1.0F);

		}
	}

	public void onPlayerStoppedUsing(ItemStack stack, World world, LivingEntity entityLiving, int timeLeft)
	{
		PlayerEntity player = (PlayerEntity)entityLiving;
		if (!world.isRemote())
		{
			int timeLoading = this.getUseDuration(stack) - timeLeft;
			float loadProgress = getLoadProgress(timeLoading);

			if (loadProgress >= 0.9F && !isLoaded(stack) && checkAndConsumeAmmo(player)) {

				setLoaded(stack, true);
			}
			else //Click because the loading did not finish
			{
				world.playSound(null, player.posX, player.posY, player.posZ, ModSounds.flintlockmisfire, SoundCategory.PLAYERS, 2.0F, 1.0F);

			}
		}
	}


	public void func_219972_a(World worldIn, LivingEntity livingEntityIn, ItemStack stack, int useTime) {
		if (!worldIn.isRemote) {

			float loadProgress = (float)(stack.getUseDuration() - useTime) / (float)getLoadTime();

			GuncraftMod.LOGGER.info("Load progress = " + loadProgress);

			if (loadProgress > 0.0f && this.cockStage == 0) {
				this.cockStage = 1;	        	 
				worldIn.playSound((PlayerEntity)null, livingEntityIn.posX, livingEntityIn.posY, livingEntityIn.posZ, ModSounds.flintlockcockstart, SoundCategory.PLAYERS, 0.5F, 1.0F);

			}

			if (loadProgress >= 0.5F && this.cockStage == 1) {
				this.cockStage = 2;
				worldIn.playSound((PlayerEntity)null, livingEntityIn.posX, livingEntityIn.posY, livingEntityIn.posZ, ModSounds.flintlockcockmiddle, SoundCategory.PLAYERS, 0.5F, 1.0F);
			}

			if (loadProgress >= 0.9F && this.cockStage == 2) {
				this.cockStage = 3;
				worldIn.playSound((PlayerEntity)null, livingEntityIn.posX, livingEntityIn.posY, livingEntityIn.posZ,  ModSounds.flintlockcockfull, SoundCategory.PLAYERS, 0.5F, 1.0F);
			}
		}

	}
}