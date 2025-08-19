package net.mehvahdjukaar.amendments.mixins;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import java.util.EnumMap;

@Mixin(WallSignBlock.class)
public abstract class WallSignMixin extends Block {

    @Unique
    private static final EnumMap<Direction, VoxelShape> AMENDMENTS_VISUAL_SHAPE = Maps.newEnumMap(ImmutableMap.of(
            Direction.NORTH, Block.box(0.0, 3, 14.0, 16.0, 13, 16.0),
            Direction.SOUTH, Block.box(0.0, 3, 0.0, 16.0, 13, 2.0),
            Direction.EAST, Block.box(0.0, 3, 0.0, 2.0, 13, 16.0),
            Direction.WEST, Block.box(14.0, 3, 0.0, 16.0, 13, 16.0)));

    public WallSignMixin(Properties properties) {
        super(properties);
    }


    //technically unsafe
    @ModifyReturnValue(method = "getShape", at = @At("RETURN"))
    public VoxelShape getShape(VoxelShape original, BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if ((level instanceof Level l) && l.isClientSide && ClientConfigs.PIXEL_CONSISTENT_SIGNS.get()) {
            return AMENDMENTS_VISUAL_SHAPE.get(state.getValue(WallSignBlock.FACING));
        }
        return original;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}