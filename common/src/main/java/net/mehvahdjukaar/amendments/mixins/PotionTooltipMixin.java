package net.mehvahdjukaar.amendments.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PotionItem.class)
public class PotionTooltipMixin {

    @WrapOperation(method = "getName", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/alchemy/PotionContents;getName(Ljava/lang/String;)Lnet/minecraft/network/chat/Component;"))
    public Component amendments$changePotionDescription(PotionContents contents, String prefix, Operation<Component> op) {
        boolean onlyCustomEffects = contents.customName().isEmpty() && contents.potion().isEmpty() && !contents.customEffects().isEmpty();
        if (onlyCustomEffects) {
            return Component.translatable(prefix + "mixed");
        }
        return op.call(contents, prefix);
    }
}
