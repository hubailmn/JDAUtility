package cc.hubailmn.util.config;

import cc.hubailmn.util.config.annotation.LoadConfig;
import cc.hubailmn.util.log.CSend;
import lombok.Data;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Data
public class ConfigBuilder {

    private String name;
    private String path;
    private File file;
    private Map<String, Object> config = new LinkedHashMap<>();

    public ConfigBuilder() {
        LoadConfig annotation = this.getClass().getAnnotation(LoadConfig.class);
        if (annotation == null) {
            CSend.error("Failed to load config: " + this.getClass().getSimpleName() + ". Config class must be annotated with @LoadConfig.");
            return;
        }
        this.path = annotation.path();
        this.name = path.split("/")[path.split("/").length - 1];
        this.file = new File(this.path);

        loadConfig();
        CSend.info("Loaded config: " + name + " (" + path + ")");
    }

    @SuppressWarnings("unchecked")
    private Object getValue(String path) {
        String[] keys = path.split("\\.");
        Map<String, Object> current = config;

        for (int i = 0; i < keys.length; i++) {
            Object value = current.get(keys[i]);
            if (value == null) return null;

            if (i == keys.length - 1) return value;
            if (!(value instanceof Map)) return null;

            current = (Map<String, Object>) value;
        }
        return null;
    }

    public String getString(String path) {
        Object value = getValue(path);
        return value instanceof String ? (String) value : null;
    }

    public int getInt(String path) {
        Object value = getValue(path);
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    public double getDouble(String path) {
        Object value = getValue(path);
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return 0.0;
        }
    }

    public boolean getBoolean(String path) {
        Object value = getValue(path);
        if (value instanceof Boolean) return (Boolean) value;
        try {
            return Boolean.parseBoolean(value.toString());
        } catch (Exception e) {
            return false;
        }
    }

    public float getFloat(String path) {
        Object value = getValue(path);
        if (value instanceof Number) return ((Number) value).floatValue();
        try {
            return Float.parseFloat(value.toString());
        } catch (Exception e) {
            return 0f;
        }
    }

    public long getLong(String path) {
        Object value = getValue(path);
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (Exception e) {
            return 0L;
        }
    }

    public List<String> getStringList(String path) {
        Object value = getValue(path);
        if (value instanceof List<?> list) {
            List<String> result = new ArrayList<>();
            for (Object o : list) {
                if (o != null) result.add(o.toString());
            }
            return result;
        }
        return Collections.emptyList();
    }

    public List<Integer> getIntegerList(String path) {
        Object value = getValue(path);
        if (value instanceof List<?> list) {
            List<Integer> result = new ArrayList<>();
            for (Object o : list) {
                try {
                    result.add(Integer.parseInt(o.toString()));
                } catch (Exception ignored) {
                }
            }
            return result;
        }
        return Collections.emptyList();
    }

    public List<Double> getDoubleList(String path) {
        Object value = getValue(path);
        if (value instanceof List<?> list) {
            List<Double> result = new ArrayList<>();
            for (Object o : list) {
                try {
                    result.add(Double.parseDouble(o.toString()));
                } catch (Exception ignored) {
                }
            }
            return result;
        }
        return Collections.emptyList();
    }

    public List<Float> getFloatList(String path) {
        Object value = getValue(path);
        if (value instanceof List<?> list) {
            List<Float> result = new ArrayList<>();
            for (Object o : list) {
                try {
                    result.add(Float.parseFloat(o.toString()));
                } catch (Exception ignored) {
                }
            }
            return result;
        }
        return Collections.emptyList();
    }

    public List<Long> getLongList(String path) {
        Object value = getValue(path);
        if (value instanceof List<?> list) {
            List<Long> result = new ArrayList<>();
            for (Object o : list) {
                try {
                    result.add(Long.parseLong(o.toString()));
                } catch (Exception ignored) {
                }
            }
            return result;
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    public void set(String stringPath, Object value) {
        String[] keys = stringPath.split("\\.");
        Map<String, Object> currentMap = config;

        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            if (i == keys.length - 1) {
                currentMap.put(key, value);
            } else {
                if (!(currentMap.get(key) instanceof Map)) {
                    currentMap.put(key, new LinkedHashMap<>());
                }
                currentMap = (Map<String, Object>) currentMap.get(key);
            }
        }

        save();
    }

    public void save() {
        try {
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    CSend.error("Failed to create directories for: " + file.getPath());
                    return;
                }
            }

            try (FileOutputStream fos = new FileOutputStream(file);
                 OutputStreamWriter writer = new OutputStreamWriter(fos)) {
                new Yaml().dump(config, writer);
            }
        } catch (IOException e) {
            CSend.error("Failed to save config: " + e.getMessage());
            CSend.error(e);
        }
    }

    public void reload() {
        loadConfig();
    }

    private void loadConfig() {
        try {
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    CSend.error("Failed to create config directories for: " + file.getPath());
                    return;
                }
            }

            if (!file.exists()) {
                if (!file.createNewFile()) {
                    CSend.error("Failed to create config file: " + file.getName());
                    return;
                }

                try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(file.getName())) {
                    if (inputStream != null) {
                        Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        CSend.info("Config file created and copied: " + file.getName());
                    } else {
                        CSend.info("New empty config file created: " + file.getName());
                    }
                }
            }

            try (InputStream inputStream = new FileInputStream(file)) {
                Yaml yaml = new Yaml();
                config = yaml.load(inputStream);
                if (config == null) config = new LinkedHashMap<>();
            }

        } catch (IOException e) {
            CSend.error("Error loading config: " + e.getMessage());
            CSend.error(e);
        }
    }

}
