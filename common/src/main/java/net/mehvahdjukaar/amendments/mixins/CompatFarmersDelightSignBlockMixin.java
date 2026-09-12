package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import vectorwing.farmersdelight.common.block.StandingCanvasSignBlock;
import vectorwing.farmersdelight.common.block.WallCanvasSignBlock;

@Pseudo
@Mixin({StandingCanvasSignBlock.class, WallCanvasSignBlock.class})
public abstract class CompatFarmersDelightSignBlockMixin extends Block {

    public CompatFarmersDelightSignBlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        if (PlatHelper.getPhysicalSide().isClient() && AmendmentsClient.WAS_INIT) {
            if (ClientConfigs.PIXEL_CONSISTENT_SIGNS.get()) return RenderShape.MODEL;
        }
        return super.getRenderShape(state);
    }
}
