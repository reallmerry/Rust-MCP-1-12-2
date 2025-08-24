package io.reallmerry.rstudio.townycenturies.api;

import io.reallmerry.rstudio.townycenturies.Age;
import org.bukkit.OfflinePlayer;

/**
 * API для получения/изменения «века» города.
 * Реализацию регистрирует AgesPlugin в Bukkit Services.
 */
public interface TownAgeService {

    /**
     * Получить текущий век города по имени.
     */
    Age getTownAge(String townName);

    /**
     * Задать новый век города без валидации.
     */
    void setTownAge(String townName, Age age);

    /**
     * Проверить и установить новый век — с событием и проверками.
     * Возвращает true, если смена века разрешена.
     */
    boolean setTownAgeValidated(String townName, Age newAge);

    /**
     * Утилита: узнать век по игроку (если поддерживается Towny API).
     */
    Age getTownAgeForPlayer(OfflinePlayer player);
}
