package me.hubailmn.util;

import lombok.Getter;
import lombok.Setter;
import me.hubailmn.util.config.ConfigUtil;
import me.hubailmn.util.config.file.BotConfig;
import me.hubailmn.util.database.DataBaseConnection;
import me.hubailmn.util.log.CSend;
import me.hubailmn.util.register.Register;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

public abstract class BaseBot {

    @Getter
    @Setter
    private static String name;

    @Getter
    @Setter
    private static boolean debug;

    @Getter
    @Setter
    private static String token;

    @Getter
    @Setter
    private static boolean database;

    @Getter
    @Setter
    private static ShardManager shardManager;

    public static void launch(Class<? extends BaseBot> botClass) {
        try {
            BaseBot bot = botClass.getDeclaredConstructor().newInstance();
            bot.preStart();

            if (getToken() == null || getToken().isEmpty()) {
                throw new IllegalStateException("Bot token is not set!");
            }

            BotConfig config = ConfigUtil.getConfig(BotConfig.class);

            if (config == null) {
                throw new IllegalStateException("BotConfig is missing. Did you register it?");
            }

            ShardManager shardManager = DefaultShardManagerBuilder.createDefault(config.getToken())
                    .setActivity(Activity.of(Activity.ActivityType.valueOf(config.getActivityType()), config.getActivityName()))
                    .build();

            setShardManager(shardManager);
            bot.postStart();
        } catch (Exception e) {
            CSend.error("Failed to launch bot: " + e.getMessage());
            CSend.error(e);
        }
    }

    public void preStart() {
        Register.config();
    }

    public void postStart() {
        if (isDatabase()) {
            Register.database();
        }

        Register.modals();
        Register.commands();
        Register.listeners();
    }

    public void shutdown() {
        if (isDatabase()) {
            DataBaseConnection.close();
        }

    }
}
