package com.daniel366cobra.guncraft.items;

import com.daniel366cobra.guncraft.GunCraft;
import com.daniel366cobra.guncraft.Reference;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemNVGoggles extends ItemArmor
{

	public ItemNVGoggles(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn)
	{
		super(materialIn, renderIndexIn, equipmentSlotIn);
		setNoRepair();
		setUnlocalizedName("nvgoggles");
		setRegistryName(Reference.MODID, "itemnvgoggles");
		setCreativeTab(GunCraft.guncrafttab);	
	}
	
	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack)
	{
		if (player.isPotionActive(MobEffects.BLINDNESS))
		{
			player.removePotionEffect(MobEffects.BLINDNESS);
		}
		player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 250, 0, true, false));
		
	}
	
	@SideOnly(Side.CLIENT)
    public void initModel()	{
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

}
