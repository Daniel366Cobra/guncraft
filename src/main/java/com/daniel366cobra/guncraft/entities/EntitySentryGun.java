package com.daniel366cobra.guncraft.entities;

import java.util.UUID;

import javax.annotation.Nullable;

import com.daniel366cobra.guncraft.init.ModItems;
import com.daniel366cobra.guncraft.init.ModSounds;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackRanged;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntitySentryGun extends EntityGolem implements IRangedAttackMob
{
	protected static final DataParameter<Optional<UUID>> OWNER_UNIQUE_ID = EntityDataManager.<Optional<UUID>>createKey(EntitySentryGun.class, DataSerializers.OPTIONAL_UNIQUE_ID);

	public EntitySentryGun(World worldIn)
	{
		super(worldIn);
		this.setSize(0.7F, 1.0F);		
	}

	public EntitySentryGun(World worldIn, EntityPlayer placer)
	{
		this(worldIn);		
		this.setOwner(placer);		
		this.setCustomNameTag(I18n.format("turretowner.name") + placer.getName());		
	}
	
	public static void registerFixesSentry(DataFixer fixer)
	{
		EntityLiving.registerFixesMob(fixer, EntitySentryGun.class);
	}

	public boolean processInteract(EntityPlayer player, EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);

		if (stack.isEmpty() && this.isOwner(player))
		{			
			if (!this.world.isRemote)
			{
				this.dropItem(ModItems.sentrybox, 1);
			}
			this.setDead();
			return true;
		}
		else
		{
			return false;
		}
	}
	
	
	
	//Retaliate against other players and their sentries
	public boolean attackEntityFrom(DamageSource source, float amount)
	{
		if (this.isEntityInvulnerable(source))
		{
			return false;
		}
		else
		{
			Entity attacker = source.getTrueSource();
			Entity attackTool = source.getImmediateSource();	
			
			boolean attackedByOwner = ((attacker instanceof EntityPlayer) && ((EntityPlayer)attacker == this.getOwner()));
			boolean attackedMelee = (attacker == attackTool);
			boolean attackExplosive = source.isExplosion();
			boolean friendlyFire = (attacker instanceof EntitySentryGun) && (((EntitySentryGun) attacker).getOwner() == this.getOwner());
			
			if (friendlyFire) return false;
			
			if (attackedByOwner) //When hit by owner, do no damage
			{
				if (attackedMelee && !attackExplosive)
				{
					return false;
				}
				else //When attacked by owner indirectly, get damage but not retaliate
				{				
					this.lastDamage = amount;
                    this.hurtResistantTime = this.maxHurtResistantTime;
                    this.damageEntity(source, amount);
                    this.maxHurtTime = 10;
                    this.hurtTime = this.maxHurtTime;
                    return true;
				}
			}
			else //If attacked by anything other than owner, retaliate
			{			
				return super.attackEntityFrom(source, amount);	
			}
		}
	}
	
	//Generate a small cloud of particles
	@SideOnly(Side.CLIENT)
    private void spawnParticles(EnumParticleTypes particleType)
    {
        for (int i = 0; i < 5; ++i)
        {
            double d0 = this.rand.nextGaussian() * 0.02D;
            double d1 = this.rand.nextGaussian() * 0.02D;
            double d2 = this.rand.nextGaussian() * 0.02D;
            this.world.spawnParticle(particleType, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + 1.0D + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, d0, d1, d2);
        }
    }
	
	//When killed, drop a sentry kit
	public void onDeath(DamageSource cause)
	{
		super.onDeath(cause);
		if (!this.world.isRemote)
		{
			this.dropItem(ModItems.sentrybox, 1);
		}
	}

	protected void initEntityAI()
	{		
		this.tasks.addTask(0, new EntityAIAttackRanged(this, 0.0D, 5, 5, 40.0F));
		this.tasks.addTask(1, new EntityAILookIdle(this));
				
		this.targetTasks.addTask(1, new EntityAINearestAttackableTarget<EntityLiving>(this, EntityLiving.class, 2, true, false, new Predicate<EntityLiving>()
		{
			public boolean apply(@Nullable EntityLiving entityTarget)
			{
				return entityTarget != null && IMob.VISIBLE_MOB_SELECTOR.apply(entityTarget) && !(entityTarget instanceof EntityEnderman);
			}
		}));
		this.targetTasks.addTask(2, new EntityAIHurtByTarget(this, false, new Class[0]));
	}

	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.0D);
	}
	
	public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor)
	{		   
		EntityGenericBullet entitybullet = new EntityGenericBullet(this.world, this, 5.0D, 0.1D, false);
		entitybullet.shoot(target.posX - this.posX, target.posY - this.posY + target.getEyeHeight() * 0.1F , target.posZ - this.posZ, 5.0F, 1.5F);
		this.playSound(ModSounds.sentryfire, 6.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
		this.world.spawnEntity(entitybullet);
	}

	public float getEyeHeight()
	{
		return 0.875F;
	}


	protected SoundEvent getDeathSound()
	{
		return SoundEvents.ENTITY_IRONGOLEM_HURT;
	}

	public void setOwnerId(@Nullable UUID owner_UUID)
	{
		this.dataManager.set(OWNER_UNIQUE_ID, Optional.fromNullable(owner_UUID));        
	}

	public void setOwner(EntityPlayer player)
	{
		this.setOwnerId(player.getUniqueID());
	}

	@Nullable
	public UUID getOwnerId()
	{
		return (UUID)((Optional<UUID>)this.dataManager.get(OWNER_UNIQUE_ID)).orNull();
	}

	@Nullable
	public EntityPlayer getOwner()
	{
		try
		{
			UUID uuid = this.getOwnerId();
			return uuid == null ? null : this.world.getPlayerEntityByUUID(uuid);
		}
		catch (IllegalArgumentException var2)
		{
			return null;
		}
	}

	public boolean isOwner(EntityLivingBase entityIn)
	{
		return entityIn == this.getOwner();
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
		this.dataManager.register(OWNER_UNIQUE_ID, Optional.absent());

	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound)
	{
		super.readEntityFromNBT(compound);
		this.setOwnerId(UUID.fromString(compound.getString("OwnerUUID")));
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound)
	{
		super.writeEntityToNBT(compound);

		if (this.getOwnerId() == null)
		{
			compound.setString("OwnerUUID", "");
		}
		else
		{
			compound.setString("OwnerUUID", this.getOwnerId().toString());
		}
	}

	@Override
	public void setSwingingArms(boolean swingingArms)
	{		
	}


}
