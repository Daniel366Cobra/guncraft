package com.daniel366cobra.guncraft.init;

import com.daniel366cobra.guncraft.Reference;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = Reference.MODID)
public class ModSounds
{	
	public static final SoundEvent musketshot = new SoundEvent(new ResourceLocation(Reference.MODID, "musket.shot")).setRegistryName("musket.shot");
	public static final SoundEvent flintlockcock = new SoundEvent(new ResourceLocation(Reference.MODID, "musket.flintlockcock")).setRegistryName("musket.flintlockcock");
	public static final SoundEvent flintlockmisfire = new SoundEvent(new ResourceLocation(Reference.MODID, "musket.flintlockmisfire")).setRegistryName("musket.flintlockmisfire");

	public static final SoundEvent leveractionload = new SoundEvent(new ResourceLocation(Reference.MODID, "leveraction.load")).setRegistryName("leveraction.load");
	public static final SoundEvent leveractionshot_reload = new SoundEvent(new ResourceLocation(Reference.MODID, "leveraction.shot_reload")).setRegistryName("leveraction.shot_reload");
	public static final SoundEvent leveractiondryfire = new SoundEvent(new ResourceLocation(Reference.MODID, "leveraction.dryfire")).setRegistryName("leveraction.dryfire");

	public static final SoundEvent pumpactionload = new SoundEvent(new ResourceLocation(Reference.MODID, "pumpaction.load")).setRegistryName("pumpaction.load");
	public static final SoundEvent pumpactionshot_reload = new SoundEvent(new ResourceLocation(Reference.MODID, "pumpaction.shot_reload")).setRegistryName("pumpaction.shot_reload");
	public static final SoundEvent pumpactiondryfire = new SoundEvent(new ResourceLocation(Reference.MODID, "pumpaction.dryfire")).setRegistryName("pumpaction.dryfire");

	public static final SoundEvent bullethit = new SoundEvent(new ResourceLocation(Reference.MODID, "bullet.hit")).setRegistryName("bullet.hit");

	public static final SoundEvent grenadefuse = new SoundEvent(new ResourceLocation(Reference.MODID, "grenade.fuse")).setRegistryName("grenade.fuse");
	public static final SoundEvent grenadetoss = new SoundEvent(new ResourceLocation(Reference.MODID, "grenade.toss")).setRegistryName("grenade.toss");

	public static final SoundEvent sentryfire = new SoundEvent(new ResourceLocation(Reference.MODID, "sentry.fire")).setRegistryName("sentry.fire");

	@SubscribeEvent
	public static void registerSounds(RegistryEvent.Register<SoundEvent> event)
	{
		ModSounds.register(event.getRegistry());
	} 

	public static void register(final IForgeRegistry<SoundEvent> registry)
	{				
		final SoundEvent[] soundEvents = {
				musketshot,
				flintlockcock,
				flintlockmisfire,
				leveractionload,
				leveractionshot_reload,
				leveractiondryfire,
				pumpactionload,
				pumpactionshot_reload,
				pumpactiondryfire,				
				bullethit,
				grenadefuse,
				grenadetoss,
				sentryfire
		};
		registry.registerAll(soundEvents);
	}
}
