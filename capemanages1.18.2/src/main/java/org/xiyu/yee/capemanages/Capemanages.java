package org.xiyu.yee.capemanages;


import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xiyu.yee.capemanages.cape.CapeManager;
import org.xiyu.yee.capemanages.command.CapeCommand;
import org.xiyu.yee.capemanages.event.CapeEventHandler;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("capemanages")
public class Capemanages {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "capemanages";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LoggerFactory.getLogger("capemanages");

    public Capemanages() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new CapeEventHandler());
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Registering network channels...");
        CapeManager.registerNetworking();

        LOGGER.info("Initializing Cape Manager...");
        CapeManager.INSTANCE.init();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        LOGGER.info("Registering Cape Commands...");
        CapeCommand.register(event.getDispatcher());
    }
}
