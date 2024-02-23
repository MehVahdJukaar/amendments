package net.mehvahdjukaar.amendments.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PotionBrewing.class)
public abstract class BrewingMixin {

    @WrapOperation(method = "mix", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/alchemy/PotionUtils;setPotion(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/alchemy/Potion;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack amendments$mixEffects(ItemStack newPot, Potion potion, Operation<ItemStack> original,
                                                   @Local(ordinal = 1) ItemStack potionStack) {
        if (potion == Potions.EMPTY && potionStack.hasTag()) {
            newPot.setTag(potionStack.getTag().copy());
            return newPot;
        }
        return original.call(newPot, potion);
    }

}
