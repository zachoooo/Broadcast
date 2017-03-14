package io.github.zachoooo;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import io.github.zachoooo.broadcast.Broadcast;
import io.github.zachoooo.broadcast.BroadcastAnnounce;
import io.github.zachoooo.command.ReloadCommand;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Broadcast
 * Copyright (C) 2017  Zachary Sugano
 * *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
@Plugin(id = "broadcast", name = "Broadcast", version = "1.0", description = "Automatically make broadcasts to your server.")
public class    BroadcastPlugin {

    @Inject
    private Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private Path defaultConfig;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private ConfigurationLoader<CommentedConfigurationNode> config;

    private List<Broadcast> broadcasts = new ArrayList<Broadcast>();
    private int messageIndex = 0;
    private Task asyncBroadcastTask;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        loadMessages();
        CommandSpec reloadCommand = CommandSpec.builder()
                .description(Text.of("Reload the configuration for Broadcast"))
                .permission("broadcast.admin")
                .executor(new ReloadCommand(this))
                .build();
        Sponge.getCommandManager().register(this, reloadCommand, "broadcast-reload");
    }


    public Logger getLogger() {
        return logger;
    }

    public Path getConfigPath() {
        return defaultConfig;
    }

    public List<Broadcast> getBroadcasts() {
        return broadcasts;
    }

    public int getMessageIndex() {
        return messageIndex;
    }

    public Task getAsyncBroadcastTask() {
        return asyncBroadcastTask;
    }

    public void setMessageIndex(int messageIndex) {
        this.messageIndex = messageIndex;
    }

    public void setAsyncBroadcastTask(Task asyncBroadcastTask) {
        this.asyncBroadcastTask = asyncBroadcastTask;
    }

    public void loadMessages() {
        getLogger().info("Loading messages...");
        Path potentialFile = getConfigPath();
        URL jarConfigFile = Sponge.getAssetManager().getAsset(this, "defaultConfig.conf").get().getUrl();
        ConfigurationLoader<CommentedConfigurationNode> loader;
        if (Files.exists(potentialFile)) {
            loader =
                    HoconConfigurationLoader.builder().setPath(potentialFile).build();
        } else {
            loader =
                    HoconConfigurationLoader.builder().setURL(jarConfigFile).build();
        }
        ConfigurationNode rootNode;
        try {
            rootNode = loader.load();
        } catch(IOException e) {
            e.printStackTrace();
            getLogger().error("Unable to load config. Please see stack trace above. Plugin will not start.");
            return;
        }
        try {
            for (String s : rootNode.getNode("messages").getList(TypeToken.of(String.class))) {
                Text text = TextSerializers.FORMATTING_CODE.deserialize(rootNode.getNode("prefix"). getString() + s);
                broadcasts.add(new BroadcastAnnounce(this, text));
            }
        } catch (ObjectMappingException e) {
            e.printStackTrace();
            getLogger().error("Configuration file was invalid and could not be loaded. Please ensure your config is proper HOCON. Plugin will not load.");
            return;
        }
        if (broadcasts.size() == 0) {
            getLogger().error("No messages listed in config file. Please add some messages!");
            return;
        }
        try {
            loader =
                    HoconConfigurationLoader.builder().setPath(potentialFile).build();
            loader.save(rootNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
        getLogger().info("Successfully loaded messages.");
        long delay = rootNode.getNode("delay").getInt();
        asyncBroadcastTask = Task.builder().async().interval(delay, TimeUnit.SECONDS).name("Broadcast - Schedule Messages").execute(() -> {
            if (rootNode.getNode("random").getBoolean(false)) {
                Random random = new Random();
                Broadcast randomBroadcast = broadcasts.get(random.nextInt(broadcasts.size()));
                randomBroadcast.runBroadcast();
            } else {
                Broadcast broadcast = broadcasts.get(messageIndex++);
                broadcast.runBroadcast();
                if (messageIndex == broadcasts.size()) {
                    messageIndex = 0;
                }
            }
        }).submit(this);
    }
}
