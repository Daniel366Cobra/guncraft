package com.daniel366cobra.guncraft.items;

import com.daniel366cobra.guncraft.GuncraftMod;
import com.daniel366cobra.guncraft.Reference;

import net.minecraft.item.Item;

public class ItemAmmunition extends Item{

	public static Item makeDefaultItem(String registryname) {
		Item.Properties props = new Item.Properties()
			.maxStackSize(64)
			.group(GuncraftMod.GUNCRAFT_AMMO_ITEMGROUP);
		Item item = new ItemAmmunition(props)
			.setRegistryName(Reference.MODID, registryname);
		return item;
	}
	
	public ItemAmmunition(Properties builder) {
		super(builder);	
	}

}
