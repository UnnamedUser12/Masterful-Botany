package velrondevs.masterfulbotany.common.block;

import javax.annotation.Nonnull;
import velrondevs.masterfulbotany.client.render.block_entity.RenderReturnPortal;
import velrondevs.masterfulbotany.common.block.block_entity.TileReturnPortal;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.moddingx.libx.base.tile.BlockBE;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.registration.SetupContext;

public class BlockReturnPortal extends BlockBE<TileReturnPortal> {

    public BlockReturnPortal(ModX mod, Properties properties) {
        super(mod, TileReturnPortal.class, properties, null);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void setupClient(SetupContext ctx) {
        ctx.enqueue(() -> BlockEntityRenderers.register(this.getBlockEntityType(), mgr -> new RenderReturnPortal()));
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public RenderShape getRenderShape(@Nonnull BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
}
