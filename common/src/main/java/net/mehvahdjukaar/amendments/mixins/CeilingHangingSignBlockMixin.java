package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.common.IExtendedHangingSign;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//unused
@Mixin(CeilingHangingSignBlock.class)
public abstract class CeilingHangingSignBlockMixin extends Block implements EntityBlock {

    protected CeilingHangingSignBlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "updateShape", at = @At("HEAD"))
    public void updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> cir) {
        if (level.getBlockEntity(currentPos) instanceof IExtendedHangingSign tile) {
            tile.getExtension().updateShape(state, direction, neighborState, level, currentPos, neighborPos);
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof IExtendedHangingSign tile) {
            tile.getExtension().updateAttachments(level, pos, state);
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (level.isClientSide && ClientConfigs.SWINGING_SIGNS.get() &&
                level.getBlockEntity(pos) instanceof IExtendedHangingSign tile && tile.getExtension().canSwing()) {
            tile.getExtension().animation.hitByEntity(entity, state, pos);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return !pLevel.isClientSide ? null : (level, blockPos, blockState, blockEntity) -> {
            if (ClientConfigs.SWINGING_SIGNS.get() && blockEntity instanceof IExtendedHangingSign te) {
                te.getExtension().clientTick(level, blockPos, blockState);
            }
        };
    }


}
