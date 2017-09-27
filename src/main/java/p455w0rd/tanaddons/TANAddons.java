package p455w0rd.tanaddons;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import p455w0rd.tanaddons.init.ModGlobals;
import p455w0rd.tanaddons.proxy.CommonProxy;

@Mod(modid = ModGlobals.MODID, name = ModGlobals.NAME, version = ModGlobals.VERSION, dependencies = ModGlobals.DEPENDANCIES, acceptedMinecraftVersions = "[1.12,1.12.2]")
public class TANAddons {

	@SidedProxy(clientSide = ModGlobals.CLIENT_PROXY, serverSide = ModGlobals.SERVER_PROXY)
	public static CommonProxy PROXY;

	@Mod.Instance(ModGlobals.MODID)
	public static TANAddons INSTANCE;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent e) {
		INSTANCE = this;
		PROXY.preInit(e);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent e) {
		PROXY.init(e);
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent e) {
		PROXY.postInit(e);
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent e) {
		PROXY.serverStarting(e);
	}
}
