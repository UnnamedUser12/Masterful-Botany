package velrondevs.masterfulbotany.common.item.equipment;

import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import velrondevs.masterfulbotany.common.pylon.PylonRepairable;
import velrondevs.masterfulbotany.config.MasterfulConfig;
import velrondevs.masterfulbotany.MasterfulBotany;
import velrondevs.masterfulbotany.registry.ModItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.moddingx.libx.creativetab.CreativeTabItemProvider;
import org.moddingx.libx.creativetab.CreativeTabX;
import org.moddingx.libx.registration.Registerable;
import org.moddingx.libx.registration.SetupContext;
import velrondevs.botania.common.helper.PlayerHelper;
import velrondevs.botania.common.item.equipment.tool.terrasteel.TerraShattererItem;
import velrondevs.botania.common.item.equipment.tool.ToolCommons;
import velrondevs.botania.common.item.relic.RingOfThorItem;
import velrondevs.botania.common.item.StoneOfTemperanceItem;
import velrondevs.botania.common.lib.BotaniaTags;
import velrondevs.botania.common.lib.ResourceLocationHelper;

public class AlfsteelPick extends TerraShattererItem implements PylonRepairable, Registerable, CreativeTabItemProvider {
    
    public AlfsteelPick(Properties props) {
        super(props.durability(MasterfulConfig.alftools.durability.pickaxe.max_durability()));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void setupClient(SetupContext ctx) {
        ctx.enqueue(() -> {
            ItemProperties.register(ModItems.alfsteelPick, MasterfulBotany.getInstance().resource("tipped"), (stack, level, entity, seed) -> isTipped(stack) ? 1 : 0);
            ItemProperties.register(ModItems.alfsteelPick, MasterfulBotany.getInstance().resource("active"), (stack, level, entity, seed) -> isEnabled(stack) ? 1 : 0);
        });
    }

    @Override
    public int getManaPerDamage() {
        return MasterfulConfig.alftools.durability.pickaxe.mana_per_durability();
    }

    @Override
    public void breakOtherBlock(Player player, ItemStack stack, BlockPos pos, BlockPos originPos, Direction side) {
        if (isEnabled(stack)) {
            Level level = player.level();
            BlockState state = level.getBlockState(pos);

            Predicate<BlockState> blockTest = (BlockState stateToMine) -> {
                if (stateToMine.requiresCorrectToolForDrops() && !stack.isCorrectToolForDrops(stateToMine)) return false;
                if (stack.getDestroySpeed(stateToMine) <= 0) return false;
                return stack.getDestroySpeed(stateToMine) > 1 || stateToMine.is(BlockTags.MINEABLE_WITH_PICKAXE) || stateToMine.is(BlockTags.MINEABLE_WITH_SHOVEL) || stateToMine.is(BlockTags.MINEABLE_WITH_HOE);
            };
            
            if (!level.isEmptyBlock(pos) && blockTest.test(state)) {
                if (!level.isEmptyBlock(pos)) {
                    boolean thor = !RingOfThorItem.getThorRing(player).isEmpty();
                    boolean doX = thor || side.getStepX() == 0;
                    boolean doY = thor || side.getStepY() == 0;
                    boolean doZ = thor || side.getStepZ() == 0;
                    int origLevel = getLevel(stack);
                    int miningLevel = origLevel + (thor ? 1 : 0);
                    int rangeDepth = miningLevel / 2;
                    if (StoneOfTemperanceItem.hasTemperanceActive(player) && miningLevel > 2) {
                        miningLevel = 2;
                        rangeDepth = 0;
                    }

                    int range = miningLevel - 1;
                    int rangeY = Math.max(1, range);
                    if (range != 0 || miningLevel == 1) {
                        Vec3i beginDiff = new Vec3i(doX ? -range : 0, doY ? -1 : 0, doZ ? -range : 0);
                        Vec3i endDiff = new Vec3i(doX ? range : rangeDepth * -side.getStepX(), doY ? rangeY * 2 - 1 : 0, doZ ? range : rangeDepth * -side.getStepZ());
                        ToolCommons.removeBlocksInIteration(player, stack, level, pos, beginDiff, endDiff, blockTest);
                        if (origLevel == 5) {
                            PlayerHelper.grantCriterion((ServerPlayer)player, ResourceLocationHelper.prefix("challenge/rank_ss_pick"), "code_triggered");
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean isValidRepairItem(@Nonnull ItemStack toRepair, @Nonnull ItemStack repair) {
        return repair.getItem() == ModItems.alfsteelIngot || (!Ingredient.of(BotaniaTags.Items.INGOTS_TERRASTEEL).test(repair) && super.isValidRepairItem(toRepair, repair));
    }

    @Override
    public int getRepairManaPerTick(ItemStack stack) {
        return (int) (2.5 * this.getManaPerDamage());
    }

    @Override
    public ItemStack repairOneTick(ItemStack stack) {
        stack.setDamageValue(Math.max(0, stack.getDamageValue() - 5));
        return stack;
    }

    @Override
    public int getEntityLifespan(ItemStack itemStack, Level level) {
        return Integer.MAX_VALUE;
    }

    @Override
    public Stream<ItemStack> makeCreativeTabStacks(CreativeTabX.TabContext ctx) {
        List<ItemStack> stacks = new ArrayList<>(8);
        stacks.add(new ItemStack(this));
        for (int mana : new int[] { 1000000 - 1, 10000000 - 1, 100000000 - 1, 1000000000 - 1, Integer.MAX_VALUE - 1 }) {
            ItemStack stack = new ItemStack(this);
            setMana(stack, mana);
            stacks.add(stack);
        }
        {
            ItemStack stack = new ItemStack(this);
            setMana(stack, 1000000 - 1);
            setTipped(stack);
            stacks.add(stack);
        }
        return stacks.stream();
    }
}
