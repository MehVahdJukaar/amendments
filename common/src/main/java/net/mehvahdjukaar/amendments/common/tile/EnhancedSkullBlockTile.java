package net.mehvahdjukaar.amendments.common.tile;

import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class EnhancedSkullBlockTile extends BlockEntity {

    @Nullable
    protected SkullBlockEntity innerTile = null;

    public EnhancedSkullBlockTile(BlockEntityType type, BlockPos pWorldPosition, BlockState pBlockState) {
        super(type, pWorldPosition, pBlockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.saveInnerTile("Skull", this.innerTile, tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.innerTile = loadInnerTile("Skull", this.innerTile, tag, registries);
    }

    protected void saveInnerTile(String tagName, @Nullable SkullBlockEntity tile, CompoundTag tag, HolderLookup.Provider registries) {
        if (tile != null) {
            tag.put(tagName + "State", NbtUtils.writeBlockState(tile.getBlockState()));
            tag.put(tagName, tile.saveWithFullMetadata(registries));
        }
    }

    @Nullable
    protected SkullBlockEntity loadInnerTile(String tagName, @Nullable SkullBlockEntity tile, CompoundTag tag,
                                             HolderLookup.Provider registries) {
        if (tag.contains(tagName)) {
            BlockState state = Utils.readBlockState(tag.getCompound(tagName + "State"), this.level);
            CompoundTag tileTag = tag.getCompound(tagName);
            if (tile == null) {
                BlockEntity newTile = BlockEntity.loadStatic(this.getBlockPos(), state, tileTag, registries);
                if (newTile instanceof SkullBlockEntity skullTile) return skullTile;
            } else {
                tile.loadWithComponents(tileTag, registries);
                return tile;
            }
        }
        return null;
    }

    @Override
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    public ItemStack getSkullItem() {
        if (this.innerTile != null) {
            return new ItemStack(innerTile.getBlockState().getBlock());
        }
        return ItemStack.EMPTY;
    }

    public void initialize(SkullBlockEntity oldTile, ItemStack stack, Player player, InteractionHand hand) {
        // this.setOwner(oldTile.getOwnerProfile());
        var registries = player.level().registryAccess();
        this.innerTile = (SkullBlockEntity) oldTile.getType().create(this.getBlockPos(), oldTile.getBlockState());
        if (this.innerTile != null) this.innerTile.loadWithComponents(oldTile
                .saveWithoutMetadata(registries), registries);
    }

    @Nullable
    public BlockState getSkull() {
        if (innerTile != null) {
            return innerTile.getBlockState();
        }
        return null;
    }

    @Nullable
    public BlockEntity getSkullTile() {
        return innerTile;
    }

    protected void tick(Level level, BlockPos pos, BlockState state) {
        tickInner(level, pos, innerTile);
    }

    /**
     * Drives the wrapped skull's own block-entity ticker (e.g. Caverns &amp; Chasms peeper/mime heads
     * turn to track the nearest player when powered). The inner skull isn't a real block entity in
     * the world, so the level never ticks it - we have to pump its ticker ourselves. Without this its
     * animation state never advances and, because the renderer interpolates by partial tick, the head
     * jitters wildly on the client instead of holding still.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected static void tickInner(Level level, BlockPos pos, @Nullable SkullBlockEntity inner) {
        if (inner == null) return;
        BlockState b = inner.getBlockState();
        if (b.getBlock() instanceof EntityBlock eb) {
            BlockEntityTicker ticker = eb.getTicker(level, b, inner.getType());
            if (ticker != null) {
                ticker.tick(level, pos, b, inner);
            }
        }
    }
}
