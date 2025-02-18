package net.mehvahdjukaar.amendments.common.entity;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ImprovedFireball {

    @Nullable
    Vec3 amendments$getLasParticlePos();

    void amendments$setLastParticlePos(@NotNull Vec3 pos);

}
