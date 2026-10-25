package velrondevs.masterfulbotany.common.block.block_entity;

import java.util.List;
import velrondevs.masterfulbotany.common.pylon.PylonRepairable;
import velrondevs.masterfulbotany.common.pylon.PylonRepairables;
import velrondevs.masterfulbotany.MasterfulBotany;
import velrondevs.masterfulbotany.network.PylonMessage;
import velrondevs.masterfulbotany.registry.ModCriteria;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.moddingx.libx.base.tile.TickingBlock;

public class TileAlfsteelPylon extends BlockEntityMana implements TickingBlock {

    public static final int MAX_MANA = 100000;

    public TileAlfsteelPylon(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, MAX_MANA, true, false);
    }

    @Override
    protected boolean canReceive() {
        return true;
    }

    @Override
    public void tick() {
        //noinspection ConstantConditions
        if (!this.level.isClientSide) {
            List<ItemEntity> items = this.getItems();
            if (items.size() == 1) {
                ItemEntity item = items.get(0);
                ItemStack stack = item.getItem();
                PylonRepairable repairable = PylonRepairables.getRepairInfo(stack);
                if (repairable != null && stack.getCount() == 1) {
                    int manaCost = repairable.getRepairManaPerTick(stack);
                    if (this.mana >= manaCost) {
                        if (item.getOwner() instanceof ServerPlayer player) {
                            ModCriteria.ALF_REPAIR.trigger(player, stack);
                        }
                        this.mana -= manaCost;
                        stack = repairable.repairOneTick(stack);
                        item.setItem(stack);
                        if (stack.getDamageValue() > 0) {
                            MasterfulBotany.getNetwork().setItemMagnetImmune(item);
                        } else {
                            MasterfulBotany.getNetwork().removeItemMagnetImmune(item);
                        }
                        this.setChanged();
                        PacketDistributor.sendToPlayersTrackingChunk((net.minecraft.server.level.ServerLevel) this.level, new net.minecraft.world.level.ChunkPos(this.worldPosition), new PylonMessage(this.worldPosition));
                    }
                }
            }
        }
    }

    private List<ItemEntity> getItems() {
        //noinspection ConstantConditions
        return this.level.getEntitiesOfClass(ItemEntity.class, new AABB(Vec3.atLowerCornerOf(this.worldPosition.offset(0, 1, 0)), Vec3.atLowerCornerOf(this.worldPosition.offset(1, 2, 1))));
    }

    @Override
    protected int getManaColor() {
        return 0xEE7C00;
    }
}
