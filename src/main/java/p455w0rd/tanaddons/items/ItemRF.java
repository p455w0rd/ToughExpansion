package p455w0rd.tanaddons.items;

import cofh.redstoneflux.api.IEnergyContainerItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;

/**
 * @author p455w0rd
 *
 */
public class ItemRF extends ItemBase implements IEnergyContainerItem {

	protected int capacity;
	protected int maxReceive;
	protected int maxExtract;

	public ItemRF(int capacity, String name) {
		this(capacity, capacity, capacity, name);
	}

	public ItemRF(int capacity, int maxTransfer, String name) {
		this(capacity, maxTransfer, maxTransfer, name);
	}

	public ItemRF(int capacity, int maxReceive, int maxExtract, String name) {
		super(name);
		this.capacity = capacity;
		this.maxReceive = maxReceive;
		this.maxExtract = maxExtract;
		canRepair = false;
		setMaxStackSize(1);
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return true;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		double max = getMaxEnergyStored(stack);
		return (max - getEnergyStored(stack)) / max;
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
		if (isInCreativeTab(tab)) {
			subItems.add(new ItemStack(this));
			ItemStack item = new ItemStack(this);
			setFull(item);
			subItems.add(item);
		}
	}

	public ItemRF setCapacity(int capacity) {
		this.capacity = capacity;
		return this;
	}

	public ItemRF setMaxTransfer(int maxTransfer) {
		setMaxReceive(maxTransfer);
		setMaxExtract(maxTransfer);
		return this;
	}

	public ItemRF setMaxReceive(int maxReceive) {
		this.maxReceive = maxReceive;
		return this;
	}

	public ItemRF setMaxExtract(int maxExtract) {
		this.maxExtract = maxExtract;
		return this;
	}

	@Override
	public int receiveEnergy(ItemStack container, int maxReceive, boolean simulate) {
		if (!container.hasTagCompound()) {
			container.setTagCompound(new NBTTagCompound());
		}
		int energy = container.getTagCompound().getInteger("Energy");
		int energyReceived = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));
		if (!simulate) {
			energy += energyReceived;
			container.getTagCompound().setInteger("Energy", energy);
		}
		return energyReceived;
	}

	@Override
	public int extractEnergy(ItemStack container, int maxExtract, boolean simulate) {
		if ((container.getTagCompound() == null) || (!container.getTagCompound().hasKey("Energy"))) {
			return 0;
		}
		int energy = container.getTagCompound().getInteger("Energy");
		int energyExtracted = Math.min(energy, Math.min(this.maxExtract, maxExtract));
		if (!simulate) {
			energy -= energyExtracted;
			container.getTagCompound().setInteger("Energy", energy);
		}
		return energyExtracted;
	}

	@Override
	public int getEnergyStored(ItemStack container) {
		if ((container.getTagCompound() == null) || (!container.getTagCompound().hasKey("Energy"))) {
			return 0;
		}
		return container.getTagCompound().getInteger("Energy");
	}

	@Override
	public int getMaxEnergyStored(ItemStack container) {
		return capacity;
	}

	public void setEnergyStored(ItemStack container, int energy) {
		if (container.getTagCompound() == null) {
			container.setTagCompound(new NBTTagCompound());
		}
		container.getTagCompound().setInteger("Energy", energy);
	}

	public ItemStack setFull(ItemStack container) {
		setEnergyStored(container, capacity);
		return container;
	}

	public int getMaxExtract(ItemStack item) {
		return maxExtract;
	}

}