package me.hubailmn.util.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.hubailmn.util.commands.annotation.BotSubCommand;
import me.hubailmn.util.log.CSend;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class SubCommandBuilder extends ListenerAdapter {

    String name;
    String description;
    SubcommandData subcommandData;

    public SubCommandBuilder() {
        BotSubCommand annotation = this.getClass().getAnnotation(BotSubCommand.class);

        if (annotation == null) {
            this.name = this.getClass().getSimpleName().replaceAll("Command$", "").toLowerCase();
            this.description = "";
        } else {
            this.name = annotation.name();
            this.description = annotation.description();
        }

        register();
    }

    private void register() {
        this.subcommandData = new SubcommandData(name, description);
        addOptions();
    }

    public void addOptions() {

    }

    public void setPermissions() {

    }

    public abstract void execute(SlashCommandInteractionEvent e);

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent e) {
        if (!e.getName().equals(getName())) return;

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

}
