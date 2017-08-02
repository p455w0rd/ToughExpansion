package p455w0rd.tanaddons.blocks;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.tanaddons.init.ModCreativeTab;
import p455w0rd.tanaddons.tiles.TileTempRegulator;

/**
 * @author p455w0rd
 *
 */
public class BlockTempRegulator extends BlockBase {

	public static final PropertyBool ACTIVE = PropertyBool.create("active");
	private static final String NAME = "temp_regulator";

	public BlockTempRegulator() {
		super(Material.IRON, NAME, 10.0f, 60000.0f);
		setCreativeTab(ModCreativeTab.TAB);
		setHarvestLevel("pickaxe", 1);
		setDefaultState(blockState.getBaseState().withProperty(ACTIVE, Boolean.valueOf(false)));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (hand == EnumHand.MAIN_HAND && heldItem == null && getTE(world, pos) != null) {
			if (!world.isRemote) {
				if (!player.isSneaking()) {
					getTE(world, pos).nextMode();
				}
			}
			else {
				if (player.isSneaking()) {
					player.addChatMessage(new TextComponentString("RF: " + getTE(world, pos).getEnergyStored() + " (" + getTE(world, pos).getEnergyUse() + " RF/t per player while adjusting temp)"));
				}
				else {

					String[] msg = {
							"message.tanaddons.noredstonerequired",
							"message.tanaddons.redstoneignored",
							"message.tanaddons.redstonerequired"
					};
					player.addChatMessage(new TextComponentString(I18n.format(msg[getTE(world, pos).getMode()])));
					return true;
				}
			}
		}
		return false;
	}

	private TileTempRegulator getTE(World world, BlockPos pos) {
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
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock) {
		updatePowered(world, pos, state);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		updatePowered(world, pos, state);
	}

	private void updatePowered(World world, BlockPos pos, IBlockState state) {
		if (getTE(world, pos) == null) {
			return;
		}
		TileTempRegulator te = getTE(world, pos);
		boolean running = te.isRunning();
		if (running != world.getBlockState(pos).getValue(ACTIVE)) {
			world.setBlockState(pos, state.withProperty(ACTIVE, Boolean.valueOf(running)), 2);
		}
	}

	@Override
	public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
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

}
