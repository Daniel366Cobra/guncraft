package com.daniel366cobra.guncraft.itemgroups;

import com.daniel366cobra.guncraft.init.ModItems;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class GuncraftPartsItemGroup extends ItemGroup{
	

	public GuncraftPartsItemGroup() {
		super("guncraft.parts");		
	}

	@Override
	public ItemStack createIcon() {		
		return new ItemStack(ModItems.trigger_mechanism);
	}


}
