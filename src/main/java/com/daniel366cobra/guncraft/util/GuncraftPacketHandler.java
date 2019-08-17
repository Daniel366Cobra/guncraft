package com.daniel366cobra.guncraft.util;

import com.daniel366cobra.guncraft.Reference;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public final class GuncraftPacketHandler {
	
	
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel HANDLER = NetworkRegistry.newSimpleChannel(
		    new ResourceLocation(Reference.MODID, "main"),
		    () -> PROTOCOL_VERSION,
		    PROTOCOL_VERSION::equals,
		    PROTOCOL_VERSION::equals
		);	

	public static void register() {
		//int channelid = 0;
		//HANDLER.registerMessage(channelid++, LaserSpawnPacket.class, LaserSpawnPacket::encode, LaserSpawnPacket::decode, LaserSpawnPacket.Handler::handle);
	}
	
}

