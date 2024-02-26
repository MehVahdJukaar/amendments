package net.mehvahdjukaar.amendments.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PotionItem.class)
public class PotionTooltipMixin {

    @WrapOperation(method = "getDescriptionId", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/alchemy/Potion;getName(Ljava/lang/String;)Ljava/lang/String;"))
    public String getDescriptionId(Potion potion, String prefix, Operation<String> op, @Local ItemStack stack) {
        if(potion == Potions.EMPTY && stack.hasTag() && stack.getTag().get(PotionUtils.TAG_CUSTOM_POTION_EFFECTS) != null) {
            return prefix + "mixed";
        }
        return op.call(potion, prefix);
    }
}
