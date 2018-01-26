package com.daniel366cobra.guncraft.items;

import com.daniel366cobra.guncraft.GunCraft;
import com.daniel366cobra.guncraft.Reference;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemShotgunShell extends Item {
	public ItemShotgunShell()
	{
		setUnlocalizedName("shotgunshell");
		setRegistryName(Reference.MODID, "itemshotgunshell");
		setMaxStackSize(64);
		setCreativeTab(GunCraft.guncrafttab);
	}
	
	@SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

}
