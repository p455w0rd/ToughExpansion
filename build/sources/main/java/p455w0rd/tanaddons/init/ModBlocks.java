package p455w0rd.tanaddons.init;

import net.minecraftforge.fml.common.registry.GameRegistry;
import p455w0rd.tanaddons.blocks.BlockAC;
import p455w0rd.tanaddons.blocks.BlockHeater;
import p455w0rd.tanaddons.blocks.BlockTempRegulator;
import p455w0rd.tanaddons.tiles.TilePoweredTempSpread;
import p455w0rd.tanaddons.tiles.TileTempRegulator;

/**
 * @author p455w0rd
 *
 */
public class ModBlocks {

	public static BlockAC AC;
	public static BlockHeater HEATER;
	public static BlockTempRegulator TEMP_REGULATOR;

	public static void init() {
		AC = new BlockAC();
		HEATER = new BlockHeater();
		TEMP_REGULATOR = new BlockTempRegulator();
		GameRegistry.registerTileEntity(TileTempRegulator.class, ModGlobals.MODID + ":tempregulator");
		GameRegistry.registerTileEntity(TilePoweredTempSpread.class, ModGlobals.MODID + ":poweredtempspread");
	}

	public static void initModels() {
		AC.initModel();
		HEATER.initModel();
		TEMP_REGULATOR.initModel();
	}

}
