package com.daniel366cobra.guncraft.itemgroups;

import com.daniel366cobra.guncraft.init.ModItems;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class GuncraftWeaponsItemGroup extends ItemGroup{
	

	public GuncraftWeaponsItemGroup() {
		super("guncraft.weapons");		
	}

	@Override
	public ItemStack createIcon() {		
		return new ItemStack(ModItems.dark_oak_musket);
	}


}
