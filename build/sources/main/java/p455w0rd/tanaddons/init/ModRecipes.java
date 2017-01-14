package p455w0rd.tanaddons.init;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import toughasnails.api.item.TANItems;

/**
 * @author p455w0rd
 *
 */
public class ModRecipes {

	public static void init() {
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.HEATER), new Object[] {
				"aba", "bcb", "aba", Character.valueOf('b'), new ItemStack(Items.REDSTONE), Character.valueOf('c'), new ItemStack(Items.BLAZE_ROD), Character.valueOf('a'), new ItemStack(Blocks.IRON_BLOCK)
		}));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.AC), new Object[] {
				"aba", "bcb", "aba", Character.valueOf('b'), new ItemStack(Items.REDSTONE), Character.valueOf('c'), new ItemStack(TANItems.freeze_rod), Character.valueOf('a'), new ItemStack(Blocks.IRON_BLOCK)
		}));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.TEMP_REGULATOR), new Object[] {
				"aaa", "bcd", "eee", Character.valueOf('a'), new ItemStack(Items.BLAZE_ROD), Character.valueOf('b'), new ItemStack(ModBlocks.AC), Character.valueOf('c'), new ItemStack(Blocks.IRON_BLOCK), Character.valueOf('d'), new ItemStack(ModBlocks.HEATER), Character.valueOf('e'), new ItemStack(TANItems.freeze_rod)
		}));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.TEMP_REGULATOR), new Object[] {
				"aba", "aca", "ada", Character.valueOf('a'), new ItemStack(Items.IRON_INGOT), Character.valueOf('b'), new ItemStack(ModBlocks.AC), Character.valueOf('c'), new ItemStack(ModBlocks.TEMP_REGULATOR), Character.valueOf('d'), new ItemStack(ModBlocks.HEATER)
		}));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.THIRST_QUENCHER), new Object[] {
				"aba", "aca", "ada", Character.valueOf('a'), new ItemStack(Items.IRON_INGOT), Character.valueOf('b'), new ItemStack(TANItems.charcoal_filter), Character.valueOf('c'), new ItemStack(TANItems.canteen), Character.valueOf('d'), new ItemStack(Items.POTIONITEM)
		}));

	}

}
