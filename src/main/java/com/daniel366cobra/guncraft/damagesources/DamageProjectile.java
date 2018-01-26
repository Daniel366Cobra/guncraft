package com.daniel366cobra.guncraft.damagesources;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class DamageProjectile extends EntityDamageSource
{
	private final Entity trueSource;

	public DamageProjectile(String damageTypeIn, Entity source, @Nullable Entity indirectEntityIn)
	{
		super(damageTypeIn, source);
		this.trueSource = indirectEntityIn;
	}

	@Override
	@Nullable
	public Entity getImmediateSource()
	{
		return this.damageSourceEntity;
	}

	/**
	 * Retrieves the true cause of the damage, e.g. the player who fired an arrow, the shulker who fired the bullet,
	 * etc.
	 */
	@Override
	@Nullable
	public Entity getTrueSource()
	{
		return this.trueSource;
	}

	/**
	 * Gets the death message that is displayed when the player dies
	 */
	@Override
	public ITextComponent getDeathMessage(EntityLivingBase entityLivingBaseIn)
	{
		ITextComponent itextcomponent = this.trueSource == null ? this.damageSourceEntity.getDisplayName() : this.trueSource.getDisplayName();
		ItemStack itemstack = this.trueSource instanceof EntityLivingBase ? ((EntityLivingBase)this.trueSource).getHeldItemMainhand() : ItemStack.EMPTY;
		String s = "death.attack." + this.damageType;
		String s1 = s + ".item";
		return !itemstack.isEmpty() && itemstack.hasDisplayName() ? new TextComponentTranslation(s1, new Object[] {entityLivingBaseIn.getDisplayName(), itextcomponent, itemstack.getTextComponent()}) : new TextComponentTranslation(s, new Object[] {entityLivingBaseIn.getDisplayName(), itextcomponent});
	}

}
