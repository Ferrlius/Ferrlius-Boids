package com.ferrlius.boids.ferboids;

import com.ferrlius.boids.ferboids.command.BoidCommand;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Ferboids.MODID)
public class Ferboids {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "ferboids";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();


    public Ferboids() {

        // Register the command event
        MinecraftForge.EVENT_BUS.addListener(BoidCommand::onRegisterCommands);
        LOGGER.info("Ferboids command registered.");

        // You can also perform additional setup here:
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Ferboids common setup complete.");
    }
}