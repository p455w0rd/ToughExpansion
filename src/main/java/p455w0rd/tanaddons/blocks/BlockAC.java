package p455w0rd.tanaddons.blocks;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.tanaddons.init.ModCreativeTab;
import p455w0rd.tanaddons.tiles.TilePoweredTempSpread;

/**
 * @author p455w0rd
 *
 */
public class BlockAC extends BlockBase {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	public static final PropertyBool ACTIVE = PropertyBool.create("active");

	private static final String NAME = "ac";

	public BlockAC(String name) {
		super(Material.IRON, name, 10.0f, 60000.0f);
		setCreativeTab(ModCreativeTab.TAB);
		setHarvestLevel("pickaxe", 1);
		setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(ACTIVE, Boolean.valueOf(false)));
	}

	public BlockAC() {
		this(NAME);
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
					player.addChatMessage(new TextComponentString("RF: " + getTE(world, pos).getEnergyStored() + " (" + getTE(world, pos).getEnergyUse() + " RF/t)"));
				}
				else {
					String[] msg = {
							"message.tanaddons.noredstonerequired", "message.tanaddons.redstoneignored", "message.tanaddons.redstonerequired"
					};
					player.addChatMessage(new TextComponentString(I18n.format(msg[getTE(world, pos).getMode()])));
				}
			}
			return true;
		}
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		EnumFacing enumfacing = state.getValue(FACING);
		if (state.getValue(ACTIVE) == true && world.rand.nextDouble() < 0.25D) {
			double d0 = pos.getX() + 0.5D;
			double d1 = pos.getY() + rand.nextDouble() * 6.0D / 16.0D;
			double d2 = pos.getZ() + 0.5D;
			double d3 = 1.0D;
			double d4 = rand.nextDouble() * 0.6D - 0.3D;
			switch (enumfacing) {
			case WEST:
				world.spawnParticle(EnumParticleTypes.FALLING_DUST, (d0 + 0.3) - d3, d1 + 0.9, d2 + d4, 0.0D, 0.0D, 0.0D, new int[] {
						Block.getStateId(Blocks.SNOW.getDefaultState())
				});
				break;
			case EAST:
				world.spawnParticle(EnumParticleTypes.FALLING_DUST, (d0 - 0.3) + d3, d1 + 0.9, d2 + d4, 0.0D, 0.0D, 0.0D, new int[] {
						Block.getStateId(Blocks.SNOW.getDefaultState())
				});
				break;
			case NORTH:
				world.spawnParticle(EnumParticleTypes.FALLING_DUST, d0 + d4, d1 + 0.9, (d2 + 0.3) - d3, 0.0D, 0.0D, 0.0D, new int[] {
						Block.getStateId(Blocks.SNOW.getDefaultState())
				});
				break;
			case SOUTH:
				world.spawnParticle(EnumParticleTypes.FALLING_DUST, d0 + d4, d1 + 0.9, (d2 - 0.3) + d3, 0.0D, 0.0D, 0.0D, new int[] {
						Block.getStateId(Blocks.SNOW.getDefaultState())
				});
			default:
				break;
			}
		}
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
		TilePoweredTempSpread te = getTE(world, pos);
		boolean running = te.isRunning();
		if (running != world.getBlockState(pos).getValue(ACTIVE)) {
			if (running) {
				te.fill();
			}
			else {
				te.reset();
			}
			world.setBlockState(pos, state.withProperty(ACTIVE, Boolean.valueOf(running)), 2);
		}
	}

	protected TilePoweredTempSpread getTE(World world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TilePoweredTempSpread) {
			return (TilePoweredTempSpread) te;
		}
		return null;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TilePoweredTempSpread(-10);
	}

	@Override
	public int getLightValue(IBlockState state) {
		return state.getValue(ACTIVE) ? 7 : 0;
	}

	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		setDefaultFacing(worldIn, pos, state);
	}

	private void setDefaultFacing(World worldIn, BlockPos pos, IBlockState state) {
		if (!worldIn.isRemote) {
			IBlockState iblockstate = worldIn.getBlockState(pos.north());
			IBlockState iblockstate1 = worldIn.getBlockState(pos.south());
			IBlockState iblockstate2 = worldIn.getBlockState(pos.west());
			IBlockState iblockstate3 = worldIn.getBlockState(pos.east());
			EnumFacing enumfacing = state.getValue(FACING);

			if (enumfacing == EnumFacing.NORTH && iblockstate.isFullBlock() && !iblockstate1.isFullBlock()) {
				enumfacing = EnumFacing.SOUTH;
			}
			else if (enumfacing == EnumFacing.SOUTH && iblockstate1.isFullBlock() && !iblockstate.isFullBlock()) {
				enumfacing = EnumFacing.NORTH;
			}
			else if (enumfacing == EnumFacing.WEST && iblockstate2.isFullBlock() && !iblockstate3.isFullBlock()) {
				enumfacing = EnumFacing.EAST;
			}
			else if (enumfacing == EnumFacing.EAST && iblockstate3.isFullBlock() && !iblockstate2.isFullBlock()) {
				enumfacing = EnumFacing.WEST;
			}

			worldIn.setBlockState(pos, state.withProperty(FACING, enumfacing).withProperty(ACTIVE, Boolean.valueOf(getTE(worldIn, pos).isRunning())), 2);
		}
	}

	@Override
	public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		boolean running = getTE(world, pos) == null ? false : getTE(world, pos).isInvalid();
		return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite()).withProperty(ACTIVE, Boolean.valueOf(running));
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		//worldIn.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()).withProperty(ACTIVE, Boolean.valueOf(false)), 3);
		world.notifyBlockUpdate(pos, state, state, 2);
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess blockAccessor, BlockPos pos) {
		TileEntity te;
		if (blockAccessor instanceof ChunkCache) {
			te = ((ChunkCache) blockAccessor).func_190300_a(pos, Chunk.EnumCreateEntityType.CHECK);

		}
		else {
			te = blockAccessor.getTileEntity(pos);
		}
		EnumFacing facing = state.getValue(FACING);
		boolean isActive = te == null ? Boolean.valueOf(false) : ((TilePoweredTempSpread) te).isRunning();
		return state.withProperty(FACING, facing).withProperty(ACTIVE, isActive);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing facing = EnumFacing.getHorizontal(meta);
		return getDefaultState().withProperty(FACING, facing);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getHorizontalIndex();
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return state.withProperty(FACING, rot.rotate(state.getValue(FACING))).withProperty(ACTIVE, state.getValue(ACTIVE));
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
		return state.withRotation(mirrorIn.toRotation(state.getValue(FACING))).withProperty(ACTIVE, state.getValue(ACTIVE));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {
				FACING, ACTIVE
		});
	}

}
