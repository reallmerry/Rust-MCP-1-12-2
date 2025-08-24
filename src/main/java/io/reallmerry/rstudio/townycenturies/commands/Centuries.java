package io.reallmerry.rstudio.townycenturies.commands;

import io.reallmerry.rstudio.townycenturies.Age;
import io.reallmerry.rstudio.townycenturies.api.TownAgeService;
import io.reallmerry.rstudio.townycenturies.logic.PolicyMode;
import io.reallmerry.rstudio.townycenturies.TownyCenturies;
import io.reallmerry.rstudio.townycenturies.config.Messages;
import io.reallmerry.rstudio.townycenturies.data.TownAgeManager;
import io.reallmerry.rstudio.townycenturies.logic.AllowedRecipes;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.stream.Collectors;

public class Centuries implements CommandExecutor, TabCompleter {

    private final TownyCenturies plugin;
    private final TownAgeManager ages;
    private final AllowedRecipes allowed;
    private final Messages msg;

    public Centuries(TownyCenturies plugin, TownAgeManager ages, AllowedRecipes allowed, Messages msg) {
        this.plugin = plugin;
        this.ages = ages;
        this.allowed = allowed;
        this.msg = msg;
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            msg.send(s, "usage", "label", label);
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "get": {
                if (!s.hasPermission("ages.view")) { deny(s); return true; }
                handleGet(s, args);
                return true;
            }
            case "list": {
                if (!s.hasPermission("ages.view")) { deny(s); return true; }
                handleList(s);
                return true;
            }
            case "set": {
                if (!s.hasPermission("ages.admin")) { deny(s); return true; }
                if (args.length < 3) { msg.send(s, "usage-set", "label", label); return true; }
                String town = joinArgs(args, 1, args.length - 1);
                String ageName = args[args.length - 1];
                handleSetForce(s, town, ageName);
                return true;
            }
            case "mode": {
                if (!s.hasPermission("ages.admin")) { deny(s); return true; }
                if (args.length < 2) { msg.send(s, "usage-mode", "label", label); return true; }
                String m = args[1].toLowerCase(Locale.ROOT);
                PolicyMode newMode = (m.equals("whitelist")) ? PolicyMode.WHITELIST : PolicyMode.GATED_ONLY;
                plugin.setPolicyMode(newMode);
                plugin.getConfig().set("policy", newMode == PolicyMode.WHITELIST ? "whitelist" : "gated-only");
                plugin.saveConfig();
                msg.send(s, "mode-changed", "mode", newMode == PolicyMode.WHITELIST ? "whitelist" : "gated-only");
                return true;
            }
            case "reload": {
                if (!s.hasPermission("ages.admin")) { deny(s); return true; }
                plugin.reloadConfig();
                msg.load();
                allowed.buildFromDefaultsAndConfig();
                plugin.setPolicyMode(PolicyMode.fromString(plugin.getConfig().getString("policy", "gated-only")));
                msg.send(s, "reloaded");
                return true;
            }
            case "gated": {
                // Сервисный алиас для отладки: показать, какие материалы вообще под контролем
                if (!s.hasPermission("ages.admin")) { deny(s); return true; }
                String list = allowed.allGated().stream()
                        .map(Material::name).sorted().limit(200) // защитимся от спама
                        .collect(Collectors.joining(", "));
                msg.send(s, "gated-list", "materials", list);
                return true;
            }
            default:
                msg.send(s, "usage", "label", label);
                return true;
        }
    }

    private void handleGet(CommandSender s, String[] args) {
        if (args.length < 2) {
            // Требуем явное указание города, чтобы не тянуть Towny здесь
            msg.send(s, "usage-get");
            return;
        }
        String town = joinArgs(args, 1, args.length);
        Age age = ages.getAgeForTown(town, plugin.defaultAge());
        msg.send(s, "view-town", "town", town, "age", age.name());
    }

    private void handleList(CommandSender s) {
        String names = Arrays.stream(Age.values()).map(Enum::name).collect(Collectors.joining(" → "));
        msg.send(s, "list-ages", "ages", names);
    }

    private void handleSetForce(CommandSender s, String town, String ageName) {
        Age age;
        try {
            age = Age.fromName(ageName);
        } catch (Exception e) {
            msg.send(s, "unknown-age", "age", ageName);
            return;
        }
        Age old = ages.getAgeForTown(town, plugin.defaultAge());
        // Форсируем без валидации (админ)
        ages.setAgeForTown(town, age);
        msg.send(s, "set-success", "town", town, "old", old.name(), "age", age.name());
    }

    private void handleNextValidated(CommandSender s, String town) {
        TownAgeService svc = Bukkit.getServicesManager().load(TownAgeService.class);
        if (svc == null) {
            msg.send(s, "no-service");
            return;
        }
        Age current = svc.getTownAge(town);
        Age next = Age.nextOf(current);
        if (next == current) {
            msg.send(s, "next-max", "town", town, "age", current.name());
            return;
        }
        boolean ok = svc.setTownAgeValidated(town, next);
        if (!ok) {
            msg.send(s, "next-denied", "town", town, "from", current.name(), "to", next.name());
            return;
        }
        msg.send(s, "next-success", "town", town, "from", current.name(), "to", next.name());
    }

    private void deny(CommandSender s) {
        msg.send(s, "no-perm");
    }

    private String joinArgs(String[] args, int start, int endExclusive) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < endExclusive; i++) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(args[i]);
        }
        return sb.toString();
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command cmd, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            List<String> subs = Arrays.asList("get", "list", "set", "mode", "reload");
            StringUtil.copyPartialMatches(args[0], subs, out);
            Collections.sort(out);
            return out;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "get": {
                if (args.length == 2) {
                    // без Towny не подсказываем список городов; оставим пустым
                    return Collections.emptyList();
                }
                return Collections.emptyList();
            }
            case "set": {
                // Последний аргумент — имя возраста
                if (args.length >= 3) {
                    List<String> agesList = Arrays.stream(Age.values()).map(a -> a.name().toLowerCase()).collect(Collectors.toList());
                    StringUtil.copyPartialMatches(args[args.length - 1], agesList, out);
                    Collections.sort(out);
                    return out;
                }
                return Collections.emptyList();
            }
            case "next": {
                // ожидание: /ages next <town>
                return Collections.emptyList();
            }
            case "mode": {
                if (args.length == 2) {
                    StringUtil.copyPartialMatches(args[1], Arrays.asList("gated-only", "whitelist"), out);
                    return out;
                }
                return Collections.emptyList();
            }
            default:
                return Collections.emptyList();
        }
    }
}