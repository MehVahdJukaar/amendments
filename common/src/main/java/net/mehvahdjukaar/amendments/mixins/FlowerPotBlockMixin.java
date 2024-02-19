package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.common.block.HangingFlowerPotBlock;
import net.mehvahdjukaar.amendments.common.tile.HangingFlowerPotBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlowerPotBlock.class)
public abstract class FlowerPotBlockMixin {

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;awardStat(Lnet/minecraft/resources/ResourceLocation;)V"))
    public void amendments$addPotSound(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                       BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        HangingFlowerPotBlock.playPlantSound(level, pos, player);
    }
}
