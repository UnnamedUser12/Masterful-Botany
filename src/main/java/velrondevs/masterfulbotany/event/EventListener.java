package velrondevs.masterfulbotany.event;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import velrondevs.masterfulbotany.common.block.block_entity.TileReturnPortal;
import velrondevs.masterfulbotany.common.block.BlockMjoellnir;
import velrondevs.masterfulbotany.common.crafting.RecipeRemover;
import velrondevs.masterfulbotany.common.helper.Andwari;
import velrondevs.masterfulbotany.common.helper.MasterfulPlayerData;
import velrondevs.masterfulbotany.common.item.equipment.AlfsteelHelm;
import velrondevs.masterfulbotany.common.world.alfheim.Alfheim;
import velrondevs.masterfulbotany.common.world.alfheim.AlfheimPortalHandler;
import velrondevs.masterfulbotany.common.world.alfheim.AlfheimTeleporter;
import velrondevs.masterfulbotany.config.MasterfulConfig;
import velrondevs.masterfulbotany.MasterfulBotany;
import velrondevs.masterfulbotany.registry.ModBlocks;
import velrondevs.masterfulbotany.registry.ModEnchantments;
import velrondevs.masterfulbotany.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import velrondevs.botania.api.item.AncientWillContainer;
import velrondevs.botania.api.mana.ManaItemHandler;
import velrondevs.botania.api.recipe.ElvenPortalUpdateEvent;
import velrondevs.botania.common.block.block_entity.AlfheimPortalBlockEntity;
import velrondevs.botania.common.item.equipment.armor.terrasteel.TerrasteelHelmItem;
import velrondevs.botania.registry.BotaniaItems;

public class EventListener {

    @Nullable
    public static LivingEntity lightningImmuneEntity = null;

    private final Set<UUID> crittingPlayers = new HashSet<>();

    @SubscribeEvent
    public void serverStarting(ServerStartingEvent event) {
        RecipeRemover.removeRecipes(event.getServer().getRecipeManager());
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void datapackSync(OnDatapackSyncEvent event) {
        RecipeRemover.removeRecipes(event.getPlayerList().getServer().getRecipeManager());
    }

    @SubscribeEvent
    public void onDamage(LivingIncomingDamageEvent event) {
        // The rings work for the attacker: the target of a player wearing the ring is ignited / slowed down
        if (event.getSource().getEntity() instanceof Player attacker) {
            ICuriosItemHandler curios = CuriosApi.getCuriosInventory(attacker).orElse(null);
            if (curios != null && curios.findFirstCurio(ModItems.fireRing).isPresent()) {
                if (event.getEntity().getRemainingFireTicks() <= 1) {
                    event.getEntity().igniteForSeconds(1);
                }
            }
            if (curios != null && curios.findFirstCurio(ModItems.iceRing).isPresent()) {
                if (!event.getEntity().hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
                    event.getEntity().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 99));
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void citicalHit(CriticalHitEvent event) {
        if (event.isCriticalHit()) {
            if (((AlfsteelHelm) ModItems.alfsteelHelmet).hasArmorSet(event.getEntity())) {
                if (((AlfsteelHelm) ModItems.alfsteelHelmet).hasAncientWill(event.getEntity().getItemBySlot(EquipmentSlot.HEAD), AncientWillContainer.AncientWillType.DHAROK)) {
                    float calculatedModifier = event.getDamageMultiplier() * (1f + (1f - event.getEntity().getHealth() / event.getEntity().getMaxHealth()) * 0.5f);
                    if (calculatedModifier != 1 && calculatedModifier != 0 && Float.isFinite(calculatedModifier)) {
                        event.setDamageMultiplier(calculatedModifier);
                    }
                }
                this.crittingPlayers.add(event.getEntity().getUUID());
            }
        }
    }

    @SubscribeEvent
    public void attackEntity(LivingIncomingDamageEvent event) {
        if (event.getSource().is(DamageTypeTags.IS_LIGHTNING) && event.getEntity() == lightningImmuneEntity) {
            event.setCanceled(true);
            return;
        }
        if (event.getSource().is(DamageTypeTags.IS_FIRE)) {
            ICuriosItemHandler curios = CuriosApi.getCuriosInventory(event.getEntity()).orElse(null);
            if (curios != null && curios.findFirstCurio(ModItems.fireRing).isPresent()) {
                event.setCanceled(true);
                return;
            }
        }
        if (event.getSource().is(DamageTypes.CRAMMING) || event.getSource().is(DamageTypes.IN_WALL) || event.getSource().is(DamageTypes.DRY_OUT)) {
            ICuriosItemHandler curios = CuriosApi.getCuriosInventory(event.getEntity()).orElse(null);
            if (curios != null && curios.findFirstCurio(ModItems.iceRing).isPresent()) {
                event.setCanceled(true);
                return;
            }
        }
        if (event.getSource().getEntity() instanceof Player player && this.crittingPlayers.contains(player.getUUID())) {
            TerrasteelHelmItem.onEntityAttacked(event.getSource(), event.getAmount(), player, event.getEntity());
        }
    }

    @SubscribeEvent
    public void endTick(ServerTickEvent.Post event) {
        this.crittingPlayers.clear();
    }

    @SubscribeEvent
    public void playerClone(PlayerEvent.Clone event) {
        MasterfulPlayerData.copy(event.getOriginal(), event.getEntity());
    }

    @SubscribeEvent
    public void placeBlock(BlockEvent.EntityPlaceEvent event) {
        if (!event.getLevel().isClientSide() && event.getPlacedBlock().getBlock() == Blocks.GOLD_BLOCK && event.getEntity() instanceof LivingEntity living && event.getLevel() instanceof ServerLevel serverLevel) {
            CuriosApi.getCuriosInventory(living).ifPresent(curios -> curios.findFirstCurio(ModItems.andwariRing).ifPresent(result -> {
                boolean hasMana = true;
                if (living instanceof Player player) {
                    hasMana = ManaItemHandler.instance().requestManaExact(result.stack(), player, 2000, true);
                }
                if (hasMana) {
                    if (!(living instanceof Player player && player.isCreative())) {
                        String id = result.slotContext().identifier();
                        int slot = result.slotContext().index();
                        ItemStack stack = result.stack();
                        stack.hurtAndBreak(1, serverLevel, living, item -> {
                            ICurioStacksHandler curio = curios.getCurios().get(id);
                            if (curio != null)
                                curio.getStacks().setStackInSlot(slot, new ItemStack(ModItems.cursedAndwariRing));
                        });
                    }
                    ItemStack drop = Andwari.randomAndwariItem(event.getLevel().getRandom(), serverLevel.registryAccess());
                    living.spawnAtLocation(drop);
                }
            }));
        }
    }

    @SubscribeEvent
    public void pickupItem(ItemEntityPickupEvent.Pre event) {
        if (!event.getItemEntity().getCommandSenderWorld().isClientSide) {
            if (event.getItemEntity().getItem().getItem() == ModBlocks.mjoellnir.asItem()) {
                if (!BlockMjoellnir.canHold(event.getPlayer())) {
                    event.setCanPickup(TriState.FALSE);
                }
            }
        }
    }

    @SubscribeEvent
    public void alfPortalUpdate(ElvenPortalUpdateEvent event) {
        BlockEntity portal = event.getPortalTile();
        if (event.isOpen() && portal.getLevel() != null && !portal.getLevel().isClientSide) {
            if (Alfheim.DIMENSION.equals(portal.getLevel().dimension())) {
                // Alfheim portals in alfheim make no sense. better close this one.
                if (portal instanceof AlfheimPortalBlockEntity alfPortal) {
                    // A portal will close if it tries to consume mana without pylons.
                    // So we just let it consume 0 mana with 0 pylons which should close
                    // the portal.
                    alfPortal.consumeMana(new ArrayList<>(), 0, true);
                }
            }
            // We only teleport from the overworld.
            if (Level.OVERWORLD.equals(portal.getLevel().dimension()) && AlfheimPortalHandler.shouldCheck(portal.getLevel())) {
                List<Player> playersInPortal = portal.getLevel().getEntitiesOfClass(Player.class, event.getAabb());
                for (Player player : playersInPortal) {
                    if (player instanceof ServerPlayer && MasterfulPlayerData.getData(player).getBoolean("KvasirKnowledge")) {
                        if (AlfheimPortalHandler.setInPortal(portal.getLevel(), player)) {
                            if (!AlfheimTeleporter.teleportToAlfheim((ServerPlayer) player, portal.getBlockPos())) {
                                player.sendSystemMessage(Component.translatable("message.masterfulbotany.alfheim_not_loaded"));
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void itemDespawn(ItemExpireEvent event) {
        if (!event.getEntity().level().isClientSide && Alfheim.DIMENSION.equals(event.getEntity().level().dimension())) {
            if (event.getEntity().getItem().getItem() == BotaniaItems.pixieDust) {
                BlockPos pos = event.getEntity().blockPosition();
                if (TileReturnPortal.validPortal(event.getEntity().level(), pos)) {
                    event.getEntity().level().setBlock(pos, ModBlocks.returnPortal.defaultBlockState(), 3);
                    event.getEntity().remove(Entity.RemovalReason.DISCARDED);
                }
            }
        }
    }

    @SubscribeEvent
    public void playerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().tickCount % 4 == 1 && !event.getEntity().level().isClientSide && Alfheim.DIMENSION.equals(event.getEntity().level().dimension())) {
            if (MasterfulConfig.lockAlfheim && !MasterfulPlayerData.getData(event.getEntity()).getBoolean("KvasirKnowledge")) {
                // Player used another mod to get to alfheim
                event.getEntity().addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, true, false, true));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void sleepFinished(SleepFinishedTimeEvent event) {
        if (event.getLevel() instanceof ServerLevel level && !level.isClientSide) {
            if (Alfheim.DIMENSION.equals(level.dimension())) {
                level.getServer().overworld().setDayTime(event.getNewTime());
            }
        }
    }

    /**
     * Mjoellnir gets bonus damage from sharpness and bonus attack speed from hammer mobility.
     * This can't be done in the default attribute modifiers as these depend on the enchantments.
     */
    @SubscribeEvent
    public void itemAttributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() == ModBlocks.mjoellnir.asItem()) {
            int mobility = level(stack, ModEnchantments.HAMMER_MOBILITY);
            if (mobility > 0) {
                event.addModifier(Attributes.ATTACK_SPEED, new AttributeModifier(MasterfulBotany.getInstance().resource("hammer_mobility"), MasterfulConfig.mjoellnir.attack_speed_multiplier * mobility, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            }
            int sharpness = level(stack, Enchantments.SHARPNESS);
            if (sharpness > 0) {
                float bonus = 1.0f + Math.max(0, sharpness - 1) * 0.5f;
                event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(MasterfulBotany.getInstance().resource("mjoellnir_sharpness"), (MasterfulConfig.mjoellnir.enchantment_multiplier - 1) * bonus, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            }
        }
    }

    private static int level(ItemStack stack, net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment> key) {
        for (var entry : stack.getEnchantments().entrySet()) {
            if (entry.getKey().is(key)) return entry.getIntValue();
        }
        return 0;
    }
}
