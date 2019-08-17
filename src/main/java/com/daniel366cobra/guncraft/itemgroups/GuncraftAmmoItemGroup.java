package com.daniel366cobra.guncraft.itemgroups;

import com.daniel366cobra.guncraft.init.ModItems;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class GuncraftAmmoItemGroup extends ItemGroup{
	

	public GuncraftAmmoItemGroup() {
		super("guncraft.ammo");		
	}

	@Override
	public ItemStack createIcon() {		
		return new ItemStack(ModItems.lever_action_cartridge);
	}


}
