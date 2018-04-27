package p455w0rd.tanaddons.items;

import static p455w0rd.tanaddons.init.ModGlobals.MODID_BAUBLES;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.realmsclient.gui.ChatFormatting;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
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
import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;
import toughasnails.api.thirst.ThirstHelper;
import toughasnails.fluids.PurifiedWaterFluid;
import toughasnails.thirst.ThirstHandler;

/**
 * @author p455w0rd
 *
 */
@Interface(iface = "baubles.api.IBauble", modid = MODID_BAUBLES, striprefs = true)
public class ItemThirstQuencher extends ItemForgeEnergy implements IBauble {

	private static final String NAME = "thirst_quencher";
	private final int FLUID_CAPACITY;
	protected int FLUID_STORED = 0;
	public static final String TAG_FLUID_STORED = "FluidStored";
	public static final String TAG_TIME = "TimeStart";

	public ItemThirstQuencher() {
		super(Options.THIRST_QUENCHER_RF_CAPACITY, Options.THIRST_QUENCHER_RF_CAPACITY, 40, NAME);
		setCreativeTab(ModCreativeTab.TAB);
		FLUID_CAPACITY = (Options.THIRST_QUENCHER_WATER_CAPACITY < 8 ? 8 : Options.THIRST_QUENCHER_WATER_CAPACITY) * 1000;
		addPropertyOverride(new ResourceLocation("filllevel"), (stack, world, entity) -> (getFluidStored(stack) / 1000) / 2);
	}

	@Override
	public ICapabilityProvider initCapabilities(@Nonnull final ItemStack stack, @Nullable NBTTagCompound nbt) {
		return new ICapabilityProvider() {

			@Override
			public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
				return capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY || (Options.REQUIRE_ENERGY && capability == CapabilityEnergy.ENERGY);
			}

			@Override
			public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
				return capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY ? CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY.cast((FluidHandlerItemStackSettable) getFluidProvider(stack)) : (Options.REQUIRE_ENERGY && capability == CapabilityEnergy.ENERGY) ? CapabilityEnergy.ENERGY.cast(getForgeEnergyStorage(stack)) : null;
			}

		};
	}

	public FluidHandlerItemStackSettable getFluidStorage(ItemStack stack) {
		if (stack.getItem() instanceof ItemThirstQuencher) {
			return new FluidHandlerItemStackSettable(stack, FLUID_CAPACITY);
		}
		return null;
	}

	protected ICapabilityProvider getFluidProvider(ItemStack stack) {
		return stack.getItem() instanceof ItemThirstQuencher ? ((ItemThirstQuencher) stack.getItem()).getFluidStorage(stack) : null;
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
		if (isInCreativeTab(tab)) {
			ItemStack item = new ItemStack(this);
			ItemStack item2 = new ItemStack(this);
			ItemStack item3 = new ItemStack(this);
			setFluidStored(item2, FLUID_CAPACITY);
			subItems.add(new ItemStack(this)); // 0 RF - 0 Fluid
			if (Options.REQUIRE_ENERGY) {
				setFullEnergy(item);
				subItems.add(item); // full RF - 0 fluid
			}
			subItems.add(item2); // 0 RF - full fluid
			if (Options.REQUIRE_ENERGY) {
				setFluidStored(item3, FLUID_CAPACITY);
				setFullEnergy(item3);
				subItems.add(item3); // full RF - full fluid
			}
		}
	}

	public int getFluidStored(ItemStack stack) {
		return getFluidStorage(stack).getFluidStored();
	}

	private void setFluidStored(ItemStack stack, int amount) {
		getFluidStorage(stack).setFluidStored(amount);
	}

	private int getTime(ItemStack stack) {
		init(stack);
		return stack.getTagCompound().getInteger(TAG_TIME);
	}

	private void setTime(ItemStack stack, int amount) {
		init(stack);
		stack.getTagCompound().setLong(TAG_TIME, amount);
	}

	private void drainFluid(ItemStack stack, int amount) {
		int amountStored = getFluidStored(stack) - amount;
		if (amountStored < 0) {
			amountStored = 0;
		}
		setFluidStored(stack, amountStored);
	}

	private void init(ItemStack stack) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		NBTTagCompound nbt = stack.getTagCompound();
		if (!nbt.hasKey(TAG_FLUID_STORED)) {
			nbt.setInteger(TAG_FLUID_STORED, 0);
		}
		if (!nbt.hasKey(TAG_TIME)) {
			nbt.setInteger(TAG_TIME, 100);
		}
	}

	private void doTick(Entity entity, ItemStack stack) {
		if (entity instanceof EntityPlayer && SyncedConfig.getBooleanValue(GameplayOption.ENABLE_THIRST)) {
			int energyPerTick = Options.THIRST_QUNCHER_RF_PER_TICK;
			if (Options.REQUIRE_ENERGY && (getEnergyStored(stack) < energyPerTick || getFluidStored(stack) < 100)) {
				return;
			}
			EntityPlayer player = (EntityPlayer) entity;
			ThirstHandler thirstHandler = (ThirstHandler) ThirstHelper.getThirstData(player);
			int currentThirst = thirstHandler.getThirst();
			int currentTime = getTime(stack);
			if (currentThirst < 20) {
				if (currentTime <= 0) {
					player.removePotionEffect(TANPotions.thirst);
					if (currentThirst < 20) {
						thirstHandler.setThirst(currentThirst + 1);
						thirstHandler.setHydration(5.0F);
						thirstHandler.setExhaustion(0.0F);
					}
					drainFluid(stack, 100);
					setTime(stack, 50);
					if (Options.REQUIRE_ENERGY) {
						setEnergyStored(stack, getEnergyStored(stack) - energyPerTick);
					}
				}
				else {
					setTime(stack, currentTime - 1);
					if (Options.REQUIRE_ENERGY) {
						setEnergyStored(stack, getEnergyStored(stack) - energyPerTick);
					}
				}
			}
			else {
				if (getTime(stack) != 50) {
					setTime(stack, 50);
				}
			}
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {

		ItemStack stack = playerIn.getHeldItemMainhand();
		if (stack.isEmpty()) {
			return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
		}
		init(stack);
		if (getFluidStored(stack) >= FLUID_CAPACITY) {
			return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
		}
		RayTraceResult ray = rayTrace(worldIn, playerIn, true);
		if (ray == null || ray.typeOfHit == null || hand == null) {
			return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
		}
		if (ray.typeOfHit == RayTraceResult.Type.BLOCK && hand == EnumHand.MAIN_HAND && !worldIn.isRemote) {
			BlockPos blockpos = ray.getBlockPos();
			IBlockState iblockstate = worldIn.getBlockState(blockpos);
			Material material = iblockstate.getMaterial();

			if (material == Material.WATER && iblockstate.getValue(BlockLiquid.LEVEL).intValue() == 0) {
				worldIn.setBlockState(blockpos, Blocks.AIR.getDefaultState(), 11);
				playerIn.addStat(StatList.getObjectUseStats(this));
				playerIn.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
				setFluidStored(stack, Math.min(getFluidStored(stack) + 1000, FLUID_CAPACITY));
				return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
			}
			else if (iblockstate.getBlock().hasTileEntity(iblockstate) && worldIn.getTileEntity(blockpos).hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, ray.sideHit)) {
				IFluidHandler fluidHandler = worldIn.getTileEntity(blockpos).getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, ray.sideHit);
				for (IFluidTankProperties property : fluidHandler.getTankProperties()) {
					Fluid containedFluid = property.getContents().getFluid();
					if ((containedFluid == FluidRegistry.WATER || containedFluid == PurifiedWaterFluid.instance) && property.getContents().amount >= 1000 && property.canDrain()) {
						fluidHandler.drain(new FluidStack(FluidRegistry.WATER, 1000), true);
						setFluidStored(stack, Math.min(getFluidStored(stack) + 1000, FLUID_CAPACITY));
					}
				}
			}

		}
		return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		return getTime(stack) < 50 && (!Options.REQUIRE_ENERGY || getEnergyStored(stack) > 10);
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		if (oldStack.getItem() == newStack.getItem()) {
			return false;
		}
		return slotChanged;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag advanced) {
		if (Options.REQUIRE_ENERGY) {
			tooltip.add(ChatFormatting.ITALIC + "" + ReadableNumberConverter.INSTANCE.toWideReadableForm(getEnergyStored(stack)) + "/" + ReadableNumberConverter.INSTANCE.toWideReadableForm(getMaxEnergyStored(stack)) + " Energy");
		}
		tooltip.add("Stored Water: " + getFluidStored(stack) + "/" + FLUID_CAPACITY + " mB");
		tooltip.add("");
		tooltip.add(I18n.format("tooltip.tanaddons.thirstquencher.desc"));
		String requirementMessage = I18n.format("tooltip.tanaddons.thirstquencher.desc2");
		if (Options.REQUIRE_ENERGY) {
			requirementMessage += " " + I18n.format("tooltip.tanaddons.thirstquencher.desc4");
		}
		//requirementMessage += " " + I18n.format("tooltip.tanaddons.thirstquencher.desc4");
		tooltip.add(requirementMessage + " " + I18n.format("tooltip.tanaddons.thirstquencher.desc5"));
		//tooltip.add(I18n.format("tooltip.tanaddons.thirstquencher.desc3"));
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

	public static class FluidHandlerItemStackSettable extends FluidHandlerItemStack {

		ItemThirstQuencher item;

		public FluidHandlerItemStackSettable(ItemStack container, int capacity) {
			super(container, capacity);
			if (container.getItem() instanceof ItemThirstQuencher) {
				item = (ItemThirstQuencher) container.getItem();
			}
		}

		@Override
		public FluidStack getFluid() {
			return new FluidStack(FluidRegistry.WATER, item.getFluidStored(getContainer()));
		}

		@Override
		public int fill(FluidStack resource, boolean doFill) {
			if (getContainer().getCount() != 1 || resource == null || resource.amount <= 0 || !canFillFluidType(resource)) {
				return 0;
			}

			FluidStack contained = getFluid();
			if (contained == null) {
				int fillAmount = Math.min(item.FLUID_CAPACITY, resource.amount);

				FluidStack filled = resource.copy();
				if (doFill) {
					filled.amount = fillAmount;
					setFluid(filled);
				}
				setFluidStored(filled.amount);
				return fillAmount;
			}
			else {
				if (!contained.isFluidEqual(resource)) {
					resource = new FluidStack(contained, resource.amount);
				}
				int fillAmount = Math.min(item.FLUID_CAPACITY - contained.amount, resource.amount);

				if (doFill && fillAmount > 0) {
					contained.amount += fillAmount;
					setFluid(contained);
				}
				setFluidStored(contained.amount);
				return fillAmount;

			}
		}

		@Override
		public boolean canFillFluidType(FluidStack stack) {
			Fluid fluid = stack.getFluid();
			return fluid == FluidRegistry.WATER || fluid == PurifiedWaterFluid.instance;
		}

		public int getFluidStored() {
			init(getContainer());
			return getContainer().getTagCompound().getInteger(TAG_FLUID_STORED);
		}

		private void setFluidStored(int amount) {
			init(getContainer());
			getContainer().getTagCompound().setInteger(TAG_FLUID_STORED, amount);
		}

		private void init(ItemStack stack) {
			if (!stack.hasTagCompound()) {
				stack.setTagCompound(new NBTTagCompound());
			}
			NBTTagCompound nbt = stack.getTagCompound();
			if (!nbt.hasKey(ItemThirstQuencher.TAG_FLUID_STORED)) {
				nbt.setInteger(TAG_FLUID_STORED, 0);
			}
			if (!nbt.hasKey(TAG_TIME)) {
				nbt.setInteger(TAG_TIME, 100);
			}
		}

	}

}
