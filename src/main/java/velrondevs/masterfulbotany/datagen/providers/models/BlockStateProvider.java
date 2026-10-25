package velrondevs.masterfulbotany.datagen.providers.models;

import java.util.Objects;
import velrondevs.masterfulbotany.common.block.flower.BlockFloatingFunctionalFlower;
import velrondevs.masterfulbotany.common.block.flower.BlockFunctionalFlower;
import velrondevs.masterfulbotany.datagen.util.FloatingFlowerModelBuilder;
import velrondevs.masterfulbotany.registry.ModBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.ModelFile.UncheckedModelFile;
import net.minecraft.core.registries.BuiltInRegistries;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.model.BlockStateProviderBase;

public class BlockStateProvider extends BlockStateProviderBase {

    public BlockStateProvider(DatagenContext ctx) {
        super(ctx);
    }

    @Override
    protected void setup() {
        this.manualModel(ModBlocks.manaInfuser);
        this.manualModel(ModBlocks.alfsteelPylon);
        this.manualModel(ModBlocks.yggdrasilBranch);
        this.manualModel(ModBlocks.runeHolder);
        this.manualModel(ModBlocks.centralRuneHolder);
        this.manualModel(ModBlocks.mjoellnir);
        this.manualModel(ModBlocks.returnPortal, this.models().getBuilder(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(ModBlocks.returnPortal)).getPath())
                .texture("particle", blockTexture(Blocks.GLASS)));
        this.manualModel(ModBlocks.manaCollector, this.models().cubeBottomTop(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(ModBlocks.manaCollector)).getPath(),
                this.mod.resource("block/mana_collector_side"),
                this.mod.resource("block/mana_collector_bottom"),
                this.mod.resource("block/mana_collector_top")
        ));
    }

    @Override
    protected ModelFile defaultModel(ResourceLocation id, Block block) {
        if (block instanceof BlockFunctionalFlower<?>) {
            return this.models().getBuilder(id.getPath()).parent(new UncheckedModelFile(ResourceLocation.fromNamespaceAndPath("botania", "block/shapes/cross")))
                    .renderType(RenderTypes.CUTOUT_MIPPED)
                    .texture("cross", ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath()));
        } else if (block instanceof BlockFloatingFunctionalFlower<?>) {
            //noinspection ConstantConditions
            return FloatingFlowerModelBuilder.create(this.models(), id.getPath())
                    .flower(BuiltInRegistries.BLOCK.getKey(((BlockFloatingFunctionalFlower<?>) block).getNonFloatingBlock()))
                    .renderType(RenderTypes.CUTOUT_MIPPED)
                    .parent(new UncheckedModelFile(ResourceLocation.fromNamespaceAndPath("minecraft", "block/block")));
        } else {
            return super.defaultModel(id, block);
        }
    }
}
