package net.mehvahdjukaar.amendments.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(PotionItem.class)
public class PotionTooltipMixin {

    @WrapOperation(method = "getDescriptionId", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/alchemy/Potion;getName(Ljava/util/Optional;Ljava/lang/String;)Ljava/lang/String;"))
    public String amendments$changePotionDescription(Optional<Holder<Potion>> potion, String prefix, Operation<String> op, @Local(argsOnly = true) ItemStack stack) {
        if (potion.isEmpty() && !stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)
                .customEffects().isEmpty()) {
            return prefix + "mixed";
        }
        return op.call(potion, prefix);
    }
}
