package com.daniel366cobra.guncraft.client.gui;

import org.lwjgl.opengl.GL11;

import com.daniel366cobra.guncraft.Reference;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class HUDSniperScope extends Gui
{
	private static final ResourceLocation SCOPE_TEXTURE = new ResourceLocation(Reference.MODID, "textures/gui/sniperscope.png");
	
	public HUDSniperScope(Minecraft mc)
	{
		ScaledResolution scaled = new ScaledResolution(mc);
		int width = scaled.getScaledWidth();
		int height = scaled.getScaledHeight();
		
		float fov = mc.gameSettings.fovSetting;
		float fovScopeMultiplier = 70.0F / fov;
		
		float scopesize = 128 * fovScopeMultiplier;		
		
		GlStateManager.pushMatrix();		

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_TEXTURE_2D);		
		
		mc.getTextureManager().bindTexture(SCOPE_TEXTURE);
		
		Gui.drawScaledCustomSizeModalRect((int) (width / 2 - scopesize / 2), 
										  (int) (height / 2 - scopesize / 2), 
										  0.0F, 
										  0.0F, 
										  (int)scopesize, 
										  (int)scopesize, 
										  (int)scopesize, 
										  (int)scopesize, 
										  scopesize, 
										  scopesize);		
		
		GlStateManager.popMatrix();
	}
}
