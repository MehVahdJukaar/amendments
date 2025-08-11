package net.mehvahdjukaar.amendments.mixins.fabric;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import vectorwing.farmersdelight.common.block.StandingCanvasSignBlock;
import vectorwing.farmersdelight.common.block.WallCanvasSignBlock;

@Pseudo
@Mixin(WallCanvasSignBlock.class)
public abstract class CompatFDWallSignBlockEntityMixin extends Block {

    public CompatFDWallSignBlockEntityMixin(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
