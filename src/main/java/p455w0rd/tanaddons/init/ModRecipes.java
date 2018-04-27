package p455w0rd.tanaddons.init;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import p455w0rdslib.util.RecipeUtils;
import toughasnails.api.TANBlocks;
import toughasnails.api.item.TANItems;

/**
 * @author p455w0rd
 *
 */
public class ModRecipes {

	private static RecipeUtils recipeUtils = new RecipeUtils(ModGlobals.MODID, ModGlobals.NAME);

	private static final ModRecipes INSTANCE = new ModRecipes();
	private static boolean init = true;

	public static final List<IRecipe> CRAFTING_RECIPES = Lists.<IRecipe>newLinkedList();

	public IRecipe tempRegulator;
	public IRecipe portableTempRegulator;
	public IRecipe thirstQuencher;

	public static ModRecipes getInstance() {
		return INSTANCE;
	}

	public static void init() {
		if (init) {
			getInstance().addRecipes();
			init = false;
		}
	}

	@SuppressWarnings("deprecation")
	public void addRecipes() {

		CRAFTING_RECIPES.add(tempRegulator = recipeUtils.addOldShaped(new ItemStack(ModBlocks.TEMP_REGULATOR), new Object[] {
				"aaa",
				"bcd",
				"eee",
				Character.valueOf('a'),
				new ItemStack(TANItems.magma_shard),
				Character.valueOf('b'),
				new ItemStack(TANBlocks.temperature_coil),
				Character.valueOf('c'),
				new ItemStack(Blocks.IRON_BLOCK),
				Character.valueOf('d'),
				new ItemStack(TANBlocks.temperature_coil, 1, 1),
				Character.valueOf('e'),
				new ItemStack(TANItems.ice_cube)
		}));

		CRAFTING_RECIPES.add(portableTempRegulator = recipeUtils.addOldShaped(new ItemStack(ModItems.TEMP_REGULATOR), new Object[] {
				"aba",
				"aca",
				"ada",
				Character.valueOf('a'),
				new ItemStack(Items.IRON_INGOT),
				Character.valueOf('b'),
				new ItemStack(TANBlocks.temperature_coil),
				Character.valueOf('c'),
				new ItemStack(ModBlocks.TEMP_REGULATOR),
				Character.valueOf('d'),
				new ItemStack(TANBlocks.temperature_coil, 1, 1)
		}));

		ItemStack waterBottle = PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.WATER);
		CRAFTING_RECIPES.add(thirstQuencher = recipeUtils.addOldShaped(new ItemStack(ModItems.THIRST_QUENCHER), new Object[] {
				"aba",
				"aca",
				"ada",
				Character.valueOf('a'),
				new ItemStack(Items.IRON_INGOT),
				Character.valueOf('b'),
				new ItemStack(TANItems.charcoal_filter),
				Character.valueOf('c'),
				new ItemStack(TANItems.canteen),
				Character.valueOf('d'),
				waterBottle
		}));

		for (IRecipe recipe : CRAFTING_RECIPES) {
			ForgeRegistries.RECIPES.register(recipe);
		}

	}

}
