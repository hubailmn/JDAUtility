package me.hubailmn.util.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.hubailmn.util.commands.annotation.BotCommand;
import me.hubailmn.util.log.CSend;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class CommandBuilder extends ListenerAdapter {

    private String name;
    private String description;
    private SlashCommandData commandData;

    public CommandBuilder() {
        BotCommand annotation = this.getClass().getAnnotation(BotCommand.class);

        if (annotation == null) {
            CSend.error("Failed to load command: " + this.getClass().getSimpleName() + ". Command class must be annotated with @BotCommand.");
            return;
        }

        this.name = annotation.name();
        this.description = annotation.description();
        register();
    }

    private void register() {
        setCommandData(Commands.slash(getName(), getDescription()));
    }

    public void addOptions() {

    }

    public void setPermissions() {

    }

    public abstract void execute(SlashCommandInteractionEvent e);

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent e) {
        if (!e.getName().equals(getName())) return;
        logUsage(e);

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
        if (!e.getName().equals(getName())) {
        }

    }

    public void logUsage(SlashCommandInteractionEvent event) {
        CSend.debug("Command used: " + getName() + " by " + event.getUser().getName());
    }

}