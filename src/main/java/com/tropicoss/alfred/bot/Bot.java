package com.tropicoss.alfred.bot;

import com.google.gson.JsonObject;
import com.tropicoss.alfred.Alfred;
import com.tropicoss.alfred.PlayerInfoFetcher;
import com.tropicoss.alfred.config.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;

import static com.tropicoss.alfred.Alfred.LOGGER;

public class Bot {

    private static final Bot instance;

    private final JDA BOT;

    private final TextChannel CHANNEL;

    private Webhook WEBHOOK = null;

    private final String iconUrl = "https://cdn2.iconfinder.com/data/icons/whcompare-isometric-web-hosting-servers/50/value-server-512.png";

    static {
        try {
            instance = new Bot();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }



    private Bot() throws InterruptedException {
        try {
            BOT = JDABuilder.createDefault(Config.Bot.token)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(new Listeners())
                    .build()
                    .awaitReady();

            CHANNEL = BOT.getTextChannelById(Config.Bot.channel);

            for (Webhook webhook : CHANNEL.getGuild().retrieveWebhooks().complete()) {
                if ("Alfred".equals(webhook.getName())) {
                    WEBHOOK = webhook;
                }
            }

            if(WEBHOOK == null) {
                WEBHOOK = CHANNEL.createWebhook("Alfred").complete();
            }

        } catch (Exception e) {
            switch (e.getClass().getSimpleName()) {
                case "InvalidTokenException":
                    Alfred.LOGGER.error("Invalid bot token. Please check your config file.");
                    break;
                case "IllegalArgumentException":
                    Alfred.LOGGER.error("Invalid bot channel. Please check your config file.");
                    break;
                default:
                    Alfred.LOGGER.error("Error starting bot: " + e.getMessage());
                    break;
            }
            throw e;
        }
    }

    public static Bot getInstance() {
        return instance;
    }

    public void shutdown() throws InterruptedException {

        BOT.shutdown();

        BOT.awaitShutdown();
    }

    public void sendEmbedMessage(String message, String serverName) {

        if (CHANNEL == null) {
            Alfred.LOGGER.error("Chat channel not found. Please check your config file.");
            return;
        }

        EmbedBuilder builder = new EmbedBuilder()
                        .setDescription(message)
                        .setFooter(serverName, iconUrl)
                        .setTimestamp(Instant.now())
                                .setAuthor(serverName)
                                .setColor(39129);

        CHANNEL.sendMessageEmbeds(builder.build()).queue();
    }

    public void sendWebhook(String message, PlayerInfoFetcher.Profile profile, String serverName) {
        try {
            JsonObject body = new JsonObject();

            body.addProperty("username", String.format("%s - %s", profile.data.player.username, serverName));
            body.addProperty("content", message);
            body.addProperty("avatar_url", profile.data.player.avatar);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(WEBHOOK.getUrl()))
                    .header("Content-Type", "application/json")
                    .method("POST", HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        }  catch (IOException | InterruptedException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void sendServerStartingMessage(String serverName) {
        CHANNEL
                .sendMessageEmbeds(
                        new EmbedBuilder()
                                .setAuthor(serverName,null, iconUrl )
                                .setDescription("Server is starting...")
                                .setTimestamp(Instant.now())
                                .setFooter(serverName, iconUrl)
                                .setColor(Color.ORANGE)
                                .build())
                .queue();
    }

    public void sendServerStartedMessage(String serverName, Long uptime) {
        String description = String.format("Server started in %sS 🕛", uptime / 1000);

        CHANNEL
                .sendMessageEmbeds(
                        new EmbedBuilder()
                                .setAuthor(serverName,null, iconUrl )
                                .setDescription(description)
                                .setTimestamp(Instant.now())
                                .setFooter(serverName, iconUrl)
                                .setColor(Color.GREEN)
                                .build())
                .queue();
    }

    public void sendServerStoppingMessage(String serverName) {
        CHANNEL
                .sendMessageEmbeds(
                        new EmbedBuilder()
                                .setAuthor(serverName,null, "https://cdn2.iconfinder.com/data/icons/whcompare-isometric-web-hosting-servers/50/value-server-512.png" )
                                .setTitle("Server is stopping...")
                                .setTimestamp(Instant.now())
                                .setFooter(serverName, iconUrl)
                                .setColor(Color.ORANGE)
                                .build())
                .queue();
    }

    public void sendServerStoppedMessage(String serverName) {
        CHANNEL
                .sendMessageEmbeds(
                        new EmbedBuilder()
                                .setAuthor(serverName,null, "https://cdn2.iconfinder.com/data/icons/whcompare-isometric-web-hosting-servers/50/value-server-512.png" )
                                .setTitle("Server stopped!")
                                .setTimestamp(Instant.now())
                                .setFooter(serverName, iconUrl)
                                .setColor(Color.RED)
                                .build())
                .queue();
    }

    public void sendJoinMessage(PlayerInfoFetcher.Profile profile, String serverName) {

        String nameMCProfile = String.format("https://namemc.com/profile/%s", profile.data.player.username);

        CHANNEL.sendMessageEmbeds(
                new EmbedBuilder()
                        .setAuthor(profile.data.player.username, nameMCProfile, profile.data.player.avatar )
                        .setTitle("Joined the server")
                        .setTimestamp(Instant.now())
                        .setFooter(serverName, iconUrl)
                        .setColor(Color.BLUE)
                        .build()
        ).queue();
    }

    public void sendLeaveMessage(PlayerInfoFetcher.Profile profile, String serverName) {
        String nameMCProfile = String.format("https://namemc.com/profile/%s", profile.data.player.username);

        CHANNEL.sendMessageEmbeds(
                new EmbedBuilder()
                        .setAuthor(profile.data.player.username, nameMCProfile, profile.data.player.avatar )
                        .setTitle("Left the server")
                        .setTimestamp(Instant.now())
                        .setFooter(serverName, iconUrl)
                        .setColor(Color.orange)
                        .build()
        ).queue();
    }
}