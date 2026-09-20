package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
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

    @Unique
    private static boolean amendments$hasNewModel() {
        return PlatHelper.getPhysicalSide().isClient() && AmendmentsClient.WAS_INIT &&
                ClientConfigs.JUKEBOX_MODEL.get();
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        if (amendments$hasNewModel()) return SMALL_SHAPE;
        return super.getOcclusionShape(state, level, pos);
    }

    @Override
    public SoundType getSoundType(BlockState state) {
        if (amendments$hasNewModel()) return SoundType.WOOD;
        return super.getSoundType(state);
    }
}
