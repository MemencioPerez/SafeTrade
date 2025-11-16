package io.github.cjcool06.safetrade.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NumberNBT;
import net.minecraft.nbt.StringNBT;

import java.util.HashMap;
import java.util.Map;

public class GsonUtils {
    public static final Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
    public static final Gson uglyGson = new GsonBuilder().create();

    public static String serialize(CompoundNBT nbt) {
        Map<String, Object> map = nbtToMap(nbt);
        return uglyGson.toJson(map);
    }

    @SuppressWarnings("unchecked")
    public static CompoundNBT deserialize(String json) {
        Map<String, Object> map = uglyGson.fromJson(json, Map.class);
        return nbtFromMap(map);
    }

    public static Map<String, Object> nbtToMap(CompoundNBT nbt) {
        Map<String, Object> map = new HashMap<>();

        for (String key : nbt.getAllKeys()) {
            try {
                INBT base = nbt.get(key);

                if (base instanceof StringNBT)
                    map.put(key, base.getAsString());
                else if (base instanceof NumberNBT)
                    map.put(key, ((NumberNBT)base).getAsDouble());
                else if (base instanceof CompoundNBT)
                    map.put(key, nbtToMap((CompoundNBT)base));
            }
            catch (Exception ignored) {

            }
        }

        return map;
    }

    @SuppressWarnings("unchecked")
    public static CompoundNBT nbtFromMap(Map<String, Object> map) {
        CompoundNBT nbt = new CompoundNBT();

        for (String key : map.keySet()) {
            try {
                if (map.get(key) instanceof String)
                    nbt.putString(key, (String)map.get(key));
                else if (map.get(key) instanceof Map)
                    nbt.put(key, nbtFromMap((Map<String, Object>)map.get(key)));
                else
                    nbt.putDouble(key, (Double)map.get(key));
            }
            catch (Exception ignored) {

            }
        }

        return nbt;
    }
}
