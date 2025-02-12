package org.xiyu.yee.capemanage.mixin;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.xiyu.yee.capemanage.Capemanage;
import org.xiyu.yee.capemanage.data.PlayerCapeData;
import org.xiyu.yee.capemanage.util.CapeManager;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.File;

@Mixin(AbstractClientPlayer.class)
public class PlayerRendererMixin {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Inject(method = "getCloakTextureLocation",
            at = @At("HEAD"),
            cancellable = true)
    private void onGetCapeLocation(CallbackInfoReturnable<ResourceLocation> cir) {
        AbstractClientPlayer player = (AbstractClientPlayer)(Object)this;
        String playerName = player.getName().getString();
        File playerDataFile = new File(Capemanage.getCapeFolder(), playerName + ".json");

        if (playerDataFile.exists()) {
            PlayerCapeData capeData = CapeManager.loadCapeData(playerDataFile);
            if (capeData != null && capeData.isCapeEnabled()) {
                String capeName = capeData.getCapeName();
                ResourceLocation capeLocation;

                if (capeName.startsWith("egg.")) {
                    String eggName = capeName.substring(4);
                    String eggFileName = getEggTextureFileName(eggName);
                    capeLocation = new ResourceLocation(Capemanage.MODID,
                        "textures/capes/egg/" + eggFileName);

                } else {
                    String fileName = getTextureFileName(capeName);
                    capeLocation = new ResourceLocation(Capemanage.MODID,
                        "textures/capes/" + fileName);

                }

                cir.setReturnValue(capeLocation);
                cir.cancel();
            }
        }
    }

    private String getTextureFileName(String capeId) {
        String fileName = switch (capeId) {
            case "1m" -> "1mcape.png";
            case "bugtracker" -> "bugtracker_cape.png";
            case "bacon" -> "bacon_cape.png";
            case "15year" -> "15year_texture.png";
            case "cherry" -> "cherry_cape_texture.png";
            case "translator_chinese" -> "chinese_translator_cape.png";
            case "translator" -> "crowdin_translator_cape.png";
            case "db" -> "db_cape.png";
            case "cobalt" -> "cobalt_cape.png";
            case "eyeblossom" -> "eyeblossom_texture.png";
            case "followers" -> "followers_cape_texture.png";
            case "julian" -> "julian_clark.png";
            case "realms_mapmaker" -> "mapmaker_cape_texture.png";
            case "mcc" -> "mc_championship_texture.png";
            case "minecon_2016" -> "minecon_2016_cape.png";
            case "minecon_2011" -> "minecon_2011_cape.png";
            case "minecon_2015" -> "minecon_2015_cape_texture.png";
            case "experience" -> "minecraft_experience_rescue_texture.png";
            case "minecon_2012" -> "minecon_2012_cape.png";
            case "mojang_classic" -> "mojang_cape.png";
            case "mojang_2015" -> "mojang_cape_2015.png";
            case "mrmessiah" -> "mrmessiah_2d.png";
            case "mojang_studios" -> "ms_cape.png";
            case "minecon_2013" -> "minecon_2013_cape.png";
            case "party" -> "party_cape.png";
            case "prismarine" -> "prismarine_cape.png";
            case "purple_heart" -> "purple_heart_cape.png";
            case "scrolls" -> "scrolls_cape.png";
            case "turtle" -> "turtle_cape.png";
            case "vanilla" -> "vanilla_cape.png";
            case "valentine" -> "valentine_cape.png";
            case "volgar" -> "volgar_cape.png";
            default -> capeId.toLowerCase() + ".png";
        };
        return fileName.toLowerCase();
    }

    private String getEggTextureFileName(String eggId) {
        String fileName = switch (eggId) {
            case "banana" -> "banana.png";
            case "dragongril" -> "dragongril.png";
            case "gengsheng" -> "gengsheng.png";
            case "ikun" -> "ikun.png";
            case "kobe" -> "kobe.png";
            case "kong_xing" -> "kong_xing.png";
            case "fdbclient" -> "fdbclient.png";
            case "cheems" -> "cheems.png";
            case "ydmy" -> "ydmy.png";
            case "dingzhen" -> "dingzhen.png";
            default -> eggId.toLowerCase() + "_cape.png";
        };
        return fileName.toLowerCase();
    }
}