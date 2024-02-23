package net.mehvahdjukaar.amendments.client.model;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.mehvahdjukaar.moonlight.api.client.model.CustomGeometry;
import net.mehvahdjukaar.moonlight.api.client.model.CustomModelLoader;
import net.minecraft.util.GsonHelper;

import static net.mehvahdjukaar.moonlight.api.client.model.NestedModelLoader.parseModel;

public class CauldronModelLoader implements CustomModelLoader {

    @Override
    public CustomGeometry deserialize(JsonObject json, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        var cauldron = json.get("cauldron");
        var fluid = json.get("fluid");
        boolean translucent;
        if (json.has("translucent")) {
            translucent = GsonHelper.getAsBoolean(json, "translucent");
        } else {
            translucent = false;
        }
        return (modelBaker, spriteGetter, transform, location) -> {

            var c = CustomModelLoader.parseModel(cauldron, modelBaker, spriteGetter, transform, location);
            var f = CustomModelLoader.parseModel(fluid, modelBaker, spriteGetter, transform, location);
            return new CauldronBakedModel(c, f, transform, translucent);
        };
    }


}
