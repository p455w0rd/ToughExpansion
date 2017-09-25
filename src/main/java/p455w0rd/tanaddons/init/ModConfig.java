package p455w0rd.tanaddons.init;

import java.io.File;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author p455w0rd
 *
 */
public class ModConfig {
	public static Configuration CONFIG;

	private static final String DEF_CAT = "General";

	public static void init() {
		if (CONFIG == null) {
			CONFIG = new Configuration(new File(ModGlobals.CONFIG_FILE));
			MinecraftForge.EVENT_BUS.register(new ModConfig());
		}
		Options.TEMP_REGULATOR_RADIUS = CONFIG.getInt("TempRegulatorBlockRadius", DEF_CAT, 20, 1, 40, "Effective radius of Temperature Regulator Block");
		Options.TEMP_REGULATOR_RF_CAPACITY = CONFIG.getInt("TempRegulatorBlockRFCap", DEF_CAT, 1600000, 1, 16000000, "RF Capacity of Temperator Regulator Block");
		Options.THIRST_HEALTH_REGEN_FIX = CONFIG.getBoolean("ThirstHealthFix", DEF_CAT, true, "Makes health regen respect same logic as hunger. (If player has less than 10 thirst drops, health will not regen.)");
		Options.THIRST_QUENCHER_RF_CAPACITY = CONFIG.getInt("ThirstQuencherRFCap", DEF_CAT, 400000, 1, 1600000, "RF Capacity for Thirst Quencher");
		Options.PORTABLE_TEMP_REGULATOR_CAPACITY = CONFIG.getInt("PortableTempRegulatorCap", DEF_CAT, 400000, 1, 1600000, "RF Capacity for Portable Temperature Regular");
		Options.PORTABLE_TEMP_REGULATOR_RF_PER_TICK = CONFIG.getInt("PortableTempRegulatorRFPerTick", DEF_CAT, 10, 1, 100, "How much RF per tick is consumed while the Portable Temp Regulator is actively regulating temperature");
		Options.THIRST_QUNCHER_RF_PER_TICK = CONFIG.getInt("ThirstQuencherRFPerTick", DEF_CAT, 10, 1, 100, "How much RF per tick is consumed while the Thirst Quencher is actively quenching thirst");
		Options.TEMP_REGULATOR_RF_PER_TICK = CONFIG.getInt("TempRegulatorRFPerTick", DEF_CAT, 100, 1, 10000, "How much RF per tick is consumed while the Temp Regulator (block) is actively regulating temperature");
		Options.THIRST_QUENCHER_WATER_CAPACITY = CONFIG.getInt("ThirstQuencherWaterCapacity", DEF_CAT, 16, 8, 64, "How much water (in buckets) the Thirst Quencher can hold");

		if (CONFIG.hasChanged()) {
			CONFIG.save();
		}
	}

	@SubscribeEvent
	public void onConfigChange(ConfigChangedEvent.OnConfigChangedEvent e) {
		if (e.getModID().equals(ModGlobals.MODID)) {
			init();
		}
	}

	public static class Options {

		public static int TEMP_REGULATOR_RADIUS = 20;
		public static int TEMP_REGULATOR_RF_CAPACITY = 1600000;
		public static boolean THIRST_HEALTH_REGEN_FIX = true;
		public static int THIRST_QUENCHER_RF_CAPACITY = 400000;
		public static int PORTABLE_TEMP_REGULATOR_CAPACITY = 1600000;
		public static int PORTABLE_TEMP_REGULATOR_RF_PER_TICK = 10;
		public static int THIRST_QUNCHER_RF_PER_TICK = 10;
		public static int TEMP_REGULATOR_RF_PER_TICK = 100;
		public static int THIRST_QUENCHER_WATER_CAPACITY = 16;

	}

}
