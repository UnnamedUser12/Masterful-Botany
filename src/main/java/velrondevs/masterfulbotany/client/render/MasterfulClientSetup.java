package velrondevs.masterfulbotany.client.render;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.entity.BlockEntityType;
import velrondevs.masterfulbotany.client.render.block_entity.RenderAlfsteelPylon;
import velrondevs.masterfulbotany.client.render.block_entity.RenderFunctionalFlower;
import velrondevs.masterfulbotany.client.render.block_entity.RenderMjoellnir;
import velrondevs.masterfulbotany.client.render.block_entity.RenderReturnPortal;
import velrondevs.masterfulbotany.client.render.block_entity.RenderRuneHolder;
import velrondevs.masterfulbotany.client.render.block_entity.RenderYggdrasilBranch;
import velrondevs.masterfulbotany.common.block.block_entity.TileAlfsteelPylon;
import velrondevs.masterfulbotany.common.block.block_entity.TileMjoellnir;
import velrondevs.masterfulbotany.common.block.block_entity.TileReturnPortal;
import velrondevs.masterfulbotany.common.block.block_entity.TileRuneHolder;
import velrondevs.masterfulbotany.common.block.block_entity.TileYggdrasilBranch;
import velrondevs.masterfulbotany.common.block.block_entity.flower.FunctionalFlowerBase;

/**
 * Client-only renderer registration. The common block classes call these methods without naming
 * any client class in their own bytecode, so a dedicated server can load them safely.
 */
public final class MasterfulClientSetup {

    private MasterfulClientSetup() {}

    public static void alfsteelPylon(BlockEntityType<TileAlfsteelPylon> type) {
        BlockEntityRenderers.register(type, RenderAlfsteelPylon::new);
    }

    public static void mjoellnir(BlockEntityType<TileMjoellnir> type) {
        BlockEntityRenderers.register(type, mgr -> new RenderMjoellnir());
    }

    public static void returnPortal(BlockEntityType<TileReturnPortal> type) {
        BlockEntityRenderers.register(type, mgr -> new RenderReturnPortal());
    }

    public static <T extends TileRuneHolder> void runeHolder(BlockEntityType<T> type) {
        BlockEntityRenderers.register(type, mgr -> new RenderRuneHolder());
    }

    public static void yggdrasilBranch(BlockEntityType<TileYggdrasilBranch> type) {
        BlockEntityRenderers.register(type, mgr -> new RenderYggdrasilBranch());
    }

    public static <T extends FunctionalFlowerBase> void functionalFlower(BlockEntityType<T> type) {
        BlockEntityRenderers.register(type, mgr -> new RenderFunctionalFlower<>());
    }
}
