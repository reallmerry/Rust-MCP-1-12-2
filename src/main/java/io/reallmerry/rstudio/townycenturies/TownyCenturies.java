package io.reallmerry.rstudio.townycenturies;

import io.reallmerry.rstudio.townycenturies.Age;
import io.reallmerry.rstudio.townycenturies.api.TownAgeService;
import io.reallmerry.rstudio.townycenturies.commands.Centuries;
import io.reallmerry.rstudio.townycenturies.config.Messages;
import io.reallmerry.rstudio.townycenturies.data.TownAgeManager;
import io.reallmerry.rstudio.townycenturies.hook.TownyHook;
import io.reallmerry.rstudio.townycenturies.internal.TownAgeServiceImpl;
import io.reallmerry.rstudio.townycenturies.logic.AllowedRecipes;
import io.reallmerry.rstudio.townycenturies.logic.CraftingListener;
import io.reallmerry.rstudio.townycenturies.logic.PolicyMode;
import io.reallmerry.rstudio.townycenturies.menu.AgesMenu;
import io.reallmerry.rstudio.townycenturies.menu.TownInventoryMenu;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class TownyCenturies extends JavaPlugin {

    private static TownyCenturies instance;

    private TownAgeService serviceInstance;
    private TownAgeManager townAgeManager;
    private AllowedRecipes allowedRecipes;
    private TownyHook townyHook;
    private Messages messages;
    private PolicyMode policyMode;
    private MiniMessage miniMessage;

    public static TownyCenturies get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        ConfigurationSection agesMenuConfig = getConfig().getConfigurationSection("menus.ages");
        AgesMenu agesMenu = new AgesMenu(this, townAgeManager, townyHook, messages, agesMenuConfig);
        TownInventoryMenu townInvMenu = new TownInventoryMenu(this, townyHook);
        // MiniMessage и сообщения
        this.miniMessage = MiniMessage.miniMessage();
        this.messages = new Messages(this, miniMessage);
        this.messages.load();

        // Данные
        this.townAgeManager = new TownAgeManager(this);
        this.townAgeManager.load();

        // Политика
        this.policyMode = PolicyMode.fromString(getConfig().getString("policy", "gated-only"));

        // Разрешённые рецепты
        this.allowedRecipes = new AllowedRecipes(this);
        this.allowedRecipes.buildFromDefaultsAndConfig();

        // Хуки
        this.townyHook = new TownyHook(this);

        // Слушатели
        Bukkit.getPluginManager().registerEvents(
                new CraftingListener(this, townAgeManager, allowedRecipes, townyHook, messages, policyMode),
                this
        );

        // Публичный сервис для внешних плагинов — РЕАЛИЗАЦИЯ, а не лямбда
        this.serviceInstance = new TownAgeServiceImpl(townAgeManager);
        Bukkit.getServicesManager().register(
                TownAgeService.class,
                serviceInstance,
                this,
                ServicePriority.Normal
        );

        // Команда
        Centuries exec = new Centuries(this, townAgeManager, allowedRecipes, messages);
        getCommand("ages").setExecutor(exec);
        getCommand("ages").setTabCompleter(exec);

        getLogger().info("TownAges enabled. Policy: " + policyMode.name());
    }

    @Override
    public void onDisable() {
        if (townAgeManager != null) {
            townAgeManager.saveSync();
        }
        if (serviceInstance != null) {
            Bukkit.getServicesManager().unregister(TownAgeService.class, serviceInstance);
            serviceInstance = null;
        }
    }

    public Age defaultAge() {
        String def = getConfig().getString("default-age", "PRIMITIVE");
        try {
            return Age.fromName(def);
        } catch (Exception e) {
            return Age.PRIMITIVE;
        }
    }

    public Messages messages() { return messages; }
    public PolicyMode policyMode() { return policyMode; }
    public void setPolicyMode(PolicyMode mode) { this.policyMode = mode; }
    public MiniMessage miniMessage() { return miniMessage; }
}
