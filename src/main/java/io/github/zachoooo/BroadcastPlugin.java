package io.github.zachoooo;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import io.github.zachoooo.broadcast.Broadcast;
import io.github.zachoooo.broadcast.BroadcastAnnounce;
import io.github.zachoooo.broadcast.BroadcastCommand;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.io.IOException;
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
@Plugin(id = "broadcast", name = "Broadcast", version = "1.1.3", description = "Automatically make broadcasts to your server.")
public class BroadcastPlugin {

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
    private int previousIndex = 0;
    private Task asyncBroadcastTask;
    private boolean noRepeat;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        loadMessages();
    }

    @Listener
    public void reload(GameReloadEvent event) {
        getAsyncBroadcastTask().cancel();
        setMessageIndex(0);
        getBroadcasts().clear();
        loadMessages();
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

    public void setMessageIndex(int messageIndex) {
        this.messageIndex = messageIndex;
    }

    public Task getAsyncBroadcastTask() {
        return asyncBroadcastTask;
    }

    public void setAsyncBroadcastTask(Task asyncBroadcastTask) {
        this.asyncBroadcastTask = asyncBroadcastTask;
    }

    public void setPreviousIndex(int i) {
        previousIndex = i;
    }

    public void loadMessages() {
        getLogger().info("Loading messages...");
        Path potentialFile = getConfigPath();
        ConfigurationLoader<CommentedConfigurationNode> loader;
        if (!Files.exists(potentialFile)) {
            try {
                Sponge.getAssetManager().getAsset(this, "defaultConfig.conf").get().copyToFile(potentialFile);
            } catch (IOException e) {
                e.printStackTrace();
                getLogger().error("Unable to load default config. Shutting down plugin. Report this error to plugin developer.");
                return;
            }
        }
        loader =
                HoconConfigurationLoader.builder().setPath(potentialFile).build();
        ConfigurationNode rootNode;
        try {
            rootNode = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().error("Unable to load config. Please see stack trace above. Plugin will not start.");
            return;
        }
        try {
            for (Text text : rootNode.getNode("messages").getList(TypeToken.of(Text.class))) {
                Broadcast broadcast;
                if (text.toPlain().startsWith("/")) {
                    broadcast = new BroadcastCommand(this, text.toPlain());
                } else {
                    text = rootNode.getNode("prefix").getValue(TypeToken.of(Text.class)).concat(text);
                    broadcast = new BroadcastAnnounce(this, text);
                }

                broadcasts.add(broadcast);
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
        getLogger().info("Successfully loaded messages.");
        long delay = rootNode.getNode("delay").getInt();
        noRepeat = rootNode.getNode("no-repeat").getBoolean();
        asyncBroadcastTask = Task.builder().async().interval(delay, TimeUnit.SECONDS).name("Broadcast - Schedule Messages").execute(() -> {
            if (rootNode.getNode("random").getBoolean(false)) {
                Random random = new Random();
                int nextIndex = random.nextInt(broadcasts.size());
                if (noRepeat && broadcasts.size() > 1) {
                    while (nextIndex == previousIndex) {
                        nextIndex = random.nextInt(broadcasts.size());
                    }
                }
                Broadcast randomBroadcast = broadcasts.get(nextIndex);
                randomBroadcast.runBroadcast();
                previousIndex = nextIndex;
            } else {
                Broadcast broadcast = broadcasts.get(messageIndex++);
                broadcast.runBroadcast();
                if (messageIndex == broadcasts.size()) {
                    messageIndex = 0;
                }
            }
        }).submit(this);
    }

    public boolean isNoRepeat() {
        return noRepeat;
    }
}
