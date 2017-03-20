package p455w0rd.tanaddons.init;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * @author p455w0rd
 *
 */
public class ModCreativeTab extends CreativeTabs {

	public static final CreativeTabs TAB = new ModCreativeTab();

	public ModCreativeTab() {
		super(ModGlobals.MODID);
		//setBackgroundImageName(ModGlobals.MODID + ".png");
	}

	@Override
	public ItemStack getIconItemStack() {
		return new ItemStack(ModBlocks.TEMP_REGULATOR);
		//return new ItemStack(ModItems.dankNullItem, 1, 5);
	}

	@Override
	public Item getTabIconItem() {
		return new ItemStack(ModBlocks.TEMP_REGULATOR).getItem();
	}
}
