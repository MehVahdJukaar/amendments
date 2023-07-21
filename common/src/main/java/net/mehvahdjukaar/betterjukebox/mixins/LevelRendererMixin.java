package net.mehvahdjukaar.betterjukebox.mixins;

import net.mehvahdjukaar.betterjukebox.BetterJukeboxes;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.LevelEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Shadow @Nullable
    private ClientLevel level;

    @Inject(method = "levelEvent", at = @At("HEAD"))
    public void levelEvent(int type, BlockPos pos, int data, CallbackInfo ci){
        if(type == LevelEvent.SOUND_PLAY_JUKEBOX_SONG || type == LevelEvent.SOUND_STOP_JUKEBOX_SONG){
            BetterJukeboxes.setPlayingJukebox(this.level, pos,type == LevelEvent.SOUND_PLAY_JUKEBOX_SONG, data);
        }
    }
}
