package com.daniel366cobra.guncraft;

import net.minecraft.util.ResourceLocation;

public class Reference {

	public static final String MODID = "guncraft";
	public static final String VERSION = "1.0";
	
	public static ResourceLocation location(String name)
	{
		return new ResourceLocation(MODID, name);
	}
	
}
