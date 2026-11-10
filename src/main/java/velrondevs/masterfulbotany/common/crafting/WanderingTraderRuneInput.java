package velrondevs.masterfulbotany.common.crafting;

import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.google.common.collect.ImmutableMultimap;
import com.mojang.datafixers.util.Either;
import java.util.List;
import java.util.UUID;
import velrondevs.masterfulbotany.MasterfulBotany;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

public class WanderingTraderRuneInput extends SpecialRuneInput {

    public static final WanderingTraderRuneInput INSTANCE = new WanderingTraderRuneInput();
    
    private ItemStack traderStack;

    private WanderingTraderRuneInput() {
        super(MasterfulBotany.getInstance().id("wandering_trader"));
    }

    private ItemStack traderStack() {
        if (this.traderStack != null) {
            return this.traderStack;
        }
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        PropertyMap properties = new PropertyMap(ImmutableMultimap.of("textures", new Property("textures", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWYxMzc5YTgyMjkwZDdhYmUxZWZhYWJiYzcwNzEwZmYyZWMwMmRkMzRhZGUzODZiYzAwYzkzMGM0NjFjZjkzMiJ9fX0=")));
        GameProfile profile = new GameProfile(UUID.fromString("3358ddae-3a41-4ba0-bdfa-ee54b6c55cf5"), "wandering_trader", properties);
        stack.set(DataComponents.PROFILE, ResolvableProfile.createResolved(profile));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.translatable("tooltip.masterfulbotany.sacrifice_entity1").withStyle(Style.EMPTY.withItalic(false)).withStyle(ChatFormatting.AQUA),
                Component.translatable("tooltip.masterfulbotany.sacrifice_entity2").withStyle(Style.EMPTY.withItalic(false)).withStyle(ChatFormatting.AQUA)
        )));
        stack.set(DataComponents.CUSTOM_NAME, Component.translatable("entity.minecraft.wandering_trader").withStyle(Style.EMPTY.withItalic(false)).withStyle(ChatFormatting.DARK_AQUA));
        this.traderStack = stack;
        return stack;
    }

    @Override
    public Either<MutableComponent, CompoundTag> apply(Level level, BlockPos pos, RuneRitualRecipe recipe) {
        Vec3 center = new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        AABB aabb = new AABB(center, center).inflate(3);
        List<WanderingTrader> traders = level.getEntities(EntityType.WANDERING_TRADER, aabb, e -> true);
        if (traders.isEmpty()) {
            return Either.left(Component.translatable("message.masterfulbotany.ritual_no_trader"));
        }
        WanderingTrader trader = traders.get(0);
        if (trader.getVehicle() != null) {
            trader.stopRiding();
        }
        if (trader.isVehicle()) {
            trader.ejectPassengers();
        }
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        trader.saveWithoutId(output);
        CompoundTag traderData = output.buildResult();
        trader.remove(Entity.RemovalReason.KILLED);
        return Either.right(traderData);
    }

    @Override
    public void cancel(Level level, BlockPos pos, RuneRitualRecipe recipe, CompoundTag nbt) {
        WanderingTrader trader = new WanderingTrader(EntityType.WANDERING_TRADER, level);
        UUID uid = trader.getUUID();
        trader.setPos(pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5);
        trader.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), nbt));
        trader.setUUID(uid);
        level.addFreshEntity(trader);
    }

    @Override
    public List<Ingredient> getJeiInputItems() {
        return ImmutableList.of(DataComponentIngredient.of(true, this.traderStack()));
    }
}
