package velrondevs.masterfulbotany.datagen.util;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import velrondevs.masterfulbotany.MasterfulBotany;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class FloatingFlowerModelBuilder extends BlockModelBuilder {

    private ResourceLocation flower = null;

    public FloatingFlowerModelBuilder(ResourceLocation outputLocation, ExistingFileHelper existingFileHelper) {
        super(outputLocation, existingFileHelper);
    }

    public static FloatingFlowerModelBuilder create(BlockModelProvider provider, String path) {
        Preconditions.checkNotNull(path, "Path must not be null");
        ResourceLocation outputLoc = MasterfulBotany.getInstance().resource("block/" + path);
        return (FloatingFlowerModelBuilder) provider.generatedModels.computeIfAbsent(outputLoc, rl -> new FloatingFlowerModelBuilder(rl, provider.existingFileHelper));
    }

    public FloatingFlowerModelBuilder flower(ResourceLocation flower) {
        if (flower.getPath().startsWith("block/")) {
            this.flower = flower;
        } else {
            this.flower = ResourceLocation.fromNamespaceAndPath(flower.getNamespace(), "block/" + flower.getPath());
        }
        return this;
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = super.toJson();
        json.addProperty("loader", "botania:floating_flower");
        if (this.flower != null) {
            JsonObject flowerObj = new JsonObject();
            flowerObj.addProperty("parent", this.flower.toString());
            flowerObj.addProperty("render_type", this.renderType);
            json.add("flower", flowerObj);
        }
        return json;
    }
}
