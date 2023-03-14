package fr.modcraftmc;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.modcraftmc.rabbitmq.RabbitmqConnection;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.concurrent.TimeoutException;

@Plugin(id = "datasync", name = "DataSync", version = "0.1.0-SNAPSHOT",
        description = "fire events to datasync mods on servers", authors = {"ModCraftMC"}, url = "https://modcraftmc.fr")
public class DataSync {
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    public RabbitmqConnection rabbitmqConnection;

    @Inject
    public DataSync(ProxyServer server, @DataDirectory Path dataDirectory, Logger logger) {
        this.server = server;
        this.dataDirectory = dataDirectory;
        this.logger = logger;

    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {
        logger.info("DataSync initializing !");
        loadConfig();

        server.getEventManager().register(this, new EventRegister(this));
    }

    @Subscribe
    public void onShutdown(ProxyShutdownEvent event) {
        logger.info("DataSync shutting down !");
        rabbitmqConnection.close();
    }

    private Toml readConfig() {
        File configFile = new File(dataDirectory.toFile(), "config.toml");
        if (!configFile.getParentFile().exists()) {
            configFile.getParentFile().mkdirs();
        }
        if (!configFile.exists()) {
            logger.info("Config file not found, creating one !");
            try {
                configFile.createNewFile();
                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(configFile));
                writer.write(defaultFileContent());
                writer.close();
            } catch (Exception e) {
                logger.error("Error while creating config file : %s".formatted(e.getMessage()));
            }
        }
        return new Toml().read(configFile);
    }

    private String defaultFileContent(){
        return """
                [rabbitmq]
                host = "localhost"
                port = 5672
                username = "guest"
                password = "guest"
                vhost = "/"
                """;
    }

    public void loadConfig(){
        logger.info("Loading config file");
        if(rabbitmqConnection != null){
            rabbitmqConnection.close();
        }

        Toml config = readConfig();
        ConfigData configData;
        try {
            configData = config.getTable("rabbitmq").to(ConfigData.class);
        } catch (Exception e) {
            logger.error("Error while reading config file : %s".formatted(e.getMessage()));
            throw new RuntimeException(e);
        }

        try {
            this.rabbitmqConnection = new RabbitmqConnection(configData.host, configData.port, configData.username, configData.password, configData.vhost);
        } catch (IOException | TimeoutException e) {
            logger.error("Error while connecting to RabbitMQ : %s".formatted(e.getMessage()));
            throw new RuntimeException(e);
        }
        logger.info("Connected to RabbitMQ");
    }

    public Logger getLogger() {
        return logger;
    }

    public ProxyServer getProxyServer() {
        return server;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    private class ConfigData {
        private String host;
        private int port;
        private String username;
        private String password;
        private String vhost;
    }
}
