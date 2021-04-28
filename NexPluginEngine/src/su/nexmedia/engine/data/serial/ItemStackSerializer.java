package su.nexmedia.engine.data.serial;

import java.lang.reflect.Type;

import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import su.nexmedia.engine.utils.ItemUT;

public class ItemStackSerializer implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

    @Override
    public JsonElement serialize(ItemStack item, Type type, JsonSerializationContext context) {
        JsonObject o = new JsonObject();
        o.addProperty("data64", ItemUT.toBase64(item));

        return o;
    }

    @Override
    public ItemStack deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject o = json.getAsJsonObject();
        ItemStack item = ItemUT.fromBase64(o.get("data64").getAsString());

        return item;
    }

}
