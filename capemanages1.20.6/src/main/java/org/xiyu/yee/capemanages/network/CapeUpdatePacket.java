package org.xiyu.yee.capemanages.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkDirection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.client.Minecraft;

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

    public static void handle(CapeUpdatePacket packet, CustomPayloadEvent.Context context){
        context.enqueueWork(() -> {

            // 服务器端：转发给所有玩家
            if (context.getDirection().getReceptionSide() == LogicalSide.SERVER) {
                ServerPlayer sender = context.getSender();
                if (sender != null) {
                    MinecraftServer server = sender.getServer();
                    if (server != null) {
                        System.out.println("Server forwarding cape update from " + packet.playerName);
                        // 转发给所有玩家，包括发送者
                        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                            CapeManager.NETWORK.send(packet, player.connection.getConnection());
                        }
                    }
                }
            }
            // 客户端端：应用更新
            else {
                CapeManager.INSTANCE.handleUpdatePacket(packet.playerName, packet.url);
            }
        });
        context.setPacketHandled(true);
    }
}