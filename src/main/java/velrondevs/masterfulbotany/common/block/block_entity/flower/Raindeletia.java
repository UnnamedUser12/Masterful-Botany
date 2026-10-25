package velrondevs.masterfulbotany.common.block.block_entity.flower;

import velrondevs.masterfulbotany.config.MasterfulConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import velrondevs.botania.registry.BotaniaBlocks;

public class Raindeletia extends FunctionalFlowerBase {

    public Raindeletia(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 0x1E1CD8, true);
    }

    @Override
    protected void tickFlower() {
        //noinspection ConstantConditions
        if (!this.level.isClientSide) {
            float multiplier = 0;
            if (this.level.isRainingAt(this.worldPosition)) {
                multiplier = this.level.isThundering() ? MasterfulConfig.flowers.raindeletia.thunder : MasterfulConfig.flowers.raindeletia.rain;
            }
            if (this.level.getBlockState(this.worldPosition.below()).getBlock() == BotaniaBlocks.enchantedSoil) {
                multiplier = multiplier * MasterfulConfig.flowers.raindeletia.enchanted_soil;
            } else if (this.level.getBlockState(this.worldPosition.below()).getBlock() == BotaniaBlocks.vividGrass) {
                multiplier = multiplier * MasterfulConfig.flowers.raindeletia.vivid_grass;
            }  else if (this.level.getBlockState(this.worldPosition.below()).getBlock() == BotaniaBlocks.dryGrass) {
                multiplier = multiplier * MasterfulConfig.flowers.raindeletia.dry_grass;
            }

            int manaTransfer = Math.round(multiplier * MasterfulConfig.flowers.raindeletia.base);
            this.mana = Mth.clamp(this.mana + manaTransfer, 0, this.maxMana);
            this.didWork = true;
        }
    }
}
