package com.daniel366cobra.guncraft.init;

import com.daniel366cobra.guncraft.Reference;
import com.daniel366cobra.guncraft.client.render.RenderBullet;
import com.daniel366cobra.guncraft.client.render.RenderGrenade;
import com.daniel366cobra.guncraft.client.render.RenderSentry;
import com.daniel366cobra.guncraft.entities.EntityGenericBullet;
import com.daniel366cobra.guncraft.entities.EntitySentryGun;
import com.daniel366cobra.guncraft.entities.EntityThrownGrenade;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = Reference.MODID)
public class ModEntities
{
	@SubscribeEvent
	public static void registerEntities(RegistryEvent.Register<EntityEntry> event)
	{
		ModEntities.register(event.getRegistry());	
	}
	
	public static void register(final IForgeRegistry<EntityEntry> registry)
	{
		registry.register(EntityEntryBuilder.create()
				.entity(EntityGenericBullet.class)
				.id(new ResourceLocation(Reference.MODID, "genericbullet"), 1)
				.name("genericbullet")
				.tracker(128, 3, true)
				.build()
				);
		registry.register(EntityEntryBuilder.create()
				.entity(EntityThrownGrenade.class)
				.id(new ResourceLocation(Reference.MODID, "throwngrenade"), 2)
				.name("throwngrenade")
				.tracker(64, 5, true)
				.build()
				);
		registry.register(EntityEntryBuilder.create()
				.entity(EntitySentryGun.class)
				.id(new ResourceLocation(Reference.MODID, "sentrygun"), 3)
				.name("sentrygun")
				.tracker(128, 3, false)				
				.build()
				);
		System.out.println("Registered entities");
	}

	@SideOnly(value = Side.CLIENT)
	public static void registerRenders() {
		RenderingRegistry.registerEntityRenderingHandler(EntityGenericBullet.class, RenderBullet.FACTORY);
		RenderingRegistry.registerEntityRenderingHandler(EntityThrownGrenade.class, RenderGrenade.FACTORY);
		RenderingRegistry.registerEntityRenderingHandler(EntitySentryGun.class, RenderSentry.FACTORY);
		System.out.println("Registered entity renders");
	}
}
