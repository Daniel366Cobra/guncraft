package com.daniel366cobra.guncraft;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.daniel366cobra.guncraft.client.init.ModRenderers;
import com.daniel366cobra.guncraft.itemgroups.GuncraftAmmoItemGroup;
import com.daniel366cobra.guncraft.itemgroups.GuncraftPartsItemGroup;
import com.daniel366cobra.guncraft.itemgroups.GuncraftWeaponsItemGroup;
import com.daniel366cobra.guncraft.util.GuncraftPacketHandler;

import net.minecraft.item.ItemGroup;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Reference.MODID)
public class GuncraftMod
{
	// Directly reference a log4j logger.
	public static final Logger LOGGER = LogManager.getLogger();

	public static final ItemGroup GUNCRAFT_WEAPONS_ITEMGROUP = new GuncraftWeaponsItemGroup();
	public static final ItemGroup GUNCRAFT_PARTS_ITEMGROUP = new GuncraftPartsItemGroup();
	public static final ItemGroup GUNCRAFT_AMMO_ITEMGROUP = new GuncraftAmmoItemGroup();

	public GuncraftMod() {
		
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientRegistries);
		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);
		}
	
	 private void setup(final FMLCommonSetupEvent event)
	    {	//  preinit code  
	    	
	    	DeferredWorkQueue.runLater(() -> {
	    		GuncraftPacketHandler.register();
	    	});
	    
	    }
	
	private void clientRegistries(final FMLClientSetupEvent event)
	{		
		ModRenderers.registerEntityRenderers();		
	}
}

