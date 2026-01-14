package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StandingSignBlock.class)
public abstract class SignBlockMixin extends Block {

    public SignBlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        if (PlatHelper.getPhysicalSide().isClient() && AmendmentsClient.WAS_INIT) {
            if (ClientConfigs.isPixelConsistentSign(state)) return RenderShape.MODEL;
        }
        return super.getRenderShape(state);
    }
}

