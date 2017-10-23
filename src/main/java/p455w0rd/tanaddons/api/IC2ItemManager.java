package p455w0rd.tanaddons.api;

import cofh.redstoneflux.api.IEnergyContainerItem;
import ic2.api.item.ElectricItem;
import ic2.api.item.IBackupElectricItemManager;
import ic2.api.item.IElectricItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;

/**
 * @author p455w0rd
 *
 */
public class IC2ItemManager implements IBackupElectricItemManager {

	private static IC2ItemManager INSTANCE;

	public static IC2ItemManager getInstance() {
		return INSTANCE == null ? new IC2ItemManager() : INSTANCE;
	}

	private double convertToEU(int amount) {
		return (double) amount * 4;
	}

	private int convertToRF(double amount) {
		return (int) (amount / 4);
	}

	@Override
	public double charge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit, boolean simulate) {
		if (getTier(stack) > tier) {
			return 0.0D;
		}
		final double limit = getTransferLimit(stack);
		final IEnergyContainerItem poweredItem = (IEnergyContainerItem) stack.getItem();
		double toAdd = amount;
		if (!ignoreTransferLimit && amount > limit) {
			toAdd = limit;
		}
		return convertToEU(poweredItem.receiveEnergy(stack, convertToRF(toAdd), simulate));
	}

	@Override
	public double discharge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit, boolean externally, boolean simulate) {
		return 0;
	}

	@Override
	public double getCharge(ItemStack stack) {
		final IEnergyContainerItem poweredItem = (IEnergyContainerItem) stack.getItem();
		return (int) convertToEU(poweredItem.getEnergyStored(stack));
	}

	@Override
	public double getMaxCharge(ItemStack stack) {
		final IEnergyContainerItem poweredItem = (IEnergyContainerItem) stack.getItem();
		return convertToEU(poweredItem.getMaxEnergyStored(stack));
	}

	@Override
	public boolean canUse(ItemStack stack, double amount) {
		return getCharge(stack) > amount;
	}

	@Override
	public boolean use(ItemStack stack, double amount, EntityLivingBase entity) {
		final IEnergyContainerItem poweredItem = (IEnergyContainerItem) stack.getItem();

		if (canUse(stack, amount)) {
			final double toUse = convertToRF(amount);
			poweredItem.extractEnergy(stack, (int) toUse, false);
			return true;
		}
		return false;
	}

	@Override
	public void chargeFromArmor(ItemStack stack, EntityLivingBase entity) {
		if (entity instanceof EntityPlayer && Loader.isModLoaded("ic2")) {
			EntityPlayer player = (EntityPlayer) entity;
			ItemStack chestStack = player.inventory.armorItemInSlot(2);
			if (chestStack != null) {
				Item chestItem = chestStack.getItem();
				if (chestItem instanceof IElectricItem && ((IElectricItem) chestItem).canProvideEnergy(stack)) {
					charge(stack, 40, 4, true, false);
					if (!player.capabilities.isCreativeMode) {
						ElectricItem.manager.discharge(chestStack, 40, 4, true, false, false);
					}
				}
			}
		}
	}

	@Override
	public String getToolTip(ItemStack stack) {
		return null;
	}

	@Override
	public int getTier(ItemStack stack) {
		return 1;
	}

	@Override
	public boolean handles(ItemStack stack) {
		return !stack.isEmpty() && (stack.getItem() instanceof IEnergyContainerItem);
	}

	private double getTransferLimit(ItemStack itemStack) {
		return Math.max(32, getMaxCharge(itemStack) / 200);
	}

}
