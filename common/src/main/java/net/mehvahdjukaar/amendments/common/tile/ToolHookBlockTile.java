package net.mehvahdjukaar.amendments.common.tile;

import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.reg.ModBlockProperties;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.amendments.reg.ModTags;
import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.moonlight.api.client.util.LOD;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ToolHookBlockTile extends ItemDisplayTile implements IExtraModelDataProvider {

    public static final ModelDataKey<Boolean> IS_FANCY = ModBlockProperties.FANCY;
    public static final ModelDataKey<ItemStack> ITEM = ModBlockProperties.ITEM;

    // lod stuff (client)
    private boolean isFancy = false; // current
    private int extraFancyTicks = 0;

    public ToolHookBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.TOOL_HOOK_TILE.get(), pos, state);
    }


    @Override
    protected Component getDefaultName() {
        return Component.literal("tool hook");
    }

    public boolean isNeverFancy() {
        return !ClientConfigs.ANIMATED_HOOKS.get();
    }

    @Override
    public void addExtraModelData(ExtraModelData.Builder builder) {
        builder.with(IS_FANCY, this.isFancy);
        builder.with(ITEM, this.getDisplayedItem());
    }

    public void onFancyChanged(boolean fancy) {
    }

    // call in your tile renderer
    public boolean rendersFancy() {
        return isFancy;
    }

    public boolean shouldRenderFancy(Vec3 cameraPos) {
        if (isNeverFancy()) return false;
        LOD lod = new LOD(cameraPos, this.getBlockPos());
        boolean newFancyStatus = lod.isVeryNear();
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

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        Item item = stack.getItem();
        if (isValidTool(item)) {
            return super.canPlaceItem(index, stack);
        }
        return false;
    }

    public static boolean isValidTool(Item item) {
        return item instanceof DiggerItem || item instanceof SwordItem || item instanceof TridentItem ||
                item.builtInRegistryHolder().is(ModTags.GOES_IN_TRIPWIRE_HOOK);
    }


    @Override
    public void updateTileOnInventoryChanged() {
        super.updateTileOnInventoryChanged();
        if (this.getDisplayedItem().isEmpty() && level != null) {
            this.level.setBlockAndUpdate(worldPosition, Blocks.TRIPWIRE_HOOK.withPropertiesOf(this.getBlockState()));
        }
    }

    @Override
    public void updateClientVisualsOnLoad() {
        super.updateClientVisualsOnLoad();
        this.requestModelReload();
    }
}
