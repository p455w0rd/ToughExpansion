package p455w0rd.tanaddons.items;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import p455w0rd.tanaddons.init.ModConfig.Options;

/**
 * @author p455w0rd
 *
 */
public class ItemForgeEnergy extends ItemBase {

	protected int capacity;
	protected int maxReceive;
	protected int maxExtract;

	public ItemForgeEnergy(int capacity, String name) {
		this(capacity, capacity, capacity, name);
	}

	public ItemForgeEnergy(int capacity, int maxTransfer, String name) {
		this(capacity, maxTransfer, maxTransfer, name);
	}

	public ItemForgeEnergy(int capacity, int maxReceive, int maxExtract, String name) {
		super(name);
		this.capacity = capacity;
		this.maxReceive = maxReceive;
		this.maxExtract = maxExtract;
		canRepair = false;
		setMaxStackSize(1);
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
		if (isInCreativeTab(tab)) {
			subItems.add(new ItemStack(this));
			if (Options.REQUIRE_ENERGY) {
				ItemStack item = new ItemStack(this);
				setFullEnergy(item);
				subItems.add(item);
			}
		}
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return Options.REQUIRE_ENERGY;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		if (!Options.REQUIRE_ENERGY) {
			return 0;
		}
		double max = getMaxEnergyStored(stack);
		return (max - getEnergyStored(stack)) / max;
	}

	@Override
	public ICapabilityProvider initCapabilities(@Nonnull final ItemStack stack, @Nullable NBTTagCompound nbt) {
		return getEnergyProvider(stack);
	}

	public static ICapabilityProvider getEnergyProvider(ItemStack stack) {
		return stack.getItem() instanceof ItemForgeEnergy ? ((ItemForgeEnergy) stack.getItem()).getForgeEnergyStorage(stack) : null;
	}

	public EnergyStorageSettable getForgeEnergyStorage(ItemStack stack) {
		if (Options.REQUIRE_ENERGY && stack.getItem() instanceof ItemForgeEnergy) {
			ItemForgeEnergy item = (ItemForgeEnergy) stack.getItem();
			return new EnergyStorageSettable(stack, item.getCapacity(), item.getMaxReceive(), item.getMaxExtract());
		}
		return null;
	}

	public int getCapacity() {
		return capacity;
	}

	public int getMaxReceive() {
		return maxReceive;
	}

	public int getMaxExtract() {
		return maxExtract;
	}

	public void setEnergyStored(ItemStack stack, int amount) {
		if (Options.REQUIRE_ENERGY) {
			getForgeEnergyStorage(stack).setEnergyStored(amount);
		}
	}

	public int getEnergyStored(ItemStack stack) {
		if (!Options.REQUIRE_ENERGY) {
			return 0;
		}
		return getForgeEnergyStorage(stack).getEnergyStored();
	}

	public int getMaxEnergyStored(ItemStack stack) {
		if (!Options.REQUIRE_ENERGY) {
			return 0;
		}
		return getForgeEnergyStorage(stack).getMaxEnergyStored();
	}

	public int extractEnergy(ItemStack stack, int amount, boolean simulate) {
		if (!Options.REQUIRE_ENERGY) {
			return 0;
		}
		return getForgeEnergyStorage(stack).extractEnergy(amount, simulate);
	}

	public int receiveEnergy(ItemStack stack, int amount, boolean simulate) {
		if (!Options.REQUIRE_ENERGY) {
			return 0;
		}
		return getForgeEnergyStorage(stack).receiveEnergy(amount, simulate);
	}

	public void setFullEnergy(ItemStack stack) {
		if (Options.REQUIRE_ENERGY) {
			setEnergyStored(stack, getMaxEnergyStored(stack));
		}
	}

	public static class EnergyStorageSettable implements IEnergyStorage, ICapabilityProvider {

		ItemStack stack = ItemStack.EMPTY;
		protected int capacity;
		protected int maxReceive;
		protected int maxExtract;
		private static final String TAG_ENERGY = "ForgeEnergy";

		public EnergyStorageSettable(@Nonnull ItemStack stack, int capacity, int maxReceive, int maxExtract) {
			this.stack = stack;
			this.capacity = capacity;
			this.maxReceive = maxReceive;
			this.maxExtract = maxExtract;
		}

		public Item setEnergyStored(int amount) {
			if (amount > capacity) {
				amount = capacity;
			}
			else if (amount < 0) {
				amount = 0;
			}
			if (!stack.hasTagCompound()) {
				stack.setTagCompound(new NBTTagCompound());
			}
			stack.getTagCompound().setInteger(TAG_ENERGY, amount);
			return stack.getItem();
		}

		@Override
		public int getEnergyStored() {
			if (!stack.hasTagCompound()) {
				stack.setTagCompound(new NBTTagCompound());
			}
			if (!stack.getTagCompound().hasKey(TAG_ENERGY, Constants.NBT.TAG_INT)) {
				stack.getTagCompound().setInteger(TAG_ENERGY, 0);
			}
			return stack.getTagCompound().getInteger(TAG_ENERGY);
		}

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			if (!canReceive()) {
				return 0;
			}
			int energyReceived = Math.min(capacity - getEnergyStored(), Math.min(this.maxReceive, maxReceive));
			if (!simulate) {
				setEnergyStored(getEnergyStored() + energyReceived);
			}
			return energyReceived;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			if (!canExtract()) {
				return 0;
			}
			int energyExtracted = Math.min(getEnergyStored(), Math.min(this.maxExtract, maxExtract));
			if (!simulate) {
				setEnergyStored(getEnergyStored() - energyExtracted);
			}
			return energyExtracted;
		}

		@Override
		public int getMaxEnergyStored() {
			return capacity;
		}

		@Override
		public boolean canExtract() {
			return maxExtract > 0;
		}

		@Override
		public boolean canReceive() {
			return maxReceive > 0;
		}

		@Override
		public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
			return capability == CapabilityEnergy.ENERGY;
		}

		@Nullable
		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			if (capability == CapabilityEnergy.ENERGY) {
				if (stack.getItem() instanceof ItemForgeEnergy) {
					EnergyStorageSettable storage = ((ItemForgeEnergy) stack.getItem()).getForgeEnergyStorage(stack);
					return CapabilityEnergy.ENERGY.cast(storage);
				}
			}
			return null;
		}

	}

}
