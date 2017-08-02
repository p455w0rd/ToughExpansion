package p455w0rd.tanaddons.blocks;

import static p455w0rd.tanaddons.tiles.TileTempRegulator.TAG_ENERGY;
import static p455w0rd.tanaddons.tiles.TileTempRegulator.TAG_MODE;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.tanaddons.init.ModConfig.Options;
import p455w0rd.tanaddons.init.ModCreativeTab;
import p455w0rd.tanaddons.tiles.TileTempRegulator;

/**
 * @author p455w0rd
 *
 */
public class BlockTempRegulator extends BlockContainer {

	public static final PropertyBool ACTIVE = PropertyBool.create("active");
	private static final String NAME = "temp_regulator";

	public BlockTempRegulator() {
		super(Material.IRON);
		setUnlocalizedName(NAME);
		setRegistryName(NAME);
		setResistance(600000.0F);
		setHardness(10.0F);
		ForgeRegistries.BLOCKS.register(this);
		ItemBlock itemBlock = new ItemBlock(this);
		itemBlock.setRegistryName(NAME);
		ForgeRegistries.ITEMS.register(itemBlock);
		setCreativeTab(ModCreativeTab.TAB);
		//ForgeRegistries.ITEMS.register(new ItemBlock(this));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> tab) {
		ItemStack full = new ItemStack(this);
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger(TAG_ENERGY, Options.TEMP_REGULATOR_RF_CAPACITY);
		compound.setInteger(TAG_MODE, 2);
		full.setTagInfo("BlockEntityTag", compound);
		tab.add(full);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (hand == EnumHand.MAIN_HAND && getTE(world, pos) != null) {
			if (!world.isRemote) {
				if (!player.isSneaking()) {
					getTE(world, pos).nextMode();
				}
			}
			else {
				if (player.isSneaking()) {
					player.sendMessage(new TextComponentString("RF: " + getTE(world, pos).getEnergyStored() + " (" + getTE(world, pos).getEnergyUse() + " RF/t per player while adjusting temp)"));
				}
				else {

					String[] msg = {
							"message.tanaddons.noredstonerequired",
							"message.tanaddons.redstoneignored",
							"message.tanaddons.redstonerequired"
					};
					player.sendMessage(new TextComponentString(I18n.format(msg[getTE(world, pos).getMode()])));
					return true;
				}
			}
		}
		return false;
	}

	private TileTempRegulator getTE(IBlockAccess world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileTempRegulator) {
			return (TileTempRegulator) te;
		}
		return null;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileTempRegulator();
	}

	@Override
	public int getLightValue(IBlockState state) {
		return state.getValue(ACTIVE) ? 7 : 0;
	}

	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
		updatePowered(world, pos, world.getBlockState(pos));
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		updatePowered(world, pos, state);
	}

	private void updatePowered(IBlockAccess world, BlockPos pos, IBlockState state) {
		if (getTE(world, pos) == null) {
			return;
		}
		TileTempRegulator te = getTE(world, pos);
		boolean running = te.isRunning();
		if (running != world.getBlockState(pos).getValue(ACTIVE) && world instanceof World) {
			((World) world).setBlockState(pos, state.withProperty(ACTIVE, Boolean.valueOf(running)), 2);
		}
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		boolean running = getTE(world, pos) == null ? false : getTE(world, pos).isRunning();
		return getDefaultState().withProperty(ACTIVE, Boolean.valueOf(running));
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		world.notifyBlockUpdate(pos, state, state, 2);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(ACTIVE, Boolean.valueOf(meta == 0 ? false : true));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(ACTIVE) ? 1 : 0;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {
				ACTIVE
		});
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag advanced) {
		tooltip.add("");
		tooltip.add(I18n.format("tooltip.tanaddons.redstone_rclick.desc"));
		tooltip.add(I18n.format("tooltip.tanaddons.redstone_rclick.desc2"));
	}

	@SideOnly(Side.CLIENT)
	public void initModel() {
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
	}

	@Override
	public boolean hasTileEntity() {
		return true;
	}

	@Override
	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, @Nullable ItemStack stack) {
		ItemStack itemstack = new ItemStack(this);
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		te.writeToNBT(nbttagcompound);
		itemstack.setTagInfo("BlockEntityTag", nbttagcompound);
		spawnAsEntity(worldIn, pos, itemstack);
	}

}
