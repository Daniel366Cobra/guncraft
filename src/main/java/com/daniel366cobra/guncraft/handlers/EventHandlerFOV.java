package com.daniel366cobra.guncraft.handlers;

import com.daniel366cobra.guncraft.Reference;
import com.daniel366cobra.guncraft.client.gui.HUDSniperOverlay;
import com.daniel366cobra.guncraft.client.gui.HUDSniperScope;
import com.daniel366cobra.guncraft.init.ModItems;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Reference.MODID)
public class EventHandlerFOV
{
	private static EntityPlayer player;
	private static Minecraft mc;
	//private static ItemStack heldItem;

	@SubscribeEvent
	public static void onRenderGui(RenderGameOverlayEvent.Pre event)
	{
		if (event.getType() == ElementType.CROSSHAIRS)
		{
			mc = Minecraft.getMinecraft();

			if (player != null && player.getActiveHand() != null && mc.gameSettings.thirdPersonView == 0)
			{
				if (player.getHeldItem(player.getActiveHand()).getItem() == ModItems.leveractionrifle && player.isSneaking())
				{
					event.setCanceled(true);
					new HUDSniperScope(mc);
				}
			}
		}
		if (event.getType() == ElementType.HOTBAR)
		{
			mc = Minecraft.getMinecraft();

			if (player != null && player.getActiveHand() != null && mc.gameSettings.thirdPersonView == 0)
			{
				if (player.getHeldItem(player.getActiveHand()).getItem() == ModItems.leveractionrifle && player.isSneaking())
				{
					event.setCanceled(true);
					new HUDSniperOverlay(mc);
				}
			}
		}
	}

	@SubscribeEvent
	public static void FOVUpdate(FOVUpdateEvent event)
	{
		mc = Minecraft.getMinecraft();
		player = event.getEntity();

		//heldItem = player.getActiveItemStack();
		if (player != null && player.getActiveHand() != null && mc.gameSettings.thirdPersonView == 0)
		{
			if(player.getHeldItem(player.getActiveHand()).getItem() == ModItems.leveractionrifle && player.isSneaking())
			{
				event.setNewfov(0.15F);
			}
		}
	}

}
