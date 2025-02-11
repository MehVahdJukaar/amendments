package net.mehvahdjukaar.amendments.common.tile;

import com.mojang.authlib.GameProfile;
import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

public class DoubleSkullBlockTile extends EnhancedSkullBlockTile {

    @Nullable
    protected SkullBlockEntity innerTileUp = null;
    private Block candleUp = null;
    //client only
    private ResourceLocation waxTexture = null;

    public DoubleSkullBlockTile(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModRegistry.SKULL_PILE_TILE.get(), pWorldPosition, pBlockState);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        this.saveInnerTile("SkullUp", this.innerTileUp, tag);

        if (candleUp != null) {
            tag.putString("CandleAbove", Utils.getID(candleUp).toString());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.innerTileUp = this.loadInnerTile("SkullUp", this.innerTileUp, tag);
        Block b = null;
        if (tag.contains("CandleAbove")) {
            ResourceLocation candle = new ResourceLocation(tag.getString("CandleAbove"));
            var o = BuiltInRegistries.BLOCK.getOptional(candle);
            if (o.isPresent()) b = o.get();
        }
        setCandleUp(b);
    }

    public ItemStack getSkullItemUp() {
        if (this.innerTileUp != null) {
            return new ItemStack(innerTileUp.getBlockState().getBlock());
        }
        return ItemStack.EMPTY;
    }

    public void rotateUp(Rotation rotation) {
        if (this.innerTileUp != null) {
            BlockState state = this.innerTileUp.getBlockState();
            int r = this.innerTileUp.getBlockState().getValue(SkullBlock.ROTATION);
            this.innerTileUp.setBlockState(state.setValue(SkullBlock.ROTATION,
                    rotation.rotate(r, 16)));
        }
    }

    public void rotateUpStep(int step) {
        if (this.innerTileUp != null) {
            BlockState state = this.innerTileUp.getBlockState();
            int r = this.innerTileUp.getBlockState().getValue(SkullBlock.ROTATION);
            this.innerTileUp.setBlockState(state.setValue(SkullBlock.ROTATION,
                    ((r - step) + 16) % 16));
        }
    }

    @Override
    public void initialize(SkullBlockEntity oldTile, ItemStack skullStack, Player player, InteractionHand hand) {
        super.initialize(oldTile, skullStack, player, hand);
        if (skullStack.getItem() instanceof BlockItem bi) {
            if (bi.getBlock() instanceof SkullBlock upSkull) {
                var context = new BlockPlaceContext(player, hand, skullStack,
                        new BlockHitResult(new Vec3(0.5, 0.5, 0.5), Direction.UP, this.getBlockPos(), false));
                BlockState state = upSkull.getStateForPlacement(context);
                if (state == null) {
                    state = upSkull.defaultBlockState();
                }
                BlockEntity entity = upSkull.newBlockEntity(this.getBlockPos(), state);
                if (entity instanceof SkullBlockEntity blockEntity) {
                    this.innerTileUp = blockEntity;

                    //sets owner of upper tile
                    GameProfile gameprofile = null;
                    if (skullStack.hasTag()) {
                        CompoundTag compoundtag = skullStack.getTag();
                        if (compoundtag.contains("SkullOwner", 10)) {
                            gameprofile = NbtUtils.readGameProfile(compoundtag.getCompound("SkullOwner"));
                        } else if (compoundtag.contains("SkullOwner", 8) && !StringUtils.isBlank(compoundtag.getString("SkullOwner"))) {
                            gameprofile = new GameProfile(null, compoundtag.getString("SkullOwner"));
                        }
                    }
                    this.innerTileUp.setOwner(gameprofile);
                }
            }
        }
    }

    public void updateWax(BlockState above) {
        setCandleUp(above.getBlock());
        if (this.level instanceof ServerLevel) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
        }
    }

    private void setCandleUp(Block above) {
        this.candleUp = null;
        if (above instanceof CandleBlock) {
            this.candleUp = above;
        }
        if (PlatHelper.getPhysicalSide().isClient()) {
            this.waxTexture = null;
            if (this.candleUp != null) {
                this.waxTexture = AmendmentsClient.SKULL_CANDLES_TEXTURES.get().get(this.candleUp);
            }
        }
    }

    public ResourceLocation getWaxTexture() {
        return waxTexture;
    }

    @Nullable
    public BlockState getSkullUp() {
        if (this.innerTileUp != null) {
            return this.innerTileUp.getBlockState();
        }
        return null;
    }

    @Nullable
    public BlockEntity getSkullTileUp() {
        return this.innerTileUp;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DoubleSkullBlockTile e) {
        e.tick(level, pos, state);
        var tileUp = e.getSkullTileUp();
        if (tileUp != null) {
            var b = tileUp.getBlockState();
            if (b instanceof EntityBlock eb) {
                eb.getTicker(level, b, tileUp.getType());
            }
        }
    }
}
