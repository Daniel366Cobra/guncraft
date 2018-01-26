package com.daniel366cobra.guncraft.creativetabs;

import com.daniel366cobra.guncraft.Reference;
import com.daniel366cobra.guncraft.init.ModItems;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class TabGuncraft extends CreativeTabs {

	public TabGuncraft() {
		super(Reference.MODID);
	}

	@Override
	public ItemStack getTabIconItem() {
		return new ItemStack(ModItems.musket);
	}

}
