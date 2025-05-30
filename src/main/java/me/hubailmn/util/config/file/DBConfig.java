package me.hubailmn.util.config.file;

import lombok.Data;
import me.hubailmn.util.BaseBot;
import me.hubailmn.util.config.ConfigBuilder;
import me.hubailmn.util.config.annotation.IgnoreFile;
import me.hubailmn.util.config.annotation.LoadConfig;

@LoadConfig(path = "config/DBConfig.yml")
@IgnoreFile(database = true)
public class DBConfig extends ConfigBuilder {

    public DBConfig() {
        super();

        String key = "database.SQLite.path";
        String rawPath = getString(key);

        if (rawPath != null && rawPath.contains("%bot_name%")) {
            String resolvedPath = rawPath.replace("%bot_name%", BaseBot.getName());
            set(key, resolvedPath);
        } else if (rawPath == null) {
            set(key, BaseBot.getName() + ".db");
        }

        save();
    }

    public String getModule() {
        return getString("database.module");
    }

    public MySQLConfig getMySQLConfig() {
        return new MySQLConfig(this);
    }

    public SQLiteConfig getSQLiteConfig() {
        return new SQLiteConfig(this);
    }

    @Data
    public static class MySQLConfig {
        private final String connectionString;
        private final String endPoint;
        private final String databaseName;
        private final String username;
        private final String password;

        public MySQLConfig(ConfigBuilder config) {
            String path = "database.MySQL.";
            this.connectionString = config.getString(path + "connection-string");
            this.endPoint = config.getString(path + "endpoint");
            this.databaseName = config.getString(path + "database-name");
            this.username = config.getString(path + "username");
            this.password = config.getString(path + "password");
        }
    }

    @Data
    public static class SQLiteConfig {
        private final String sqlitePath;

        public SQLiteConfig(ConfigBuilder config) {
            this.sqlitePath = config.getString("database.SQLite.path");
        }
    }
}
