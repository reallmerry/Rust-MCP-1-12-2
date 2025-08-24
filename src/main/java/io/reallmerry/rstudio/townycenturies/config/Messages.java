package io.reallmerry.rstudio.townycenturies.config;


import io.reallmerry.rstudio.townycenturies.Age;
import io.reallmerry.rstudio.townycenturies.TownyCenturies;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
// Для Spigot 1.16.5: сериализуем в legacy (§-коды) для отправки как String
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;

public class Messages {
    private final TownyCenturies plugin;
    private final MiniMessage mm;
    private FileConfiguration cfg;

    public Messages(TownyCenturies plugin, MiniMessage mm) {
        this.plugin = plugin;
        this.mm = mm;
    }

    public void load() {
        this.cfg = plugin.getConfig();
        // Значения по умолчанию уже в config.yml (saveDefaultConfig в onEnable)
    }

    public void send(CommandSender to, String path, String... placeholders) {
        String raw = cfg.getString("messages." + path, "<gray>[" + plugin.getName() + "]</gray> <red>Message missing: " + path + "</red>");
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            raw = raw.replace("%" + placeholders[i] + "%", placeholders[i + 1]);
        }
        Component comp = mm.deserialize(raw); // MiniMessage parse
        String legacy = LegacyComponentSerializer.legacySection().serialize(comp);
        to.sendMessage(legacy);
    }

    public String color(String raw) {
        if (raw == null) return "";
        boolean looksLikeMini = raw.indexOf('<') != -1 && raw.indexOf('>') != -1;
        if (looksLikeMini) {
            Component comp = mm.deserialize(raw);
            return LegacyComponentSerializer.legacySection().serialize(comp);
        }
        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    public void sendDenyCraft(Player p, String itemName, Age required) {
        String raw = cfg.getString("messages.craft-denied",
                "<red>Этот предмет недоступен на вашем веке.</red> <gray>(нужно: <yellow>%required%</yellow>)</gray>");
        raw = raw.replace("%required%", required.name()).replace("%item%", itemName);
        String legacy = LegacyComponentSerializer.legacySection().serialize(mm.deserialize(raw));
        p.sendMessage(legacy);
    }

    public String toLegacy(Component comp) {
        return LegacyComponentSerializer.legacySection().serialize(comp);
    }

    public String fromRaw(String raw) {
        return LegacyComponentSerializer.legacySection().serialize(mm.deserialize(raw));
    }
}