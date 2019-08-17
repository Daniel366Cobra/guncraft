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
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemBreakActionShotgun extends Item {

	//TODO: BUGS. Two stacks of same gun load simultaneously.	
	
	private boolean leftBarrelLoaded = false;
	private boolean rightBarrelLoaded = false;
	private boolean open = false;


	public static Item makeDefaultItem(String registryname, int durability) {
		Item.Properties props = new Item.Properties()
				.maxStackSize(1)
				.maxDamage(durability)
				.group(GuncraftMod.GUNCRAFT_WEAPONS_ITEMGROUP);
		Item item = new ItemBreakActionShotgun(props)
				.setRegistryName(Reference.MODID, registryname);
		return item;
	}

	public ItemBreakActionShotgun(Properties builder){
		super(builder);	
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
		tooltip.add(new TranslationTextComponent("shotgun.ammo.type"));

		if (stack.hasTag())
		{
			String leftBarrelAmmo = getLeftBarrelLoaded(stack);
			String rightBarrelAmmo = getRightBarrelLoaded(stack);

			tooltip.add(new TranslationTextComponent("shotgun.leftbarrelloaded"));
			tooltip.add(new TranslationTextComponent(leftBarrelAmmo));

			tooltip.add(new TranslationTextComponent("shotgun.rightbarrelloaded"));
			tooltip.add (new TranslationTextComponent(rightBarrelAmmo));

		}
	}	

	public static String getLeftBarrelLoaded(ItemStack stack) {
		CompoundNBT tag = stack.getTag();
		return tag.getString("leftBarrel");
		
	}
	public static String getRightBarrelLoaded(ItemStack stack) {
		CompoundNBT tag = stack.getTag();
		return tag.getString("rightBarrel");
	}

	public static void setLeftBarrelLoaded(ItemStack stack, String ammoType) {
		CompoundNBT tag = stack.getOrCreateTag();
		tag.putString("leftBarrel", ammoType);
	}

	public static void setRightBarrelLoaded(ItemStack stack, String ammoType) {
		CompoundNBT tag = stack.getOrCreateTag();
		tag.putString("rightBarrel", ammoType);
	}

	//Set both barrels unloaded initially.		
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {

		if (!stack.hasTag())
		{
			setLeftBarrelLoaded(stack, "");
			setRightBarrelLoaded(stack, "");			
		}
	}

	//How much time it takes to load the shotgun
	public static int getLoadTime() {	      
		return 30;
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
			ammostack = new ItemStack(ModItems.shotgun_shell_buckshot);			
		}
		return ammostack;
	}

	//Is the item usable as ammunition?
	private static boolean isAmmo(ItemStack stack) {
		Item stackItem = stack.getItem();
		return stackItem == ModItems.shotgun_shell_buckshot || stackItem == ModItems.shotgun_shell_incendiary || stackItem == ModItems.shotgun_shell_slug;
	}

	//Returns true if the player is able to load the weapon (has ammo or is in creative mode).
	//Consumes 1 unit of ammo if not in creative
	private static String checkAndConsumeAmmo(PlayerEntity player) {

		boolean creative = player.abilities.isCreativeMode;

		ItemStack ammoStack = findAmmo(player);

		if (ammoStack.isEmpty() && creative) {
			ammoStack = new ItemStack(ModItems.shotgun_shell_buckshot);	            
		}

		if (!attemptConsumeAmmo(player, ammoStack, creative)) {
			return "";
		}

		Item ammoType = ammoStack.getItem();

		if (ammoType == ModItems.shotgun_shell_buckshot) return "ammo.buck";
		if (ammoType == ModItems.shotgun_shell_incendiary) return "ammo.inc";
		if (ammoType == ModItems.shotgun_shell_slug) return "ammo.slug";
		return "";

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

	//Sets shotgun in use (loading/shooting).	
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
	{
		ItemStack heldStack = player.getHeldItem(hand);

		if (!this.open)
		{
			String leftBarrelAmmo = getLeftBarrelLoaded(heldStack);
			String rightBarrelAmmo = getRightBarrelLoaded(heldStack);

			if (!leftBarrelAmmo.isEmpty())
			{
				fire(world, player, hand, heldStack, leftBarrelAmmo);
				setLeftBarrelLoaded(heldStack, "");
				this.leftBarrelLoaded = false;
				player.getCooldownTracker().setCooldown(this, 10);
				return new ActionResult<ItemStack>(ActionResultType.SUCCESS, heldStack);
			}
			else if (!rightBarrelAmmo.isEmpty())			
			{
				fire(world, player, hand, heldStack, rightBarrelAmmo);
				setRightBarrelLoaded(heldStack, "");
				this.rightBarrelLoaded = false;
				player.getCooldownTracker().setCooldown(this, 10);
				return new ActionResult<ItemStack>(ActionResultType.SUCCESS, heldStack);
			}
			else
				if (!findAmmo(player).isEmpty())
				{					
					player.setActiveHand(hand);

					return new ActionResult<ItemStack>(ActionResultType.SUCCESS, heldStack);
				}
				else
				{
					world.playSound(null, player.posX, player.posY, player.posZ, ModSounds.breakactiondryfire, SoundCategory.PLAYERS, 2.0F, 1.0F);

				}
		}

		return new ActionResult<ItemStack>(ActionResultType.FAIL, heldStack);
	}

	public static void fire(World world, PlayerEntity shooter, Hand hand, ItemStack stack, String ammoType)
	{
		boolean creative = shooter.abilities.isCreativeMode;


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
			switch(ammoType)
			{
			case "ammo.buck":
			{
				for (int i = 0; i < 9; i++)
				{
					EntityGenericBullet bullet = new EntityGenericBullet(world, shooter, 0.5D, 0.25D, true, false);
					bullet.shoot(shooter, shooter.rotationPitch, shooter.rotationYaw, 6F, 5F);
					world.addEntity(bullet);
				}
				break;
			}
			case "ammo.inc":
			{
				for (int i = 0; i < 4; i++)
				{
					EntityGenericBullet bullet = new EntityGenericBullet(world, shooter, 1.0D, 0.25D, true, true);
					bullet.shoot(shooter, shooter.rotationPitch, shooter.rotationYaw, 6F, 5F);
					world.addEntity(bullet);
				}
				break;

			}
			case "ammo.slug":
			{
				EntityGenericBullet bullet = new EntityGenericBullet(world, shooter, 6.0D, 3.0D, false, false);
				bullet.shoot(shooter, shooter.rotationPitch, shooter.rotationYaw, 5F, 4F);
				world.addEntity(bullet);

				break;
			}
			default:
				break;

			}

			//Shot sound
			world.playSound(null, shooter.posX, shooter.posY, shooter.posZ, ModSounds.breakactionfire, SoundCategory.PLAYERS, 2.0F, 1.0F);


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

	public void onPlayerStoppedUsing(ItemStack stack, World world, LivingEntity entityLiving, int timeLeft)
	{
		PlayerEntity player = (PlayerEntity)entityLiving;

		if (!world.isRemote())
		{
			String ammo = checkAndConsumeAmmo(player);

			if (!ammo.isEmpty() && this.leftBarrelLoaded)
			{					
				setLeftBarrelLoaded(stack, ammo);
				this.leftBarrelLoaded = false;
			}

			ammo = checkAndConsumeAmmo(player);

			if (!ammo.isEmpty() && this.rightBarrelLoaded)
			{
				setRightBarrelLoaded(stack, ammo);
				this.rightBarrelLoaded = false;
			}
			if (this.open)
			{
				world.playSound((PlayerEntity)null, player.posX, player.posY, player.posZ,  ModSounds.breakactionclose, SoundCategory.PLAYERS, 0.5F, 1.0F);

				GuncraftMod.LOGGER.info("Closed gun");


				this.open = false;
			}
			else
			{
				world.playSound((PlayerEntity)null, player.posX, player.posY, player.posZ,  ModSounds.breakactiondryfire, SoundCategory.PLAYERS, 0.5F, 1.0F);

			}

		}
	}

	//Tick while in use?????
	public void func_219972_a(World worldIn, LivingEntity livingEntityIn, ItemStack stack, int useTime)
	{
		//STILL SKETCHY AF, RELOAD IN HERE POSSIBLE????
		//PLAY WITH SETTING NBT IN HERE.

		if (!worldIn.isRemote) {

			float loadProgress = (float)(stack.getUseDuration() - useTime) / (float)getLoadTime();
			GuncraftMod.LOGGER.info("Load progress = " + loadProgress);
			if (loadProgress > 0.1f && !this.open && !this.leftBarrelLoaded) { //Open shotgun
				this.open = true;     	 
				worldIn.playSound((PlayerEntity)null, livingEntityIn.posX, livingEntityIn.posY, livingEntityIn.posZ, ModSounds.breakactionopen, SoundCategory.PLAYERS, 0.5F, 1.0F);
				GuncraftMod.LOGGER.info("Opened gun");
			}

			if (loadProgress >= 0.5F && this.open && !this.leftBarrelLoaded) { //Load left barrel


				this.leftBarrelLoaded = true;
				worldIn.playSound((PlayerEntity)null, livingEntityIn.posX, livingEntityIn.posY, livingEntityIn.posZ, ModSounds.breakactionshellload, SoundCategory.PLAYERS, 0.5F, 1.0F);

				GuncraftMod.LOGGER.info("Loaded left barrel");
			}

			if (loadProgress >= 0.9F && this.open && !this.rightBarrelLoaded) { //Load right barrel



				this.rightBarrelLoaded = true;
				worldIn.playSound((PlayerEntity)null, livingEntityIn.posX, livingEntityIn.posY, livingEntityIn.posZ,  ModSounds.breakactionshellload, SoundCategory.PLAYERS, 0.5F, 1.0F);

				GuncraftMod.LOGGER.info("Loaded right barrel");
			}		

		}

	}
}

