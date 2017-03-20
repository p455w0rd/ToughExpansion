package p455w0rd.tanaddons.init;

import net.minecraftforge.fml.common.registry.GameRegistry;
import p455w0rd.tanaddons.blocks.BlockTempRegulator;
import p455w0rd.tanaddons.tiles.TileTempRegulator;

/**
 * @author p455w0rd
 *
 */
public class ModBlocks {

	public static BlockTempRegulator TEMP_REGULATOR;

	public static void init() {
		TEMP_REGULATOR = new BlockTempRegulator();
		GameRegistry.registerTileEntity(TileTempRegulator.class, ModGlobals.MODID + ":tempregulator");
	}

	public static void initModels() {
		TEMP_REGULATOR.initModel();
	}

}
