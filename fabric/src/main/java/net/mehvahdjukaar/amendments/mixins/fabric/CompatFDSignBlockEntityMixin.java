package net.mehvahdjukaar.amendments.mixins.fabric;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import vectorwing.farmersdelight.common.block.StandingCanvasSignBlock;
import vectorwing.farmersdelight.common.block.entity.CanvasSignBlockEntity;

@Pseudo
@Mixin(StandingCanvasSignBlock.class)
public abstract class CompatFDSignBlockEntityMixin extends Block {

    public CompatFDSignBlockEntityMixin(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
