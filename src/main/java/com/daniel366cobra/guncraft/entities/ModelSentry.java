package com.daniel366cobra.guncraft.entities;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * ModelSentry - Daniel366Cobra
 * Created using Tabula 7.0.0
 */
public class ModelSentry extends ModelBase
{
	public ModelRenderer head;
	public ModelRenderer base;
	public ModelRenderer barrel;
	public ModelRenderer ammodrum;
	public ModelRenderer stand;
	public ModelRenderer leg1;
	public ModelRenderer leg2;
	public ModelRenderer leg3;
	public ModelRenderer leg4;

	public ModelSentry()
	{
		this.textureWidth = 64;
		this.textureHeight = 64;
		this.head = new ModelRenderer(this, 0, 0);
		this.head.setRotationPoint(0.0F, 13.0F, 0.0F);
		this.head.addBox(-4.0F, -6.0F, -7.0F, 8, 6, 14, 0.0F);

		this.ammodrum = new ModelRenderer(this, 16, 20);
		this.ammodrum.setRotationPoint(0.0F, -3.0F, 7.0F);
		this.ammodrum.addBox(-5.0F, -3.0F, 0.0F, 10, 6, 10, 0.0F);

		this.barrel = new ModelRenderer(this, 0, 0);
		this.barrel.setRotationPoint(0.0F, -3.0F, -7.0F);
		this.barrel.addBox(-1.0F, -1.0F, -4.0F, 2, 2, 4, 0.0F);

		this.stand = new ModelRenderer(this, 0, 20);
		this.stand.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.stand.addBox(-2.0F, -9.0F, -2.0F, 4, 8, 4, 0.0F);

		this.base = new ModelRenderer(this, 0, 36);
		this.base.setRotationPoint(0.0F, 22.0F, 0.0F);
		this.base.addBox(-7.0F, -1.0F, -7.0F, 14, 1, 14, 0.0F);

		this.leg1 = new ModelRenderer(this, 0, 7);
		this.leg1.setRotationPoint(6.0F, 0.0F, 6.0F);
		this.leg1.addBox(-1.0F, 0.0F, -1.0F, 2, 2, 2, 0.0F);

		this.leg2 = new ModelRenderer(this, 0, 7);
		this.leg2.setRotationPoint(-6.0F, 0.0F, 6.0F);
		this.leg2.addBox(-1.0F, 0.0F, -1.0F, 2, 2, 2, 0.0F);

		this.leg3 = new ModelRenderer(this, 0, 7);
		this.leg3.setRotationPoint(-6.0F, 0.0F, -6.0F);
		this.leg3.addBox(-1.0F, 0.0F, -1.0F, 2, 2, 2, 0.0F);

		this.leg4 = new ModelRenderer(this, 0, 7);
		this.leg4.setRotationPoint(6.0F, 0.0F, -6.0F);
		this.leg4.addBox(-1.0F, 0.0F, -1.0F, 2, 2, 2, 0.0F);

		this.base.addChild(this.stand);
		this.base.addChild(this.leg1);
		this.base.addChild(this.leg2);
		this.base.addChild(this.leg3);
		this.base.addChild(this.leg4);
		
		this.head.addChild(this.barrel);
		this.head.addChild(this.ammodrum);
	}

	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn)
	{
		this.head.rotateAngleY = netHeadYaw * 0.017453292F;
		this.head.rotateAngleX = headPitch * 0.017453292F;		
	}
	
	@Override
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
	{ 
		this.base.render(scale);
		this.head.render(scale);
	}    
	
}