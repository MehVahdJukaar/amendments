package net.mehvahdjukaar.amendments.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(JukeboxBlock.class)
public abstract class JukeboxBlockMixin extends Block {

    @Unique
    private static final VoxelShape SMALL_SHAPE = Block.box(0,0,0,16,15,16);

    protected JukeboxBlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return SMALL_SHAPE;
    }

    @Override
    public SoundType getSoundType(BlockState state) {
        return SoundType.WOOD;
    }
}
