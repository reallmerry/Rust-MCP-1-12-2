package io.reallmerry.rstudio.townycenturies.data;

import io.reallmerry.rstudio.townycenturies.Age;
import io.reallmerry.rstudio.townycenturies.TownyCenturies;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TownAgeManager {

    private final TownyCenturies plugin;
    private final Map<String, Age> cache = new ConcurrentHashMap<>();
    private File file;
    private YamlConfiguration data;

    public TownAgeManager(TownyCenturies plugin) {
        this.plugin = plugin;
    }

    public void load() {
        try {
            file = new File(plugin.getDataFolder(), "town-ages.yml");
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            data = YamlConfiguration.loadConfiguration(file);
            // Предзагрузка в кэш
            for (String town : data.getKeys(false)) {
                try {
                    Age a = Age.fromName(data.getString(town));
                    cache.put(town.toLowerCase(), a);
                } catch (Exception ignored) {}
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load town-ages.yml: " + e.getMessage());
        }
    }

    public Age getAgeForTown(String town, Age def) {
        if (town == null || town.isEmpty()) return def;
        return cache.getOrDefault(town.toLowerCase(), def);
    }

    public void setAgeForTown(String town, Age age) {
        cache.put(town.toLowerCase(), age);
        data.set(town, age.name());
        saveAsync();
    }

    public void saveSync() {
        try {
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save town-ages.yml: " + e.getMessage());
        }
    }
    // Вернуть текущую эпоху города (или DEFAULT, если не найден)
    public Age getAge(String townName) {
        return getAgeForTown(townName, Age.PRIMITIVE); // DEFAULT — или любая твоя стартовая эпоха
    }

    // Установить эпоху для города
    public void setAge(String townName, Age age) {
        setAgeForTown(townName, age);
    }

    private void saveAsync() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::saveSync);
    }
}