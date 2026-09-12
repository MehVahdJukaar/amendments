package net.mehvahdjukaar.amendments.client.renderers;

import net.mehvahdjukaar.amendments.reg.ModBlockProperties;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.sprite.SpriteId;
import org.jetbrains.annotations.Nullable;

public class HangingSignRenderData {

    public boolean enhanced;
    public boolean wallSign;
    public boolean attached;
    //rats pirate signs
    public boolean translucent;
    public boolean veryNear;
    public float yaw;
    public float swing;
    public float colorMult = 1;
    public @Nullable ModBlockProperties.PostType left;
    public @Nullable ModBlockProperties.PostType right;
    public @Nullable SpriteId extensionSprite;
    //supplementaries flags
    public @Nullable SpriteId frontFlag;
    public @Nullable SpriteId backFlag;
    public final ItemStackRenderState frontItem = new ItemStackRenderState();
    public final ItemStackRenderState backItem = new ItemStackRenderState();
}
