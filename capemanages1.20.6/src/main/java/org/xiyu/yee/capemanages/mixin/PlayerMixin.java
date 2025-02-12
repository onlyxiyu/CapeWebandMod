package org.xiyu.yee.capemanages.mixin;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.xiyu.yee.capemanages.cape.CapeManager;

@Mixin(AbstractClientPlayer.class)
public class PlayerMixin {
    @Inject(method = "getSkin", at = @At("HEAD"), cancellable = true)
    private void onGetCapeLocation(CallbackInfoReturnable<ResourceLocation> cir) {
        AbstractClientPlayer player = (AbstractClientPlayer) (Object) this;
        ResourceLocation cape = CapeManager.INSTANCE.getPlayerCape(player.getGameProfile().getName());
        if (cape != null && CapeManager.INSTANCE.isEnabled()) {
            cir.setReturnValue(cape);
            cir.cancel();
        }
    }
}