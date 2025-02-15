// CapeUpdatePacket.java
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
                    if (context.isServerSide()) {
                        ServerPlayer sender = context.getSender();
                        if (sender != null) {
                            MinecraftServer server = sender.getServer();
                            if (server != null) {
                                System.out.println("Server forwarding cape update from " + packet.playerName);
                                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                                    CapeManager.NETWORK.send(packet, player.connection.getConnection());
                                }
                            }
                        }
                    } else {
                        CapeManager.INSTANCE.handleUpdatePacket(packet.playerName, packet.url);
                    }
                });
                context.setPacketHandled(true);
            }
        }