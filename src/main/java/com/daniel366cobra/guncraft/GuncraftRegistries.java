package com.daniel366cobra.guncraft;

import org.apache.logging.log4j.Logger;

import com.daniel366cobra.guncraft.entities.EntityGenericBullet;
import com.daniel366cobra.guncraft.init.ModEntities;
import com.daniel366cobra.guncraft.init.ModItems;
import com.daniel366cobra.guncraft.init.ModSounds;
import com.daniel366cobra.guncraft.items.ItemAmmunition;
import com.daniel366cobra.guncraft.items.ItemBreakActionShotgun;
import com.daniel366cobra.guncraft.items.ItemMusket;
import com.daniel366cobra.guncraft.items.ItemWeaponPart;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class GuncraftRegistries 
{

	public static final Logger LOGGER = GuncraftMod.LOGGER;	

	@SubscribeEvent
	public static void registerItems(final RegistryEvent.Register<Item> event)
	{
		event.getRegistry().registerAll
		(
				ModItems.gun_barrel = ItemWeaponPart.makeDefaultItem("gun_barrel"),
				
				ModItems.oak_gun_stock = ItemWeaponPart.makeDefaultItem("stock/oak_gun_stock"),
				ModItems.birch_gun_stock = ItemWeaponPart.makeDefaultItem("stock/birch_gun_stock"),
				ModItems.spruce_gun_stock = ItemWeaponPart.makeDefaultItem("stock/spruce_gun_stock"),
				ModItems.jungle_gun_stock = ItemWeaponPart.makeDefaultItem("stock/jungle_gun_stock"),
				ModItems.acacia_gun_stock = ItemWeaponPart.makeDefaultItem("stock/acacia_gun_stock"),
				ModItems.dark_oak_gun_stock = ItemWeaponPart.makeDefaultItem("stock/dark_oak_gun_stock"),
				
				ModItems.trigger_mechanism = ItemWeaponPart.makeDefaultItem("trigger_mechanism"),

				ModItems.musket_ball = ItemAmmunition.makeDefaultItem("musket_ball"),

				ModItems.lever_action_cartridge = ItemAmmunition.makeDefaultItem("lever_action_cartridge"),

				ModItems.shotgun_shell_buckshot = ItemAmmunition.makeDefaultItem("shotgun_shell_buckshot"),
				ModItems.shotgun_shell_incendiary = ItemAmmunition.makeDefaultItem("shotgun_shell_incendiary"),
				ModItems.shotgun_shell_slug = ItemAmmunition.makeDefaultItem("shotgun_shell_slug"),


				ModItems.oak_musket = ItemMusket.makeDefaultItem("musket/oak/oak_musket", 350),
				ModItems.birch_musket = ItemMusket.makeDefaultItem("musket/birch/birch_musket", 325),
				ModItems.spruce_musket = ItemMusket.makeDefaultItem("musket/spruce/spruce_musket", 325),
				ModItems.jungle_musket = ItemMusket.makeDefaultItem("musket/jungle/jungle_musket", 375),
				ModItems.acacia_musket = ItemMusket.makeDefaultItem("musket/acacia/acacia_musket", 375),
				ModItems.dark_oak_musket = ItemMusket.makeDefaultItem("musket/dark_oak/dark_oak_musket", 400),

				ModItems.oak_break_action_shotgun = ItemBreakActionShotgun.makeDefaultItem("break_action_shotgun/oak_break_action_shotgun", 350),
				ModItems.birch_break_action_shotgun = ItemBreakActionShotgun.makeDefaultItem("break_action_shotgun/birch_break_action_shotgun", 325),
				ModItems.spruce_break_action_shotgun = ItemBreakActionShotgun.makeDefaultItem("break_action_shotgun/spruce_break_action_shotgun", 325),
				ModItems.jungle_break_action_shotgun = ItemBreakActionShotgun.makeDefaultItem("break_action_shotgun/jungle_break_action_shotgun", 375),
				ModItems.acacia_break_action_shotgun = ItemBreakActionShotgun.makeDefaultItem("break_action_shotgun/acacia_break_action_shotgun", 375),
				ModItems.dark_oak_break_action_shotgun = ItemBreakActionShotgun.makeDefaultItem("break_action_shotgun/dark_oak_break_action_shotgun", 400)



				);

		//ModEntities.registerEntitySpawnEggs(event);

		LOGGER.info("Items registered.");
	}

	@SubscribeEvent
	public static void registerBlocks(final RegistryEvent.Register<Block> event)
	{
		event.getRegistry().registerAll
		(

				);

		LOGGER.info("Blocks registered.");
	}

	@SubscribeEvent
	public static void registerEntities(final RegistryEvent.Register<EntityType<?>> event)
	{
		event.getRegistry().registerAll
		(
				
				ModEntities.genericbullet = EntityType.Builder.<EntityGenericBullet>create(EntityGenericBullet::new,EntityClassification.MISC).size(0.25F,0.25F)
				.setShouldReceiveVelocityUpdates(true)
				.setTrackingRange(96)
				.setUpdateInterval(1)
				.setCustomClientFactory((spawnEntity,world) -> new EntityGenericBullet(world))
				.build("genericbullet")
				.setRegistryName(Reference.MODID,"genericbullet")
				);		
		LOGGER.info("Entities registered.");
	}

	@SubscribeEvent
	public static void registerSounds(RegistryEvent.Register<SoundEvent> event)
	{
		event.getRegistry().registerAll
		(
				ModSounds.musketshot = new SoundEvent(new ResourceLocation(Reference.MODID, "musket.shot")).setRegistryName("musket.shot"),
				ModSounds.flintlockcockstart = new SoundEvent(new ResourceLocation(Reference.MODID, "musket.flintlockcockstart")).setRegistryName("musket.flintlockcockstart"),
				ModSounds.flintlockcockmiddle = new SoundEvent(new ResourceLocation(Reference.MODID, "musket.flintlockcockmiddle")).setRegistryName("musket.flintlockcockmiddle"),
				ModSounds.flintlockcockfull = new SoundEvent(new ResourceLocation(Reference.MODID, "musket.flintlockcockfull")).setRegistryName("musket.flintlockcockfull"),

				ModSounds.flintlockmisfire = new SoundEvent(new ResourceLocation(Reference.MODID, "musket.flintlockmisfire")).setRegistryName("musket.flintlockmisfire"),

				ModSounds.breakactionopen = new SoundEvent(new ResourceLocation(Reference.MODID, "breakaction.open")).setRegistryName("breakaction.open"),
				ModSounds.breakactionshellload = new SoundEvent(new ResourceLocation(Reference.MODID, "breakaction.shellload")).setRegistryName("breakaction.shellload"),
				ModSounds.breakactionclose = new SoundEvent(new ResourceLocation(Reference.MODID, "breakaction.close")).setRegistryName("breakaction.close"),
				ModSounds.breakactionfire = new SoundEvent(new ResourceLocation(Reference.MODID, "breakaction.shot")).setRegistryName("breakaction.shot"),
				ModSounds.breakactiondryfire = new SoundEvent(new ResourceLocation(Reference.MODID, "breakaction.dryfire")).setRegistryName("breakaction.dryfire"),


				ModSounds.leveractionload = new SoundEvent(new ResourceLocation(Reference.MODID, "leveraction.load")).setRegistryName("leveraction.load"),
				ModSounds.leveractionshot_reload = new SoundEvent(new ResourceLocation(Reference.MODID, "leveraction.shot_reload")).setRegistryName("leveraction.shot_reload"),
				ModSounds.leveractiondryfire = new SoundEvent(new ResourceLocation(Reference.MODID, "leveraction.dryfire")).setRegistryName("leveraction.dryfire"),

				ModSounds.pumpactionload = new SoundEvent(new ResourceLocation(Reference.MODID, "pumpaction.load")).setRegistryName("pumpaction.load"),
				ModSounds.pumpactionshot_reload = new SoundEvent(new ResourceLocation(Reference.MODID, "pumpaction.shot_reload")).setRegistryName("pumpaction.shot_reload"),
				ModSounds.pumpactiondryfire = new SoundEvent(new ResourceLocation(Reference.MODID, "pumpaction.dryfire")).setRegistryName("pumpaction.dryfire"),

				ModSounds.bullethit = new SoundEvent(new ResourceLocation(Reference.MODID, "bullet.hit")).setRegistryName("bullet.hit"),
				ModSounds.bulletricochet = new SoundEvent(new ResourceLocation(Reference.MODID, "bullet.ricochet")).setRegistryName("bullet.ricochet"),

				ModSounds.grenadefuse = new SoundEvent(new ResourceLocation(Reference.MODID, "grenade.fuse")).setRegistryName("grenade.fuse"),
				ModSounds.grenadetoss = new SoundEvent(new ResourceLocation(Reference.MODID, "grenade.toss")).setRegistryName("grenade.toss"),

				ModSounds.sentryfire = new SoundEvent(new ResourceLocation(Reference.MODID, "sentry.fire")).setRegistryName("sentry.fire")


				);

		LOGGER.info("Sound events registered.");
	}	

}	