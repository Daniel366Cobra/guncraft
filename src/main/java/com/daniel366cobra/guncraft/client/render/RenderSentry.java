package com.daniel366cobra.guncraft.client.render;

import com.daniel366cobra.guncraft.Reference;
import com.daniel366cobra.guncraft.entities.EntitySentryGun;
import com.daniel366cobra.guncraft.entities.ModelSentry;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderSentry extends RenderLiving<EntitySentryGun>
{
	private static final ResourceLocation SENTRY_TEXTURE = new ResourceLocation(Reference.MODID, "textures/entity/sentry.png");
	public static final Factory FACTORY = new Factory();

	public RenderSentry(RenderManager rendermanagerIn)
	{
		super(rendermanagerIn, new ModelSentry(), 0.5F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntitySentryGun entity)
	{		
		return SENTRY_TEXTURE;
	}

	public ModelSentry getMainModel()
	{
		return (ModelSentry)super.getMainModel();
	}

	public static class Factory implements IRenderFactory<EntitySentryGun> {

		@Override
		public Render<? super EntitySentryGun> createRenderFor(RenderManager manager) {
			return new RenderSentry(manager);
		}

	}

}
