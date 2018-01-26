package com.daniel366cobra.guncraft.entities;

import java.util.List;

import javax.annotation.Nullable;

import com.daniel366cobra.guncraft.damagesources.DamageProjectile;
import com.daniel366cobra.guncraft.init.ModSounds;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

//UPDATED 1.12
public class EntityGenericBullet extends Entity implements IProjectile
{

	@SuppressWarnings("unchecked")
	private static final Predicate<Entity> BULLET_TARGETS = Predicates.and(EntitySelectors.NOT_SPECTATING, EntitySelectors.IS_ALIVE, new Predicate<Entity>()
	{
		@Override
		public boolean apply(@Nullable Entity entity)
		{
			return entity.canBeCollidedWith();
		}
	});

	private static final DataParameter<Boolean> INCENDIARY = EntityDataManager.<Boolean>createKey(EntityGenericBullet.class, DataSerializers.BOOLEAN);

	protected boolean inGround;
	public Entity shootingEntity;
	private int ticksInGround;
	private int ticksInAir;
	private double damage;
	private double knockbackStrength;

	public EntityGenericBullet(World world)
	{
		super(world);
		this.setDamage(2.0F);
		this.setSize(0.25F, 0.25F);
	}
	//Allows to set coordinates
	public EntityGenericBullet(World world, double x, double y, double z)
	{
		this(world);
		this.setPosition(x, y, z);
	}

	public EntityGenericBullet(World world, EntityLivingBase shooter, double damage, double knockbackStrength, boolean incendiary)
	{
		this(world, shooter.posX, shooter.posY + shooter.getEyeHeight() - 0.1D, shooter.posZ);
		this.shootingEntity = shooter;
		this.setDamage(damage);
		this.setKnockbackStrength(knockbackStrength);
		this.setIncendiary(incendiary);
	}

	/**
	 * Checks if the entity is in range to render.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance)
	{
		double d0 = this.getEntityBoundingBox().getAverageEdgeLength() * 10.0D;

		if (Double.isNaN(d0))
		{
			d0 = 1.0D;
		}

		d0 = d0 * 64.0D * getRenderDistanceWeight();
		return distance < d0 * d0;
	}


	public void shoot(Entity shooter, float pitch, float yaw, float velocity, float inaccuracy)
	{
		float f = -MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
		float f1 = -MathHelper.sin(pitch * 0.017453292F);
		float f2 = MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
		this.shoot(f, f1, f2, velocity, inaccuracy);
		this.motionX += shooter.motionX;
		this.motionZ += shooter.motionZ;

		if (!shooter.onGround)
		{
			this.motionY += shooter.motionY;
		}
	}

	/**
	 * Similar to setArrowHeading, it's point the throwable entity to a x, y, z direction.
	 */
	@Override
	public void shoot(double x, double y, double z, float velocity, float inaccuracy)
	{
		float f = MathHelper.sqrt(x * x + y * y + z * z);
		x = x / f;
		y = y / f;
		z = z / f;
		x = x + this.rand.nextGaussian() * 0.0075D * inaccuracy;
		y = y + this.rand.nextGaussian() * 0.0075D * inaccuracy;
		z = z + this.rand.nextGaussian() * 0.0075D * inaccuracy;
		x = x * velocity;
		y = y * velocity;
		z = z * velocity;
		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;
		float f1 = MathHelper.sqrt(x * x + z * z);
		this.rotationYaw = (float)(MathHelper.atan2(x, z) * (180D / Math.PI));
		this.rotationPitch = (float)(MathHelper.atan2(y, f1) * (180D / Math.PI));
		this.prevRotationYaw = this.rotationYaw;
		this.prevRotationPitch = this.rotationPitch;
		this.ticksInGround = 0;
	}

	/**
	 * Set the position and rotation values directly without any clamping.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport)
	{
		this.setPosition(x, y, z);
		this.setRotation(yaw, pitch);
	}

	/**
	 * Updates the entity motion clientside, called by packets from the server
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void setVelocity(double x, double y, double z)
	{
		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;

		if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
		{
			float f = MathHelper.sqrt(x * x + z * z);
			this.rotationPitch = (float)(MathHelper.atan2(y, f) * (180D / Math.PI));
			this.rotationYaw = (float)(MathHelper.atan2(x, z) * (180D / Math.PI));
			this.prevRotationPitch = this.rotationPitch;
			this.prevRotationYaw = this.rotationYaw;
			this.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
			this.ticksInGround = 0;
		}
	}

	//Called to update the entity's position/logic.
	@Override
	public void onUpdate()
	{
		this.lastTickPosX = this.posX;
		this.lastTickPosY = this.posY;
		this.lastTickPosZ = this.posZ;

		super.onUpdate();
		//Kill if existing for more than 40 seconds to prevent spam
		if (this.ticksExisted > 800)
		{
			this.setDead();
		}
		if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
		{
			float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * (180D / Math.PI));
			this.rotationPitch = (float)(MathHelper.atan2(this.motionY, f) * (180D / Math.PI));
			this.prevRotationYaw = this.rotationYaw;
			this.prevRotationPitch = this.rotationPitch;
		}

		if (this.inGround) //If in ground - decay quickly
		{

			this.motionX = 0.0D;
			this.motionY = 0.0D;
			this.motionZ = 0.0D;

			++this.ticksInGround;

			if (this.ticksInGround >= 5)
			{
				this.setDead();
			}



		}
		else //In air
		{
			++this.ticksInAir;
			Vec3d curPos = new Vec3d(this.posX, this.posY, this.posZ);
			Vec3d nextPos = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
			RayTraceResult traceOnPath = this.world.rayTraceBlocks(curPos, nextPos, false, true, false);
			//If hit something
			if (traceOnPath != null && traceOnPath.typeOfHit != Type.MISS)
			{
				nextPos = new Vec3d(traceOnPath.hitVec.x, traceOnPath.hitVec.y, traceOnPath.hitVec.z);
			}

			Entity entityHit = this.findEntityOnPath(curPos, nextPos);

			if (entityHit != null)
			{
				traceOnPath = new RayTraceResult(entityHit);
			}

			if (traceOnPath != null && traceOnPath.entityHit instanceof EntityPlayer)
			{
				EntityPlayer entityplayer = (EntityPlayer)traceOnPath.entityHit;

				if ((this.shootingEntity instanceof EntityPlayer && !((EntityPlayer)this.shootingEntity).canAttackPlayer(entityplayer)) || entityplayer == this.shootingEntity)
				{
					traceOnPath = null;
				}
			}

			if (traceOnPath != null)
			{
				this.onHit(traceOnPath);
			}

			EnumParticleTypes currenttrail = this.ticksExisted < 2? EnumParticleTypes.EXPLOSION_NORMAL : this.isIncendiary()? EnumParticleTypes.FLAME : EnumParticleTypes.CRIT;

			for (int k = 0; k < 8; ++k)
			{
				this.world.spawnParticle(currenttrail, this.posX + this.motionX * k / 8.0D, this.posY + this.motionY * k / 8.0D, this.posZ + this.motionZ * k / 8.0D, -this.motionX * 0.05D, -this.motionY * 0.05D, -this.motionZ * 0.05D);
			}

			this.posX += this.motionX;
			this.posY += this.motionY;
			this.posZ += this.motionZ;

			//Probably something to do with facing direction??? This is confusing.
			float XYVelMagn = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * (180D / Math.PI));

			for (this.rotationPitch =
					(float)(MathHelper.atan2(this.motionY, XYVelMagn)
							* (180D / Math.PI));
					this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
			{
				;
			}

			while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
			{
				this.prevRotationPitch += 360.0F;
			}

			while (this.rotationYaw - this.prevRotationYaw < -180.0F)
			{
				this.prevRotationYaw -= 360.0F;
			}

			while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
			{
				this.prevRotationYaw += 360.0F;
			}
			//End of confusing code block

			//Update Euler angles
			this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
			this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
			//Apply physics
			float drag = 0.99F;
			float freefallAccel = this.getGravityVelocity();
			if (this.isInWater())
			{
				for (int i = 0; i < 4; ++i)
				{
					this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX, this.posY, this.posZ, -this.motionX * 0.25D, -this.motionY * 0.25D, -this.motionZ * 0.25D);
				}

				drag = 0.6F;
			}

			//Slowing down due to drag
			this.motionX *= drag;
			this.motionY *= drag;
			this.motionZ *= drag;

			if (!this.hasNoGravity())
			{
				this.motionY -= freefallAccel;
			}

			this.setPosition(this.posX, this.posY, this.posZ);
		}
	}

	//Gets the amount of gravity to apply to the thrown entity with each tick.
	protected float getGravityVelocity()
	{
		return 0.01F;
	}

	//Called when the bullet hits a block or an entity
	protected void onHit(RayTraceResult result)
	{
		World curWorld = this.world;
		if (result.typeOfHit == Type.BLOCK) //Calculate interaction, bounce and deceleration
		{
			float vel = (float)Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);

			BlockPos hitBlockPos = result.getBlockPos();
			IBlockState hitBlockState = curWorld.getBlockState(hitBlockPos);


			if ((hitBlockState.getMaterial() == Material.GLASS || hitBlockState.getMaterial() == Material.LEAVES || hitBlockState.getMaterial() == Material.GOURD) && vel > 1.0F)
			{//break through glass, gourds and leaves

				this.motionX *= 0.5D;
				this.motionY *= 0.5D;
				this.motionZ *= 0.5D;

				if (!curWorld.isRemote)
				{
					curWorld.destroyBlock(hitBlockPos, false);
				}
			}
			else
			{

				curWorld.playSound(null, this.posX, this.posY, this.posZ, ModSounds.bullethit, SoundCategory.PLAYERS, 1.0F, 1.0F);
				this.inGround = true;
				//Have a 50% chance to ignite a hit block if incendiary
				if(this.isIncendiary() && this.rand.nextBoolean() && !curWorld.isRemote)
				{
					if (curWorld.isAirBlock(hitBlockPos.offset(result.sideHit)))
					{
						curWorld.setBlockState(hitBlockPos.offset(result.sideHit), Blocks.FIRE.getDefaultState(), 3);
					}
				}

				for (int k = 0; k <= 10; k++)
				{
					curWorld.spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX, this.posY, this.posZ, -this.motionX * 0.1F + this.rand.nextFloat(), -this.motionY * 0.1F + this.rand.nextFloat(), -this.motionZ * 0.1F + this.rand.nextFloat(), new int[] {Block.getIdFromBlock(hitBlockState.getBlock())});
				}
			}
		}
		else if (result.typeOfHit == Type.ENTITY)
		{
			Entity entityHit = result.entityHit;

			DamageSource bulletdamage;

			if (this.shootingEntity == null)
			{
				bulletdamage  = new DamageProjectile("bullet", this, null);
			}
			else
			{
				bulletdamage = new DamageProjectile("bullet", this, this.shootingEntity);
			}

			if(this.isIncendiary() && entityHit != this.shootingEntity && this.ticksExisted > 2)
			{
				entityHit.setFire(10);
			}

			double totalDamage = this.getDamage();

			//Headshot and close-range hit detection
			if ((this.posY >= (entityHit.posY + entityHit.height * 0.7F)) || (this.ticksExisted <= 3))
			{
				totalDamage *= 10.0D;
			}

			if (entityHit.attackEntityFrom(bulletdamage, (float)totalDamage))
			{
				//Drop hurt resistance time to zero - for usage in shotguns
				entityHit.hurtResistantTime = 0;

				if (entityHit instanceof EntityLivingBase)
				{
					EntityLivingBase entityLivingHit = (EntityLivingBase)entityHit;


					if (this.knockbackStrength > 0)
					{
						float horizVel = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);

						if (horizVel > 0.0F)
						{
							entityLivingHit.addVelocity(this.motionX * this.knockbackStrength * 0.3D / horizVel, 0.1D, this.motionZ * this.knockbackStrength * 0.6D / horizVel);
						}
					}



					if (this.shootingEntity != null && entityLivingHit != this.shootingEntity && entityLivingHit instanceof EntityPlayer && this.shootingEntity instanceof EntityPlayerMP)
					{
						((EntityPlayerMP)this.shootingEntity).connection.sendPacket(new SPacketChangeGameState(6, 0.0F));
					}
				}

				this.setDead();
			}

		}
		else
		{
			return;
		}
	}

	@Nullable
	protected Entity findEntityOnPath(Vec3d start, Vec3d end)
	{
		Entity entity = null;
		List<Entity> list = this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1.0D), BULLET_TARGETS);
		double d0 = 0.0D;

		for (int i = 0; i < list.size(); ++i)
		{
			Entity entity1 = list.get(i);

			if (entity1 != this.shootingEntity || this.ticksInAir >= 5)
			{
				AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow(0.3D);
				RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(start, end);

				if (raytraceresult != null)
				{
					double d1 = start.squareDistanceTo(raytraceresult.hitVec);

					if (d1 < d0 || d0 == 0.0D)
					{
						entity = entity1;
						d0 = d1;
					}
				}
			}
		}

		return entity;
	}



	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	@Override
	public void writeEntityToNBT(NBTTagCompound compound)
	{
		compound.setShort("life", (short)this.ticksInGround);
		compound.setByte("inGround", (byte)(this.inGround ? 1 : 0));
		compound.setDouble("damage", this.damage);
		compound.setBoolean("incendiary", this.isIncendiary());
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	@Override
	public void readEntityFromNBT(NBTTagCompound compound)
	{
		this.ticksInGround = compound.getShort("life");
		this.inGround = compound.getByte("inGround") == 1;

		if (compound.hasKey("damage", 99))
		{
			this.damage = compound.getDouble("damage");
		}
		this.setIncendiary(compound.getBoolean("incendiary"));
	}


	/**
	 * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
	 * prevent them from trampling crops
	 */
	@Override
	protected boolean canTriggerWalking()
	{
		return false;
	}

	public void setDamage(double damage)
	{
		this.damage = damage;
	}

	public double getDamage()
	{
		return this.damage;
	}

	/**
	 * Sets the amount of knockback the bullet applies when it hits a mob.
	 */
	public void setKnockbackStrength(double knockbackStrength)
	{
		this.knockbackStrength = knockbackStrength;
	}

	/**
	 * Returns true if it's possible to attack this entity with an item.
	 */
	@Override
	public boolean canBeAttackedWithItem()
	{
		return false;
	}

	@Override
	public float getEyeHeight()
	{
		return 0.0F;
	}
	@Override
	protected void entityInit()
	{
		this.dataManager.register(INCENDIARY, Boolean.valueOf(false));

	}
	public boolean isIncendiary()
	{
		return this.dataManager.get(INCENDIARY);
	}
	public void setIncendiary(boolean incendiary)
	{
		this.dataManager.set(INCENDIARY, incendiary);
	}
}