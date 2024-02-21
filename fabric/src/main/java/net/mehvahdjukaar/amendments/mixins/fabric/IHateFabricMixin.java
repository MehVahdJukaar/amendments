package net.mehvahdjukaar.amendments.mixins.fabric;

import net.mehvahdjukaar.amendments.fabric.AmendmentsFabric;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Deprecated(forRemoval = true)
@Mixin(ResourceLocation.class)
public class IHateFabricMixin {


    @Mutable
    @Shadow
    @Final
    private String path;
    @Mutable
    @Shadow
    @Final
    private String namespace;

    @Inject(method = "<init>(Ljava/lang/String;Ljava/lang/String;Lnet/minecraft/resources/ResourceLocation$Dummy;)V",
            at = @At("TAIL"))
    public void highTechFabricRemapEvent(String string, String string2, ResourceLocation.Dummy dummy, CallbackInfo ci) {
        ResourceLocation remapped = AmendmentsFabric.shouldRemap(string, string2);
        if (remapped != null) {
            path = remapped.getPath();
            namespace = remapped.getNamespace();
        }
    }
}
