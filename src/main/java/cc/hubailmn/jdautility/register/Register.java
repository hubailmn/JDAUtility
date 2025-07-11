package cc.hubailmn.jdautility.register;

import cc.hubailmn.jdautility.BaseBot;
import cc.hubailmn.jdautility.commands.BotCommandBuilder;
import cc.hubailmn.jdautility.commands.BotCommandUtil;
import cc.hubailmn.jdautility.commands.BotSubCommandBuilder;
import cc.hubailmn.jdautility.commands.annotation.BotCommand;
import cc.hubailmn.jdautility.commands.annotation.BotSubCommand;
import cc.hubailmn.jdautility.config.BotConfigBuilder;
import cc.hubailmn.jdautility.config.BotConfigUtil;
import cc.hubailmn.jdautility.config.annotation.IgnoreFile;
import cc.hubailmn.jdautility.config.annotation.LoadConfig;
import cc.hubailmn.jdautility.database.DataBaseConnection;
import cc.hubailmn.jdautility.database.TableBuilder;
import cc.hubailmn.jdautility.database.annotation.DataBaseTable;
import cc.hubailmn.jdautility.listener.ListenerBuilder;
import cc.hubailmn.jdautility.listener.annotation.BotListener;
import cc.hubailmn.jdautility.log.CSend;
import cc.hubailmn.jdautility.modal.ModalBuilder;
import cc.hubailmn.jdautility.modal.annotation.BotModal;
import lombok.Getter;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.reflections.Reflections;

import java.util.Set;

public class Register {

    @Getter
    private static final String BASE_PACKAGE = BaseBot.getPackageName();

    @Getter
    private static final String UTIL_PACKAGE = "cc.hubailmn.util";

    public static void config() {
        Reflections reflections = ReflectionsUtil.build(
                UTIL_PACKAGE + ".config.file",
                BASE_PACKAGE + ".config"
        );

        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(LoadConfig.class);
        scanAndRegister(classes, "Config", clazz -> {
            if (!BotConfigBuilder.class.isAssignableFrom(clazz)) {
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
            Class<? extends BotConfigBuilder> typedClass = (Class<? extends BotConfigBuilder>) clazz;

            try {
                BotConfigBuilder config = typedClass.getDeclaredConstructor().newInstance();
                BotConfigUtil.getCONFIG_INSTANCE().put(typedClass, config);
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
            if (!BotCommandBuilder.class.isAssignableFrom(clazz)) {
                CSend.warn(clazz.getName() + " is annotated with @BotCommand but does not extend CommandBuilder.");
                return;
            }

            @SuppressWarnings("unchecked")
            Class<? extends BotCommandBuilder> typedClass = (Class<? extends BotCommandBuilder>) clazz;

            BotCommandBuilder commandBuilder = typedClass.getDeclaredConstructor().newInstance();
            InstanceManager.addCommand(typedClass, commandBuilder);
            BaseBot.getShardManager().addEventListener(commandBuilder);
            BotCommandUtil.addCommand(commandBuilder.getCommandData());
        });

        BotCommandUtil.registerAllGuild();
    }

    public static void subCommands() {
        Reflections reflections = ReflectionsUtil.build(
                BASE_PACKAGE + ".command"
        );

        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(BotSubCommand.class);
        scanAndRegister(classes, "Sub Command", clazz -> {
            if (!BotSubCommandBuilder.class.isAssignableFrom(clazz)) {
                CSend.warn(clazz.getName() + " is annotated with @BotSubCommand but does not extend SubCommandBuilder.");
                return;
            }

            @SuppressWarnings("unchecked")
            Class<? extends BotSubCommandBuilder> typedClass = (Class<? extends BotSubCommandBuilder>) clazz;

            BotSubCommandBuilder subCommandBuilder = typedClass.getDeclaredConstructor().newInstance();
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