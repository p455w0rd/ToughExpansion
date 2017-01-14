package p455w0rd.tanaddons.items;

import static p455w0rd.tanaddons.init.ModGlobals.MODID_BAUBLES;

import java.util.List;

import org.lwjgl.input.Keyboard;

import com.mojang.realmsclient.gui.ChatFormatting;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
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
import toughasnails.api.stat.capability.ITemperature;
import toughasnails.api.temperature.Temperature;
import toughasnails.api.temperature.TemperatureHelper;
import toughasnails.config.GameplayOption;
import toughasnails.config.SyncedConfigHandler;
import toughasnails.temperature.TemperatureHandler;

/**
 * @author p455w0rd
 *
 */
@Interface(iface = "baubles.api.IBauble", modid = MODID_BAUBLES, striprefs = true)
public class ItemTempRegulator extends ItemRF implements IBauble {

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
			nbt.setLong(TAG_TIME, -1L);
		}
	}

	private void doTick(Entity entity, ItemStack stack) {
		init(stack);
		if (entity instanceof EntityPlayer && SyncedConfigHandler.getBooleanValue(GameplayOption.ENABLE_TEMPERATURE)) {
			if (getEnergyStored(stack) < 100) {
				return;
			}

			EntityPlayer player = (EntityPlayer) entity;
			TemperatureHandler tempHandler = (TemperatureHandler) TemperatureHelper.getTemperatureData(player);
			//float temp = (float) MathUtils.clamp(tempHandler.debugger.targetTemperature, 0, TemperatureScale.getScaleTotal()) / (float) TemperatureScale.getScaleTotal();
			ITemperature data = TemperatureHelper.getTemperatureData(player);
			Temperature playerTemp = data.getTemperature();
			int currentTemp = playerTemp.getRawValue();
			if (currentTemp != 14) {
				if (getTime(stack) != -1L) {
					long startTime = getTime(stack);
					if (ModGlobals.TIMER >= startTime + 1000L) {
						//tempHandler.setChangeTime(0);
						player.removePotionEffect(TANPotions.hypothermia);
						player.removePotionEffect(TANPotions.hyperthermia);
						if (currentTemp < 14) {
							tempHandler.setTemperature(new Temperature(currentTemp + 1));
						}
						else if (currentTemp > 14) {
							tempHandler.setTemperature(new Temperature(currentTemp - 1));
						}
						setTime(stack, -1L);
						setEnergyStored(stack, getEnergyStored(stack) - 100);
					}
					else {
						setEnergyStored(stack, getEnergyStored(stack) - 100);
					}
				}
				else {
					setTime(stack, ModGlobals.TIMER);
					setEnergyStored(stack, getEnergyStored(stack) - 100);
				}
			}
			else {
				setTime(stack, -1L);
			}
		}
	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		return getTime(stack) != -1L && getEnergyStored(stack) > 0;
	}

	private long getTime(ItemStack stack) {
		init(stack);
		return stack.getTagCompound().getLong(TAG_TIME);
	}

	private void setTime(ItemStack stack, long amount) {
		init(stack);
		stack.getTagCompound().setLong(TAG_TIME, amount);
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
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
		tooltip.add(ChatFormatting.ITALIC + "" + ReadableNumberConverter.INSTANCE.toWideReadableForm(getEnergyStored(stack)) + "/" + ReadableNumberConverter.INSTANCE.toWideReadableForm(getMaxEnergyStored(stack)) + " RF");
		KeyBinding sneak = Minecraft.getMinecraft().gameSettings.keyBindSneak;
		if (player.isSneaking() || Keyboard.isKeyDown(sneak.getKeyCode())) {
			tooltip.add("");
			tooltip.add(I18n.format("tooltip.tanaddons.ptempregulator.desc"));
			tooltip.add(I18n.format("tooltip.tanaddons.ptempregulator.desc2"));
			if (Loader.isModLoaded(ModGlobals.MODID_BAUBLES)) {
				tooltip.add(I18n.format("tooltip.tanaddons.baublesitem", "any"));
			}
		}
		else {
			tooltip.add(TextFormatting.ITALIC + "" + TextFormatting.AQUA + "" + I18n.format("tooltip.tanaddons.holdshift", sneak.getDisplayName()));
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
