package fr.modcraftmc;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;

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
        Toml config = loadConfig(dataDirectory);
        String host = config.getString("rabbitmq.host");
        String username = config.getString("rabbitmq.username");
        String password = config.getString("rabbitmq.password");
        String vhost = config.getString("rabbitmq.vhost");
        this.rabbitmqConnection = new RabbitmqConnection(host, username, password, vhost);
    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {
        logger.info("DataSync initializing !");
        server.getEventManager().register(this, new EventRegister(this));
    }

    @Subscribe
    public void onShutdown(ProxyShutdownEvent event) {
        logger.info("DataSync shutting down !");
        rabbitmqConnection.close();
    }

    private Toml loadConfig(Path dataDirectory) {
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
        String content = "rabbitmq.host = \"localhost\"\n"+
                "rabbitmq.username = \"guest\"\n"+
                "rabbitmq.password = \"guest\"\n"+
                "rabbitmq.vhost = \"/\"\n";
        return content;
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
}
