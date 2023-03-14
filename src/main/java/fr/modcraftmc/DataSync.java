package fr.modcraftmc;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import javax.inject.Inject;

@Plugin(id = "datasync", name = "DataSync", version = "0.1.0-SNAPSHOT",
        description = "fire events to datasync mods on servers", authors = {"ModCraftMC"}, url = "https://modcraftmc.fr")
public class DataSync {
    private final ProxyServer server;
    private final Logger logger;

    public RabbitmqConnection rabbitmqConnection;

    @Inject
    public DataSync(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
        this.rabbitmqConnection = new RabbitmqConnection("localhost", "guest", "guest", "/");
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

    public Logger getLogger() {
        return logger;
    }

    public ProxyServer getProxyServer() {
        return server;
    }
}
