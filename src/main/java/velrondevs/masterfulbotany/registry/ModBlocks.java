package velrondevs.masterfulbotany.registry;

import velrondevs.masterfulbotany.common.block.block_entity.flower.Aquapanthus;
import velrondevs.masterfulbotany.common.block.block_entity.flower.Exoblaze;
import velrondevs.masterfulbotany.common.block.block_entity.flower.Feysythia;
import velrondevs.masterfulbotany.common.block.block_entity.flower.FunctionalFlowerBase;
import velrondevs.masterfulbotany.common.block.block_entity.flower.Hellebore;
import velrondevs.masterfulbotany.common.block.block_entity.flower.Petrunia;
import velrondevs.masterfulbotany.common.block.block_entity.flower.Raindeletia;
import velrondevs.masterfulbotany.common.block.block_entity.flower.WitherAconite;
import velrondevs.masterfulbotany.common.block.block_entity.TileAlfsteelPylon;
import velrondevs.masterfulbotany.common.block.block_entity.TileCentralRuneHolder;
import velrondevs.masterfulbotany.common.block.block_entity.TileManaCollector;
import velrondevs.masterfulbotany.common.block.block_entity.TileManaInfuser;
import velrondevs.masterfulbotany.common.block.block_entity.TileReturnPortal;
import velrondevs.masterfulbotany.common.block.block_entity.TileRuneHolder;
import velrondevs.masterfulbotany.common.block.block_entity.TileYggdrasilBranch;
import velrondevs.masterfulbotany.common.block.BlockAlfheimLeaves;
import velrondevs.masterfulbotany.common.block.BlockAlfsteelPylon;
import velrondevs.masterfulbotany.common.block.BlockCentralRuneHolder;
import velrondevs.masterfulbotany.common.block.BlockManaInfuser;
import velrondevs.masterfulbotany.common.block.BlockMjoellnir;
import velrondevs.masterfulbotany.common.block.BlockReturnPortal;
import velrondevs.masterfulbotany.common.block.BlockRuneHolder;
import velrondevs.masterfulbotany.common.block.BlockYggdrasilBranch;
import velrondevs.masterfulbotany.common.block.flower.BlockFunctionalFlower;
import velrondevs.masterfulbotany.MasterfulBotany;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import org.moddingx.libx.annotation.registration.RegisterClass;
import org.moddingx.libx.base.BlockBase;
import org.moddingx.libx.base.tile.BlockBE;
import velrondevs.botania.registry.BotaniaBlocks;

@RegisterClass(registry = "BLOCK")
public class ModBlocks {

    public static final BlockBE<TileManaInfuser> manaInfuser = new BlockManaInfuser(MasterfulBotany.getInstance(), TileManaInfuser.class, Properties.of().strength(20));
    public static final BlockBase alfsteelBlock = new BlockBase(MasterfulBotany.getInstance(), Properties.ofFullCopy(Blocks.EMERALD_BLOCK).strength(20).requiresCorrectToolForDrops());
    public static final BlockBE<TileAlfsteelPylon> alfsteelPylon = new BlockAlfsteelPylon(MasterfulBotany.getInstance(), Properties.ofFullCopy(BotaniaBlocks.naturaPylon).strength(5.5f).sound(SoundType.METAL).lightLevel((s) -> 7));
    public static final BlockBE<TileManaCollector> manaCollector = new BlockBE<>(MasterfulBotany.getInstance(), TileManaCollector.class, Properties.of().strength(5));
    public static final BlockFunctionalFlower<Exoblaze> exoblaze = new BlockFunctionalFlower<>(MasterfulBotany.getInstance(), Exoblaze.class, Properties.ofFullCopy(Blocks.RED_TULIP), false);
    public static final BlockFunctionalFlower<WitherAconite> witherAconite = new BlockFunctionalFlower<>(MasterfulBotany.getInstance(), WitherAconite.class, Properties.ofFullCopy(Blocks.RED_TULIP), true);
    public static final BlockFunctionalFlower<Aquapanthus> aquapanthus = new BlockFunctionalFlower<>(MasterfulBotany.getInstance(), Aquapanthus.class, Properties.ofFullCopy(Blocks.RED_TULIP), false);
    public static final BlockFunctionalFlower<Hellebore> hellebore = new BlockFunctionalFlower<>(MasterfulBotany.getInstance(), Hellebore.class, Properties.ofFullCopy(Blocks.RED_TULIP), false);
    public static final BlockFunctionalFlower<Raindeletia> raindeletia = new BlockFunctionalFlower<>(MasterfulBotany.getInstance(), Raindeletia.class, Properties.ofFullCopy(Blocks.RED_TULIP), true);
    public static final BlockFunctionalFlower<Feysythia> feysythia = new BlockFunctionalFlower<>(MasterfulBotany.getInstance(), Feysythia.class, Properties.ofFullCopy(Blocks.RED_TULIP), true);
    public static final BlockFunctionalFlower<Petrunia> petrunia = new BlockFunctionalFlower<>(MasterfulBotany.getInstance(), Petrunia.class, Properties.ofFullCopy(Blocks.RED_TULIP), false);
    public static final BlockBE<TileYggdrasilBranch> yggdrasilBranch = new BlockYggdrasilBranch(MasterfulBotany.getInstance(), Properties.of().strength(4).sound(SoundType.WOOD));
    public static final BlockBE<TileRuneHolder> runeHolder = new BlockRuneHolder<>(MasterfulBotany.getInstance(), TileRuneHolder.class, Properties.ofFullCopy(Blocks.IRON_BARS).strength(3).sound(SoundType.METAL));
    public static final BlockBE<TileCentralRuneHolder> centralRuneHolder = new BlockCentralRuneHolder(MasterfulBotany.getInstance(), Properties.ofFullCopy(Blocks.IRON_BARS).strength(3).sound(SoundType.METAL));
    public static final BlockMjoellnir mjoellnir = new BlockMjoellnir(MasterfulBotany.getInstance(), Properties.of().strength(-1.0F, 3600000.0F).noLootTable(), new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static final Block dreamwoodLeaves = new BlockAlfheimLeaves(MasterfulBotany.getInstance());
    public static final BlockBase elementiumOre = new BlockBase(MasterfulBotany.getInstance(), BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE).strength(4, 4).requiresCorrectToolForDrops());
    public static final BlockBase dragonstoneOre = new BlockBase(MasterfulBotany.getInstance(),BlockBehaviour.Properties.ofFullCopy(Blocks.DIAMOND_ORE).strength(5, 5).requiresCorrectToolForDrops());
    public static final BlockBase goldOre = new BlockBase(MasterfulBotany.getInstance(), BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_ORE).strength(3, 3).requiresCorrectToolForDrops());
    public static final BlockBase rawElementiumBlock = new BlockBase(MasterfulBotany.getInstance(), Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK).requiresCorrectToolForDrops());
    public static final BlockBE<TileReturnPortal> returnPortal = new BlockReturnPortal(MasterfulBotany.getInstance(), BlockBehaviour.Properties.of().noCollission().noLootTable().strength(-1.0F).sound(SoundType.GLASS).lightLevel(state -> 4));

    public static java.util.List<BlockFunctionalFlower<? extends velrondevs.masterfulbotany.common.block.block_entity.flower.FunctionalFlowerBase>> functionalFlowers() {
        return java.util.List.of(exoblaze, witherAconite, aquapanthus, hellebore, raindeletia, feysythia, petrunia);
    }
}
