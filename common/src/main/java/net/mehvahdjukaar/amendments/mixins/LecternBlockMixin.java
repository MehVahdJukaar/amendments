package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.WritableBookItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LecternBlock.class)
public abstract class LecternBlockMixin {

    @Inject(method = "openScreen", at = @At("HEAD"), cancellable = true)
    public void amendments$openCustomMenu(Level level, BlockPos pos, Player player, CallbackInfo ci) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof LecternBlockEntity be && be.getBook().getItem() instanceof WritableBookItem
                && CommonConfigs.LECTERN_STUFF.get()) {
            PlatHelper.openCustomMenu((ServerPlayer) player, be, pos);
            player.awardStat(Stats.INTERACT_WITH_LECTERN);
            ci.cancel();
        }
    }

}
