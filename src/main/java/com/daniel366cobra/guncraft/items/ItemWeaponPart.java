package com.daniel366cobra.guncraft.items;

import com.daniel366cobra.guncraft.GuncraftMod;
import com.daniel366cobra.guncraft.Reference;

import net.minecraft.item.Item;

public class ItemWeaponPart extends Item {
	
	public static Item makeDefaultItem(String registryname) {
		Item.Properties props = new Item.Properties()
			.maxStackSize(1)
			.group(GuncraftMod.GUNCRAFT_PARTS_ITEMGROUP);
		Item item = new ItemWeaponPart(props)
			.setRegistryName(Reference.MODID, registryname);
		return item;
	}
	
	public ItemWeaponPart(Properties builder) {
		super(builder);	
	}

}
