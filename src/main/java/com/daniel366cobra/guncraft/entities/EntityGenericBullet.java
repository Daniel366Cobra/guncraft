package com.daniel366cobra.guncraft.entities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.daniel366cobra.guncraft.damagesources.DamageProjectile;
import com.daniel366cobra.guncraft.init.ModEntities;
import com.daniel366cobra.guncraft.init.ModSounds;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

//UPDATED 1.14
public class EntityGenericBullet extends Entity implements IProjectile
{
	private static final DataParameter<Boolean> INCENDIARY = EntityDataManager.<Boolean>createKey(EntityGenericBullet.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Byte> RICOCHET_TIMER = EntityDataManager.<Byte>createKey(EntityGenericBullet.class, DataSerializers.BYTE);

	private static final Map<Material, Float> materialDragMultipliers = fillMaterialDragValues();

	private static final HashSet<Material> passthroughMaterials = fillPassthroughMaterials();

	private static final HashSet<Material> breakableMaterials = fillBreakableMaterials();

	protected boolean inGround = false;
	private boolean noRicochet = false;
	public UUID shootingEntityUUID;
	private int ticksInGround = 0;
	private int ticksInAir = 0;
	private double baseDamage = 2.0D;
	private double knockbackStrength = 1.0D;
	//private Vec3d previousPosition;

	public EntityGenericBullet(EntityType<? extends EntityGenericBullet> type, World world)
	{
		super(type, world);
	}

	public EntityGenericBullet(World world) {
		super(ModEntities.genericbullet, world);
		this.setRicochetTimer((byte) 0);
	}

	//Allows to set coordinates	
	public EntityGenericBullet(World world, double x, double y, double z)
	{
		this(world);
		this.setPosition(x, y, z);
	}

	public EntityGenericBullet(World world, LivingEntity shooter, double baseDamage, double knockbackStrength, boolean noRicochet, boolean incendiary)
	{
		this(world, shooter.posX, shooter.posY + shooter.getEyeHeight() - 0.1D, shooter.posZ);
		this.setShooter(shooter);
		this.setDamage(baseDamage);
		this.setKnockbackStrength(knockbackStrength);
		this.setNoRicochet(noRicochet);
		this.setIncendiary(incendiary);
		//this.previousPosition = new Vec3d(this.posX, this.posY, this.posZ);
		//		if (!this.world.isRemote())
		//		{
		//			GuncraftMod.LOGGER.info(String.format(
		//					"Bullet Init: Current position: (%f, %f, %f)",
		//					this.posX, this.posY, this.posZ
		//					));
		//			GuncraftMod.LOGGER.info(String.format(
		//					"Bullet Init: Previous position: (%f, %f, %f)",
		//					this.previousPosition.x, this.previousPosition.y, this.previousPosition.z
		//					));
		//		}
	}

	private static final HashMap<Material, Float> fillMaterialDragValues()
	{
		HashMap<Material, Float> materialDragMap = new HashMap<Material, Float>();

		materialDragMap.put(Material.BAMBOO, 0.7f);
		materialDragMap.put(Material.CACTUS, 0.6f);
		materialDragMap.put(Material.CAKE, 0.8f);
		materialDragMap.put(Material.CARPET, 0.8f);
		materialDragMap.put(Material.GLASS, 0.5f);
		materialDragMap.put(Material.GOURD, 0.6f);
		materialDragMap.put(Material.LEAVES, 0.7f);
		materialDragMap.put(Material.REDSTONE_LIGHT, 0.5f);
		materialDragMap.put(Material.SNOW, 0.8f);
		materialDragMap.put(Material.SNOW_BLOCK, 0.7f);
		materialDragMap.put(Material.SPONGE, 0.6f);

		materialDragMap.put(Material.WOOL, 0.6f);
		return materialDragMap;
	}



	private static final HashSet<Material> fillBreakableMaterials()
	{
		HashSet<Material> breakableMaterialsSet = new HashSet<Material>();

		breakableMaterialsSet.add(Material.GLASS);
		breakableMaterialsSet.add(Material.GOURD);
		breakableMaterialsSet.add(Material.CAKE);
		breakableMaterialsSet.add(Material.REDSTONE_LIGHT);

		return breakableMaterialsSet;
	}

	private static final HashSet<Material> fillPassthroughMaterials()
	{
		HashSet<Material> passthroughMaterialsSet = new HashSet<Material>();

		passthroughMaterialsSet.addAll(fillBreakableMaterials());

		passthroughMaterialsSet.add(Material.BAMBOO);
		passthroughMaterialsSet.add(Material.CACTUS);
		passthroughMaterialsSet.add(Material.CARPET);
		passthroughMaterialsSet.add(Material.LEAVES);
		passthroughMaterialsSet.add(Material.SNOW);
		passthroughMaterialsSet.add(Material.SNOW_BLOCK);
		passthroughMaterialsSet.add(Material.SPONGE);		
		passthroughMaterialsSet.add(Material.WOOL);

		return passthroughMaterialsSet;
	}

	/**
	 * Checks if the entity is in range to render.
	 */
	/**
	 * Checks if the entity is in range to render.
	 */
	@OnlyIn(Dist.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		double d0 = this.getBoundingBox().getAverageEdgeLength() * 10.0D;
		if (Double.isNaN(d0)) {
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
		Vec3d motion = new Vec3d(this.getMotion().x + shooter.getMotion().x, this.getMotion().y, this.getMotion().z + shooter.getMotion().z);
		if (!shooter.onGround) {

			motion = new Vec3d(motion.x, motion.y + shooter.getMotion().y, motion.z);
		}
		this.setMotion(motion);

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
		Vec3d motion = new Vec3d(x, y, z);
		this.setMotion(motion);
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
	@OnlyIn(Dist.CLIENT)
	public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport)
	{
		this.setPosition(x, y, z);
		this.setRotation(yaw, pitch);
	}

	/**
	 * Updates the entity motion clientside, called by packets from the server
	 */
	@Override
	@OnlyIn(Dist.CLIENT)
	public void setVelocity(double x, double y, double z)
	{
		this.setMotion(x,y,z);	


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

	@Nullable
	protected EntityRayTraceResult findEntitiesOnPath(Vec3d startVec, Vec3d endVec) {
		return ProjectileHelper.func_221271_a(this.world, this, startVec, endVec, this.getBoundingBox().expand(this.getMotion()).grow(1.0D), (validTarget) -> {
			return !validTarget.isSpectator() && validTarget.isAlive() && validTarget.canBeCollidedWith() && (validTarget != this.getShooter() || this.ticksInAir >= 3);
		});
	}

	public void tick() {
		//		if (!this.world.isRemote())
		//		{
		//			if (this.previousPosition != null) {
		//				GuncraftMod.LOGGER.info(String.format(
		//						"Bullet Tick: Previous position: (%f, %f, %f)",
		//						this.previousPosition.x, this.previousPosition.y, this.previousPosition.z
		//						));
		//			} else {
		//				GuncraftMod.LOGGER.info("No previous position set");
		//			}
		//		}

		//To prevent "phasing" through blocks and sticking after a ricochet
		if((this.getRicochetTimer()) > 0 && (!this.world.isRemote))
		{
			this.setRicochetTimer((byte) (this.getRicochetTimer() - (byte)1));
		}

		super.tick();

		++this.ticksExisted;

		this.tryDespawn();     

		Vec3d motionVec = this.getMotion();
		//Set initial values for previous rotations, I guess
		if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
			float horizVelocity = MathHelper.sqrt(motionVec.x * motionVec.x + motionVec.z * motionVec.z);
			this.rotationYaw = (float)(MathHelper.atan2(motionVec.x, motionVec.z) * (double)(180F / (float)Math.PI));
			this.rotationPitch = (float)(MathHelper.atan2(motionVec.y, (double)horizVelocity) * (double)(180F / (float)Math.PI));
			this.prevRotationYaw = this.rotationYaw;
			this.prevRotationPitch = this.rotationPitch;
		}

		if (this.inGround) {
			this.ticksInAir = 0;
			++this.ticksInGround;
			this.setMotion(Vec3d.ZERO);
		}
		else
		{    	 
			this.ticksInGround = 0;
			++this.ticksInAir;
			Vec3d positionVec = new Vec3d(this.posX, this.posY, this.posZ);
			Vec3d nextPositionVec = positionVec.add(motionVec);
			RayTraceResult obstacleTraceResult = this.world.rayTraceBlocks(new RayTraceContext(positionVec, nextPositionVec, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));
			if (obstacleTraceResult.getType() != RayTraceResult.Type.MISS) {
				nextPositionVec = obstacleTraceResult.getHitVec();
			}

			while(this.isAlive()) {
				EntityRayTraceResult entityraytraceresult = this.findEntitiesOnPath(positionVec, nextPositionVec);
				if (entityraytraceresult != null) {
					obstacleTraceResult = entityraytraceresult;
				}

				if (obstacleTraceResult != null && obstacleTraceResult.getType() == RayTraceResult.Type.ENTITY) {
					Entity entity = ((EntityRayTraceResult)obstacleTraceResult).getEntity();
					Entity entity1 = this.getShooter();
					if (entity instanceof PlayerEntity && entity1 instanceof PlayerEntity && !((PlayerEntity)entity1).canAttackPlayer((PlayerEntity)entity)) {
						obstacleTraceResult = null;
						entityraytraceresult = null;
					}
				}

				if (obstacleTraceResult != null) {
					this.onHit(obstacleTraceResult);
					this.isAirBorne = true;
				}

				if (entityraytraceresult == null) {
					break;
				}

				obstacleTraceResult = null;
			}

			motionVec = this.getMotion();
			double motionX = motionVec.x;
			double motionY = motionVec.y;
			double motionZ = motionVec.z;

			BasicParticleType currenttrail = this.isIncendiary()? ParticleTypes.FLAME : ParticleTypes.END_ROD;

			if (this.ticksExisted > 2)
			{
				this.world.addParticle(currenttrail, true, this.posX, this.posY, this.posZ, -this.getMotion().x * 0.05D, -this.getMotion().y * 0.05D, -this.getMotion().z * 0.05D);
				this.world.addParticle(currenttrail, true, this.lastTickPosX, this.lastTickPosY, this.lastTickPosZ, -this.getMotion().x * 0.05D, -this.getMotion().y * 0.05D, -this.getMotion().z * 0.05D);

			}			

			this.posX += motionX;
			this.posY += motionY;
			this.posZ += motionZ;

			double horizVel = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);

			this.rotationYaw = (float)(MathHelper.atan2(motionX, motionZ) * (180D / Math.PI));

			for(this.rotationPitch = (float)(MathHelper.atan2(motionY, horizVel) * (180D / Math.PI)); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
				;
			}

			while(this.rotationPitch - this.prevRotationPitch >= 180.0F) {
				this.prevRotationPitch += 360.0F;
			}

			while(this.rotationYaw - this.prevRotationYaw < -180.0F) {
				this.prevRotationYaw -= 360.0F;
			}

			while(this.rotationYaw - this.prevRotationYaw >= 180.0F) {
				this.prevRotationYaw += 360.0F;
			}

			this.rotationPitch = MathHelper.lerp(0.2F, this.prevRotationPitch, this.rotationPitch);
			this.rotationYaw = MathHelper.lerp(0.2F, this.prevRotationYaw, this.rotationYaw);

			double drag = this.getAirDrag();

			if (this.isInWater()) {
				for(int j = 0; j < 4; ++j) {

					this.world.addParticle(ParticleTypes.BUBBLE, this.posX - motionX * 0.25D, this.posY - motionY * 0.25D, this.posZ - motionZ * 0.25D, motionX, motionY, motionZ);
				}

				drag = this.getWaterDrag();
			}

			motionVec = motionVec.scale(drag);

			if (!this.hasNoGravity()) {
				motionVec = new Vec3d(motionVec.x, motionVec.y - this.getGravityAccel(), motionVec.z);
			}

			this.setMotion(motionVec);
			this.setPosition(this.posX, this.posY, this.posZ);
			this.doBlockCollisions();
			//this.previousPosition = new Vec3d(this.posX, this.posY, this.posZ);
		}
	}


	/**
	 * Despawn after 5 ticks in ground or 200 ticks total lifetime
	 */
	protected void tryDespawn() {

		if (!this.world.isRemote) {	   

			if ((this.ticksInGround >= 5) || (this.ticksExisted > 200))
			{
				this.remove();
			}
		}
	}


	/**
	 * Get the gravitational acceleration for the projectile.
	 */
	protected float getGravityAccel()
	{
		return 0.01F;
	}

	/**
	 * Called when the projectile impacts something.
	 */
	protected void onHit(RayTraceResult hitResult)
	{		
		if (hitResult.getType() == Type.BLOCK) //Calculate interaction, bounce and deceleration
		{
			processBlockHit(hitResult);
		}
		else if (hitResult.getType() == Type.ENTITY)
		{
			processEntityHit(hitResult);		
		}
		else
		{
			return;
		}

	}

	/**
	 * Processes block impacts.
	 */
	protected void processBlockHit(RayTraceResult hitResult) {

		World curWorld = this.world;

		Vec3d curMotion = this.getMotion();

		double motionX = curMotion.x;
		double motionY = curMotion.y;
		double motionZ = curMotion.z;

		double velMagnitude = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);

		BlockRayTraceResult hitBlockResult = (BlockRayTraceResult)hitResult;

		BlockPos hitBlockPos = hitBlockResult.getPos();
		BlockState hitBlockState = curWorld.getBlockState(hitBlockPos);
		Material hitMaterial = hitBlockState.getMaterial();

		boolean hitPassthroughMaterial = passthroughMaterials.contains(hitMaterial);

		boolean hitBreakableMaterial = breakableMaterials.contains(hitMaterial);

		if (hitPassthroughMaterial && velMagnitude > 1.0D)
		{//Slow down in passthrough blocks
			this.inGround = false;

			float dragMultiplier = materialDragMultipliers.get(hitMaterial);

			motionX *= dragMultiplier;
			motionY *= dragMultiplier;
			motionZ *= dragMultiplier;
			//double velNew = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);

			//GuncraftMod.LOGGER.info("Changed velocity from " + velMagnitude + " to " + velNew);

			//Destroy passthrough breakable blocks
			if (hitBreakableMaterial && !curWorld.isRemote)
			{
				curWorld.destroyBlock(hitBlockPos, false);
			}
		}
		else

		{		
			if (!this.isNoRicochet())
			{
				//Ricochet detection
				double hitAngle = 0.0D;
				double vecHitInPlane = 0.0D;
				double vecHitNormal = 0.0D;
				float multX = 1.0F;
				float multY = 1.0F;
				float multZ = 1.0F;
				//Which side of block did the entity hit?
				boolean hitAlongX = (hitBlockResult.getFace() == Direction.WEST || hitBlockResult.getFace() == Direction.EAST);
				boolean hitAlongZ = (hitBlockResult.getFace() == Direction.NORTH || hitBlockResult.getFace() == Direction.SOUTH);
				boolean hitAlongY = (hitBlockResult.getFace() == Direction.DOWN || hitBlockResult.getFace() == Direction.UP);

				if(hitAlongX)
				{
					vecHitInPlane = Math.sqrt(motionY * motionY + motionZ * motionZ);
					vecHitNormal = motionX;
					multX = -0.5F;
					multY = 0.5F;
					multZ = 0.5F;
				}
				else if (hitAlongZ)
				{
					vecHitInPlane = Math.sqrt(motionX * motionX + motionY * motionY);
					vecHitNormal = motionZ;
					multX = 0.5F;
					multY = 0.5F;
					multZ = -0.5F;
				}
				else if (hitAlongY)
				{
					vecHitInPlane = Math.sqrt(motionX * motionX + motionZ * motionZ);
					vecHitNormal = motionY;
					multX = 0.5F;
					multY = -0.5F;
					multZ = 0.5F;
				}
				//Compute angle from cathets
				hitAngle = Math.abs(Math.atan2(vecHitNormal, vecHitInPlane) * 180.0D / Math.PI);

				if (hitAngle <= 30.0D && velMagnitude > 3.0F) //Ricochet
				{
					if (!curWorld.isRemote)
					{
						this.setRicochetTimer((byte) 10);
					}
					curWorld.playSound(null, this.posX, this.posY, this.posZ, ModSounds.bulletricochet, SoundCategory.PLAYERS, 1.0F, 1.0F);
					motionX *= multX;
					motionY *= multY;
					motionZ *= multZ;					
				}
			}
			else //embed in block
			{
				if (this.getRicochetTimer() <= 0 || velMagnitude <= 3.0F)
				{

					curWorld.playSound(null, this.posX, this.posY, this.posZ, ModSounds.bullethit, SoundCategory.PLAYERS, 1.0F, 1.0F);
					this.inGround = true;
					//Have a 50% chance to ignite a hit block if incendiary
					if(this.isIncendiary() && this.rand.nextBoolean() && !curWorld.isRemote)
					{
						if (curWorld.isAirBlock(hitBlockPos.offset(hitBlockResult.getFace())))
						{
							curWorld.setBlockState(hitBlockPos.offset(hitBlockResult.getFace()), Blocks.FIRE.getDefaultState(), 3);
						}
					}

					for (int k = 0; k <= 5; k++)
					{
						curWorld.addParticle(new BlockParticleData(ParticleTypes.BLOCK, hitBlockState), this.posX, this.posY, this.posZ, -this.getMotion().x * 0.1F + this.rand.nextFloat(), -this.getMotion().y * 0.1F + this.rand.nextFloat(), -this.getMotion().z * 0.1F + this.rand.nextFloat());
					}
				}
			}
		}
		this.setMotion(new Vec3d(motionX, motionY, motionZ));
	}

	/**
	 * Processes entity impacts.
	 */
	protected void processEntityHit(RayTraceResult hitResult) {		

		EntityRayTraceResult hitEntityResult = (EntityRayTraceResult)hitResult;

		Entity entityHit = hitEntityResult.getEntity();
		Entity shooter = this.getShooter();

		DamageSource bulletdamage;

		if (shooter == null)
		{
			bulletdamage  = new DamageProjectile("bullet", this, null);
		}
		else
		{
			bulletdamage = new DamageProjectile("bullet", this, shooter);
			if (entityHit instanceof LivingEntity) {
				((LivingEntity)shooter).setLastAttackedEntity(entityHit);
			}
		}

		if(this.isIncendiary() && entityHit != shooter && this.ticksExisted > 2)
		{
			entityHit.setFire(10);
		}

		double totalDamage = this.getBaseDamage() * this.getMotion().length();

		//Headshot and close-range hit detection

		if (((this.posY + this.getHeight() * 0.5F) >= (entityHit.posY + entityHit.getHeight() * 0.75F)) || (this.ticksExisted <= 3))
		{
			totalDamage *= 2.0D;			
		}

		if (entityHit.attackEntityFrom(bulletdamage, (float)totalDamage))
		{
			if (entityHit instanceof LivingEntity)				
			{
				//Drop hurt resistance time to zero - for usage in shotguns
				entityHit.hurtResistantTime = 0;

				LivingEntity entityLivingHit = (LivingEntity)entityHit;


				if (this.knockbackStrength > 0)
				{
					float horizVel = MathHelper.sqrt(this.getMotion().x * this.getMotion().x + this.getMotion().z * this.getMotion().z);

					if (horizVel > 0.0F)
					{
						entityLivingHit.addVelocity(this.getMotion().x * this.knockbackStrength * 0.3D / horizVel, 0.1D, this.getMotion().z * this.knockbackStrength * 0.6D / horizVel);
					}
				}



				if (this.getShooter() != null && entityLivingHit != this.getShooter() && entityLivingHit instanceof PlayerEntity && this.getShooter() instanceof ServerPlayerEntity)
				{
					((ServerPlayerEntity)this.getShooter()).connection.sendPacket(new SChangeGameStatePacket(6, 0.0F));
				}				
			}

		}
		this.remove();

	}

	@Nullable
	protected EntityRayTraceResult traceEntitiesOnPath(Vec3d startVec, Vec3d endVec) {
		return ProjectileHelper.func_221271_a(this.world, this, startVec, endVec, this.getBoundingBox().expand(this.getMotion()).grow(1.0D), (target) -> {
			return !target.isSpectator() && target.isAlive() && target.canBeCollidedWith() && (target != this.getShooter() || this.ticksInAir >= 3);
		});
	}



	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	@Override
	public void writeAdditional(CompoundNBT compound)
	{
		compound.putShort("groundlife", (short)this.ticksInGround);
		compound.putShort("life", (short)this.ticksExisted);
		compound.putByte("inGround", (byte)(this.inGround ? 1 : 0));
		compound.putDouble("damage", this.baseDamage);
		compound.putBoolean("incendiary", this.isIncendiary());
		compound.putByte("ricochet", this.getRicochetTimer());
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	@Override
	public void readAdditional(CompoundNBT compound)
	{
		this.ticksInGround = compound.getShort("groundlife");
		this.ticksExisted = compound.getShort("life");
		this.inGround = compound.getByte("inGround") == 1;

		if (compound.contains("damage", 99))
		{
			this.baseDamage = compound.getDouble("damage");
		}
		this.setIncendiary(compound.getBoolean("incendiary"));
		this.setRicochetTimer(compound.getByte("ricochet"));
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

	protected float getEyeHeight(Pose pose, EntitySize entitySize) {
		return 0.0F;
	}

	public void setDamage(double damage)
	{
		this.baseDamage = damage;
	}

	public double getBaseDamage()
	{
		return this.baseDamage;
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

	@Nullable
	public Entity getShooter() {
		return this.shootingEntityUUID != null && this.world instanceof ServerWorld ? ((ServerWorld)this.world).getEntityByUuid(this.shootingEntityUUID) : null;
	}

	public void setShooter(@Nullable Entity shooter) {
		this.shootingEntityUUID = shooter == null ? null : shooter.getUniqueID();	      

	}

	public boolean isIncendiary()
	{
		return this.dataManager.get(INCENDIARY);
	}

	public void setIncendiary(boolean incendiary)
	{
		this.dataManager.set(INCENDIARY, incendiary);
	}

	private byte getRicochetTimer()
	{
		return this.dataManager.get(RICOCHET_TIMER);
	}

	private void setRicochetTimer(Byte ricochetTimer)
	{
		this.dataManager.set(RICOCHET_TIMER, ricochetTimer);
	}

	protected float getWaterDrag() {
		return 0.6F;
	}

	protected float getAirDrag() {
		return 0.99F;
	}

	protected void registerData() {

		this.dataManager.register(INCENDIARY, Boolean.valueOf(false));
		this.dataManager.register(RICOCHET_TIMER, Byte.valueOf((byte) 0));

	}


	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	public boolean isNoRicochet() {
		return noRicochet;
	}

	public void setNoRicochet(boolean noRicochet) {
		this.noRicochet = noRicochet;
	}
}