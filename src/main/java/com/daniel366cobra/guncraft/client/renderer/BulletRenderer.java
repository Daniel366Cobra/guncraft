package com.daniel366cobra.guncraft.client.renderer;

import com.daniel366cobra.guncraft.Reference;
import com.daniel366cobra.guncraft.entities.EntityGenericBullet;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BulletRenderer<T extends EntityGenericBullet> extends EntityRenderer<T> {


	public BulletRenderer(EntityRendererManager renderManagerIn)
	{
		super(renderManagerIn);

	}	

	/**
	 * Renders the desired Entity.
	 */
	public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks)
	{
		this.bindEntityTexture(entity);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();
		GlStateManager.translatef((float)x, (float)y, (float)z);
		GlStateManager.rotatef(MathHelper.lerp(partialTicks, entity.prevRotationYaw, entity.rotationYaw) - 90.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotatef(MathHelper.lerp(partialTicks, entity.prevRotationPitch, entity.rotationPitch), 0.0F, 0.0F, 1.0F);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();

		GlStateManager.enableRescaleNormal();       

		GlStateManager.rotatef(45.0F, 1.0F, 0.0F, 0.0F);
		GlStateManager.scalef(0.025F, 0.025F, 0.025F);
		GlStateManager.translatef(-4.0F, 0.0F, 0.0F);

		if (this.renderOutlines)
		{
			GlStateManager.enableColorMaterial();
			GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(entity));
		}

		GlStateManager.normal3f(0.05625F, 0.0F, 0.0F);
		//Tail
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.pos(-5.0D, -2.0D, -2.0D).tex(0.0D, 0.1875D).endVertex();
		bufferbuilder.pos(-5.0D, -2.0D, 2.0D).tex(0.1875D, 0.1875D).endVertex();
		bufferbuilder.pos(-5.0D, 2.0D, 2.0D).tex(0.1875D, 0.375D).endVertex();
		bufferbuilder.pos(-5.0D, 2.0D, -2.0D).tex(0.0D, 0.375D).endVertex();
		tessellator.draw();
		GlStateManager.normal3f(-0.05625F, 0.0F, 0.0F);
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.pos(-5.0D, 2.0D, -2.0D).tex(0.0D, 0.1875D).endVertex();
		bufferbuilder.pos(-5.0D, 2.0D, 2.0D).tex(0.1875D, 0.1875D).endVertex();
		bufferbuilder.pos(-5.0D, -2.0D, 2.0D).tex(0.1875D, 0.375D).endVertex();
		bufferbuilder.pos(-5.0D, -2.0D, -2.0D).tex(0.0D, 0.375D).endVertex();
		tessellator.draw();
		//Crossed body textures
		for (int j = 0; j < 4; ++j)
		{
			GlStateManager.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.normal3f(0.0F, 0.0F, 0.05625F);
			bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
			bufferbuilder.pos(-5.0D, -2.0D, 0.0D).tex(0.0D, 0.0D).endVertex();
			bufferbuilder.pos(5.0D, -2.0D, 0.0D).tex(0.3125D, 0.0D).endVertex();
			bufferbuilder.pos(5.0D, 2.0D, 0.0D).tex(0.3125D, 0.1875D).endVertex();
			bufferbuilder.pos(-5.0D, 2.0D, 0.0D).tex(0.0D, 0.1875D).endVertex();
			tessellator.draw();
		}

		if (this.renderOutlines)
		{
			GlStateManager.tearDownSolidRenderingTextureCombine();
			GlStateManager.disableColorMaterial();
		}

		GlStateManager.disableRescaleNormal();
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	/**
	 * Returns the location of an entity's texture. Execute Render.bindEntityTexture to call.
	 */
	@Override
	protected ResourceLocation getEntityTexture(EntityGenericBullet entity)
	{
		return new ResourceLocation(Reference.MODID, "textures/entity/genericbullet.png");
	}

}