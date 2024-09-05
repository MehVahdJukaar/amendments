package net.mehvahdjukaar.amendments.common;


import net.mehvahdjukaar.amendments.common.tile.HangingSignTileExtension;

public interface ExtendedHangingSign extends ISwingingTile{

    HangingSignTileExtension amendments$getExtension();

    @Override
    default SwingAnimation amendments$getAnimation() {
        return this.amendments$getExtension().getClientAnimation();
    }
}
