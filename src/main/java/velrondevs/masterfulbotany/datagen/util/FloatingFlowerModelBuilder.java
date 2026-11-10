package velrondevs.masterfulbotany.datagen.util;

import com.google.gson.JsonObject;
import java.util.function.BiConsumer;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.resources.Identifier;

public class FloatingFlowerModelBuilder {

    private FloatingFlowerModelBuilder() {

    }

    public static Identifier create(BiConsumer<Identifier, ModelInstance> output, Identifier modelId, Identifier flowerModelId) {
        output.accept(modelId, () -> {
            JsonObject json = new JsonObject();
            json.addProperty("parent", "minecraft:block/block");
            json.addProperty("loader", "botania:floating_flower");
            JsonObject flower = new JsonObject();
            flower.addProperty("parent", flowerModelId.toString());
            json.add("flower", flower);
            JsonObject textures = new JsonObject();
            textures.addProperty("particle", flowerModelId.toString());
            json.add("textures", textures);
            return json;
        });
        return modelId;
    }
}
