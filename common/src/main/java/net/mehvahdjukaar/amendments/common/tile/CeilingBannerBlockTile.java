package net.mehvahdjukaar.amendments.common.tile;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class CeilingBannerBlockTile extends BlockEntity implements Nameable {
    @Nullable
    private Component name;
    private DyeColor baseColor;
    @Nullable
    private ListTag itemPatterns;
    @Nullable
    private List<Pair<Holder<BannerPattern>, DyeColor>> patterns;

    public CeilingBannerBlockTile(BlockPos pos, BlockState state) {
        this(pos, state, DyeColor.WHITE);
    }

    public CeilingBannerBlockTile(BlockPos pos, BlockState state, DyeColor color) {
        super(ModRegistry.CEILING_BANNER_TILE.get(), pos, state);
        this.baseColor = color;
    }

    @Override
    public Component getName() {
        return this.name != null ? this.name : Component.translatable("block.minecraft.banner");
    }

    @Override
    @Nullable
    public Component getCustomName() {
        return this.name;
    }

    public void setCustomName(Component component) {
        this.name = component;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.itemPatterns != null) {
            tag.put("Patterns", this.itemPatterns);
        }

        if (this.name != null) {
            tag.putString("CustomName", Component.Serializer.toJson(this.name, registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("CustomName", 8)) {
            this.name = Component.Serializer.fromJson(tag.getString("CustomName"), registries);
        }

        if (this.hasLevel()) {
            this.baseColor = ((AbstractBannerBlock) this.getBlockState().getBlock()).getColor();
        } else {
            this.baseColor = null;
        }

        this.itemPatterns = tag.getList("Patterns", 10);
        this.patterns = null;
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

    public List<Pair<Holder<BannerPattern>, DyeColor>> getPatterns() {
        if (this.patterns == null) {
            this.patterns = BannerBlockEntity.createPatterns(this.baseColor, this.itemPatterns);
        }
        return this.patterns;
    }

    @NotNull
    public DyeColor getBaseColor() {
        return this.baseColor;
    }
}

