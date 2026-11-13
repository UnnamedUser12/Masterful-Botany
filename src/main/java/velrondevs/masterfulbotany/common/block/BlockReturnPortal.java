package velrondevs.masterfulbotany.common.block;

import javax.annotation.Nonnull;
import velrondevs.masterfulbotany.client.render.MasterfulClientSetup;
import velrondevs.masterfulbotany.common.block.block_entity.TileReturnPortal;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.moddingx.libx.base.tile.BlockBE;
import org.moddingx.libx.mod.ModX;
import org.moddingx.libx.registration.SetupContext;

public class BlockReturnPortal extends BlockBE<TileReturnPortal> {

    public BlockReturnPortal(ModX mod, Properties properties) {
        super(mod, TileReturnPortal.class, properties, null);
    }

    @Override
    public void setupClient(SetupContext ctx) {
        ctx.enqueue(() -> MasterfulClientSetup.returnPortal(this.getBlockEntityType()));
    }

    @Nonnull
    @Override
    protected RenderShape getRenderShape(@Nonnull BlockState state) {
        return RenderShape.INVISIBLE;
    }
}
