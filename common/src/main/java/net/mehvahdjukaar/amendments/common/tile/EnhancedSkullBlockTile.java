package net.mehvahdjukaar.amendments.common.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
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
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class EnhancedSkullBlockTile extends BlockEntity {

    @Nullable
    protected SkullBlockEntity innerTile = null;

    public EnhancedSkullBlockTile(BlockEntityType type, BlockPos pWorldPosition, BlockState pBlockState) {
        super(type, pWorldPosition, pBlockState);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        this.saveInnerTile("Skull", this.innerTile, output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.innerTile = loadInnerTile("Skull", this.innerTile, input);
    }

    protected void saveInnerTile(String tagName, @Nullable SkullBlockEntity tile, ValueOutput output) {
        if (tile != null) {
            output.store(tagName + "State", BlockState.CODEC, tile.getBlockState());
            tile.saveWithoutMetadata(output.child(tagName));
        }
    }

    @Nullable
    protected SkullBlockEntity loadInnerTile(String tagName, @Nullable SkullBlockEntity tile, ValueInput input) {
        var tileInput = input.child(tagName);
        var state = input.read(tagName + "State", BlockState.CODEC);
        if (tileInput.isEmpty() || state.isEmpty()) return null;
        if (tile == null) {
            if (!(state.get().getBlock() instanceof EntityBlock eb)) return null;
            if (!(eb.newBlockEntity(this.getBlockPos(), state.get()) instanceof SkullBlockEntity skullTile))
                return null;
            tile = skullTile;
        }
        tile.loadWithComponents(tileInput.get());
        return tile;
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
        if (this.innerTile != null) {
            TagValueOutput copy = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
            oldTile.saveWithoutMetadata(copy);
            this.innerTile.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, registries, copy.buildResult()));
        }
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
        //the inner skull is never placed in the world so nothing else gives it a level, and the
        //renderer needs one to read light and to be extracted at all
        if (innerTile != null && this.level != null && !innerTile.hasLevel()) innerTile.setLevel(this.level);
        return innerTile;
    }

    @Nullable
    public Identifier getWaxTexture() {
        return null;
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
