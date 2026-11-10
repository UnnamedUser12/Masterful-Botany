package velrondevs.masterfulbotany.common.block;

import javax.annotation.Nonnull;
import velrondevs.masterfulbotany.registry.tags.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.UntintedParticleLeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.RegistrationContext;

public class BlockAlfheimLeaves extends UntintedParticleLeavesBlock implements Registerable {

    protected final ModX mod;
    private final Item item;

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES).strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion().isValidSpawn((a, b, c, d) -> false).isSuffocating((a, b, c) -> false).isViewBlocking((a, b, c) -> false);
    }

    public BlockAlfheimLeaves(ModX mod, BlockBehaviour.Properties properties, Item.Properties itemProperties) {
        super(0, ParticleTypes.CHERRY_LEAVES, properties);
        this.mod = mod;
        this.item = new BlockItem(this, itemProperties.useBlockDescriptionPrefix());
    }

    @Override
    public void registerAdditional(RegistrationContext ctx, EntryCollector builder) {
        builder.register(Registries.ITEM, this.item);
    }

    @Nonnull
    @Override
    protected BlockState updateShape(@Nonnull BlockState state, @Nonnull LevelReader level, @Nonnull ScheduledTickAccess ticks, @Nonnull BlockPos pos, @Nonnull Direction direction, @Nonnull BlockPos neighborPos, @Nonnull BlockState neighborState, @Nonnull RandomSource random) {
        int distance = this.getDistanceAt(neighborState) + 1;
        if (distance != 1 || state.getValue(DISTANCE) != distance) {
            ticks.scheduleTick(pos, this, 1);
        }
        return state;
    }

    @Override
    protected void tick(@Nonnull BlockState state, @Nonnull ServerLevel level, @Nonnull BlockPos pos, @Nonnull RandomSource random) {
        level.setBlock(pos, this.updateDistance(state, level, pos), 3);
    }

    @Override
    public BlockState getStateForPlacement(@Nonnull BlockPlaceContext context) {
        return this.updateDistance(this.defaultBlockState().setValue(PERSISTENT, true), context.getLevel(), context.getClickedPos());
    }

    protected int getDistanceAt(BlockState neighbor) {
        if (neighbor.is(ModBlockTags.ALFHEIM_LOGS)) {
            return 0;
        } else {
            return neighbor.getBlock() instanceof LeavesBlock ? neighbor.getValue(DISTANCE) : 7;
        }
    }

    protected BlockState updateDistance(BlockState state, LevelAccessor level, BlockPos pos) {
        int distance = 7;
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        for (Direction dir : Direction.values()) {
            mpos.setWithOffset(pos, dir);
            distance = Math.min(distance, this.getDistanceAt(level.getBlockState(mpos)) + 1);
            if (distance == 1) {
                break;
            }
        }
        return state.setValue(DISTANCE, distance);
    }
}
