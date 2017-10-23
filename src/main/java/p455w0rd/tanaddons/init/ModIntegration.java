package p455w0rd.tanaddons.init;

import net.minecraftforge.fml.common.Loader;
import p455w0rd.tanaddons.integration.IC2;

/**
 * @author p455w0rd
 *
 */
public class ModIntegration {

	public static void postInit() {
		IC2.postInit();
	}

	public static enum Mods {
			BAUBLES("baubles", "Baubles"),
			BAUBLESAPI("Baubles|API", "Baubles API"),
			JEI("jei", "Just Enough Items"), IC2("ic2", "IndustrialCraft 2");

		private String modid, name;

		Mods(String modidIn, String nameIn) {
			modid = modidIn;
			name = nameIn;
		}

		public String getId() {
			return modid;
		}

		public String getName() {
			return name;
		}

		public boolean isLoaded() {
			return Loader.isModLoaded(getId());
		}
	}

}
