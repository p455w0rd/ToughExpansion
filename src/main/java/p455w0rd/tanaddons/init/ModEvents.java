package p455w0rd.tanaddons.init;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.tanaddons.init.ModConfig.Options;
import p455w0rd.tanaddons.network.PacketConfigSync;
import toughasnails.api.thirst.ThirstHelper;
import toughasnails.thirst.ThirstHandler;

/**
 * @author p455w0rd
 *
 */
public class ModEvents {

	public static void init() {
		MinecraftForge.EVENT_BUS.register(new ModEvents());
	}

	/*
		@SubscribeEvent
		public void itemCapabilityAttach(AttachCapabilitiesEvent<ItemStack> event) {
			ItemStack stack = event.getObject();
			if (stack.isEmpty() || !(stack.getItem() instanceof ItemForgeEnergy) || stack.hasCapability(CapabilityEnergy.ENERGY, null) || event.getCapabilities().values().stream().anyMatch(c -> c.hasCapability(CapabilityEnergy.ENERGY, null))) {
				return;
			}

			event.addCapability(new ResourceLocation(ModGlobals.MODID, "fe_cap"), ItemForgeEnergy.getEnergyProvider(stack));
		}
	*/
	@SideOnly(Side.SERVER)
	@SubscribeEvent
	public void onPlayerLogin(PlayerLoggedInEvent e) {
		Map<String, Object> configs = new HashMap<String, Object>();
		configs.put("RequireEnergy", Options.REQUIRE_ENERGY);
		configs.put("TempRegulatorBlockRadius", Options.TEMP_REGULATOR_RADIUS);
		configs.put("TempRegulatorBlockRFCap", Options.TEMP_REGULATOR_RF_CAPACITY);
		configs.put("ThirstHealthFix", Options.THIRST_HEALTH_REGEN_FIX);
		configs.put("ThirstQuencherRFCap", Options.THIRST_QUENCHER_RF_CAPACITY);
		configs.put("PortableTempRegulatorCap", Options.PORTABLE_TEMP_REGULATOR_CAPACITY);
		ModNetworking.INSTANCE.sendTo(new PacketConfigSync(configs), (EntityPlayerMP) e.player);
	}

	@SubscribeEvent
	public void onHeal(LivingHealEvent e) {
		if (e.getEntityLiving() instanceof EntityPlayer && Options.THIRST_HEALTH_REGEN_FIX) {
			EntityPlayer player = (EntityPlayer) e.getEntityLiving();
			ThirstHandler thirstHandler = (ThirstHandler) ThirstHelper.getThirstData(player);
			int currentThirst = thirstHandler.getThirst();
			if (currentThirst < 19) {
				e.setCanceled(true);
			}
		}
	}

}
