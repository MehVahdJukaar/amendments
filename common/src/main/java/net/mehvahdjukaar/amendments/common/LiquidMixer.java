package net.mehvahdjukaar.amendments.common;

import net.mehvahdjukaar.amendments.common.item.DyeBottleItem;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.DyedItemColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class LiquidMixer {

    @Nullable
    public static SoftFluidStack mixPotions(SoftFluidStack firstFluid, SoftFluidStack secondFluid) {
        var tankFluidContent = firstFluid.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        var newFluidContent = secondFluid.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        int oldCount = firstFluid.getCount();
        int newCount = oldCount + secondFluid.getCount();
        List<MobEffectInstance> combinedEffects = new ArrayList<>();

        List<MobEffectInstance> existingEffects = new ArrayList<>();
        tankFluidContent.getAllEffects().forEach(existingEffects::add);

        List<MobEffectInstance> newEffects = new ArrayList<>();
        newFluidContent.getAllEffects().forEach(newEffects::add);

        if (newEffects.equals(existingEffects)) return null; // just increment

        float oldMult = oldCount / (float) newCount;
        float newMult = 1 - oldMult;
        combineEffects(combinedEffects, existingEffects, oldMult);
        combineEffects(combinedEffects, newEffects, newMult);
        //merge similar. assumes there are no triple effects. if we merge each time there shouldnt

        Map<Holder<MobEffect>, MobEffectInstance> mergedMap = combinedEffects.stream()
                .collect(Collectors.toMap(
                        MobEffectInstance::getEffect,
                        effectInstance -> effectInstance,
                        LiquidMixer::mergeEffects));

        mergedMap.entrySet().removeIf(e -> e.getValue().getDuration() <= 0 || e.getValue().getAmplifier() < 0);

        int customPotionColor = PotionContents.getColor(mergedMap.values());
        PotionContents mergedContents = new PotionContents(Optional.empty(),
                Optional.of(customPotionColor), mergedMap.values().stream().toList());

        SoftFluidStack returnStack = firstFluid.copy();
        returnStack.set(DataComponents.POTION_CONTENTS, mergedContents);

        return returnStack;
    }

    @NotNull
    private static MobEffectInstance mergeEffects(MobEffectInstance e, MobEffectInstance e1) {
        return new MobEffectInstance(e.getEffect(), (e.getDuration() + e1.getDuration()),
                (e.getAmplifier() + e1.getAmplifier()) / 2);
    }

    private static void combineEffects(List<MobEffectInstance> combinedEffects,
                                       List<MobEffectInstance> current, float mult) {
        for (var e : current) {
            Holder<MobEffect> effect = e.getEffect();

            MobEffectInstance newInstance;
            if (effect.value().isInstantenous()) {
                newInstance = new MobEffectInstance(effect, e.getDuration(), (int) (e.getAmplifier() * mult));
            } else {
                newInstance = new MobEffectInstance(effect, (int) (e.getDuration() * mult), e.getAmplifier());
            }
            combinedEffects.add(newInstance);
        }
    }


    @Nullable
    public static SoftFluidStack mixDye(SoftFluidStack firstFluid, SoftFluidStack secondFluid) {
        DyedItemColor firstColor = firstFluid.get(DataComponents.DYED_COLOR);
        DyedItemColor secondColor = secondFluid.get(DataComponents.DYED_COLOR);
        if (firstColor == null || secondColor == null) return null;

        int oldAmount = firstFluid.getCount();
        int newAmount = secondFluid.getCount();
        int newColor = DyeBottleItem.mixColor(firstColor.rgb(), secondColor.rgb(), oldAmount, newAmount);

        SoftFluidStack returnStack = firstFluid.copy();
        returnStack.set(DataComponents.DYED_COLOR, new DyedItemColor(newColor, true));

        return returnStack;
    }

}
