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
    @OnlyIn(Dist.CLIENT)
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        // 确保在客户端执行
        if (event.getEntity().level().isClientSide()) {
            String playerName = event.getEntity().getName().getString();
            // 延迟执行以确保网络连接已建立
            Minecraft.getInstance().tell(() -> {
                if (Minecraft.getInstance().getConnection() != null) {
                    CapeManager.INSTANCE.onPlayerJoin(playerName);
                }
            });
        }
    }
} 