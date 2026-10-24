package org.exampl.inventory_ui_beautification.client.hud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class HudConfig {

    public static final class Entry {
        public boolean enabled = true;
        public int anchor = 0;
        public int dx = 0;
        public int dy = 0;
    }

    public static final Map<String, Entry> entries = new LinkedHashMap<>();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private HudConfig() {
    }

    public static Entry of(String id) {
        Entry e = entries.get(id);
        if (e == null) {
            e = new Entry();
            entries.put(id, e);
        }
        return e;
    }

    private static void setDefault(String id, boolean enabled, int anchor, int dx, int dy) {
        Entry e = of(id);
        e.enabled = enabled;
        e.anchor = anchor;
        e.dx = dx;
        e.dy = dy;
    }

    private static void loadDefaults() {
        entries.clear();
        setDefault("health", true, 0, 4, 4);
        setDefault("hunger", true, 0, 4, 20);
        setDefault("xp", true, 3, 0, -27);
        setDefault("effects", true, 1, 8, 8);
        setDefault("target", true, 4, 0, 30);
        setDefault("info", true, 2, 6, 6);
        setDefault("combat", true, 3, 0, -58);
    }

    public static void load() {
        loadDefaults();
        try {
            Path p = path();
            if (!Files.exists(p)) return;
            JsonObject root = JsonParser.parseString(Files.readString(p)).getAsJsonObject();
            if (root.has("modules") && root.get("modules").isJsonObject()) {
                JsonObject mods = root.getAsJsonObject("modules");
                for (String id : entries.keySet()) {
                    if (mods.has(id) && mods.get(id).isJsonObject()) {
                        JsonObject jo = mods.getAsJsonObject(id);
                        Entry e = entries.get(id);
                        if (jo.has("enabled")) e.enabled = jo.get("enabled").getAsBoolean();
                        if (jo.has("anchor")) e.anchor = jo.get("anchor").getAsInt();
                        if (jo.has("dx")) e.dx = jo.get("dx").getAsInt();
                        if (jo.has("dy")) e.dy = jo.get("dy").getAsInt();
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void save() {
        try {
            JsonObject root = new JsonObject();
            JsonObject mods = new JsonObject();
            for (Map.Entry<String, Entry> m : entries.entrySet()) {
                Entry e = m.getValue();
                JsonObject jo = new JsonObject();
                jo.addProperty("enabled", e.enabled);
                jo.addProperty("anchor", e.anchor);
                jo.addProperty("dx", e.dx);
                jo.addProperty("dy", e.dy);
                mods.add(m.getKey(), jo);
            }
            root.add("modules", mods);
            Files.writeString(path(), GSON.toJson(root));
        } catch (IOException ignored) {
        }
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("inventory_ui_beautification.json");
    }
}
