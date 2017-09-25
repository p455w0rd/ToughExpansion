package p455w0rd.tanaddons.init;

public class ModGlobals {

	public static final String MODID_BAUBLES = "baubles";
	public static final String MODID_TAN = "toughasnails";
	public static final String MODID_PWLIB = "p455w0rdslib";

	public static final String MODID = "tanaddons";
	public static final String VERSION = "3.1.4";
	public static final String NAME = "Tough Expansion";
	public static final String SERVER_PROXY = "p455w0rd.tanaddons.proxy.CommonProxy";
	public static final String CLIENT_PROXY = "p455w0rd.tanaddons.proxy.ClientProxy";
	public static final String GUI_FACTORY = "p455w0rd.tanaddons.client.gui.GuiFactory";
	public static final String CONFIG_FILE = "config/ToughExpansion.cfg";
	public static final String DEPENDANCIES = "required-after:" + MODID_TAN + ";required-after:" + MODID_PWLIB + ";after:" + MODID_BAUBLES;

}
