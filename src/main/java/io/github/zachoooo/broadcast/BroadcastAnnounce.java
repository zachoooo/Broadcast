package io.github.zachoooo.broadcast;

import io.github.zachoooo.BroadcastPlugin;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

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
public class BroadcastAnnounce extends Broadcast {

    private Text announcement;

    public BroadcastAnnounce(BroadcastPlugin plugin) {
        super(plugin);
    }

    public BroadcastAnnounce(BroadcastPlugin plugin, Text announcement) {
        super(plugin);
        this.announcement = announcement;
    }

    @Override
    public void runBroadcast() {
        Sponge.getServer().getBroadcastChannel().send(announcement);
    }
}
