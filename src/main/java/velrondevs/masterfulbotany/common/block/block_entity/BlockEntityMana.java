package velrondevs.masterfulbotany.common.block.block_entity;

import com.google.common.base.Predicates;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.moddingx.libx.base.tile.BlockEntityBase;
import org.moddingx.libx.LibX;
import velrondevs.botania.api.block.Wandable;
import velrondevs.botania.api.block.WandHUD;
import velrondevs.botania.api.BotaniaAPIClient;
import velrondevs.botania.api.mana.ManaReceiver;
import velrondevs.botania.api.mana.spark.ManaSpark;
import velrondevs.botania.api.mana.spark.SparkAttachable;
import velrondevs.botania.client.core.helper.RenderHelper;

public abstract class BlockEntityMana extends BlockEntityBase implements SparkAttachable, ManaReceiver, WandHUD, Wandable {

    public final int maxMana;
    private final boolean bursts;
    private final boolean sparks;

    protected int mana;

    public BlockEntityMana(BlockEntityType<?> type, BlockPos pos, BlockState state, int maxMana, boolean bursts, boolean sparks) {
        super(type, pos, state);
        this.maxMana = maxMana;
        this.bursts = bursts;
        this.sparks = sparks;
    }

    protected abstract boolean canReceive();

    public boolean isSparkAttachable() {
        return this.sparks;
    }

    @Override
    public boolean canAttachSpark(ItemStack itemStack) {
        return this.sparks;
    }

    @Override
    public int getAvailableSpaceForMana() {
        return Mth.clamp(this.maxMana - this.mana, 0, this.maxMana);
    }

    @Override
    public ManaSpark getAttachedSpark() {
        @SuppressWarnings("ConstantConditions")
        List<Entity> sparks = this.level.getEntitiesOfClass(Entity.class, new AABB(Vec3.atLowerCornerOf(this.worldPosition.above()), Vec3.atLowerCornerOf(this.worldPosition.above().offset(1, 1, 1))), Predicates.instanceOf(ManaSpark.class));
        if (sparks.size() == 1) {
            Entity e = sparks.get(0);
            return (ManaSpark) e;
        } else {
            return null;
        }
    }

    @Override
    public boolean areIncomingTranfersDone() {
        return this.mana < this.maxMana && this.canReceive();
    }

    @Override
    public boolean isFull() {
        return this.mana >= this.maxMana;
    }

    @Override
    public void receiveMana(int i) {
        this.mana = Mth.clamp(this.mana + i, 0, this.maxMana);
        this.onManaChange();
        this.setChanged();
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return this.bursts;
    }

    @Override
    public int getCurrentMana() {
        return Mth.clamp(this.mana, 0, this.maxMana);
    }

    @Override
    protected void loadAdditional(@Nonnull ValueInput input) {
        super.loadAdditional(input);
        this.mana = Mth.clamp(input.getIntOr("mana", 0), 0, this.maxMana);
    }

    @Override
    protected void saveAdditional(@Nonnull ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("mana", Mth.clamp(this.mana, 0, this.maxMana));
    }

    @Override
    public void renderHUD(GuiGraphicsExtractor graphics, Minecraft minecraft) {
        String name = I18n.get(this.getBlockState().getBlock().getDescriptionId());

        int centerX = minecraft.getWindow().getGuiScaledWidth() / 2;
        int centerY = minecraft.getWindow().getGuiScaledHeight() / 2;

        int width = Math.max(102, minecraft.font.width(name)) + 4;

        RenderHelper.renderHUDBox(graphics, centerX - width / 2, centerY + 8, centerX + width / 2, centerY + 30);
        BotaniaAPIClient.instance().drawSimpleManaHUD(graphics, this.getManaColor(), this.getCurrentMana(), this.maxMana, name);
    }

    @Override
    public boolean onUsedByWand(@Nullable Player player, ItemStack itemStack, Direction direction) {
        if (this.level != null && this.level.isClientSide()) {
            LibX.getNetwork().requestBE(this.level, this.worldPosition);
        }
        return true;
    }
    
    protected void onManaChange() {

    }
    
    protected int getManaColor() {
        return 0x0000FF;
    }

    @Override
    public Level getManaReceiverLevel() {
        return this.getLevel();
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return this.getBlockPos();
    }
}
