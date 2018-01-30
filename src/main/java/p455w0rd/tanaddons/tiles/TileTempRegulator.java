package p455w0rd.tanaddons.tiles;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import p455w0rd.tanaddons.init.ModConfig.Options;
import toughasnails.api.TANPotions;
import toughasnails.api.stat.capability.ITemperature;
import toughasnails.api.temperature.Temperature;
import toughasnails.api.temperature.TemperatureHelper;
import toughasnails.temperature.TemperatureHandler;

/**
 * @author p455w0rd
 *
 */
public class TileTempRegulator extends TileEntity implements ITickable {

	private int INPUT = 10000;
	private final int ENERGY_USE;
	private int ENERGY = 0;
	public static final String TAG_ENERGY = "Energy";
	private int REDSTONE_MODE = 0; //0 = requires redstone, 1 = requires lack of signal, 2 = redstone ignored
	public static final String TAG_MODE = "RSMode";
	private final String TAG_TIMERLIST = "TimerList";
	private final String TAG_TIMERLISTENTRY_PLAYERID = "PlayerID";
	private final String TAG_TIMERLISTENTRY_TIME = "Time";
	private Map<EntityPlayer, Integer> PLAYER_TIMERS = Maps.newHashMap();

	public TileTempRegulator() {
		ENERGY_USE = Options.TEMP_REGULATOR_RF_PER_TICK;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		return capability == CapabilityEnergy.ENERGY;
	}

	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		return hasCapability(capability, facing) ? CapabilityEnergy.ENERGY.cast(new IEnergyStorage() {

			@Override
			public int receiveEnergy(int input, boolean simulate) {
				int energyReceived = Math.min(Options.TEMP_REGULATOR_RF_CAPACITY - ENERGY, Math.min(INPUT, input));
				if (!simulate) {
					ENERGY += energyReceived;
				}
				return energyReceived;
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
				return Options.TEMP_REGULATOR_RF_CAPACITY;
			}

			@Override
			public boolean canExtract() {
				return false;
			}

			@Override
			public boolean canReceive() {
				return true;
			}

		}) : null;

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

	public int getMode() {
		return REDSTONE_MODE;
	}

	private IEnergyStorage getEnergyCap() {
		return getCapability(CapabilityEnergy.ENERGY, null);
	}

	public int getEnergyStored() {
		return getEnergyCap().getEnergyStored();
	}

	public int getEnergyUse() {
		return ENERGY_USE;
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

	public boolean isRunning() {
		switch (REDSTONE_MODE) {
		case 0:
		default:
			return (world.isBlockIndirectlyGettingPowered(pos) > 0 || world.isBlockPowered(pos)) && getEnergyStored() > ENERGY_USE;
		case 1:
			return (world.isBlockIndirectlyGettingPowered(pos) == 0 && !world.isBlockPowered(pos)) && getEnergyStored() > ENERGY_USE;
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
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (compound.hasKey(TAG_ENERGY)) {
			ENERGY = compound.getInteger(TAG_ENERGY);
		}
		if (compound.hasKey(TAG_MODE)) {
			REDSTONE_MODE = compound.getInteger(TAG_MODE);
		}
		if (compound.hasKey(TAG_TIMERLIST) && compound.getTagList(TAG_TIMERLIST, 10).tagCount() > 0) {
			NBTTagList tagList = compound.getTagList(TAG_TIMERLIST, 10);
			Map<EntityPlayer, Integer> newList = Maps.newHashMap();
			for (int i = 0; i < tagList.tagCount(); i++) {
				NBTTagCompound entry = tagList.getCompoundTagAt(i);
				if (entry == null || getWorld() == null || entry.getString(TAG_TIMERLISTENTRY_PLAYERID) == null) {
					continue;
				}
				EntityPlayer player = getWorld().getPlayerEntityByUUID(UUID.fromString(entry.getString(TAG_TIMERLISTENTRY_PLAYERID)));
				newList.put(player, entry.getInteger(TAG_TIMERLISTENTRY_TIME));
			}
			PLAYER_TIMERS = newList;
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger(TAG_ENERGY, ENERGY);
		compound.setInteger(TAG_MODE, REDSTONE_MODE);
		if (PLAYER_TIMERS.keySet().size() > 0) {
			NBTTagList tagList = new NBTTagList();
			for (EntityPlayer player : PLAYER_TIMERS.keySet()) {
				NBTTagCompound timerListNBTEntry = new NBTTagCompound();
				timerListNBTEntry.setString(TAG_TIMERLISTENTRY_PLAYERID, player.getUniqueID().toString());
				timerListNBTEntry.setInteger(TAG_TIMERLISTENTRY_TIME, PLAYER_TIMERS.get(player));
				tagList.appendTag(timerListNBTEntry);
			}
			compound.setTag(TAG_TIMERLIST, tagList);
		}
		return compound;
	}

	@Override
	public void update() {
		if (getWorld() != null) {
			IBlockState state = getWorld().getBlockState(pos);
			state.getBlock().updateTick(getWorld(), getPos(), state, getWorld().rand);
		}
		if (!isRunning() || getWorld() == null || getEnergyStored() < ENERGY_USE) {
			return;
		}

		BlockPos negPos = new BlockPos(getPos().getX() - Options.TEMP_REGULATOR_RADIUS, 0, getPos().getZ() - Options.TEMP_REGULATOR_RADIUS);
		BlockPos posPos = new BlockPos(getPos().getX() + Options.TEMP_REGULATOR_RADIUS, 255, getPos().getZ() + Options.TEMP_REGULATOR_RADIUS);
		List<EntityPlayer> playerList = getWorld().getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(negPos, posPos));
		if (playerList.size() > 0) {
			for (EntityPlayer player : playerList) {
				TemperatureHandler tempHandler = (TemperatureHandler) TemperatureHelper.getTemperatureData(player);
				ITemperature data = TemperatureHelper.getTemperatureData(player);
				Temperature playerTemp = data.getTemperature();
				int currentTemp = playerTemp.getRawValue();
				int currentTime = getTime(player);
				if (currentTemp != 14) {
					if (getTime(player) <= 0) {
						player.removePotionEffect(TANPotions.hypothermia);
						player.removePotionEffect(TANPotions.hyperthermia);
						if (currentTemp < 14) {
							tempHandler.setTemperature(new Temperature(currentTemp + 1));
						}
						else if (currentTemp > 14) {
							tempHandler.setTemperature(new Temperature(currentTemp - 1));
						}
						setTime(player, 100);
						setEnergyStored(getEnergyStored() - ENERGY_USE);
					}
					else {
						setTime(player, currentTime - 1);
						setEnergyStored(getEnergyStored() - ENERGY_USE);
					}
				}
				else {
					removePlayerTimer(player);
				}
			}
		}

	}

	private int getTime(EntityPlayer player) {
		if (PLAYER_TIMERS.containsKey(player)) {
			return PLAYER_TIMERS.get(player);
		}
		return -1;
	}

	private void removePlayerTimer(EntityPlayer player) {
		if (PLAYER_TIMERS.containsKey(player)) {
			PLAYER_TIMERS.remove(player);
		}
	}

	private void setTime(EntityPlayer player, int time) {
		if (!PLAYER_TIMERS.containsKey(player)) {
			PLAYER_TIMERS.put(player, time);
		}
		else {
			int currentTimeCached = PLAYER_TIMERS.get(player);
			PLAYER_TIMERS.replace(player, currentTimeCached, time);
		}
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
	@Nullable
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
	}

}
