package net.mehvahdjukaar.amendments.integration;

import it.crystalnest.soul_fire_d.api.FireManager;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class SoulFiredCompat {


    public static void setSecondsOnFire(Entity target, int duration, ItemStack stack) {
        FireManager.setOnFire(target, duration, getFireType(stack));
    }

    private static ResourceLocation getFireType(ItemStack stack) {
        var fires = FireManager.getFireTypes();
        ResourceLocation id = Utils.getID(stack.getItem());

        String path = id.getPath();
        for (var f : fires) {
            if (path.contains(f.getPath())) {
                return f;
            }
        }
        return FireManager.ensure(new ResourceLocation("fire"));
    }
}
