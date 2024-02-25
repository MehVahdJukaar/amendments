package net.mehvahdjukaar.amendments.common;

import net.mehvahdjukaar.amendments.common.item.DyeBottleItem;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.PotionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LiquidMixer {

    public static void mixPotions(SoftFluidStack tankFluid, SoftFluidStack newFluid) {
        CompoundTag tankTag = tankFluid.getTag();
        CompoundTag newTag = newFluid.getTag();
        if (tankTag == null || newTag == null) return;
        int oldCount = tankFluid.getCount();
        int newCount = oldCount + newFluid.getCount();
        List<MobEffectInstance> combinedEffects = new ArrayList<>();
        List<MobEffectInstance> existingEffects = PotionUtils.getAllEffects(tankTag);
        List<MobEffectInstance> newEffects = PotionUtils.getAllEffects(newFluid.getTag());
        if (newEffects.equals(existingEffects)) return;

        float oldMult = oldCount / (float) newCount;
        float newMult = 1 - oldMult;
        combineEffects(combinedEffects, existingEffects, oldMult);
        combineEffects(combinedEffects, newEffects, newMult);
        //merge similar. assumes there are no triple effects. if we merge each time there shouldnt

        Map<MobEffect, MobEffectInstance> mergedMap = combinedEffects.stream()
                .collect(Collectors.toMap(
                        MobEffectInstance::getEffect,
                        effectInstance -> effectInstance,
                        LiquidMixer::mergeEffects));

        mergedMap.entrySet().removeIf(e -> e.getValue().getDuration() <= 0 || e.getValue().getAmplifier() < 0);

        tankTag.putInt("CustomPotionColor", PotionUtils.getColor(mergedMap.values()));
        tankTag.remove("Potion"); //remove normal potion
        saveEffects(tankTag, mergedMap.values());
    }

    @NotNull
    private static MobEffectInstance mergeEffects(MobEffectInstance e, MobEffectInstance e1) {
        return new MobEffectInstance(e.getEffect(), (e.getDuration() + e1.getDuration()),
                (e.getAmplifier() + e1.getAmplifier()) / 2);
    }

    public static void saveEffects(CompoundTag tag, Collection<MobEffectInstance> effects) {
        ListTag listTag = new ListTag();
        for (MobEffectInstance mobEffectInstance : effects) {
            listTag.add(mobEffectInstance.save(new CompoundTag()));
        }
        tag.put("CustomPotionEffects", listTag);
    }

    private static void combineEffects(List<MobEffectInstance> combinedEffects,
                                       List<MobEffectInstance> current, float mult) {
        for (var e : current) {
            MobEffect effect = e.getEffect();

            MobEffectInstance newInstance;
            if (effect.isInstantenous()) {
                newInstance = new MobEffectInstance(effect, e.getDuration(), (int) (e.getAmplifier() * mult));
            } else {
                newInstance = new MobEffectInstance(effect, (int) (e.getDuration() * mult), e.getAmplifier());
            }
            combinedEffects.add(newInstance);
        }
    }


    public static void mixDye(SoftFluidStack tankFluid, SoftFluidStack newFluid) {
        CompoundTag tankTag = tankFluid.getTag();
        CompoundTag newTag = newFluid.getTag();
        if (tankTag == null || newTag == null) return;

        int oldColor = tankTag.getInt(DyeBottleItem.COLOR_TAG);
        int newColor = newTag.getInt(DyeBottleItem.COLOR_TAG);
        int oldAmount = tankFluid.getCount();
        int newAmount = newFluid.getCount();
        CompoundTag combinedTag = new CompoundTag();
        combinedTag.putInt(DyeBottleItem.COLOR_TAG, DyeBottleItem.mixColor(oldColor, newColor, oldAmount, newAmount));

        tankFluid.setTag(combinedTag);
    }

}
