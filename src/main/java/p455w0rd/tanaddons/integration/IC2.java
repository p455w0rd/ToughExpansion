package p455w0rd.tanaddons.integration;

import ic2.api.item.ElectricItem;
import p455w0rd.tanaddons.api.IC2ItemManager;
import p455w0rd.tanaddons.init.ModIntegration.Mods;

/**
 * @author p455w0rd
 *
 */
public class IC2 {

	public static void postInit() {
		if (Mods.IC2.isLoaded()) {
			ElectricItem.registerBackupManager(IC2ItemManager.getInstance());
		}
	}

}
