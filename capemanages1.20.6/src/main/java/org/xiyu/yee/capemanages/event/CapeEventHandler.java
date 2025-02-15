// CapeEventHandler.java
                    package org.xiyu.yee.capemanages.event;

                    import net.minecraft.client.Minecraft;
                    import net.minecraftforge.api.distmarker.Dist;
                    import net.minecraftforge.api.distmarker.OnlyIn;
                    import net.minecraftforge.event.entity.player.PlayerEvent;
                    import net.minecraftforge.eventbus.api.SubscribeEvent;
                    import net.minecraftforge.fml.common.Mod;
                    import org.xiyu.yee.capemanages.Capemanages;
                    import org.xiyu.yee.capemanages.cape.CapeManager;

                    @Mod.EventBusSubscriber(modid = Capemanages.MODID, value = Dist.CLIENT)
                    public class CapeEventHandler {

                        @SubscribeEvent
                        public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
                            if (event.getEntity().level().isClientSide()) {
                                String playerName = event.getEntity().getName().getString();
                                Minecraft.getInstance().execute(() -> {
                                    if (Minecraft.getInstance().getConnection() != null) {
                                        CapeManager.INSTANCE.createPlayerCapeJson(playerName);
                                        CapeManager.INSTANCE.onPlayerJoin(playerName);
                                    }
                                });
                            }
                        }
                    }