package org.example.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * JSON 工具：基于 Gson，注册 LocalDateTime / LocalDate 的序列化与反序列化。
 */
public class JsonUtil {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter D_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                @Override
                public JsonElement serialize(LocalDateTime src, Type t, JsonSerializationContext ctx) {
                    return src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.format(DT_FMT));
                }
            })
            .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                @Override
                public LocalDateTime deserialize(JsonElement json, Type t, JsonDeserializationContext ctx) {
                    if (json == null || json.isJsonNull()) {
                        return null;
                    }
                    String v = json.getAsString();
                    if (v == null || v.isEmpty()) {
                        return null;
                    }
                    if (v.length() == 10) {
                        return LocalDate.parse(v, D_FMT).atStartOfDay();
                    }
                    String n = v.replace('T', ' ');
                    if (n.length() == 16) {
                        n = n + ":00";
                    }
                    return LocalDateTime.parse(n.substring(0, 19), DT_FMT);
                }
            })
            .registerTypeAdapter(LocalDate.class, new JsonSerializer<LocalDate>() {
                @Override
                public JsonElement serialize(LocalDate src, Type t, JsonSerializationContext ctx) {
                    return src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.format(D_FMT));
                }
            })
            .registerTypeAdapter(LocalDate.class, new JsonDeserializer<LocalDate>() {
                @Override
                public LocalDate deserialize(JsonElement json, Type t, JsonDeserializationContext ctx) {
                    if (json == null || json.isJsonNull()) {
                        return null;
                    }
                    String v = json.getAsString();
                    return (v == null || v.isEmpty()) ? null : LocalDate.parse(v.substring(0, 10), D_FMT);
                }
            })
            .serializeNulls()
            .create();

    public static String toJson(Object o) {
        return GSON.toJson(o);
    }

    public static <T> T fromJson(String s, Class<T> clazz) {
        return GSON.fromJson(s, clazz);
    }

    public static <T> T fromJson(String s, Type type) {
        return GSON.fromJson(s, type);
    }
}
