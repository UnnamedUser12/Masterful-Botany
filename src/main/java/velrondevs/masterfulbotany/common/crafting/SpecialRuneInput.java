package velrondevs.masterfulbotany.common.crafting;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

public abstract class SpecialRuneInput {
    
    public final Identifier id;

    protected SpecialRuneInput(Identifier id) {
        this.id = id;
    }
    
    public abstract Either<MutableComponent, CompoundTag> apply(Level world, BlockPos pos, RuneRitualRecipe recipe);
    
    public abstract void cancel(Level world, BlockPos pos, RuneRitualRecipe recipe, CompoundTag nbt);
    
    public List<Ingredient> getJeiInputItems() {
        return ImmutableList.of();
    }
}
