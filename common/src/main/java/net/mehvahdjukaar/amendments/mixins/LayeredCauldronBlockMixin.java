package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.common.block.LiquidCauldronBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(LayeredCauldronBlock.class)
public abstract class LayeredCauldronBlockMixin extends AbstractCauldronBlock {

    @Shadow protected abstract double getContentHeight(BlockState state);

    protected LayeredCauldronBlockMixin(Properties properties, Map<Item, CauldronInteraction> interactions) {
        super(properties, interactions);
    }

    @Inject(method = "entityInside", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;clearFire()V"))
    public void amendments$playExtinguishSound(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        LiquidCauldronBlock.playExtinguishSound(level, pos, entity);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (this.isEntityInsideContent(state, pos, entity)) {
            LiquidCauldronBlock.playSplashAnimation(level,pos, entity,getContentHeight(state),
                    3694022);
            super.fallOn(level, state, pos, entity, 0);
        } else super.fallOn(level, state, pos, entity, fallDistance);
    }
}
