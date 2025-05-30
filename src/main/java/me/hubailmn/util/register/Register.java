package me.hubailmn.util.register;

import lombok.Getter;
import me.hubailmn.util.BaseBot;
import me.hubailmn.util.commands.CommandBuilder;
import me.hubailmn.util.commands.CommandUtil;
import me.hubailmn.util.commands.SubCommandBuilder;
import me.hubailmn.util.commands.annotation.BotCommand;
import me.hubailmn.util.commands.annotation.BotSubCommand;
import me.hubailmn.util.config.ConfigBuilder;
import me.hubailmn.util.config.ConfigUtil;
import me.hubailmn.util.config.annotation.IgnoreFile;
import me.hubailmn.util.config.annotation.LoadConfig;
import me.hubailmn.util.database.DataBaseConnection;
import me.hubailmn.util.database.TableBuilder;
import me.hubailmn.util.database.annotation.DataBaseTable;
import me.hubailmn.util.listener.ListenerBuilder;
import me.hubailmn.util.listener.annotation.BotListener;
import me.hubailmn.util.log.CSend;
import me.hubailmn.util.modal.ModalBuilder;
import me.hubailmn.util.modal.annotation.BotModal;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.reflections.Reflections;

import java.util.Set;

public class Register {

    @Getter
    private static final String BASE_PACKAGE = BaseBot.getPackageName();

    @Getter
    private static final String UTIL_PACKAGE = "me.hubailmn.util";

    public static void config() {
        Reflections reflections = ReflectionsUtil.build(
                UTIL_PACKAGE + ".config.file",
                BASE_PACKAGE + ".config"
        );

        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(LoadConfig.class);
        scanAndRegister(classes, "Config", clazz -> {
            if (!ConfigBuilder.class.isAssignableFrom(clazz)) {
                CSend.warn(clazz.getName() + " is annotated with @LoadConfig but does not extend ConfigBuilder.");
                return;
            }

            if (clazz.isAnnotationPresent(IgnoreFile.class)) {
                IgnoreFile ignore = clazz.getAnnotation(IgnoreFile.class);
                if ((ignore.database() && !BaseBot.isDatabase())) {
                    CSend.debug("Skipping config " + clazz.getSimpleName() + " due to @IgnoreFile conditions.");
                    return;
                }
            }

            @SuppressWarnings("unchecked")
            Class<? extends ConfigBuilder> typedClass = (Class<? extends ConfigBuilder>) clazz;

            try {
                ConfigBuilder config = typedClass.getDeclaredConstructor().newInstance();
                ConfigUtil.getCONFIG_INSTANCE().put(typedClass, config);
            } catch (Exception e) {
                CSend.error("Failed to load config " + clazz.getSimpleName() + ": " + e.getMessage());
                CSend.error(e);
            }
        });
    }

    public static void database() {
        DataBaseConnection.initialize();

        Reflections reflections = ReflectionsUtil.build(
                BASE_PACKAGE + ".database"
        );

        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(DataBaseTable.class);
        scanAndRegister(classes, "DataBase Table", clazz -> {
            if (!TableBuilder.class.isAssignableFrom(clazz)) {
                CSend.warn(clazz.getName() + " is annotated with @DataBaseTable but does not extend TableBuilder.");
                return;
            }

            clazz.getDeclaredConstructor().newInstance();
        });
    }

    public static void commands() {
        subCommands();
        Reflections reflections = ReflectionsUtil.build(
                BASE_PACKAGE + ".command"
        );

        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(BotCommand.class);
        scanAndRegister(classes, "Command", clazz -> {
            if (!CommandBuilder.class.isAssignableFrom(clazz)) {
                CSend.warn(clazz.getName() + " is annotated with @BotCommand but does not extend CommandBuilder.");
                return;
            }

            @SuppressWarnings("unchecked")
            Class<? extends CommandBuilder> typedClass = (Class<? extends CommandBuilder>) clazz;

            CommandBuilder commandBuilder = typedClass.getDeclaredConstructor().newInstance();
            InstanceManager.addCommand(typedClass, commandBuilder);
            BaseBot.getShardManager().addEventListener(commandBuilder);
            CommandUtil.addCommand(commandBuilder.getCommandData());
        });

        CommandUtil.registerAllGuild();
    }

    public static void subCommands() {
        Reflections reflections = ReflectionsUtil.build(
                BASE_PACKAGE + ".command"
        );

        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(BotSubCommand.class);
        scanAndRegister(classes, "Sub Command", clazz -> {
            if (!SubCommandBuilder.class.isAssignableFrom(clazz)) {
                CSend.warn(clazz.getName() + " is annotated with @BotSubCommand but does not extend SubCommandBuilder.");
                return;
            }

            @SuppressWarnings("unchecked")
            Class<? extends SubCommandBuilder> typedClass = (Class<? extends SubCommandBuilder>) clazz;

            SubCommandBuilder subCommandBuilder = typedClass.getDeclaredConstructor().newInstance();
            InstanceManager.addSubCommand(typedClass, subCommandBuilder);
            BaseBot.getShardManager().addEventListener(subCommandBuilder);
        });
    }

    public static void listeners() {
        Reflections reflections = ReflectionsUtil.build(
                BASE_PACKAGE + ".listener",
                UTIL_PACKAGE + ".listener.preListeners"
        );

        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(BotListener.class);
        scanAndRegister(classes, "Event Listener", clazz -> {
            if (!ListenerBuilder.class.isAssignableFrom(clazz)) {
                CSend.warn(clazz.getName() + " is annotated with @BotListener but does not extend ListenerBuilder.");
                return;
            }

            @SuppressWarnings("unchecked")
            Class<? extends ListenerAdapter> typedClass = (Class<? extends ListenerAdapter>) clazz;

            ListenerAdapter listenerInstance = typedClass.getDeclaredConstructor().newInstance();
            InstanceManager.addListener(typedClass, listenerInstance);
            BaseBot.getShardManager().addEventListener(listenerInstance);
        });
    }

    public static void modals() {
        Reflections reflections = ReflectionsUtil.build(
                BASE_PACKAGE + ".modal"
        );

        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(BotModal.class);
        scanAndRegister(classes, "Modal", clazz -> {
            if (!ModalBuilder.class.isAssignableFrom(clazz)) {
                CSend.warn(clazz.getName() + " is annotated with @BotModal but does not extend ModalBuilder.");
                return;
            }

            @SuppressWarnings("unchecked")
            Class<? extends ModalBuilder> typedClass = (Class<? extends ModalBuilder>) clazz;

            ModalBuilder modalInstance = typedClass.getDeclaredConstructor().newInstance();
            InstanceManager.addModal(typedClass, modalInstance);
            BaseBot.getShardManager().addEventListener(modalInstance);
        });
    }

    private static <T> void scanAndRegister(Set<Class<? extends T>> classes, String label, RegistryAction action) {
        if (classes.isEmpty()) {
            CSend.warn("No " + label + "s found to register.");
            return;
        }

        for (Class<?> clazz : classes) {
            try {
                action.execute(clazz);
            } catch (Exception e) {
                CSend.error("Failed to register " + label + ": " + clazz.getSimpleName() + " - " + e.getMessage());
                CSend.error(e);
            }
        }
    }

    @FunctionalInterface
    private interface RegistryAction {
        void execute(Class<?> clazz) throws Exception;
    }
}