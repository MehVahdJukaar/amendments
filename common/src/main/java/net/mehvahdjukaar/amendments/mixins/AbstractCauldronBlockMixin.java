package net.mehvahdjukaar.amendments.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.mehvahdjukaar.amendments.common.block.BoilingWaterCauldronBlock;
import net.mehvahdjukaar.amendments.common.block.CommonCauldronCode;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.events.behaviors.CauldronConversion;
import net.mehvahdjukaar.moonlight.api.fluids.MLBuiltinSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractCauldronBlock.class)
public class AbstractCauldronBlockMixin extends Block {

    public AbstractCauldronBlockMixin(Properties properties) {
        super(properties);
    }

    // why is this not an event? because it's better for compatibility
    @ModifyReturnValue(method = "useItemOn", at = @At("RETURN"))
    public ItemInteractionResult use(ItemInteractionResult original, ItemStack stack, BlockState state,
                                     Level level, BlockPos pos, Player player,
                                     InteractionHand hand, BlockHitResult hitResult) {
        // do something
        if (original == ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION && this == Blocks.CAULDRON && CommonConfigs.LIQUID_CAULDRON.get()) {
            return CauldronConversion.convert(state, pos, level, player, hand, stack, false);
        }
        //for convert interaction to water cauldron from normal one
        BlockState newState = level.getBlockState(pos);
        if (newState.getBlock() instanceof BoilingWaterCauldronBlock) {
            BlockPos belowPos = pos.below();
            boolean isFire = CommonCauldronCode.shouldBoil(level.getBlockState(belowPos),
                    SoftFluidStack.of(MLBuiltinSoftFluids.WATER.getHolder(level)), level, belowPos);
            if (isFire) {
                level.setBlockAndUpdate(pos, newState.setValue(BoilingWaterCauldronBlock.BOILING, true));
            }
        }

        return original;
    }

}
