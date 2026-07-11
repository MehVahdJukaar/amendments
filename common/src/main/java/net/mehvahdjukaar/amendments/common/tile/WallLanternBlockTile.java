package net.mehvahdjukaar.amendments.common.tile;

import net.mehvahdjukaar.amendments.common.LanternRegistry;
import net.mehvahdjukaar.amendments.common.block.WallLanternBlock;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.integration.ThinAirCompat;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.block.IBlockHolder;
import net.mehvahdjukaar.moonlight.api.block.MimicBlockTile;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class WallLanternBlockTile extends SwayingBlockTile implements IBlockHolder, IExtraModelDataProvider {

    public static final ModelDataKey<BlockState> MIMIC_KEY = MimicBlockTile.MIMIC_KEY;

    @Nullable
    private BlockState pendingLegacyLantern;
    private boolean pendingLegacyRedstone;

    public WallLanternBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.WALL_LANTERN_TILE.get(), pos, state);
    }

    @Override
    public boolean isNeverFancy() {
        return ClientConfigs.FAST_LANTERNS.get();
    }

    public boolean isRedstoneLantern() {
        return getOwnBlock().type.getId().toString().equals("charm:redstone_lantern");
    }

    public double getAttachmentOffset() {
        return getOwnBlock().type.attachmentOffset;
    }

    public WallLanternBlock getOwnBlock() {
        return (WallLanternBlock) getBlockState().getBlock();
    }

    public BlockState getLanternState() {
        return getOwnBlock().getLanternState(getBlockState());
    }

    @Override
    public BlockState getHeldBlock(int index) {
        return getLanternState();
    }

    @Override
    public boolean setHeldBlock(BlockState state, int index) {
        return false;
    }

    @Override
    public Vector3f getRotationAxis(BlockState state) {
        return state.getValue(WallLanternBlock.FACING).step();
    }

    @Override
    public void addExtraModelData(ExtraModelData.Builder builder) {
        super.addExtraModelData(builder);
        builder.with(MIMIC_KEY, getLanternState());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Lantern")) {
            pendingLegacyLantern = Utils.readBlockState(tag.getCompound("Lantern"), level);
            pendingLegacyRedstone = tag.getBoolean("IsRedstone");
            tryMigrateLegacyLantern();
        }
    }

    private void tryMigrateLegacyLantern() {
        if (pendingLegacyLantern == null || level == null || level.isClientSide) return;

        BlockState legacyLantern = pendingLegacyLantern;
        boolean legacyRedstone = pendingLegacyRedstone;
        pendingLegacyLantern = null;

        if (legacyLantern.isAir()) return;

        var type = LanternRegistry.INSTANCE.detectTypeFromBlock(legacyLantern.getBlock(), Utils.getID(legacyLantern.getBlock()));
        if (type.isEmpty()) return;

        WallLanternBlock targetWall = ModRegistry.WALL_LANTERNS.get(type.get());
        if (targetWall == null) return;

        WallLanternBlock currentWall = getOwnBlock();
        BlockState wallState = getBlockState();

        if (currentWall.type != targetWall.type) {
            wallState = copyWallState(wallState, targetWall.defaultBlockState());
        }
        wallState = applyLegacyLanternState(wallState, legacyLantern, legacyRedstone, targetWall.type);

        if (wallState != getBlockState()) {
            level.setBlock(worldPosition, wallState, Block.UPDATE_ALL);
        }

        if (CompatHandler.THIN_AIR && ThinAirCompat.isAirLantern(legacyLantern)) {
            updateThinAir(legacyLantern);
        }
        setChanged();
    }

    private static BlockState copyWallState(BlockState from, BlockState to) {
        return to.setValue(WallLanternBlock.FACING, from.getValue(WallLanternBlock.FACING))
                .setValue(WallLanternBlock.ATTACHMENT, from.getValue(WallLanternBlock.ATTACHMENT))
                .setValue(WallLanternBlock.WATERLOGGED, from.getValue(WallLanternBlock.WATERLOGGED));
    }

    private BlockState applyLegacyLanternState(BlockState wallState, BlockState legacyLantern,
                                               boolean legacyRedstone, LanternRegistry.LanternType type) {
        int light = ForgeHelper.getLightEmission(legacyLantern, level, worldPosition);
        boolean lit = true;
        if (legacyRedstone || type.getId().toString().equals("charm:redstone_lantern")) {
            lit = legacyLantern.hasProperty(WallLanternBlock.LIT) && legacyLantern.getValue(WallLanternBlock.LIT);
            light = 15;
        } else if (legacyLantern.hasProperty(WallLanternBlock.LIT)) {
            lit = legacyLantern.getValue(WallLanternBlock.LIT);
        }
        if (light == 0) lit = false;
        return wallState.setValue(WallLanternBlock.LIT, lit)
                .setValue(WallLanternBlock.LIGHT_LEVEL, Math.max(light, 5));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
    }

    public void updateThinAir(BlockState lantern) {
        if (CompatHandler.THIN_AIR && this.level != null && ThinAirCompat.isAirLantern(lantern)) {
            var newState = ThinAirCompat.maybeSetAirQuality(lantern, Vec3.atCenterOf(this.worldPosition), this.level);
            if (newState != null) {
                level.scheduleTick(worldPosition, getBlockState().getBlock(), 20, TickPriority.NORMAL);
            }
        }
    }
}
