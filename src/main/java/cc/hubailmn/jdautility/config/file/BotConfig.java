package cc.hubailmn.jdautility.config.file;

import cc.hubailmn.jdautility.config.BotConfigBuilder;
import cc.hubailmn.jdautility.config.annotation.LoadConfig;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

@LoadConfig(
        path = "config/BotConfig.yml"
)
public class BotConfig extends BotConfigBuilder {

    private final String PREFIX = "bot.";

    public String getToken() {
        return getString(PREFIX + "token");
    }

    public OnlineStatus getBotStatus() {
        return OnlineStatus.fromKey(getString(PREFIX + "status").toUpperCase());
    }

    public Activity.ActivityType getActivityType() {
        return Activity.ActivityType.valueOf(getString(PREFIX + "activity.type").toUpperCase());
    }

    public String getActivityName() {
        return getString(PREFIX + "activity.name");
    }

}
