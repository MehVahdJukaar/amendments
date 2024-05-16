package net.mehvahdjukaar.amendments.integration;

import fuzs.thinair.api.v1.AirQualityHelper;
import fuzs.thinair.world.level.block.SafetyLanternBlock;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class ThinAirCompat {
    public static BlockState maybeSetAirQuality(BlockState state, LivingEntity entity, ItemStack itemStack) {
        if (state.hasProperty(SafetyLanternBlock.AIR_QUALITY)) {
            return state.setValue(SafetyLanternBlock.AIR_QUALITY, AirQualityHelper.INSTANCE
                    .getAirQualityAtLocation(entity));
        }
        return state;
    }
}
