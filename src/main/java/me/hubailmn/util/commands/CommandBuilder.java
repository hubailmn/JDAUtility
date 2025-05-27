package me.hubailmn.util.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.hubailmn.util.commands.annotation.BotCommand;
import me.hubailmn.util.commands.annotation.BotSubCommand;
import me.hubailmn.util.log.CSend;
import me.hubailmn.util.register.ReflectionsUtil;
import me.hubailmn.util.register.Register;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class CommandBuilder extends ListenerAdapter {

    private final Map<String, SubCommandBuilder> subCommands = new HashMap<>();
    private String name;
    private String description;
    private SlashCommandData commandData;

    public CommandBuilder() {
        BotCommand annotation = this.getClass().getAnnotation(BotCommand.class);

        if (annotation == null) {
            CSend.error("Failed to load command: " + this.getClass().getSimpleName() + ". Command class must be annotated with @BotCommand.");
            return;
        }

        this.name = annotation.name().toLowerCase();
        this.description = annotation.description();
        register();
        addSubCommands();
    }

    private void register() {
        setCommandData(Commands.slash(getName(), getDescription()));
    }

    public void addOptions() {

    }

    public void setPermissions() {

    }

    public void addSubCommands() {
        Reflections reflections = ReflectionsUtil.build(
                Register.getBASE_PACKAGE() + ".command"
        );

        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(BotSubCommand.class);
        for (Class<?> clazz : classes) {
            BotSubCommand subCommandAnnotation = clazz.getAnnotation(BotSubCommand.class);
            if (subCommandAnnotation.parent().equals(this.getClass())) {
                try {
                    SubCommandBuilder subCommandInstance = (SubCommandBuilder) clazz.getDeclaredConstructor().newInstance();
                    getCommandData().addSubcommands(subCommandInstance.getSubcommandData());
                    subCommands.put(subCommandInstance.getName(), subCommandInstance);
                } catch (Exception ex) {
                    CSend.error("Failed to load subcommand: " + clazz.getSimpleName());
                    CSend.error(ex);
                }
            }
        }
    }

    public void execute(SlashCommandInteractionEvent e) {
        if (!subCommands.isEmpty()) {
            String subcommandName = e.getSubcommandName();
            if (subcommandName == null) {
                e.reply("❌ Please specify a valid subcommand.").setEphemeral(true).queue();
                return;
            }
            SubCommandBuilder subCommand = subCommands.get(subcommandName);
            if (subCommand == null) {
                e.reply("❌ Unknown subcommand `" + subcommandName + "`").setEphemeral(true).queue();
                return;
            }
            subCommand.execute(e);
        } else {
            handleCommand(e);
        }
    }

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
        if (!e.getName().equals(getName())) return;

    }

    public void logUsage(SlashCommandInteractionEvent e) {
        CSend.debug("Command used: " + getName() + " by " + e.getUser().getName());
    }

    public abstract void handleCommand(SlashCommandInteractionEvent e);

}