package p455w0rd.tanaddons.items;

import static p455w0rd.tanaddons.init.ModGlobals.MODID_BAUBLES;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.realmsclient.gui.ChatFormatting;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.tanaddons.init.ModConfig.Options;
import p455w0rd.tanaddons.init.ModCreativeTab;
import p455w0rd.tanaddons.init.ModGlobals;
import p455w0rdslib.util.ReadableNumberConverter;
import toughasnails.api.TANPotions;
import toughasnails.api.config.SyncedConfig;
import toughasnails.api.config.TemperatureOption;
import toughasnails.api.stat.capability.ITemperature;
import toughasnails.api.temperature.Temperature;
import toughasnails.api.temperature.TemperatureHelper;
import toughasnails.temperature.TemperatureHandler;

/**
 * @author p455w0rd
 *
 */
@Interface(iface = "baubles.api.IBauble", modid = MODID_BAUBLES, striprefs = true)
public class ItemTempRegulator extends ItemForgeEnergy implements IBauble {

	private static final String NAME = "portable_temp_regulator";
	private final String TAG_TIME = "TimeStart";

	public ItemTempRegulator() {
		super(Options.PORTABLE_TEMP_REGULATOR_CAPACITY, Options.PORTABLE_TEMP_REGULATOR_CAPACITY, 40, NAME);
		setCreativeTab(ModCreativeTab.TAB);
	}

	private void init(ItemStack stack) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		NBTTagCompound nbt = stack.getTagCompound();
		if (!nbt.hasKey(TAG_TIME)) {
			nbt.setInteger(TAG_TIME, 100);
		}
	}

	private void doTick(Entity entity, ItemStack stack) {
		init(stack);
		int energy = Options.PORTABLE_TEMP_REGULATOR_RF_PER_TICK;
		if (entity instanceof EntityPlayer && SyncedConfig.getBooleanValue(TemperatureOption.ENABLE_TEMPERATURE)) {
			if (getEnergyStored(stack) < energy) {
				return;
			}

			EntityPlayer player = (EntityPlayer) entity;
			TemperatureHandler tempHandler = (TemperatureHandler) TemperatureHelper.getTemperatureData(player);
			ITemperature data = TemperatureHelper.getTemperatureData(player);
			Temperature playerTemp = data.getTemperature();
			int currentTemp = playerTemp.getRawValue();
			int currentTime = getTime(stack);
			if (currentTemp != 14) {
				if (currentTime <= 0) {
					tempHandler.setChangeTime(1);
					player.removePotionEffect(TANPotions.hypothermia);
					player.removePotionEffect(TANPotions.hyperthermia);
					if (currentTemp < 14) {
						tempHandler.setTemperature(new Temperature(currentTemp + 1));
					}
					else if (currentTemp > 14) {
						tempHandler.setTemperature(new Temperature(currentTemp - 1));
					}
					setTime(stack, 100);
					setEnergyStored(stack, getEnergyStored(stack) - energy);
				}
				else {
					setTime(stack, currentTime - 1);
					setEnergyStored(stack, getEnergyStored(stack) - energy);
				}
			}
			else {
				if (getTime(stack) != 100) {
					setTime(stack, 100);
				}
			}
		}
	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		return getTime(stack) < 100 && getEnergyStored(stack) > Options.PORTABLE_TEMP_REGULATOR_RF_PER_TICK;
	}

	private int getTime(ItemStack stack) {
		init(stack);
		return stack.getTagCompound().getInteger(TAG_TIME);
	}

	private void setTime(ItemStack stack, int amount) {
		init(stack);
		stack.getTagCompound().setInteger(TAG_TIME, amount);
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		if (oldStack.getItem() == newStack.getItem()) {
			return false;
		}
		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag advanced) {
		tooltip.add(ChatFormatting.ITALIC + "" + ReadableNumberConverter.INSTANCE.toWideReadableForm(getEnergyStored(stack)) + "/" + ReadableNumberConverter.INSTANCE.toWideReadableForm(getMaxEnergyStored(stack)) + " RF");
		tooltip.add("");
		tooltip.add(I18n.format("tooltip.tanaddons.ptempregulator.desc"));
		tooltip.add(I18n.format("tooltip.tanaddons.ptempregulator.desc2"));
		if (Loader.isModLoaded(ModGlobals.MODID_BAUBLES)) {
			tooltip.add(I18n.format("tooltip.tanaddons.baublesitem", "any"));
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		if (!worldIn.isRemote) {
			doTick(entityIn, stack);
		}
	}

	@Override
	@Method(modid = MODID_BAUBLES)
	public BaubleType getBaubleType(ItemStack stack) {
		return BaubleType.TRINKET;
	}

	@Override
	@Method(modid = MODID_BAUBLES)
	public void onWornTick(ItemStack stack, EntityLivingBase player) {
		if (!player.getEntityWorld().isRemote) {
			doTick(player, stack);
		}
	}

	@Override
	@Method(modid = MODID_BAUBLES)
	public boolean willAutoSync(ItemStack itemstack, EntityLivingBase player) {
		return true;
	}

}
