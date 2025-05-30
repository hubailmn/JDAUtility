package me.hubailmn.util.listener.preListeners;

import me.hubailmn.util.BaseBot;
import me.hubailmn.util.commands.CommandUtil;
import me.hubailmn.util.listener.ListenerBuilder;
import me.hubailmn.util.listener.annotation.BotListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@BotListener(name = "Guild Event Listener", description = "Registers Commands for guilds.")
public class GuildEventListener extends ListenerBuilder {

    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent e) {
        CommandUtil.register(e.getGuild());
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent e) {
        CommandUtil.register(e.getGuild());
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        event.getJDA().getGatewayPool().schedule(() -> {
            boolean allShardsReady = BaseBot.getShardManager().getStatuses().values().stream()
                    .allMatch(status -> status == JDA.Status.CONNECTED);

            if (allShardsReady && initialized.compareAndSet(false, true)) {
                CommandUtil.updateGlobalCommands();
                CommandUtil.registerAllGuild();
            }
        }, 5, TimeUnit.SECONDS);
    }

}
