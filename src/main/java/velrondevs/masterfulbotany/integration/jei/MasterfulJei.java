package velrondevs.masterfulbotany.integration.jei;

import java.util.Objects;
import javax.annotation.Nonnull;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.ISubtypeRegistration;
import velrondevs.botania.common.item.equipment.tool.terrasteel.TerraShattererItem;
import velrondevs.botania.xplat.XplatAbstractions;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import velrondevs.masterfulbotany.registry.ModBlocks;
import velrondevs.masterfulbotany.registry.ModItems;
import velrondevs.masterfulbotany.registry.ModRecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class MasterfulJei implements IModPlugin {

    @Nonnull
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath("masterfulbotany", "jeiplugin");
    }

    @Override
    public void registerItemSubtypes(@Nonnull ISubtypeRegistration registration) {
        // The alfsteel pick and the greatest mana band have creative tab variants that only differ by their NBT data
        // (mana, tipped). Without a subtype interpreter JEI would treat them as duplicates of the same item.
        registration.registerSubtypeInterpreter(ModItems.alfsteelPick, new ISubtypeInterpreter<ItemStack>() {
            @Override
            public Object getSubtypeData(ItemStack stack, UidContext context) {
                return this.getLegacyStringSubtypeInfo(stack, context);
            }

            @Override
            public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) {
                if (context == UidContext.Recipe) {
                    return String.valueOf(TerraShattererItem.isTipped(stack));
                }
                return TerraShattererItem.getMana_(stack) + String.valueOf(TerraShattererItem.isTipped(stack));
            }
        });
        registration.registerSubtypeInterpreter(ModItems.manaRingGreatest, new ISubtypeInterpreter<ItemStack>() {
            @Override
            public Object getSubtypeData(ItemStack stack, UidContext context) {
                return this.getLegacyStringSubtypeInfo(stack, context);
            }

            @Override
            public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) {
                return String.valueOf(XplatAbstractions.INSTANCE.findManaItem(stack).getMana());
            }
        });
    }

    @Override
    public void registerCategories(@Nonnull IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new InfusionCategory(registration.getJeiHelpers().getGuiHelper()),
                new RuneRitualCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(@Nonnull IRecipeRegistration registration) {
        ClientLevel level = Minecraft.getInstance().level;
        RecipeManager recipes = Objects.requireNonNull(level).getRecipeManager();

        registration.addRecipes(InfusionCategory.TYPE, recipes.getAllRecipesFor(ModRecipes.infuser).stream().map(net.minecraft.world.item.crafting.RecipeHolder::value).toList());
        registration.addRecipes(RuneRitualCategory.TYPE, recipes.getAllRecipesFor(ModRecipes.runeRitual).stream().map(net.minecraft.world.item.crafting.RecipeHolder::value).toList());
    }

    @Override
    public void registerRecipeCatalysts(@Nonnull IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.manaInfuser), InfusionCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.centralRuneHolder), RuneRitualCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.runeHolder), RuneRitualCategory.TYPE);
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime runtime) {
        LittleBoxItemRenderer.setParent(runtime.getIngredientManager().getIngredientRenderer(VanillaTypes.ITEM_STACK));
    }
}
