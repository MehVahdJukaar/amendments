package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin {

    @ModifyVariable(method = "render", at = @At("STORE"), index = 17)
    public int amendments$centerTradeRow(int rowY) {
        return ClientConfigs.TRADE_BUTTONS.get() ? rowY + 1 : rowY;
    }
}
