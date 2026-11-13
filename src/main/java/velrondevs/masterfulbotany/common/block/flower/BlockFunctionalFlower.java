package velrondevs.masterfulbotany.common.block.flower;

import java.util.function.Consumer;
import javax.annotation.Nonnull;
import velrondevs.masterfulbotany.client.render.MasterfulClientSetup;
import velrondevs.masterfulbotany.common.block.block_entity.flower.FunctionalFlowerBase;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.Vec3;
import org.moddingx.libx.base.tile.BlockBE;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.registration.RegistrationContext;
import org.moddingx.libx.registration.SetupContext;
import velrondevs.botania.registry.BotaniaBlocks;

public class BlockFunctionalFlower<T extends FunctionalFlowerBase> extends BlockBE<T> {

    private static final VoxelShape SHAPE = box(4.8D, 0.0D, 4.8D, 12.8D, 16.0D, 12.8D);

    private final BlockFloatingFunctionalFlower<T> floatingBlock;
    public final boolean isGenerating;

    public BlockFunctionalFlower(ModX mod, Class<T> beClass, Properties properties, boolean isGenerating) {
        this(mod, beClass, properties, new Item.Properties(), isGenerating);
    }

    public BlockFunctionalFlower(ModX mod, Class<T> beClass, Properties properties, Item.Properties itemProperties, boolean isGenerating) {
        super(mod, beClass, properties.noCollision()
                .isRedstoneConductor((state, world, pos) -> false)
                .instabreak().sound(SoundType.GRASS)
                .offsetType(OffsetType.XZ), itemProperties);
        this.isGenerating = isGenerating;
        String descriptionId = this.getDescriptionId();
        String path = descriptionId.substring(descriptionId.lastIndexOf('.') + 1);
        this.floatingBlock = new BlockFloatingFunctionalFlower<>(mod, beClass, this, ResourceKey.create(Registries.BLOCK, mod.id(path + "_floating")));
    }

    @Override
    public void registerAdditional(RegistrationContext ctx, EntryCollector builder) {
        super.registerAdditional(ctx, builder);
        builder.registerNamed(Registries.BLOCK, "floating", this.floatingBlock);
    }

    @Override
    public void setupClient(SetupContext ctx) {
        ctx.enqueue(() -> MasterfulClientSetup.functionalFlower(this.getBlockEntityType()));
    }

    @Override
    protected boolean hasAnalogOutputSignal(@Nonnull BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(@Nonnull BlockState blockState, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Direction direction) {
        FunctionalFlowerBase te = this.getBlockEntity(level, pos);
        if (te.getCurrentMana() > 0) {
            return 1 + (int) ((te.getCurrentMana() / (double) te.maxMana) * 14);
        } else {
            return 0;
        }
    }

    @Nonnull
    @Override
    protected VoxelShape getShape(BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        Vec3 shift = state.getOffset(pos);
        return SHAPE.move(shift.x, shift.y, shift.z);
    }

    @Nonnull
    @Override
    protected BlockState updateShape(@Nonnull BlockState state, @Nonnull LevelReader level, @Nonnull ScheduledTickAccess ticks, @Nonnull BlockPos pos, @Nonnull Direction direction, @Nonnull BlockPos neighborPos, @Nonnull BlockState neighborState, @Nonnull RandomSource random) {
        return !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
    }

    protected boolean isValidGround(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.PODZOL) || state.is(Blocks.FARMLAND) || state.is(BotaniaBlocks.enchantedSoil)
                || state.is(Blocks.MYCELIUM) || state.is(BlockTags.DIRT) || state.canSustainPlant(level, pos, Direction.UP, this.defaultBlockState()).isTrue();
    }

    @Override
    protected boolean canSurvive(@Nonnull BlockState state, @Nonnull LevelReader level, BlockPos pos) {
        BlockPos blockpos = pos.below();
        return this.isValidGround(level.getBlockState(blockpos), level, blockpos);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return state.getFluidState().isEmpty();
    }

    @Override
    protected boolean isPathfindable(@Nonnull BlockState state, @Nonnull PathComputationType type) {
        return type == PathComputationType.AIR && !this.hasCollision || super.isPathfindable(state, type);
    }

    public static void addTooltip(Block block, boolean generating, Consumer<Component> tooltip) {
        if (generating) {
            tooltip.accept(Component.translatable("botania.flowerType.generating").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
        } else {
            tooltip.accept(Component.translatable("botania.flowerType.functional").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
        }
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        if (id != null) {
            tooltip.accept(Component.translatable("block." + id.getNamespace() + "." + id.getPath() + ".description").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }

    @Override
    protected boolean useShapeForLightOcclusion(@Nonnull BlockState state) {
        return true;
    }

    public BlockFloatingFunctionalFlower<T> getFloatingBlock() {
        return this.floatingBlock;
    }

    @Nonnull
    @Override
    protected RenderShape getRenderShape(@Nonnull BlockState state) {
        return RenderShape.MODEL;
    }
}
