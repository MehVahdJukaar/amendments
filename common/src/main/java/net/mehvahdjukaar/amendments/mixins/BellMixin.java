package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.common.IBellConnection;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.integration.SuppCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BellBlock.class)
public abstract class BellMixin extends Block {

    protected BellMixin(Properties properties) {
        super(properties);
    }


    //for bells
    @Unique
    public boolean amendments$tryConnect(BlockPos pos, BlockState facingState, LevelAccessor world) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof IBellConnection bc) {
            IBellConnection.Type connection = IBellConnection.Type.NONE;
            if (facingState.getBlock() instanceof ChainBlock && facingState.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y)
                connection = IBellConnection.Type.CHAIN;
            else if (CompatHandler.SUPPLEMENTARIES && SuppCompat.isRope(facingState.getBlock()))
                connection = IBellConnection.Type.ROPE;
            bc.amendments$setConnected(connection);
            te.setChanged();
            return true;
        }
        return false;
    }

    @Inject(method = "updateShape", at = @At("HEAD"))
    public void updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn,
                            BlockPos currentPos, BlockPos facingPos, CallbackInfoReturnable<BlockState> info) {
        try {
            if (facing == Direction.DOWN && this.amendments$tryConnect(currentPos, facingState, worldIn)) {
                if (worldIn instanceof Level level)
                    level.sendBlockUpdated(currentPos, stateIn, stateIn, Block.UPDATE_CLIENTS);

            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(worldIn, pos, state, placer, stack);
        this.amendments$tryConnect(pos, worldIn.getBlockState(pos.below()), worldIn);
    }
}