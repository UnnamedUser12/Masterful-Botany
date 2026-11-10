package velrondevs.masterfulbotany.common.block.flower;

import javax.annotation.Nonnull;
import velrondevs.masterfulbotany.client.render.block_entity.RenderFunctionalFlower;
import velrondevs.masterfulbotany.common.block.block_entity.flower.FunctionalFlowerBase;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import org.moddingx.libx.base.tile.BlockBE;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.registration.SetupContext;
import velrondevs.botania.xplat.BotaniaConfig;

public class BlockFloatingFunctionalFlower<T extends FunctionalFlowerBase> extends BlockBE<T> {

    private static final VoxelShape SHAPE = box(1.6D, 1.6D, 1.6D, 14.4D, 14.4D, 14.4D);

    private final BlockFunctionalFlower<T> nonFloatingBlock;

    public BlockFloatingFunctionalFlower(ModX mod, Class<T> beClass, BlockFunctionalFlower<T> nonFloatingBlock, ResourceKey<Block> id) {
        super(mod, beClass, Properties.ofFullCopy(Blocks.RED_TULIP).isRedstoneConductor((state, world, pos) -> false)
                .instabreak().sound(SoundType.GRASS).setId(id));
        this.nonFloatingBlock = nonFloatingBlock;
    }

    @Override
    public void setupClient(SetupContext ctx) {
        ctx.enqueue(() -> BlockEntityRenderers.register(this.getBlockEntityType(), mgr -> new RenderFunctionalFlower<>()));
    }

    public BlockFunctionalFlower<T> getNonFloatingBlock() {
        return this.nonFloatingBlock;
    }

    @Nonnull
    @Override
    protected VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        return SHAPE;
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

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return state.getFluidState().isEmpty();
    }

    @Override
    protected boolean useShapeForLightOcclusion(@Nonnull BlockState state) {
        return true;
    }

    @Nonnull
    @Override
    protected RenderShape getRenderShape(@Nonnull BlockState state) {
        if (FMLEnvironment.getDist() != Dist.CLIENT) return RenderShape.INVISIBLE;
        return BotaniaConfig.client().staticFloaters() ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }
}
