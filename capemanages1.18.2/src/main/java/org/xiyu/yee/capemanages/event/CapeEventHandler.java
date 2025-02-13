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
        // 使用1.18.1的客户端检查方法
        if (event.getPlayer().level.isClientSide) {
            String playerName = event.getPlayer().getName().getString();
            // 使用1.18.1的执行器API
            Minecraft.getInstance().execute(() -> {
                if (Minecraft.getInstance().getConnection() != null) {
                    CapeManager.INSTANCE.onPlayerJoin(playerName);
                }
            });
        }
    }
} 