 package org.xiyu.yee.capemanages.mixin;
 import net.minecraft.client.multiplayer.PlayerInfo;
 import net.minecraft.client.player.AbstractClientPlayer;
 import net.minecraft.client.resources.PlayerSkin;
 import net.minecraft.resources.ResourceLocation;
 import org.spongepowered.asm.mixin.Mixin;
 import org.spongepowered.asm.mixin.Shadow;
 import org.spongepowered.asm.mixin.injection.At;
 import org.spongepowered.asm.mixin.injection.Inject;
 import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
 import org.xiyu.yee.capemanages.cape.CapeManager;

 import javax.annotation.Nullable;

 @Mixin(AbstractClientPlayer.class)
 public abstract class PlayerMixin {
     @Shadow @Nullable private PlayerInfo playerInfo;

     @Inject(method = "getSkin", at = @At("HEAD"), cancellable = true)
     private void getCustomSkin(CallbackInfoReturnable<PlayerSkin> cir) {
         if (playerInfo != null) {
             String playerName = playerInfo.getProfile().getName();
             ResourceLocation customSkinLocation = CapeManager.INSTANCE.getPlayerCape(playerName);
             if (customSkinLocation != null) {
                 PlayerSkin customSkin = new PlayerSkin(customSkinLocation, playerInfo.getSkin().textureUrl(), customSkinLocation, customSkinLocation, playerInfo.getSkin().model(), playerInfo.getSkin().secure());
                 cir.setReturnValue(customSkin);
             }
         }
     }
 }