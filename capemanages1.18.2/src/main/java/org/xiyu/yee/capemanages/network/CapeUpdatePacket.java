package org.xiyu.yee.capemanages.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.xiyu.yee.capemanages.cape.CapeManager;
import java.util.function.Supplier;

public class CapeUpdatePacket {
    private final String playerName;
    private final String url;
    
    public CapeUpdatePacket(String playerName, String url) {
        this.playerName = playerName;
        this.url = url;
    }
    
    public static void encode(CapeUpdatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.playerName);
        buffer.writeUtf(packet.url);
    }
    
    public static CapeUpdatePacket decode(FriendlyByteBuf buffer) {
        return new CapeUpdatePacket(
            buffer.readUtf(),
            buffer.readUtf()
        );
    }
    
    public static void handle(CapeUpdatePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide() == LogicalSide.SERVER) {
                ServerPlayer sender = ctx.get().getSender();
                if (sender != null) {
                    MinecraftServer server = sender.getServer();
                    if (server != null) {
                        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                            CapeManager.NETWORK.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
                        }
                    }
                }
            } else {
                CapeManager.INSTANCE.handleUpdatePacket(packet.playerName, packet.url);
            }
        });
        ctx.get().setPacketHandled(true);
    }
} 