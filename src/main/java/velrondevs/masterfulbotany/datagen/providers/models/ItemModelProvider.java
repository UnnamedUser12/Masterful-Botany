package velrondevs.masterfulbotany.datagen.providers.models;

import velrondevs.masterfulbotany.common.block.flower.BlockFloatingFunctionalFlower;
import velrondevs.masterfulbotany.common.block.flower.BlockFunctionalFlower;
import velrondevs.masterfulbotany.registry.ModBlocks;
import velrondevs.masterfulbotany.registry.ModItems;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.model.ItemModelProviderBase;

public class ItemModelProvider extends ItemModelProviderBase {

    private static final Identifier ISLAND = Identifier.fromNamespaceAndPath("botania", "block/islands/island_grass");

    public ItemModelProvider(DatagenContext ctx) {
        super(ctx);
    }

    @Override
    protected void setup() {
        this.handheld(ModItems.alfsteelSword);
        this.manualModel(ModItems.alfsteelPick);
        this.manualModel(ModItems.alfsteelAxe);
        this.manualModel(ModItems.fadedNetherStar);
        this.manualModel(ModBlocks.alfsteelPylon.asItem());
        this.specialBlock(ModBlocks.yggdrasilBranch);
    }

    @Override
    protected void defaultBlock(Identifier id, BlockItem item, ItemModelGenerators itemModels) {
        if (item.getBlock() instanceof BlockFunctionalFlower<?>) {
            Identifier model = ModelTemplates.FLAT_ITEM.create(
                    Identifier.fromNamespaceAndPath(id.getNamespace(), "item/" + id.getPath()),
                    TextureMapping.layer0(this.material(Identifier.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath()))),
                    itemModels.modelOutput
            );
            itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(model));
        } else if (item.getBlock() instanceof BlockFloatingFunctionalFlower<?>) {
            itemModels.itemModelOutput.accept(item, ItemModelUtils.composite(
                    ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath())),
                    ItemModelUtils.plainModel(ISLAND)
            ));
        } else {
            super.defaultBlock(id, item, itemModels);
        }
    }
}
