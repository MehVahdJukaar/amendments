package net.mehvahdjukaar.amendments.client.colors;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.ChargedProjectiles;
import org.jetbrains.annotations.Nullable;

public record CrossbowArrowTint() implements ItemTintSource {

    public static final MapCodec<CrossbowArrowTint> MAP_CODEC = MapCodec.unit(new CrossbowArrowTint());

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        ChargedProjectiles projectiles = stack.get(DataComponents.CHARGED_PROJECTILES);
        if (projectiles == null || projectiles.isEmpty()) return -1;
        ItemStackTemplate arrow = projectiles.items().getFirst();
        Item i = arrow.item().value();
        if (i == Items.TIPPED_ARROW) {
            PotionContents contents = arrow.get(DataComponents.POTION_CONTENTS);
            return contents == null ? -1 : ARGB.opaque(contents.getColor());
        }
        if (i == Items.SPECTRAL_ARROW) return 0xFFFFB700;
        return -1;
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() {
        return MAP_CODEC;
    }
}
