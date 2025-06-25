package cc.hubailmn.jdautility.config;

import cc.hubailmn.jdautility.log.CSend;
import cc.hubailmn.jdautility.register.Register;
import lombok.Getter;

import java.util.*;

public class BotConfigUtil {

    @Getter
    private static final Map<Class<?>, BotConfigBuilder> CONFIG_INSTANCE = new HashMap<>();

    private BotConfigUtil() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    @SuppressWarnings("unchecked")
    public static <T> T getConfig(Class<T> clazz) {
        return (T) CONFIG_INSTANCE.get(clazz);
    }

    public static void reloadAll() {
        if (CONFIG_INSTANCE.isEmpty()) {
            Register.config();
        }
        CONFIG_INSTANCE.values().forEach(BotConfigBuilder::reload);
        CSend.info("Reloaded all configs (" + CONFIG_INSTANCE.size() + ")");
    }

    public static void reloadAllExcept(Class<?>... exclude) {
        Set<Class<?>> exclusions = exclude.length > 0 ? new HashSet<>(Set.of(exclude)) : Collections.emptySet();

        CONFIG_INSTANCE.forEach((clazz, builder) -> {
            if (!exclusions.contains(clazz)) {
                builder.reload();
                CSend.info("Reloaded config: " + clazz.getSimpleName());
            }
        });
    }

    public static void reloadSelected(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            reload(clazz);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> void reload(Class<T> clazz) {
        BotConfigBuilder builder = CONFIG_INSTANCE.get(clazz);
        if (builder != null) {
            builder.reload();
            CSend.info("Reloaded config: " + clazz.getSimpleName());
        } else {
            try {
                BotConfigBuilder instance = ((Class<? extends BotConfigBuilder>) clazz).getDeclaredConstructor().newInstance();
                CONFIG_INSTANCE.put(clazz, instance);
                CSend.info("Loaded and registered config: " + clazz.getSimpleName());
            } catch (Exception e) {
                CSend.error("Failed to reload or register config: " + clazz.getSimpleName());
                throw new RuntimeException(e);
            }
        }
    }

    public static void saveAll() {
        CONFIG_INSTANCE.values().forEach(BotConfigBuilder::save);
    }

    public static void save(Class<?> clazz) {
        BotConfigBuilder builder = CONFIG_INSTANCE.get(clazz);
        if (builder != null) {
            builder.save();
        }
    }

    public static void unregister(Class<?> clazz) {
        CONFIG_INSTANCE.remove(clazz);
    }

    public static boolean isRegistered(Class<?> clazz) {
        return CONFIG_INSTANCE.containsKey(clazz);
    }

    public static Set<Class<?>> listRegistered() {
        return Collections.unmodifiableSet(CONFIG_INSTANCE.keySet());
    }
}
