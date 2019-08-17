package com.daniel366cobra.guncraft.client.init;

import org.apache.logging.log4j.Logger;

import com.daniel366cobra.guncraft.GuncraftMod;
import com.daniel366cobra.guncraft.client.renderer.BulletRenderer;
import com.daniel366cobra.guncraft.entities.EntityGenericBullet;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

@OnlyIn(Dist.CLIENT)
public class ModRenderers 
{	
	public static final Logger LOGGER = GuncraftMod.LOGGER;	
	
	public static void registerEntityRenderers()
	{		
		RenderingRegistry.registerEntityRenderingHandler(EntityGenericBullet.class, BulletRenderer::new);
		LOGGER.info("Entity renderers registered");
	}
}