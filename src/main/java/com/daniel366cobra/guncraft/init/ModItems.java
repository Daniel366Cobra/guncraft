package com.daniel366cobra.guncraft.init;

import com.daniel366cobra.guncraft.Reference;
import com.daniel366cobra.guncraft.items.ItemGrenade;
import com.daniel366cobra.guncraft.items.ItemLeverActionCartridge;
import com.daniel366cobra.guncraft.items.ItemLeverActionCartridgeIncendiary;
import com.daniel366cobra.guncraft.items.ItemLeverActionRifle;
import com.daniel366cobra.guncraft.items.ItemMusket;
import com.daniel366cobra.guncraft.items.ItemMusketBall;
import com.daniel366cobra.guncraft.items.ItemNVGoggles;
import com.daniel366cobra.guncraft.items.ItemPumpActionShotgun;
import com.daniel366cobra.guncraft.items.ItemSentryBox;
import com.daniel366cobra.guncraft.items.ItemShotgunShell;
import com.daniel366cobra.guncraft.items.ItemShotgunShellIncendiary;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = Reference.MODID)
public class ModItems {

	public static final ItemMusket musket = new ItemMusket();
	public static final ItemMusketBall musketball = new ItemMusketBall();

	public static final ItemLeverActionRifle leveractionrifle = new ItemLeverActionRifle();
	public static final ItemLeverActionCartridge leveractioncartridge = new ItemLeverActionCartridge();
	public static final ItemLeverActionCartridgeIncendiary leveractioncartridgeincendiary = new ItemLeverActionCartridgeIncendiary();

	public static final ItemPumpActionShotgun pumpactionshotgun = new ItemPumpActionShotgun();
	public static final ItemShotgunShell shotgunshell = new ItemShotgunShell();
	public static final ItemShotgunShellIncendiary shotgunshellincendiary = new ItemShotgunShellIncendiary();

	public static final ItemGrenade grenade = new ItemGrenade();

	public static final ItemSentryBox sentrybox = new ItemSentryBox();

	public static final ItemNVGoggles nvgoggles = new ItemNVGoggles(ArmorMaterial.LEATHER, 0, EntityEquipmentSlot.HEAD);

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		register(event.getRegistry());
	}

	public static void register(final IForgeRegistry<Item> registry)
	{

		final Item[] items = {
				musket,
				musketball,
				leveractionrifle,
				leveractioncartridge,
				leveractioncartridgeincendiary,
				pumpactionshotgun,
				shotgunshell,
				shotgunshellincendiary,
				grenade,
				sentrybox
		};
		registry.registerAll(items);
		registry.register(nvgoggles);
	}


	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void registerModels(ModelRegistryEvent event) {

		ModItems.initModels();
	}

	public static void initModels()
	{
		musket.initModel();
		musketball.initModel();
		leveractionrifle.initModel();
		leveractioncartridge.initModel();
		leveractioncartridgeincendiary.initModel();
		pumpactionshotgun.initModel();
		shotgunshell.initModel();
		shotgunshellincendiary.initModel();
		grenade.initModel();
		sentrybox.initModel();
		nvgoggles.initModel();

	}
}
