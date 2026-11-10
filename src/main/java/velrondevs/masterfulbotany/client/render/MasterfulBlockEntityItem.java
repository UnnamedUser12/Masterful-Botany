package velrondevs.masterfulbotany.client.render;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import velrondevs.masterfulbotany.MasterfulBotany;
import velrondevs.masterfulbotany.client.render.block_entity.RenderAlfsteelPylon;
import velrondevs.masterfulbotany.registry.ModBlocks;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import velrondevs.botania.client.render.BlockEntityItemRendererHelper;

public class MasterfulBlockEntityItem {

    public static void register(RegisterSpecialModelRendererEvent event) {
        event.register(MasterfulBotany.getInstance().id("block_entity"), Unbaked.MAP_CODEC);
    }

    public record Unbaked(Block block) implements SpecialModelRenderer.Unbaked<ItemStack> {

        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(Unbaked::block)
        ).apply(instance, Unbaked::new));

        @Nullable
        @Override
        public SpecialModelRenderer<ItemStack> bake(SpecialModelRenderer.BakingContext context) {
            if (this.block == ModBlocks.alfsteelPylon) {
                return new BlockEntityItemRendererHelper(new RenderAlfsteelPylon.ItemRenderer(this.block));
            }
            return null;
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
