package p455w0rd.tanaddons.api;

import ic2.api.item.ElectricItem;
import ic2.api.item.IBackupElectricItemManager;
import ic2.api.item.IElectricItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.CapabilityEnergy;
import p455w0rd.tanaddons.init.ModIntegration.Mods;
import p455w0rd.tanaddons.items.ItemForgeEnergy;

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
		if (!handles(stack) || getTier(stack) > tier) {
			return 0.0D;
		}
		final double limit = getTransferLimit(stack);
		double toAdd = amount;
		if (!ignoreTransferLimit && amount > limit) {
			toAdd = limit;
		}
		return convertToEU(getEnergyItem(stack).receiveEnergy(stack, convertToRF(toAdd), simulate));
	}

	@Override
	public double discharge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit, boolean externally, boolean simulate) {
		return 0;
	}

	@Override
	public double getCharge(ItemStack stack) {
		if (handles(stack)) {
			return (int) convertToEU(getEnergyItem(stack).getEnergyStored(stack));
		}
		return 0;
	}

	@Override
	public double getMaxCharge(ItemStack stack) {
		if (handles(stack)) {
			return convertToEU(getEnergyItem(stack).getMaxEnergyStored(stack));
		}
		return 0;
	}

	@Override
	public boolean canUse(ItemStack stack, double amount) {
		return getCharge(stack) > amount;
	}

	@Override
	public boolean use(ItemStack stack, double amount, EntityLivingBase entity) {
		if (handles(stack) && canUse(stack, amount)) {
			final double toUse = convertToRF(amount);
			getEnergyItem(stack).extractEnergy(stack, (int) toUse, false);
			return true;
		}
		return false;
	}

	@Override
	public void chargeFromArmor(ItemStack stack, EntityLivingBase entity) {
		if (entity instanceof EntityPlayer && Mods.IC2.isLoaded()) {
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
		return !stack.isEmpty() && stack.hasCapability(CapabilityEnergy.ENERGY, null);
	}

	public ItemForgeEnergy getEnergyItem(ItemStack stack) {
		return handles(stack) && (stack.getItem() instanceof ItemForgeEnergy) ? (ItemForgeEnergy) stack.getItem() : null;
	}

	private double getTransferLimit(ItemStack itemStack) {
		return Math.max(32, getMaxCharge(itemStack) / 200);
	}

}
