package p455w0rd.tanaddons.proxy;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import p455w0rd.tanaddons.init.ModBlocks;
import p455w0rd.tanaddons.init.ModConfig;
import p455w0rd.tanaddons.init.ModEvents;
import p455w0rd.tanaddons.init.ModIntegration;
import p455w0rd.tanaddons.init.ModItems;
import p455w0rd.tanaddons.init.ModNetworking;
import p455w0rd.tanaddons.init.ModRecipes;

public class CommonProxy {

	public void preInit(FMLPreInitializationEvent e) {
		ModConfig.init();
		ModBlocks.init();
		ModItems.init();
		ModRecipes.init();
		ModNetworking.init();
	}

	public void init(FMLInitializationEvent e) {
		ModEvents.init();
	}

	public void postInit(FMLPostInitializationEvent e) {
		ModIntegration.postInit();
	}

	public void serverStarting(FMLServerStartingEvent e) {

	}

}
