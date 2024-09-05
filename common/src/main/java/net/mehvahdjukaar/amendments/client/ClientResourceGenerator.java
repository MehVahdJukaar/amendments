package net.mehvahdjukaar.amendments.client;

import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.Lighting;
import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.amendments.common.CakeRegistry;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.mixins.SignRendererAccessor;
import net.mehvahdjukaar.moonlight.api.events.AfterLanguageLoadEvent;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.StaticResource;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynClientResourcesGenerator;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicTexturePack;
import net.mehvahdjukaar.moonlight.api.resources.textures.ImageTransformer;
import net.mehvahdjukaar.moonlight.api.resources.textures.Palette;
import net.mehvahdjukaar.moonlight.api.resources.textures.Respriter;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureImage;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;

import java.util.stream.Stream;

public class ClientResourceGenerator extends DynClientResourcesGenerator {
    public ClientResourceGenerator() {
        super(new DynamicTexturePack(Amendments.res("generated_pack")));
        this.dynamicPack.addNamespaces("minecraft");
    }

    @Override
    public Logger getLogger() {
        return Amendments.LOGGER;
    }

    @Override
    public boolean dependsOnLoadedPacks() {
        return true;
    }

    @Override
    public void regenerateDynamicAssets(ResourceManager manager) {

        //need this here for reasons I forgot
        WallLanternModelsManager.refreshModels(manager);

        generateJukeboxAssets(manager);

        generateDoubleCakesAssets(manager);

        generateHangingSignAssets(manager);

        if (ClientConfigs.COLORED_ARROWS.get()) {
            this.dynamicPack.addItemModel(new ResourceLocation("crossbow_arrow"), JsonParser.parseString(
                    """ 
                            {
                                "parent": "item/crossbow",
                                "textures": {
                                    "layer0": "item/crossbow_arrow_base",
                                    "layer1": "item/crossbow_arrow_tip"
                                }
                            }
                            """));
        }

        if (ClientConfigs.JUKEBOX_MODEL.get()) {
            this.dynamicPack.addItemModel(new ResourceLocation("jukebox"), JsonParser.parseString(
                    """ 
                            {
                              "parent": "amendments:block/jukebox"
                            }
                            """));
            this.dynamicPack.addBlockState(new ResourceLocation("jukebox"), JsonParser.parseString(
                    """ 
                            {
                              "variants": {
                                "has_record=true": {
                                  "model": "amendments:block/jukebox_on"
                                },
                                "has_record=false": {
                                  "model": "amendments:block/jukebox"
                                }
                              }
                            }
                            """));
        }
    }

    private void generateHangingSignAssets(ResourceManager manager) {
        ImageTransformer transformer = ImageTransformer.builder(32, 64, 16, 16)
                .copyRect(26, 0, 2, 4, 4, 0)
                .copyRect(26, 8, 6, 8, 4, 4)
                .copyRect(28, 24, 4, 8, 0, 4)
                .copyRect(26, 20, 2, 4, 6, 0)
                //cheaty as it has to be flipped. todo: find a way to rotate it instead as this work wotk with packs
                .copyRect(26, 28, 1, 8, 11, 4)
                .copyRect(27, 28, 1, 8, 10, 4)
                .build();

        for (WoodType w : WoodTypeRegistry.getTypes()) {
            Block hangingSign = w.getBlockOfThis("hanging_sign");
            if (hangingSign == null) continue;
            //hanging sign extension textures
            net.minecraft.world.level.block.state.properties.WoodType vanilla = w.toVanilla();
            if (vanilla == null) {
                Amendments.LOGGER.error("Vanilla wood type for wood {} was null. This is a bug", w);
                continue;
            }
            Material hangingSignMaterial = Sheets.getHangingSignMaterial(vanilla);
            if (hangingSignMaterial == null) {
                try {
                    BlockEntity be = ((EntityBlock) hangingSign).newBlockEntity(BlockPos.ZERO, hangingSign.defaultBlockState());
                    BlockEntityRenderer<?> renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(be);
                    if (renderer instanceof SignRendererAccessor sr) {
                        hangingSignMaterial = sr.invokeGetSignMaterial(vanilla);
                    }
                } catch (Exception e) {
                    Amendments.LOGGER.error("Failed to get hanging sign material for wood (from block entity renderer) {}, ", w, e);
                    continue;
                }
            }
            if (hangingSignMaterial == null) {
                Amendments.LOGGER.error("Hanging sign material for wood {} was null. " +
                        "This is likely due to some mod not registering their wood type properly by not adding it to the vanilla texture map. Sheets.getHangingSignMaterial is NOT Nullable, i shouldn't even have this check.", w);
                continue;
            }
            try (TextureImage vanillaTexture = TextureImage.open(manager,
                    hangingSignMaterial.texture())) {
                TextureImage flipped = vanillaTexture.createRotated(Rotation.CLOCKWISE_90);
                TextureImage newIm = flipped.createResized(0.5f, 0.25f);
                newIm.clear();

                transformer.apply(flipped, newIm);
                flipped.close();
                this.dynamicPack.addAndCloseTexture(Amendments.res("entity/signs/hanging/" + w.getVariantId("extension")), newIm);
            } catch (Exception e) {
                Amendments.LOGGER.warn("Failed to generate hanging sign extension texture for {}, ", w, e);
            }
        }
        if (CompatHandler.FARMERS_DELIGHT) {
            //hanging sign extension textures
            try (TextureImage vanillaTexture = TextureImage.open(manager,
                    new ResourceLocation("farmersdelight:entity/signs/hanging/canvas"))) {
                TextureImage flipped = vanillaTexture.createRotated(Rotation.CLOCKWISE_90);
                TextureImage newIm = flipped.createResized(0.5f, 0.25f);
                newIm.clear();

                transformer.apply(flipped, newIm);
                flipped.close();
                this.dynamicPack.addAndCloseTexture(Amendments.res("entity/signs/hanging/farmersdelight/extension_canvas"), newIm);
            } catch (Exception e) {
                Amendments.LOGGER.warn("Failed to generate hanging sign extension texture for {}, ", "canvas sign", e);
            }
        }
    }

    private void generateJukeboxAssets(ResourceManager manager) {

        ImageTransformer transformer = ImageTransformer.builder(16, 16, 16, 16)
                .copyRect(5, 6, 3, 2, 6, 6)
                .copyRect(8, 6, 1, 1, 9, 7)
                .copyRect(7, 7, 3, 2, 7, 8)
                .copyRect(6, 8, 1, 1, 6, 8)
                .copyRect(9, 6, 1, 1, 9, 6)
                .copyRect(5, 8, 1, 1, 6, 9)
                .build();
        try (TextureImage fallback = TextureImage.open(manager,
                Amendments.res("block/music_discs/music_disc_generic"));
             TextureImage template = TextureImage.open(manager,
                     Amendments.res("block/music_discs/music_disc_template"));
             TextureImage mask = TextureImage.open(manager,
                     Amendments.res("block/music_discs/music_disc_mask"));) {
            Respriter respriter = Respriter.of(template);

            for (var e : AmendmentsClient.getAllRecords().entrySet()) {
                ResourceLocation res = Amendments.res(e.getValue().texture().getPath());
                if (alreadyHasTextureAtLocation(manager, res)) continue;
                //hanging sign extension textures
                try (TextureImage vanillaTexture = TextureImage.open(manager,
                        RPUtils.findFirstItemTextureLocation(manager, e.getKey()))) {

                    var p = Palette.fromImage(vanillaTexture, mask);
                    amendPalette(p);
                    TextureImage newImage = respriter.recolor(p);
                    transformer.apply(vanillaTexture, newImage);
                    if (newImage.getImage().getPixelRGBA(6, 6) == p.get(p.size() - 2).rgb().toInt()) {
                        newImage.setFramePixel(0, 6, 6, p.getLightest().rgb().toInt());
                        newImage.setFramePixel(0, 9, 9, p.getLightest().rgb().toInt());
                    }
                    this.dynamicPack.addAndCloseTexture(res, newImage);
                } catch (Exception ex) {
                    getLogger().warn("Failed to generate record item texture for {}. No model / texture found", e.getKey());
                    this.dynamicPack.addAndCloseTexture(res, fallback.makeCopy());
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void amendPalette(Palette p) {
        float averLum = p.getAverageLuminanceStep();
        if (averLum > 0.06) {
            p.increaseInner();
        }
        var darkest = p.getDarkest();
        var beforeDarkest = p.get(1);
        if (beforeDarkest.luminance() - darkest.luminance() > averLum - 0.005) {
            p.remove(darkest);
            p.increaseDown();
        }
    }


    private void generateDoubleCakesAssets(ResourceManager manager) {
        StaticResource[] cakeModels = Stream.of("full", "slice1", "slice2", "slice3", "slice4", "slice5", "slice6")
                .map(s -> StaticResource.getOrLog(manager,
                        ResType.BLOCK_MODELS.getPath(Amendments.res("double_cake/vanilla_" + s)))).toArray(StaticResource[]::new);

        StaticResource doubleCakeModelState = StaticResource.getOrLog(manager,
                ResType.BLOCKSTATES.getPath(Amendments.res("double_cake")));
        for (var t : CakeRegistry.INSTANCE.getValues()) {
            if (!t.isVanilla()) {
                try {
                    ResourceLocation dcId = Utils.getID(t.getBlockOfThis("double_cake"));
                    ResourceLocation top = RPUtils.findFirstBlockTextureLocation(manager, t.cake, s -> s.contains("top"));
                    ResourceLocation side = RPUtils.findFirstBlockTextureLocation(manager, t.cake, s -> s.contains("side"));
                    ResourceLocation bottom = RPUtils.findFirstBlockTextureLocation(manager, t.cake, s -> s.contains("bottom"));
                    ResourceLocation inner = RPUtils.findFirstBlockTextureLocation(manager, t.cake, s -> s.contains("inner"));

                    for (var m : cakeModels) {
                        addSimilarJsonResource(manager, m, s -> s
                                        .replace("amendments:block/double_cake", "")
                                        .replace("minecraft:block/cake", "")
                                        .replace("\"/", "\"amendments:block/double_cake/")
                                        .replace("_top", top.toString())
                                        .replace("_side", side.toString())
                                        .replace("_inner", inner.toString())
                                        .replace("_bottom", bottom.toString()),
                                s -> s.replace("vanilla", dcId.getPath()));
                    }
                    addSimilarJsonResource(manager, doubleCakeModelState,
                            s -> s.replace("vanilla", dcId.getPath()),
                            s -> s.replace("double_cake", dcId.getPath()));
                } catch (Exception e) {
                    Amendments.LOGGER.error("Failed to generate model for double cake {},", t);
                }
            }
        }
    }


    @Override
    public void addDynamicTranslations(AfterLanguageLoadEvent languageEvent) {
        if (languageEvent.isDefault()) {
            languageEvent.addEntry("item.minecraft.lingering_potion.effect.empty", "Lingering Mixed Potion");
            languageEvent.addEntry("item.minecraft.splash_potion.effect.empty", "Splash Mixed Potion");
            languageEvent.addEntry("item.minecraft.potion.effect.empty", "Mixed Potion");
        }
    }
}
