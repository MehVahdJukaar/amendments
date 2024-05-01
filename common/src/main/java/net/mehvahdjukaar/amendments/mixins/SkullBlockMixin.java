package net.mehvahdjukaar.amendments.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = {"net.minecraft.world.level.block.SkullBlock", "net.minecraft.world.level.block.WallSkullBlock"})
public abstract class SkullBlockMixin extends AbstractSkullBlock implements SimpleWaterloggedBlock {

    public SkullBlockMixin(SkullBlock.Type type, Properties properties) {
        super(type, properties);
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
        if (!state.hasProperty(BlockStateProperties.WATERLOGGED)) return false;
        if (!state.getValue(BlockStateProperties.WATERLOGGED) && fluidState.getType() == Fluids.WATER) {
            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(BlockStateProperties.WATERLOGGED, true), 3);
                level.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(level));
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public ItemStack pickupBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        if (!state.hasProperty(BlockStateProperties.WATERLOGGED)) return ItemStack.EMPTY;
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            level.setBlock(pos, state.setValue(BlockStateProperties.WATERLOGGED, false), 3);
            if (!state.canSurvive(level, pos)) {
                level.destroyBlock(pos, true);
            }

            return new ItemStack(Items.WATER_BUCKET);
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void amendments$dangerousAddWaterlogging(SkullBlock.Type type, BlockBehaviour.Properties properties, CallbackInfo ci) {
        if (this.defaultBlockState().hasProperty(BlockStateProperties.WATERLOGGED)) {
            this.registerDefaultState(this.defaultBlockState()
                    .setValue(BlockStateProperties.WATERLOGGED, Boolean.FALSE));
        }
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return (state.hasProperty(BlockStateProperties.WATERLOGGED) &&
                state.getValue(BlockStateProperties.WATERLOGGED)) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.hasProperty(BlockStateProperties.WATERLOGGED) && stateIn.getValue(BlockStateProperties.WATERLOGGED)) {
            worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }

        return stateIn;
    }

    @ModifyReturnValue(method = "getStateForPlacement", at = @At("RETURN"))
    public BlockState amendments$addPlacementWaterlogging(BlockState original, BlockPlaceContext context) {
        if (original == null || !original.hasProperty(BlockStateProperties.WATERLOGGED)) return original;
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return original.setValue(BlockStateProperties.WATERLOGGED, fluidstate.is(FluidTags.WATER) && fluidstate.getAmount() == 8);
    }

    @Inject(method = "createBlockStateDefinition", at = @At("RETURN"))
    protected void amendments$addWaterlogging(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        if (!builder.properties.containsValue(BlockStateProperties.WATERLOGGED)) {
            builder.add(BlockStateProperties.WATERLOGGED);
        }
    }
}
