package net.mehvahdjukaar.amendments.common.tile;

import net.mehvahdjukaar.amendments.common.PendulumAnimation;
import net.mehvahdjukaar.amendments.common.SwingAnimation;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.moonlight.api.block.DynamicRenderedBlockTile;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

public abstract class SwayingBlockTile extends DynamicRenderedBlockTile {

    private SwingAnimation animation;

    protected SwayingBlockTile(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    @Override
    public boolean isNeverFancy() {
        return ClientConfigs.FAST_LANTERNS.get();
    }

    @Override
    public void onFancyChanged(boolean newFancy) {
        if (!newFancy) this.getAnimation().reset();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }


    public static void clientTick(Level pLevel, BlockPos pPos, BlockState pState, SwayingBlockTile tile) {
        if (tile.rendersFancy()) {
            tile.getAnimation().tick(pLevel, pPos, pState);
        }
    }

    //rotation axis rotate 90 deg
    public abstract Vector3f getRotationAxis(BlockState state);

    // Just cal from client
    public SwingAnimation getAnimation() {
        if (animation == null) {
            animation = new PendulumAnimation(ClientConfigs.WALL_LANTERN_CONFIG, this::getRotationAxis);
        }
        return animation;
    }
}
