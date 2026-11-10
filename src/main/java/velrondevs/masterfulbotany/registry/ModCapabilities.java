package velrondevs.masterfulbotany.registry;

import java.util.List;
import velrondevs.masterfulbotany.common.block.block_entity.BlockEntityMana;
import velrondevs.masterfulbotany.common.block.block_entity.flower.FunctionalFlowerBase;
import velrondevs.masterfulbotany.common.block.block_entity.TileManaInfuser;
import velrondevs.masterfulbotany.common.block.block_entity.TileRuneHolder;
import velrondevs.masterfulbotany.common.block.block_entity.TileYggdrasilBranch;
import velrondevs.masterfulbotany.common.block.flower.BlockFunctionalFlower;
import velrondevs.masterfulbotany.common.item.equipment.bauble.GreatestManaRing;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import velrondevs.botania.api.block.WandHUD;
import velrondevs.botania.api.BotaniaCapabilities;
import velrondevs.botania.api.BotaniaClientCapabilities;
import velrondevs.botania.common.item.equipment.tool.terrasteel.TerraShattererItem;

public class ModCapabilities {

    public static void register(RegisterCapabilitiesEvent event) {
        for (BlockEntityType<? extends BlockEntityMana> type : List.of(ModBlocks.manaCollector.getBlockEntityType(), ModBlocks.alfsteelPylon.getBlockEntityType(), ModBlocks.yggdrasilBranch.getBlockEntityType())) {
            event.registerBlockEntity(BotaniaCapabilities.MANA_RECEIVER, type, (be, side) -> be);
            event.registerBlockEntity(BotaniaCapabilities.WANDABLE, type, (be, ctx) -> be);
            event.registerBlockEntity(BotaniaCapabilities.SPARK_ATTACHABLE, type, (be, side) -> be.isSparkAttachable() ? be : null);
        }
        
        BlockEntityType<TileManaInfuser> infuser = ModBlocks.manaInfuser.getBlockEntityType();
        event.registerBlockEntity(BotaniaCapabilities.MANA_RECEIVER, infuser, (be, side) -> be);
        event.registerBlockEntity(BotaniaCapabilities.SPARK_ATTACHABLE, infuser, (be, side) -> be);

        for (BlockFunctionalFlower<? extends FunctionalFlowerBase> flower : ModBlocks.functionalFlowers()) {
            event.registerBlockEntity(BotaniaCapabilities.WANDABLE, flower.getBlockEntityType(), (be, ctx) -> be);
            event.registerBlockEntity(BotaniaCapabilities.WANDABLE, flower.getFloatingBlock().getBlockEntityType(), (be, ctx) -> be);
        }

        BlockEntityType<TileYggdrasilBranch> branch = ModBlocks.yggdrasilBranch.getBlockEntityType();
        event.registerBlockEntity(Capabilities.Item.BLOCK, branch, (be, side) -> be.getInventory());
        BlockEntityType<TileRuneHolder> runeHolder = ModBlocks.runeHolder.getBlockEntityType();
        event.registerBlockEntity(Capabilities.Item.BLOCK, runeHolder, (be, side) -> be.getInventory());
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlocks.centralRuneHolder.getBlockEntityType(), (be, side) -> be.getInventory());

        event.registerItem(BotaniaCapabilities.MANA_ITEM, (stack, ctx) -> new GreatestManaRing.GreatestManaItemImpl(stack), ModItems.manaRingGreatest);
        event.registerItem(BotaniaCapabilities.MANA_ITEM, (stack, ctx) -> new TerraShattererItem.ManaItemImpl(stack), ModItems.alfsteelPick);
    }

    public static void registerClient(RegisterCapabilitiesEvent event) {
        for (BlockEntityType<? extends BlockEntityMana> type : List.of(ModBlocks.manaCollector.getBlockEntityType(), ModBlocks.alfsteelPylon.getBlockEntityType(), ModBlocks.yggdrasilBranch.getBlockEntityType())) {
            event.registerBlockEntity(BotaniaClientCapabilities.WAND_HUD, type, (be, ctx) -> (WandHUD) be);
        }
        for (BlockFunctionalFlower<? extends FunctionalFlowerBase> flower : ModBlocks.functionalFlowers()) {
            event.registerBlockEntity(BotaniaClientCapabilities.WAND_HUD, flower.getBlockEntityType(), (be, ctx) -> (WandHUD) be);
            event.registerBlockEntity(BotaniaClientCapabilities.WAND_HUD, flower.getFloatingBlock().getBlockEntityType(), (be, ctx) -> (WandHUD) be);
        }
    }
}
