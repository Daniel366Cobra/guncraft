package com.daniel366cobra.guncraft.entities;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.daniel366cobra.guncraft.damagesources.DamageProjectile;
import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class EntityThrownGrenade extends Entity implements IProjectile
{
	private int xTile;
	private int yTile;
	private int zTile;
	private Block inTile;
	protected boolean inGround;
	protected EntityLivingBase thrower;
	private String throwerName;
	private int ticksInAir;
	private int fuseLength = 80;
	static private List<Vec3d> distribSphereResult = null;

	public EntityThrownGrenade(World worldIn)
	{
		super(worldIn);
		this.xTile = -1;
		this.yTile = -1;
		this.zTile = -1;
		this.setSize(0.3F, 0.3F);
	}

	public EntityThrownGrenade(World worldIn, double x, double y, double z, int fuseTicks)
	{
		this(worldIn);
		this.setPosition(x, y, z);
		this.setFuseLength(fuseTicks);
	}

	public EntityThrownGrenade(World worldIn, EntityLivingBase throwerIn, int fuseTicks)
	{
		this(worldIn, throwerIn.posX, throwerIn.posY + throwerIn.getEyeHeight() - 0.1D, throwerIn.posZ, fuseTicks);
		this.thrower = throwerIn;
	}

	@Override
	protected void entityInit()
	{
	}

	/**
	 * Checks if the entity is in range to render.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance)
	{
		double d0 = this.getEntityBoundingBox().getAverageEdgeLength() * 4.0D;

		if (Double.isNaN(d0))
		{
			d0 = 4.0D;
		}

		d0 = d0 * 64.0D;
		return distance < d0 * d0;
	}

	/**
	 * Sets throwable heading based on an entity that's throwing it
	 */
	public void shoot(Entity entityThrower, float rotationPitchIn, float rotationYawIn, float pitchOffset, float velocity, float inaccuracy)
	{
		float f = -MathHelper.sin(rotationYawIn * 0.017453292F) * MathHelper.cos(rotationPitchIn * 0.017453292F);
		float f1 = -MathHelper.sin((rotationPitchIn + pitchOffset) * 0.017453292F);
		float f2 = MathHelper.cos(rotationYawIn * 0.017453292F) * MathHelper.cos(rotationPitchIn * 0.017453292F);
		this.shoot(f, f1, f2, velocity, inaccuracy);
		this.motionX += entityThrower.motionX;
		this.motionZ += entityThrower.motionZ;

		if (!entityThrower.onGround)
		{
			this.motionY += entityThrower.motionY;
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
		x = x + this.rand.nextGaussian() * 0.007499999832361937D * inaccuracy;
		y = y + this.rand.nextGaussian() * 0.007499999832361937D * inaccuracy;
		z = z + this.rand.nextGaussian() * 0.007499999832361937D * inaccuracy;
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
		this.ticksInAir = 0;
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
			this.rotationYaw = (float)(MathHelper.atan2(x, z) * (180D / Math.PI));
			this.rotationPitch = (float)(MathHelper.atan2(y, f) * (180D / Math.PI));
			this.prevRotationYaw = this.rotationYaw;
			this.prevRotationPitch = this.rotationPitch;
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
		//Decrease fuse, blow up if zero
		--this.fuseLength;
		if (this.fuseLength <= 0)
		{
			World curWorld = this.world;
			Vec3d curPos = new Vec3d(this.posX + this.width, this.posY  + 1.0F, this.posZ);

			//Lists of entities in blast radius, those hit by shrapnel and blocks to be smashed
			List<Entity> entitiesInRadius = curWorld.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow(7.5D));
			List<Entity> entitiesHit = Lists.<Entity>newArrayList();
			Set<BlockPos> lowerDoorParts = new HashSet<BlockPos>();

			List<Vec3d> rayPoints = EntityThrownGrenade.distribSphere();
			Iterator<Vec3d> RayPoint = rayPoints.iterator();
			while (RayPoint.hasNext())
			{
				Vec3d relPoint = RayPoint.next();
				Vec3d castPosAbs = new Vec3d(this.posX + relPoint.x, this.posY + relPoint.y, this.posZ + relPoint.z);
				//Cast a ray from the entity's position to the point on the sphere to get blocks
				for (;;) {
					RayTraceResult hitRes = this.world.rayTraceBlocks(curPos, castPosAbs, false, true, true);
					if (hitRes != null && hitRes.typeOfHit != Type.MISS)
					{
						if (hitRes.typeOfHit == Type.BLOCK)//Redundant because rayTraceBlocks gives only blocks
						{
							BlockPos hitBlockPos = hitRes.getBlockPos();
							IBlockState hitBlockState = curWorld.getBlockState(hitBlockPos);
							boolean isBlockVulnerable = (
									hitBlockState.getMaterial() == Material.GLASS
									|| hitBlockState.getMaterial() == Material.GOURD
									|| hitBlockState.getMaterial() == Material.PLANTS
									|| hitBlockState.getMaterial() == Material.VINE
									);

							if (isBlockVulnerable && !curWorld.isRemote)//Destroy
							{
								curWorld.destroyBlock(hitBlockPos, false);
							}
							else
							{
								if (hitBlockState.getBlock() instanceof BlockDoor && hitBlockState.getMaterial() == Material.WOOD)
								{

									BlockPos lowerPartPos = hitBlockState.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.LOWER ? hitBlockPos : hitBlockPos.down();
									lowerDoorParts.add(lowerPartPos);
								}
								castPosAbs = new Vec3d(hitRes.hitVec.x, hitRes.hitVec.y, hitRes.hitVec.z);
								break;
							}
						}
					}
					else
					{
						break;
					}
				}
				//For each cast ray, also check if it hits an entity within blast radius
				for (int i = 0; i < entitiesInRadius.size(); i++)
				{
					Entity curEntity = entitiesInRadius.get(i);
					AxisAlignedBB curEntityAABB = curEntity.getEntityBoundingBox().grow(0.3D);
					RayTraceResult entityHitRes = curEntityAABB.calculateIntercept(curPos, castPosAbs);
					if (entityHitRes != null)
					{//Add to the list of hit entities
						entitiesHit.add(curEntity);
					}

				}


			}

			if (!curWorld.isRemote)
			{
				//Make a small explosion
				this.world.newExplosion(this, this.posX, this.posY, this.posZ, 1.0F, false, true);
				//Open or break doors
				Iterator<BlockPos> hitDoorPosIter = lowerDoorParts.iterator();
				while(hitDoorPosIter.hasNext()) {
					BlockPos doorPos = hitDoorPosIter.next();
					//Bypass the weird glitch of casting air to door
					if (curWorld.getBlockState(doorPos).getBlock() instanceof BlockDoor)
					{
						BlockDoor door = (BlockDoor)curWorld.getBlockState(doorPos).getBlock();
						EnumFacing facing = curWorld.getBlockState(doorPos).getValue(BlockDoor.FACING);
						boolean open = false;

						switch (facing)
						{
						case EAST:
							open = (this.posX < doorPos.getX());
							break;
						case WEST:
							open = (this.posX > doorPos.getX());
							break;
						case NORTH:
							open = (this.posZ > doorPos.getZ());
							break;
						case SOUTH:
							open = (this.posZ < doorPos.getZ());
							break;
						default:
							open = false;
							break;
						}

						if (open)
						{
							door.toggleDoor(curWorld, doorPos, true);
						}
						else
						{
							curWorld.destroyBlock(doorPos, true);
							curWorld.playSound(null, this.posX, this.posY, this.posZ,SoundEvents.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, SoundCategory.PLAYERS, 1.0F, 1.0F);
						}
					}
				}
				//Hurt entities
				Iterator<Entity> hitEntity = entitiesHit.iterator();
				while (hitEntity.hasNext())
				{
					Entity curHit =	hitEntity.next();
					curHit.attackEntityFrom(new DamageProjectile("shrapnel", this, this.getThrower()), 10.0F);
					curHit.hurtResistantTime = 0;
				}


				this.setDead();
			}

		}
		else
		{

			if (this.inGround) //If lying on a block
			{
				if (this.world.getBlockState(new BlockPos(this.xTile, this.yTile, this.zTile)).getBlock() != this.inTile)
				{
					//If suddenly not on the same block - reset the counters
					this.inGround = false;
					this.ticksInAir = 0;
				}

			}
			else
			{
				++this.ticksInAir;
			}
			//Collision detection block: raytrace from current to next position
			Vec3d curPos = new Vec3d(this.posX, this.posY, this.posZ);
			Vec3d nextPos = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
			RayTraceResult traceOnPath = this.world.rayTraceBlocks(curPos, nextPos);
			curPos = new Vec3d(this.posX, this.posY, this.posZ);
			nextPos = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
			//If raytrace collided, get collision coords
			if (traceOnPath != null)
			{
				nextPos = new Vec3d(traceOnPath.hitVec.x, traceOnPath.hitVec.y, traceOnPath.hitVec.z);
			}
			//Find entity on path
			Entity entityHit = findEntityOnPath(curPos, nextPos);
			if (entityHit != null)
			{//Get coordinates of hit entity as a trace result.
				traceOnPath = new RayTraceResult(entityHit);
			}

			if (traceOnPath != null && traceOnPath.entityHit instanceof EntityPlayer)
			{
				EntityPlayer entityPlayerHit = (EntityPlayer)traceOnPath.entityHit;

				if (this.getThrower() instanceof EntityPlayer && !((EntityPlayer)this.getThrower()).canAttackPlayer(entityPlayerHit))
				{
					traceOnPath = null;
				}
			}

			if (traceOnPath != null) //If hit something...
			{
				if (traceOnPath.typeOfHit == RayTraceResult.Type.BLOCK && this.world.getBlockState(traceOnPath.getBlockPos()).getBlock() == Blocks.PORTAL)
				{//..and it is a portal block - set entity in portal.
					this.setPortal(traceOnPath.getBlockPos());
				}
				else
				{//else just pass the trace result to the impact calculation routine
					this.onImpact(traceOnPath);
				}
			}

			//Leave a smoke trail
			this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + this.height * 0.5F, this.posZ, 0.0D, 0.0D, 0.0D, new int[0]);

			//Calculate new positions from velocities
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
			if (!this.inGround)
			{
				if (this.isInWater())
				{
					for (int j = 0; j < 4; ++j) //Leave a trail of bubbles underwater
					{
						this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * 0.25D, this.posY - this.motionY * 0.25D, this.posZ - this.motionZ * 0.25D, this.motionX, this.motionY, this.motionZ);
					}

					drag = 0.8F;
				}
				//Slowing down due to drag
				this.motionX *= drag;
				this.motionY *= drag;
				this.motionZ *= drag;
				//And accelerating downwards
				if (!this.hasNoGravity())
				{
					this.motionY -= freefallAccel;
				}
			}
			else
			{
				this.motionY = 0.0D;
			}
			//And finally set the new position
			this.setPosition(this.posX, this.posY, this.posZ);
		}
	}

	//Gets the amount of gravity to apply to the thrown entity with each tick.
	protected float getGravityVelocity()
	{
		return 0.03F;
	}

	//Called when this EntityThrowable hits a block or entity.
	protected void onImpact(RayTraceResult result)
	{
		World curWorld = this.world;
		if (result.typeOfHit == Type.BLOCK) //Calculate interaction, bounce and deceleration
		{
			float vel = (float)Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);

			BlockPos hitBlockPos = result.getBlockPos();
			IBlockState hitBlockState = curWorld.getBlockState(hitBlockPos);

			if (hitBlockState.getMaterial() == Material.GLASS && vel > 0.5F)
			{//break through glass with minor deceleration
				this.motionX *= 0.5D;
				this.motionY *= 0.5D;
				this.motionZ *= 0.5D;
				if (!curWorld.isRemote)
				{
					curWorld.destroyBlock(hitBlockPos, false);
				}
			}
			else if (hitBlockState.getMaterial() != Material.VINE)
			{
				//Which side of block did the entity hit?
				boolean hitAlongX = (result.sideHit == EnumFacing.WEST || result.sideHit == EnumFacing.EAST);
				boolean hitAlongZ = (result.sideHit == EnumFacing.NORTH || result.sideHit == EnumFacing.SOUTH);
				boolean hitCeiling = (result.sideHit == EnumFacing.DOWN);
				boolean hitFloor = (result.sideHit == EnumFacing.UP);

				float multX = 0.0F;
				float multY = 0.0F;
				float multZ = 0.0F;

				if (hitAlongX) {multX = -0.3F; multY = 0.3F; multZ = 0.3F;}
				if (hitAlongZ) {multX = 0.3F; multY = 0.3F; multZ = -0.3F;}
				if (hitCeiling) {multX = 0.3F; multY = -0.3F; multZ = 0.3F;}
				if (hitFloor) {multX = 0.1F; multY = -0.1F; multZ = 0.1F;}

				this.motionX *= multX;
				this.motionY *= multY;
				this.motionZ *= multZ;


				if (hitFloor && (vel < 0.03F))
				{
					this.motionX = 0.0D;
					this.motionY = 0.0D;
					this.motionZ = 0.0D;
					this.posX = result.hitVec.x;
					this.posY = result.hitVec.y;
					this.posZ = result.hitVec.z;
					this.ticksInAir = 0;
					this.xTile = hitBlockPos.getX();
					this.yTile = hitBlockPos.getY();
					this.zTile = hitBlockPos.getZ();
					this.inTile = hitBlockState.getBlock();
					this.inGround = true;
				}

			}
		}
		else if (result.typeOfHit == Type.ENTITY)
		{
			Entity hitEntity = result.entityHit;
			if (this.getThrower()==null)
			{
				hitEntity.attackEntityFrom(new DamageProjectile("grenadehit", this, null), 4.0F);
			}
			else
			{
				hitEntity.attackEntityFrom(new DamageProjectile("grenadehit", this, this.getThrower()), 4.0F);

			}
		}
	}

	private static List<Vec3d> distribSphere ()
	{
		if (EntityThrownGrenade.distribSphereResult == null )
		{
			int numPoints = 1000;
			float radius = 8.0F;

			List<Vec3d> ptsCoords = Lists.<Vec3d>newArrayList();
			float offset = 2.0F / numPoints;
			float increment = (float) (Math.PI * (3.0F - Math.sqrt(5.0F)));
			float x, y, z, r, phi;
			for (int i = 0; i < numPoints; i++)
			{
				y = ((i * offset) - 1.0F) + (offset / 2.0F);
				r = (float) Math.sqrt(1 - Math.pow(y, 2.0D));
				phi = ((i + 1) % numPoints) * increment;
				x = (float) (Math.cos(phi) * r);
				z = (float) (Math.sin(phi) * r);
				ptsCoords.add(new Vec3d(radius * x, radius * y, radius * z));
			}
			EntityThrownGrenade.distribSphereResult = ptsCoords;
		}
		return EntityThrownGrenade.distribSphereResult;
	}

	//Find entity on the line between start and end positions
	private Entity findEntityOnPath(Vec3d start, Vec3d end)
	{
		//Obtain a list of potentially collidable entities
		Entity entityHit = null;
		List<Entity> listHit = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1.0D));

		double d0 = 0.0D;

		//Iterate through the list
		for (int i = 0; i < listHit.size(); ++i)
		{
			Entity curEntity = listHit.get(i);
			//Ignore owner if freshly thrown.
			if (curEntity != this.thrower || this.ticksInAir >= 2)
			{
				AxisAlignedBB axisalignedbb = curEntity.getEntityBoundingBox().grow(0.3D);
				RayTraceResult entityHitTrace = axisalignedbb.calculateIntercept(start, end);

				if (entityHitTrace != null)
				{
					double hitEntityDist = start.squareDistanceTo(entityHitTrace.hitVec);

					if (hitEntityDist < d0 || d0 == 0.0D)
					{//If sufficiently close to hit entity, set hit
						entityHit = curEntity;
						d0 = hitEntityDist;
					}
				}
			}
		}
		return entityHit;
	}

	@Override
	@Nullable
	public void move(MoverType type, double x, double y, double z)
	{
		super.move(type, x, y, z);

		if (this.inGround)
		{
			this.xTile = MathHelper.floor(this.posX);
			this.yTile = MathHelper.floor(this.posY);
			this.zTile = MathHelper.floor(this.posZ);
		}
	}

	public static void registerFixesThrowable(DataFixer fixer, String name)
	{
	}

	//(abstract) Protected helper method to write subclass entity data to NBT.
	@Override
	public void writeEntityToNBT(NBTTagCompound compound)
	{
		compound.setInteger("xTile", this.xTile);
		compound.setInteger("yTile", this.yTile);
		compound.setInteger("zTile", this.zTile);
		ResourceLocation resourcelocation = Block.REGISTRY.getNameForObject(this.inTile);
		compound.setString("inTile", resourcelocation == null ? "" : resourcelocation.toString());
		compound.setInteger("fuseTicks", this.fuseLength);
		compound.setByte("inGround", (byte)(this.inGround ? 1 : 0));

		if ((this.throwerName == null || this.throwerName.isEmpty()) && this.thrower instanceof EntityPlayer)
		{
			this.throwerName = this.thrower.getName();
		}

		compound.setString("ownerName", this.throwerName == null ? "" : this.throwerName);
	}

	//(abstract) Protected helper method to read subclass entity data from NBT.
	@Override
	public void readEntityFromNBT(NBTTagCompound compound)
	{
		this.xTile = compound.getInteger("xTile");
		this.yTile = compound.getInteger("yTile");
		this.zTile = compound.getInteger("zTile");
		this.fuseLength = compound.getInteger("fuseTicks");
		if (compound.hasKey("inTile", 8))
		{
			this.inTile = Block.getBlockFromName(compound.getString("inTile"));
		}
		else
		{
			this.inTile = Block.getBlockById(compound.getByte("inTile") & 255);
		}

		this.inGround = compound.getByte("inGround") == 1;
		this.thrower = null;
		this.throwerName = compound.getString("ownerName");

		if (this.throwerName != null && this.throwerName.isEmpty())
		{
			this.throwerName = null;
		}

		this.thrower = this.getThrower();
	}

	@Nullable
	public EntityLivingBase getThrower()
	{
		if (this.thrower == null && this.throwerName != null && !this.throwerName.isEmpty())
		{
			this.thrower = this.world.getPlayerEntityByName(this.throwerName);

			if (this.thrower == null && this.world instanceof WorldServer)
			{
				try
				{
					Entity entity = ((WorldServer)this.world).getEntityFromUuid(UUID.fromString(this.throwerName));

					if (entity instanceof EntityLivingBase)
					{
						this.thrower = (EntityLivingBase)entity;
					}
				}
				catch (Throwable var2)
				{
					this.thrower = null;
				}
			}
		}

		return this.thrower;
	}

	private void setFuseLength(int fuseTicks) {
		this.fuseLength = fuseTicks;

	}
}
