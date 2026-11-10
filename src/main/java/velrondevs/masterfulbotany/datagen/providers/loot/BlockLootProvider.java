package velrondevs.masterfulbotany.datagen.providers.loot;

import velrondevs.masterfulbotany.registry.ModBlocks;
import velrondevs.masterfulbotany.registry.ModItems;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.neoforged.neoforge.common.Tags;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.provider.loot.BlockLootProviderBase;
import velrondevs.botania.registry.BotaniaItems;

public class BlockLootProvider extends BlockLootProviderBase {

    private final HolderGetter<Item> itemLookup;

    public BlockLootProvider(DatagenContext ctx) {
        super(ctx);
        this.itemLookup = ctx.system().lookupProvider().join().lookupOrThrow(Registries.ITEM);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void setup() {
        this.drops(ModBlocks.elementiumOre, true, this.stack(ModItems.rawElementium).with(this.fortuneOres()));
        this.drops(ModBlocks.goldOre, true, this.stack(Items.RAW_GOLD).with(this.fortuneOres()));
        this.drops(ModBlocks.dragonstoneOre, true, this.stack(BotaniaItems.dragonstone).with(this.fortuneOres()));
        this.drops(ModBlocks.dreamwoodLeaves, this.first(
                this.element().with(this.or(this.silkCondition(), MatchTool.toolMatches(ItemPredicate.Builder.item().of(this.itemLookup, Tags.Items.TOOLS_SHEAR)))),
                this.combine(
                        this.stack(BotaniaItems.dreamwoodTwig).with(this.randomFortune(0.06f)).with(this.count(1, 2)),
                        this.stack(ModItems.dreamCherry).with(this.randomFortune(0.015f))
                )
        ));
    }
}
