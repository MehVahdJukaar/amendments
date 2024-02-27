package net.mehvahdjukaar.amendments.common.tile;

import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.moonlight.api.client.util.LOD;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@Deprecated(forRemoval = true)
public abstract class DynamicRenderedBlockTile extends BlockEntity implements IExtraModelDataProvider {

    public static final ModelDataKey<Boolean> IS_FANCY = new ModelDataKey<>(Boolean.class);

    // lod stuff (client)
    private boolean isFancy = false; // current
    private int extraFancyTicks = 0;

    protected DynamicRenderedBlockTile(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    public abstract boolean isNeverFancy();

    //called when data is actually refreshed
    @Override
    public ExtraModelData getExtraModelData() {
        return ExtraModelData.builder()
                .with(IS_FANCY, this.isFancy)
                .build();
    }

    public void onFancyChanged(boolean fancy) {
    }

    // call in your tile renderer
    public boolean rendersFancy() {
        return isFancy;
    }

    public boolean shouldRenderFancy(Vec3 cameraPos) {
        LOD lod = new LOD(cameraPos, this.getBlockPos());
        boolean newFancyStatus = lod.isNear();
        boolean oldStatus = this.isFancy;
        if (oldStatus != newFancyStatus) {
            this.isFancy = newFancyStatus;
            onFancyChanged(isFancy);
            if (this.level == Minecraft.getInstance().level) {
                this.requestModelReload();
                this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_IMMEDIATE);
            }
            if (!isFancy) extraFancyTicks = 4;
        }
        if (extraFancyTicks > 0) {
            extraFancyTicks--;
            return true;
        }
        // 1 tick delay
        return isFancy;
    }

}
