package me.hubailmn.util.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.hubailmn.util.commands.annotation.BotSubCommand;
import me.hubailmn.util.log.CSend;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class SubCommandBuilder extends ListenerAdapter {

    private final List<Permission> requiredPermission;
    String name;
    String description;
    SubcommandData subcommandData;

    public SubCommandBuilder() {
        BotSubCommand annotation = this.getClass().getAnnotation(BotSubCommand.class);

        if (annotation == null) {
            this.name = this.getClass().getSimpleName().replaceAll("Command$", "").toLowerCase();
            this.description = "";
            this.requiredPermission = new ArrayList<>();
            CSend.error("Failed to load subcommand: " + this.getClass().getSimpleName() + ". Subcommand class must be annotated with @BotSubCommand.");
            return;
        } else {
            this.name = annotation.name();
            this.description = annotation.description();
            this.requiredPermission = Arrays.asList(annotation.permission());
        }

        register();
    }

    private void register() {
        this.subcommandData = new SubcommandData(name, description);
        addOptions();
    }

    public void addOptions() {

    }

    public abstract void execute(SlashCommandInteractionEvent e);

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent e) {
        if (!e.getName().equals(getName())) return;

        Member user = e.getMember();

        if (user == null) return;
        if (requiredPermission != null && !requiredPermission.isEmpty()) {
            if (!user.hasPermission(requiredPermission)) {
                e.reply("❌ You don't have permission to use this subcommand.")
                        .setEphemeral(true)
                        .queue();

                CSend.warn("User " + (user.getEffectiveName()) + " tried to use subcommand " + getName() + " without required permissions.");
                return;
            }
        }

        try {
            execute(e);
        } catch (Exception ex) {
            e.reply("❌ An error occurred while processing the command.").setEphemeral(true).queue();
            CSend.error("Error executing command: " + getName());
            CSend.error(ex);
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent e) {
        if (!e.getName().equals(getName())) return;
        if (e.getSubcommandName() == null) return;
    }

}
