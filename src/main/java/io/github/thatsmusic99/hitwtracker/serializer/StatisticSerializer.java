package io.github.thatsmusic99.hitwtracker.serializer;

import com.google.gson.*;
import io.github.thatsmusic99.hitwtracker.game.Statistic;

import java.lang.reflect.Type;
import java.util.Date;

public class StatisticSerializer implements JsonSerializer<Statistic>, JsonDeserializer<Statistic> {

    @Override
    public JsonElement serialize(Statistic src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.add("placement", new JsonPrimitive(src.placement()));
        obj.add("ties", toJSON(src.ties()));
        obj.add("map", new JsonPrimitive(src.map()));
        obj.add("deathCause", new JsonPrimitive(src.deathCause()));
        obj.add("walls", new JsonPrimitive(src.walls()));
        obj.add("time", new JsonPrimitive(src.seconds()));
        obj.add("score", new JsonPrimitive(src.score()));
        obj.add("date", new JsonPrimitive(src.date().getTime()));
        obj.add("plobby", new JsonPrimitive(src.plobby()));
        return obj;
    }

    @Override
    public Statistic deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject object = json.getAsJsonObject();
        final byte placement = object.get("placement").getAsByte();
        final String[] ties = fromJSON(object.get("ties").getAsJsonArray());
        final String map = object.get("map").getAsString();
        final String deathCause = object.get("deathCause").getAsString();
        final byte walls = object.get("walls").getAsByte();
        final short time = object.get("time").getAsShort();
        final short score = object.get("score").getAsShort();
        final boolean plobby = object.get("plobby") != null && object.get("plobby").getAsBoolean();
        final Date date = new Date(object.get("date").getAsLong());
        return new Statistic(placement, ties, map, deathCause, walls, time, score, plobby, date);
    }

    private JsonArray toJSON(String[] arr) {
        final JsonArray array = new JsonArray(arr.length);
        for (String element : arr) {
            array.add(element);
        }
        return array;
    }

    private String[] fromJSON(JsonArray arr) {
        final String[] array = new String[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            array[i] = arr.get(i).getAsString();
        }
        return array;
    }
}
