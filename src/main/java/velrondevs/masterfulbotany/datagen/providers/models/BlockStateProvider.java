package velrondevs.masterfulbotany.datagen.providers.models;

import java.util.Optional;
import velrondevs.masterfulbotany.common.block.flower.BlockFloatingFunctionalFlower;
import velrondevs.masterfulbotany.common.block.flower.BlockFunctionalFlower;
import velrondevs.masterfulbotany.datagen.util.FloatingFlowerModelBuilder;
import velrondevs.masterfulbotany.registry.ModBlocks;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.model.BlockStateProviderBase;

public class BlockStateProvider extends BlockStateProviderBase {

    private static final TextureSlot CROSS = TextureSlot.create("cross");
    private static final ModelTemplate CROSS_TEMPLATE = new ModelTemplate(Optional.of(Identifier.fromNamespaceAndPath("botania", "block/shapes/cross")), Optional.empty(), CROSS);

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
        this.manualModel(ModBlocks.returnPortal, this.createBlockModel(
                blockModelId(ModBlocks.returnPortal),
                ModelTemplates.PARTICLE_ONLY,
                TextureMapping.particle(this.material(Identifier.withDefaultNamespace("block/glass")))
        ));
        this.manualModel(ModBlocks.manaCollector, this.createBlockModel(
                blockModelId(ModBlocks.manaCollector),
                ModelTemplates.CUBE_BOTTOM_TOP,
                new TextureMapping()
                        .put(TextureSlot.SIDE, this.material(this.mod.id("block/mana_collector_side")))
                        .put(TextureSlot.BOTTOM, this.material(this.mod.id("block/mana_collector_bottom")))
                        .put(TextureSlot.TOP, this.material(this.mod.id("block/mana_collector_top")))
        ));
    }

    @Override
    protected Identifier defaultModel(Identifier id, Block block) {
        if (block instanceof BlockFunctionalFlower<?>) {
            return this.createBlockModel(blockModelId(id), CROSS_TEMPLATE, new TextureMapping().put(CROSS, this.material(textureId(id))));
        } else if (block instanceof BlockFloatingFunctionalFlower<?> floating) {
            Identifier flowerId = BuiltInRegistries.BLOCK.getKey(floating.getNonFloatingBlock());
            return FloatingFlowerModelBuilder.create(this.models().modelOutput, blockModelId(id), blockModelId(flowerId));
        } else {
            return super.defaultModel(id, block);
        }
    }
}
