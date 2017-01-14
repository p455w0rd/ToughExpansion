package p455w0rd.tanaddons.tiles;

import cofh.api.energy.IEnergyReceiver;
import cofh.api.energy.IEnergyStorage;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import p455w0rd.tanaddons.init.ModConfig.Options;
import toughasnails.tileentity.TileEntityTemperatureSpread;

/**
 * @author p455w0rd
 *
 */
public class TilePoweredTempSpread extends TileEntityTemperatureSpread implements IEnergyStorage, IEnergyReceiver {

	private int CAPACITY = 16000000;
	private int INPUT = 1000;
	private int ENERGY_USE = 40;
	private int ENERGY = 0;
	private final String TAG_ENERGY = "Energy";
	private int REDSTONE_MODE = 0; //0 = requires redstone, 1 = 0 redstone signal required, 2 = redstone ignored
	private final String TAG_MODE = "RSMode";

	public TilePoweredTempSpread() {
		super();
	}

	public TilePoweredTempSpread(int spread) {
		super(spread);
	}

	public int getMode() {
		return REDSTONE_MODE;
	}

	public int getEnergyUse() {
		return ENERGY_USE;
	}

	public boolean isRunning() {
		switch (REDSTONE_MODE) {
		case 0:
		default:
			return (worldObj.isBlockIndirectlyGettingPowered(pos) > 0 || worldObj.isBlockPowered(pos)) && getEnergyStored() > ENERGY_USE;
		case 1:
			return (worldObj.isBlockIndirectlyGettingPowered(pos) == 0 && !worldObj.isBlockPowered(pos)) && getEnergyStored() > ENERGY_USE;
		case 2:
			return getEnergyStored() > ENERGY_USE;
		}
	}

	public void nextMode() {
		int newMode = REDSTONE_MODE + 1;
		if (newMode > 2) {
			newMode = 0;
		}
		REDSTONE_MODE = newMode;
		markDirty();
	}

	@Override
	public void markDirty() {
		super.markDirty();
		if (getWorld() != null) {
			IBlockState state = getWorld().getBlockState(pos);
			if (state != null) {
				getWorld().notifyBlockUpdate(pos, state, state, 3);
			}
		}
	}

	@Override
	public int getEnergyStored(EnumFacing paramEnumFacing) {
		return ENERGY;
	}

	@Override
	public int getMaxEnergyStored(EnumFacing paramEnumFacing) {
		return CAPACITY;
	}

	private void setEnergyStored(int amount) {
		if (amount > Options.TEMP_REGULATOR_RF_CAPACITY) {
			ENERGY = Options.TEMP_REGULATOR_RF_CAPACITY;
			return;
		}
		if (amount < 0) {
			ENERGY = 0;
			return;
		}
		ENERGY = amount;
	}

	@Override
	public boolean canConnectEnergy(EnumFacing paramEnumFacing) {
		return true;
	}

	@Override
	public int receiveEnergy(EnumFacing paramEnumFacing, int input, boolean simulate) {
		int energyReceived = Math.min(CAPACITY - ENERGY, Math.min(INPUT, input));
		if (!simulate) {
			ENERGY += energyReceived;
		}
		return energyReceived;
	}

	@Override
	public int receiveEnergy(int input, boolean simulate) {
		return receiveEnergy(EnumFacing.DOWN, input, simulate);
	}

	@Override
	public int extractEnergy(int paramInt, boolean simulate) {
		return 0;
	}

	@Override
	public int getEnergyStored() {
		return ENERGY;
	}

	@Override
	public int getMaxEnergyStored() {
		return CAPACITY;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (compound.hasKey(TAG_ENERGY)) {
			ENERGY = compound.getInteger(TAG_ENERGY);
		}
		if (compound.hasKey(TAG_MODE)) {
			REDSTONE_MODE = compound.getInteger(TAG_MODE);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger(TAG_ENERGY, ENERGY);
		compound.setInteger(TAG_MODE, REDSTONE_MODE);
		return compound;
	}

	@Override
	public void update() {
		IBlockState state = getWorld().getBlockState(pos);
		state.getBlock().updateTick(getWorld(), getPos(), state, getWorld().rand);
		if (!isRunning()) {
			return;
		}
		setEnergyStored(getEnergyStored() - ENERGY_USE);
		super.update();
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
		return oldState.getBlock() != newSate.getBlock();
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound nbt = super.getUpdateTag();
		return writeToNBT(nbt);
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
	}

}
