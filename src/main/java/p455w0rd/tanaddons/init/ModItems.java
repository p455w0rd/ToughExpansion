package p455w0rd.tanaddons.init;

import p455w0rd.tanaddons.items.ItemTempRegulator;
import p455w0rd.tanaddons.items.ItemThirstQuencher;

/**
 * @author p455w0rd
 *
 */
public class ModItems {

	public static ItemTempRegulator TEMP_REGULATOR;
	public static ItemThirstQuencher THIRST_QUENCHER;

	public static void init() {
		TEMP_REGULATOR = new ItemTempRegulator();
		THIRST_QUENCHER = new ItemThirstQuencher();
	}

	public static void initModels() {
		TEMP_REGULATOR.initModel();
		THIRST_QUENCHER.initModel();
	}
}
