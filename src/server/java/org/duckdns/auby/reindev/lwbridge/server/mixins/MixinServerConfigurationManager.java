package org.duckdns.auby.reindev.lwbridge.server.mixins;

import java.util.List;

import net.minecraft.silveros.Config;
import net.minecraft.src.server.ChatAllowedCharacters;

import net.minecraft.server.MinecraftServer;
import net.minecraft.src.server.ServerConfigurationManager;
import net.minecraft.src.server.packets.Packet;
import net.minecraft.src.server.packets.Packet3Chat;
import net.minecraft.src.game.entity.player.EntityPlayerMP;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.duckdns.auby.reindev.lwbridge.DuckServer;

@Mixin(ServerConfigurationManager.class)
public class MixinServerConfigurationManager {
	@Inject(method="sendPacketToAllPlayers", at=@At(value="HEAD"), require=1)
	public void sendPacketToAllPlayers(Packet p, CallbackInfo ci) {
		if(p instanceof Packet3Chat && !((Packet3Chat) p).message.startsWith("[bridge] ")) {
			DuckServer.get_instance().send_into_bridge(((Packet3Chat) p).message);
		}
	}
}
