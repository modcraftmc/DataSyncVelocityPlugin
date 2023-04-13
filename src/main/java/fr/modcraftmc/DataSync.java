package fr.modcraftmc;

import com.google.common.base.MoreObjects;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.modcraftmc.message.MessageHandler;
import fr.modcraftmc.rabbitmq.*;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Plugin(id = "datasync", name = "DataSync", version = "0.1.0-SNAPSHOT",
        description = "fire events to datasync mods on servers", authors = {"ModCraftMC"}, url = "https://modcraftmc.fr")
public class DataSync {
    public static DataSync instance;
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final Map<String, String[]> groups = new HashMap<>();

    public RabbitmqConnection rabbitmqConnection;
    public List<Runnable> onConfigLoad;

    @Inject
    public DataSync(ProxyServer server, @DataDirectory Path dataDirectory, Logger logger) {
        instance = this;
        this.server = server;
        this.dataDirectory = dataDirectory;
        this.logger = logger;

    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {
        logger.info("DataSync initializing !");
        MessageHandler.init();
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
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
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
                
                [groups]
                # Example :
                # group1 = ["server1", "server2"]
                """;
    }

    public void loadConfig(){
        logger.info("Loading config file");
        if(rabbitmqConnection != null){
            rabbitmqConnection.close();
        }

        Toml config = readConfig();
        RabbitmqConfigData configData;
        try {
            configData = config.getTable("rabbitmq").to(RabbitmqConfigData.class);
        } catch (Exception e) {
            logger.error("Error while reading config file : %s".formatted(e.getMessage()));
            throw new RuntimeException(e);
        }

        if(this.rabbitmqConnection != null) this.rabbitmqConnection.close();
        this.rabbitmqConnection = new RabbitmqConnection(configData.host, configData.port, configData.username, configData.password, configData.vhost);
        new RabbitmqDirectPublisher(rabbitmqConnection);
        new RabbitmqDirectSubscriber(rabbitmqConnection);
        new RabbitmqPublisher(rabbitmqConnection);
        new RabbitmqSubscriber(rabbitmqConnection);
        logger.info("Connected to RabbitMQ");

        groups.clear();
        Toml groupsConfig = config.getTable("groups");
        groupsConfig.toMap().forEach((key, value) -> {
            if(value instanceof List<?> serverList){
                List<String> serverList1 = (List<String>) serverList; // Todo: find a better way to do this
                for(String server : serverList1){
                    String group = getServerGroup(server);
                    if(group != null){
                        logger.error("Server %s is already in group %s could not add %s group".formatted(server, group, key));
                        return;
                    }
                }
                groups.put(key, serverList1.toArray(new String[0]));
            }
        });
        logger.info("Loaded %d groups".formatted(groups.size()));

        onConfigLoad.forEach(Runnable::run);
    }

    public String getServerGroup(String serverName){
        for (Map.Entry<String, String[]> entry : groups.entrySet()) {
            for (String server : entry.getValue()) {
                if(server.equals(serverName)){
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    public boolean areServersInSameGroup(String server1, String server2){
        String group = getServerGroup(server1);
        if(group == null) return false;
        return Arrays.asList(groups.get(group)).contains(server2);
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

    private class RabbitmqConfigData {
        private String host;
        private int port;
        private String username;
        private String password;
        private String vhost;
    }
}
