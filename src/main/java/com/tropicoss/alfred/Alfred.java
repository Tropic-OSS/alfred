package com.tropicoss.alfred;

import com.tropicoss.alfred.callback.ServerLifecycleCallback;
import com.tropicoss.alfred.callback.ServerMessageCallback;
import com.tropicoss.alfred.callback.ServerPlayerConnectionCallback;
import com.tropicoss.alfred.socket.Client;
import com.tropicoss.alfred.socket.Server;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Alfred implements DedicatedServerModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("Alfred");

    public static Server SOCKET_SERVER;

    public static Client SOCKET_CLIENT;

    public static MinecraftServer MINECRAFT_SERVER;

    @Override
    public void onInitializeServer() {
        try {

            ServerLifecycleCallback serverLifecycleCallback = new ServerLifecycleCallback();

            ServerPlayerConnectionCallback serverPlayerConnectionCallback = new ServerPlayerConnectionCallback();

            ServerMessageCallback serverMessageCallback = new ServerMessageCallback();


            ServerLifecycleEvents.SERVER_STARTING.register(serverLifecycleCallback);

            ServerLifecycleEvents.SERVER_STARTED.register(serverLifecycleCallback);

            ServerLifecycleEvents.SERVER_STOPPING.register(serverLifecycleCallback);

            ServerLifecycleEvents.SERVER_STOPPED.register(serverLifecycleCallback);


            ServerPlayConnectionEvents.JOIN.register(serverPlayerConnectionCallback);

            ServerPlayConnectionEvents.DISCONNECT.register(serverPlayerConnectionCallback);


            ServerMessageEvents.CHAT_MESSAGE.register(serverMessageCallback);

            LOGGER.info("Alfred Has Started");
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }
}