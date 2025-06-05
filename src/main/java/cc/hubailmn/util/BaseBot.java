package cc.hubailmn.util;

import lombok.Getter;
import lombok.Setter;
import cc.hubailmn.util.config.ConfigUtil;
import cc.hubailmn.util.config.file.BotConfig;
import cc.hubailmn.util.database.DataBaseConnection;
import cc.hubailmn.util.log.CSend;
import cc.hubailmn.util.register.Register;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

public abstract class BaseBot {

    @Getter
    @Setter
    private static String name;

    @Getter
    @Setter
    private static String packageName;

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
            setPackageName(botClass.getPackage().getName());
            String[] packageParts = getPackageName().split("\\.");
            String botName = packageParts[packageParts.length - 1];
            setName(botName);

            BaseBot bot = botClass.getDeclaredConstructor().newInstance();

            bot.preStart();

            Register.config();

            BotConfig config = ConfigUtil.getConfig(BotConfig.class);

            if (config == null) {
                throw new IllegalStateException("BotConfig is missing.");
            }

            setToken(config.getToken());

            if (getToken() == null || getToken().isEmpty()) {
                throw new IllegalStateException("Bot token is not set!");
            }

            ShardManager shardManager = DefaultShardManagerBuilder.createDefault(config.getToken())
                    .setActivity(Activity.of(config.getActivityType(), config.getActivityName()))
                    .setStatus(config.getBotStatus())
                    .build();

            setShardManager(shardManager);

            bot.postStart();

        } catch (Exception e) {
            CSend.error("Failed to launch bot: " + e.getMessage());
            CSend.error(e);
        }
    }

    public void preStart() {

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
