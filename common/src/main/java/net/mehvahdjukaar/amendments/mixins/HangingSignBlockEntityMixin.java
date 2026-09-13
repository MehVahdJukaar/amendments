package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.common.ExtendedHangingSign;
import net.mehvahdjukaar.amendments.common.tile.HangingSignTileExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(HangingSignBlockEntity.class)
public abstract class HangingSignBlockEntityMixin extends BlockEntity implements ExtendedHangingSign {

    @Unique
    private final HangingSignTileExtension amendments$extension = new HangingSignTileExtension(this.getBlockState());

    protected HangingSignBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        this.amendments$extension.saveAdditional(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.amendments$extension.load(input);
    }

    @Override
    public HangingSignTileExtension amendments$getExtension() {
        return amendments$extension;
    }
}
