package me.hubailmn.util.config.file;

import me.hubailmn.util.config.ConfigBuilder;
import me.hubailmn.util.config.annotation.LoadConfig;

@LoadConfig(
        path = "config/BotConfig.yml"
)
public class BotConfig extends ConfigBuilder {

    private final String PREFIX = "bot-settings.";

    public String getToken() {
        return getString(PREFIX + "token");
    }

    public String getBotStatus() {
        return getString(PREFIX + "status");
    }

    public String getActivityType() {
        return getString(PREFIX + "activity.type").toUpperCase();
    }

    public String getActivityName() {
        return getString(PREFIX + "activity.name");
    }

}
