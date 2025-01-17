package safro.zenith.adventure.client;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStack.TooltipPart;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableInt;
import safro.zenith.Zenith;
import safro.zenith.adventure.AdventureModule;
import safro.zenith.adventure.affix.Affix;
import safro.zenith.adventure.affix.AffixHelper;
import safro.zenith.adventure.affix.AffixInstance;
import safro.zenith.adventure.affix.socket.SocketHelper;
import safro.zenith.adventure.affix.socket.gem.GemItem;
import safro.zenith.util.ItemAccess;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class AdventureModuleClient {

//	public static List<BossSpawnData> BOSS_SPAWNS = new ArrayList<>();

	public static void init() {
	//	MenuScreens.register(Apoth.Menus.REFORGING.get(), ReforgingScreen::new);
	//	MenuScreens.register(Apoth.Menus.SALVAGE.get(), SalvagingScreen::new);
	//	MenuScreens.register(Apoth.Menus.GEM_CUTTING.get(), GemCuttingScreen::new);
	//	BlockEntityRenderers.register(Apoth.Tiles.REFORGING_TABLE.get(), k -> new ReforgingTableTileRenderer());
	}
/*
	public static void onBossSpawn(BlockPos pos, float[] color) {
		BOSS_SPAWNS.add(new BossSpawnData(pos, color, new MutableInt()));
		Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(SoundEvents.END_PORTAL_SPAWN, SoundSource.HOSTILE, AdventureConfig.bossAnnounceVolume, 1.25F, Minecraft.getInstance().player.random, Minecraft.getInstance().player.blockPosition()));
	}

	public static class ModBusSub {
		public static void models(ModelEvent.RegisterAdditional e) {
			e.register(new ResourceLocation(Apotheosis.MODID, "item/hammer"));
		}

		public static void onRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders e) {
			e.register("item_layers", FItemLayerModel.Loader.INSTANCE);
		}

		public static void tooltipComps(RegisterClientTooltipComponentFactoriesEvent e) {
			e.register(SocketComponent.class, SocketTooltipRenderer::new);
		}

		public static void addGemModels(ModelEvent.RegisterAdditional e) {
			Set<ResourceLocation> locs = Minecraft.getInstance().getResourceManager().listResources("models", loc -> loc.getNamespace().equals(Apotheosis.MODID) && loc.getPath().contains("/gems/") && loc.getPath().endsWith(".json")).keySet();
			for (ResourceLocation s : locs) {
				String path = s.getPath().substring("models/".length(), s.getPath().length() - ".json".length());
				e.register(new ResourceLocation(Apotheosis.MODID, path));
			}
		}

		public static void replaceGemModel(ModelEvent.BakingCompleted e) {
			ModelResourceLocation key = new ModelResourceLocation(Apotheosis.loc("gem"), "inventory");
			BakedModel oldModel = e.getModels().get(key);
			if (oldModel != null) {
				e.getModels().put(key, new GemModel(oldModel, e.getModelBakery()));
			}
		}
	}

        public static void render(RenderLevelStageEvent e) {
            if (e.getStage() != Stage.AFTER_TRIPWIRE_BLOCKS) return;
            PoseStack stack = e.getPoseStack();
            MultiBufferSource.BufferSource buf = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            Player p = Minecraft.getInstance().player;
            for (int i = 0; i < BOSS_SPAWNS.size(); i++) {
                BossSpawnData data = BOSS_SPAWNS.get(i);
                stack.pushPose();
                float partials = e.getPartialTick();
                Vec3 vec = Minecraft.getInstance().getCameraEntity().getEyePosition(partials);
                stack.translate(-vec.x, -vec.y, -vec.z);
                stack.translate(data.pos().getX(), data.pos().getY(), data.pos().getZ());
                BeaconRenderer.renderBeaconBeam(stack, buf, BeaconRenderer.BEAM_LOCATION, partials, 1, p.level.getGameTime(), 0, 64, data.color(), 0.166F, 0.33F);
                stack.popPose();
            }
            buf.endBatch();
        }

        public static void time(ClientTickEvent e) {
            if (e.phase != Phase.END) return;
            for (int i = 0; i < BOSS_SPAWNS.size(); i++) {
                BossSpawnData data = BOSS_SPAWNS.get(i);
                if (data.ticks().getAndIncrement() > 400) {
                    BOSS_SPAWNS.remove(i--);
                }
            }
        }
    */
	public static void tooltips() {
		ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
			int markIdx1 = -1, markIdx2 = -1;
		for (int i = 0; i < lines.size(); i++) {
			if (lines.get(i).getContents() instanceof LiteralContents tc) {
				if (tc.text().equals("APOTH_REMOVE_MARKER")) {
					markIdx1 = i;
				}
				if (tc.text().equals("APOTH_REMOVE_MARKER_2")) {
					markIdx2 = i;
					break;
				}
			}
		}
		if (markIdx1 == -1 || markIdx2 == -1) return;
		var it = lines.listIterator(markIdx1);
		for (int i = markIdx1; i < markIdx2 + 1; i++) {
			it.next();
			it.remove();
		}
		int flags = getHideFlags(stack);
		if (shouldShowInTooltip(flags, TooltipPart.MODIFIERS)) {
		//	applyModifierTooltips(e.getEntity(), stack, it::add);
		}
		if (AffixHelper.getAffixes(stack).containsKey(AdventureModule.SOCKET.get())) it.add(Component.literal("APOTH_REMOVE_MARKER"));
		});
	}

	public static void comps() {
		ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
		AffixInstance socket = AffixHelper.getAffixes(stack).get(AdventureModule.SOCKET.get());
		if (socket == null) return;

	//	List<Either<FormattedText, TooltipComponent>> list; = e.getTooltipElements();
		int rmvIdx = -1;/*
		for (int i = 0; i < lines.size(); i++) {
			Optional<FormattedText> o = list.get(i).left();
			if (o.isPresent() && o.get() instanceof Component comp && comp.getContents() instanceof LiteralContents tc) {
				if (tc.text().equals("APOTH_REMOVE_MARKER")) {
					rmvIdx = i;
					list.remove(i);
					break;
				}
			}
		} */
		if (rmvIdx == -1) return;
		int size = (int) socket.level();
	//	lines.add(rmvIdx, Either.right(new SocketTooltipRenderer.SocketComponent(stack, SocketHelper.getGems(stack, size))));
	});
	}

	public static void affixTooltips() {
		ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
			if (stack.hasTag()) {
				Map<Affix, AffixInstance> affixes = AffixHelper.getAffixes(stack);
				List<Component> components = new ArrayList<>();
				affixes.values().stream().sorted(Comparator.comparingInt(a -> a.affix().getType().ordinal())).forEach(inst -> inst.addInformation(components::add));
				lines.addAll(1, components);
			}

			if (stack.getItem() == Items.ENCHANTED_BOOK && !FabricLoader.getInstance().isModLoaded("enchdesc")) {
				var enchMap = EnchantmentHelper.getEnchantments(stack);
				if (enchMap.size() == 1) {
					var ench = enchMap.keySet().iterator().next();
					if (Registry.ENCHANTMENT.getKey(ench).getNamespace().equals(Zenith.MODID)) {
						lines.add(Component.translatable(ench.getDescriptionId() + ".desc").withStyle(ChatFormatting.DARK_GRAY));
					}
				}
			}
		});
	}

	public static Multimap<Attribute, AttributeModifier> sortedMap() {
		return TreeMultimap.create((k1, k2) -> id(k1).compareTo(id(k2)), (v1, v2) -> {
			int compOp = Integer.compare(v1.getOperation().ordinal(), v2.getOperation().ordinal());
			int compValue = Double.compare(v2.getAmount(), v1.getAmount());
			return compOp == 0 ? compValue == 0 ? v1.getId().compareTo(v2.getId()) : compValue : compOp;
		});
	}

	private static ResourceLocation id(Attribute attr) {
		return Registry.ATTRIBUTE.getKey(attr);
	}

	public static Multimap<Attribute, AttributeModifier> getSortedModifiers(ItemStack stack, EquipmentSlot slot) {
		var unsorted = stack.getAttributeModifiers(slot);
		Multimap<Attribute, AttributeModifier> map = sortedMap();
		for (Map.Entry<Attribute, AttributeModifier> ent : unsorted.entries()) {
			if (ent.getKey() != null && ent.getValue() != null) map.put(ent.getKey(), ent.getValue());
			else AdventureModule.LOGGER.debug("Detected broken attribute modifier entry on item {}.  Attr={}, Modif={}", stack, ent.getKey(), ent.getValue());
		}
		return map;
	}

	private static boolean shouldShowInTooltip(int pHideFlags, TooltipPart pPart) {
		return (pHideFlags & pPart.getMask()) == 0;
	}

	private static int getHideFlags(ItemStack stack) {
		return stack.hasTag() && stack.getTag().contains("HideFlags", 99) ? stack.getTag().getInt("HideFlags") : stack.getItem().getDefaultTooltipHideFlags(stack);
	}

	private static void applyModifierTooltips(@Nullable Player player, ItemStack stack, Consumer<Component> tooltip) {
		Multimap<Attribute, AttributeModifier> mainhand = getSortedModifiers(stack, EquipmentSlot.MAINHAND);
		Multimap<Attribute, AttributeModifier> offhand = getSortedModifiers(stack, EquipmentSlot.OFFHAND);
		Multimap<Attribute, AttributeModifier> dualHand = sortedMap();
		for (Attribute atr : mainhand.keys()) {
			Collection<AttributeModifier> modifMh = mainhand.get(atr);
			Collection<AttributeModifier> modifOh = offhand.get(atr);
			modifMh.stream().filter(a1 -> modifOh.stream().anyMatch(a2 -> a1.getName().equals(a2.getName()))).forEach(modif -> dualHand.put(atr, modif));
		}

		dualHand.values().forEach(m -> {
			mainhand.values().remove(m);
			offhand.values().removeIf(m1 -> m1.getName().equals(m.getName()));
		});

		int sockets = SocketHelper.getSockets(stack);
		Set<UUID> skips = new HashSet<>();
		if (sockets > 0) {
			for (ItemStack gem : SocketHelper.getGems(stack, sockets)) {
				skips.addAll(GemItem.getUUIDs(gem));
			}
		}

		applyTextFor(player, stack, tooltip, dualHand, "both_hands", skips);
		applyTextFor(player, stack, tooltip, mainhand, EquipmentSlot.MAINHAND.getName(), skips);
		applyTextFor(player, stack, tooltip, offhand, EquipmentSlot.OFFHAND.getName(), skips);

		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot.ordinal() < 2) continue;
			Multimap<Attribute, AttributeModifier> modifiers = getSortedModifiers(stack, slot);
			applyTextFor(player, stack, tooltip, modifiers, slot.getName(), skips);
		}
	}

	private static MutableComponent padded(String padding, Component comp) {
		return Component.literal(padding).append(comp);
	}

	private static MutableComponent list() {
		return Component.literal(" \u2507 ").withStyle(ChatFormatting.GRAY);
	}

	private static void applyTextFor(@Nullable Player player, ItemStack stack, Consumer<Component> tooltip, Multimap<Attribute, AttributeModifier> modifierMap, String group, Set<UUID> skips) {
		if (!modifierMap.isEmpty()) {
			modifierMap.values().removeIf(m -> skips.contains(m.getId()));

			tooltip.accept(Component.empty());
			tooltip.accept(Component.translatable("item.modifiers." + group).withStyle(ChatFormatting.GRAY));

			if (modifierMap.isEmpty()) return;

			AttributeModifier baseAD = null, baseAS = null;
			List<AttributeModifier> dmgModifs = new ArrayList<>(), spdModifs = new ArrayList<>();

			for (AttributeModifier modif : modifierMap.get(Attributes.ATTACK_DAMAGE)) {
				if (modif.getId() == ItemAccess.getBaseAD()) baseAD = modif;
				else dmgModifs.add(modif);
			}

			for (AttributeModifier modif : modifierMap.get(Attributes.ATTACK_SPEED)) {
				if (modif.getId() == ItemAccess.getBaseAS()) baseAS = modif;
				else spdModifs.add(modif);
			}

			if (baseAD != null) {
				double base = baseAD.getAmount() + (player == null ? 0 : player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
				double rawBase = base;
				double amt = base;
				for (AttributeModifier modif : dmgModifs) {
					if (modif.getOperation() == Operation.ADDITION) base = amt = amt + modif.getAmount();
					else if (modif.getOperation() == Operation.MULTIPLY_BASE) amt += modif.getAmount() * base;
					else amt *= 1 + modif.getAmount();
				}
				amt += EnchantmentHelper.getDamageBonus(stack, MobType.UNDEFINED);
				MutableComponent text = Component.translatable("attribute.modifier.equals.0", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(amt), Component.translatable(Attributes.ATTACK_DAMAGE.getDescriptionId()));
				tooltip.accept(padded(" ", text).withStyle(dmgModifs.isEmpty() ? ChatFormatting.DARK_GREEN : ChatFormatting.GOLD));
				if (Screen.hasShiftDown() && !dmgModifs.isEmpty()) {
					text = Component.translatable("attribute.modifier.equals.0", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(rawBase), Component.translatable(Attributes.ATTACK_DAMAGE.getDescriptionId()));
					tooltip.accept(list().append(text.withStyle(ChatFormatting.DARK_GREEN)));
					for (AttributeModifier modifier : dmgModifs) {
						tooltip.accept(list().append(GemItem.toComponent(Attributes.ATTACK_DAMAGE, modifier)));
					}
					float bonus = EnchantmentHelper.getDamageBonus(stack, MobType.UNDEFINED);
					if (bonus > 0) {
						tooltip.accept(list().append(Component.translatable("attribute.modifier.plus.0", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(bonus), Component.translatable(Attributes.ATTACK_DAMAGE.getDescriptionId())).withStyle(ChatFormatting.BLUE)));
					}
				}
			}

			if (baseAS != null) {
				double base = baseAS.getAmount() + (player == null ? 0 : player.getAttributeBaseValue(Attributes.ATTACK_SPEED));
				double rawBase = base;
				double amt = base;
				for (AttributeModifier modif : spdModifs) {
					if (modif.getOperation() == Operation.ADDITION) base = amt = amt + modif.getAmount();
					else if (modif.getOperation() == Operation.MULTIPLY_BASE) amt += modif.getAmount() * base;
					else amt *= 1 + modif.getAmount();
				}
				MutableComponent text = Component.translatable("attribute.modifier.equals.0", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(amt), Component.translatable(Attributes.ATTACK_SPEED.getDescriptionId()));
				tooltip.accept(Component.literal(" ").append(text).withStyle(spdModifs.isEmpty() ? ChatFormatting.DARK_GREEN : ChatFormatting.GOLD));
				if (Screen.hasShiftDown() && !spdModifs.isEmpty()) {
					text = Component.translatable("attribute.modifier.equals.0", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(rawBase), Component.translatable(Attributes.ATTACK_SPEED.getDescriptionId()));
					tooltip.accept(list().append(text.withStyle(ChatFormatting.DARK_GREEN)));
					for (AttributeModifier modifier : spdModifs) {
						tooltip.accept(list().append(GemItem.toComponent(Attributes.ATTACK_SPEED, modifier)));
					}
				}
			}

			for (Attribute attr : modifierMap.keySet()) {
				if ((baseAD != null && attr == Attributes.ATTACK_DAMAGE) || (baseAS != null && attr == Attributes.ATTACK_SPEED)) continue;
				Collection<AttributeModifier> modifs = modifierMap.get(attr);
				if (modifs.size() > 1) {
					double[] sums = new double[3];
					boolean[] merged = new boolean[3];
					Map<Operation, List<AttributeModifier>> shiftExpands = new HashMap<>();
					for (AttributeModifier modifier : modifs) {
						if (modifier.getAmount() == 0) continue;
						if (sums[modifier.getOperation().ordinal()] != 0) merged[modifier.getOperation().ordinal()] = true;
						sums[modifier.getOperation().ordinal()] += modifier.getAmount();
						shiftExpands.computeIfAbsent(modifier.getOperation(), k -> new LinkedList<>()).add(modifier);
					}
					for (int i = 0; i < 3; i++) {
						if (sums[i] == 0) continue;
						String key = "attribute.modifier." + (sums[i] < 0 ? "take." : "plus.") + i;
						if (i != 0) key = "attribute.modifier.apotheosis." + (sums[i] < 0 ? "take." : "plus.") + i;
						Style style;
						if (merged[i]) style = sums[i] < 0 ? Style.EMPTY.withColor(TextColor.fromRgb(0xF93131)) : Style.EMPTY.withColor(TextColor.fromRgb(0x7A7AF9));
						else style = sums[i] < 0 ? Style.EMPTY.withColor(ChatFormatting.RED) : Style.EMPTY.withColor(ChatFormatting.BLUE);
						if (sums[i] < 0) sums[i] *= -1;
						if (attr == Attributes.KNOCKBACK_RESISTANCE) sums[i] *= 10;
						tooltip.accept(Component.translatable(key, ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(sums[i]), Component.translatable(attr.getDescriptionId())).withStyle(style));
						if (merged[i] && Screen.hasShiftDown()) {
							shiftExpands.get(Operation.fromValue(i)).forEach(modif -> tooltip.accept(list().append(GemItem.toComponent(attr, modif))));
						}
					}
				} else modifs.forEach(m -> {
					if (m.getAmount() != 0) tooltip.accept(GemItem.toComponent(attr, m));
				});
			}
		}
	}
/*
	// Unused, doesn't actually work to render beacons without depth.
	private static abstract class CustomBeacon extends RenderStateShard {

		public CustomBeacon(String pName, Runnable pSetupState, Runnable pClearState) {
			super(pName, pSetupState, pClearState);
		}

		//Formatter::off
		static final BiFunction<ResourceLocation, Boolean, RenderType> BEACON_BEAM = Util.memoize((p_173224_, p_173225_) -> {
			RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder()
				.setShaderState(RENDERTYPE_BEACON_BEAM_SHADER)
				.setTextureState(new TextureStateShard(p_173224_, false, false))
				.setTransparencyState(p_173225_ ? TRANSLUCENT_TRANSPARENCY : NO_TRANSPARENCY)
				.setWriteMaskState(p_173225_ ? COLOR_WRITE : COLOR_WRITE)
				.setDepthTestState(NO_DEPTH_TEST)
				.setCullState(NO_CULL)
				.createCompositeState(false);
			return RenderType.create("custom_beacon_beam", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 256, false, true, rendertype$compositestate);
		});
		//Formatter::on
	}

	static final RenderType beaconBeam(ResourceLocation tex, boolean color) {
		return CustomBeacon.BEACON_BEAM.apply(tex, color);
	}

	public static void renderBeaconBeam(PoseStack pPoseStack, MultiBufferSource pBufferSource, ResourceLocation pBeamLocation, float pPartialTick, float pTextureScale, long pGameTime, int pYOffset, int pHeight, float[] pColors, float pBeamRadius, float pGlowRadius) {
		int i = pYOffset + pHeight;
		pPoseStack.pushPose();
		pPoseStack.translate(0.5D, 0.0D, 0.5D);
		float f = (float) Math.floorMod(pGameTime, 40) + pPartialTick;
		float f1 = pHeight < 0 ? f : -f;
		float f2 = Mth.frac(f1 * 0.2F - (float) Mth.floor(f1 * 0.1F));
		float f3 = pColors[0];
		float f4 = pColors[1];
		float f5 = pColors[2];
		pPoseStack.pushPose();
		pPoseStack.mulPose(Vector3f.YP.rotationDegrees(f * 2.25F - 45.0F));
		float f6 = 0.0F;
		float f8 = 0.0F;
		float f9 = -pBeamRadius;
		float f12 = -pBeamRadius;
		float f15 = -1.0F + f2;
		float f16 = (float) pHeight * pTextureScale * (0.5F / pBeamRadius) + f15;
		BeaconRenderer.renderPart(pPoseStack, pBufferSource.getBuffer(beaconBeam(pBeamLocation, false)), f3, f4, f5, 1.0F, pYOffset, i, 0.0F, pBeamRadius, pBeamRadius, 0.0F, f9, 0.0F, 0.0F, f12, 0.0F, 1.0F, f16, f15);
		pPoseStack.popPose();
		f6 = -pGlowRadius;
		float f7 = -pGlowRadius;
		f8 = -pGlowRadius;
		f9 = -pGlowRadius;
		f15 = -1.0F + f2;
		f16 = (float) pHeight * pTextureScale + f15;
		BeaconRenderer.renderPart(pPoseStack, pBufferSource.getBuffer(beaconBeam(pBeamLocation, true)), f3, f4, f5, 0.125F, pYOffset, i, f6, f7, pGlowRadius, f8, f9, pGlowRadius, pGlowRadius, pGlowRadius, 0.0F, 1.0F, f16, f15);
		pPoseStack.popPose();
	}
*/
}
