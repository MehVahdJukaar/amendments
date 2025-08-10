package net.mehvahdjukaar.amendments.mixins;

import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StandingSignBlock.class)
public abstract class SignBlockMixin extends Block {

    public SignBlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
