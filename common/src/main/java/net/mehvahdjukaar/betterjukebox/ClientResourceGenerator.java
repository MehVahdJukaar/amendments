package net.mehvahdjukaar.betterjukebox;

import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynClientResourcesGenerator;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicTexturePack;
import net.mehvahdjukaar.moonlight.api.resources.textures.*;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.Logger;

public class ClientResourceGenerator extends DynClientResourcesGenerator {
    protected ClientResourceGenerator() {
        super(new DynamicTexturePack(BetterJukeboxes.res("generated_pack")));
    }

    @Override
    public Logger getLogger() {
        return BetterJukeboxes.LOGGER;
    }

    @Override
    public boolean dependsOnLoadedPacks() {
        return true;
    }

    @Override
    public void regenerateDynamicAssets(ResourceManager manager) {

        ImageTransformer transformer = ImageTransformer.builder(16, 16, 16, 16)
                .copyRect(5, 6, 3, 2, 6, 6)
                .copyRect(8, 6, 1, 1, 9, 7)
                .copyRect(7, 7, 3, 2, 7, 8)
                .copyRect(6, 8, 1, 1, 6, 8)
                .copyRect(9, 6, 1, 1, 9, 6)
                .copyRect(5, 8, 1, 1, 6, 9)
                .build();
        try (TextureImage template = TextureImage.open(manager,
                BetterJukeboxes.res("block/music_disc_template"));
             TextureImage mask = TextureImage.open(manager,
                     BetterJukeboxes.res("block/music_disc_mask"));) {
            Respriter respriter = Respriter.of(template);

            for (var e : BetterJukeboxesClient.getAllRecords().entrySet()) {
                //hanging sign extension textures
                try (TextureImage vanillaTexture = TextureImage.open(manager,
                        RPUtils.findFirstItemTextureLocation(manager, e.getKey()))) {

                    var p = Palette.fromImage(vanillaTexture, mask);
                    if (p.getAverageLuminanceStep() > 0.06) {
                        var newDark = p.increaseInner();
                        PaletteColor darkest = p.getDarkest();
                        p.remove(darkest);
                        p.add(new PaletteColor(newDark.lab().mixWith(darkest.lab())));
                    }
                    TextureImage newImage = respriter.recolor(p);
                    transformer.apply(vanillaTexture, newImage);
                    this.dynamicPack.addAndCloseTexture(BetterJukeboxes.res(e.getValue().texture().getPath()), newImage);
                } catch (Exception ex) {
                    getLogger().warn("Failed to generate record item texture for {}", e.getKey(), ex);
                }
            }
        } catch (Exception ignored) {

        }
    }
}
