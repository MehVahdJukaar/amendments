package net.mehvahdjukaar.amendments.common.tile;

import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

public abstract class DynamicRenderedBlockTile extends BlockEntity implements IExtraModelDataProvider {

    public static final ModelDataKey<Boolean> IS_FANCY = new ModelDataKey<>(Boolean.class);

    // lod stuff (client)
    protected boolean shouldBeFancy = false; // current
    protected boolean wasFancy = false; // old
    private int ticksToSwitchMode = 0;

    protected DynamicRenderedBlockTile(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    public abstract boolean isNeverFancy();

    //called when data is actually refreshed
    @Override
    public ExtraModelData getExtraModelData() {
        this.ticksToSwitchMode = 2;
        return ExtraModelData.builder()
                .with(IS_FANCY, this.shouldBeFancy)
                .build();
    }

    // call each render tick from your tile renderer
    public void setFancyRenderer(boolean fancy) {
        if (this.isNeverFancy()) fancy = false;
        if (fancy != this.shouldBeFancy) {
            this.wasFancy = this.shouldBeFancy;
            this.shouldBeFancy = fancy;
            //model data doesn't like other levels. linked to crashes with other mods
            if (this.level == Minecraft.getInstance().level) {
                this.requestModelReload();
                this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_IMMEDIATE);
            }
        }
    }

    // call in your tile renderer
    public boolean shouldRenderFancy() {
        if (this.wasFancy != this.shouldBeFancy && !this.wasFancy) {
            // makes TESR wait 1 render cycle,
            // so it's in sync with model data refreshVisuals
            this.wasFancy = true;
        }
        return wasFancy;
    }

    public void clientTick() {
        if (this.wasFancy != this.shouldBeFancy && this.wasFancy && this.ticksToSwitchMode > 0) {
            this.ticksToSwitchMode--;
            if (this.ticksToSwitchMode == 0) {
                //makes TESR wait 1 render cycle,
                // so it's in sync with model data refreshVisuals
                this.wasFancy = false;
            }
        }
    }

}
