package com.staffchat.config;

import net.minecraft.util.Formatting;
import java.util.HashMap;
import java.util.Map;

public class ModConfig {

    public Map<String, ChannelConfig> channels = new HashMap<>();
    public MessagesConfig messages = new MessagesConfig();

    public ModConfig() {
        // Default values
        channels.put("staffchat", new ChannelConfig("staffchat", "StaffChat", "AQUA"));
        channels.put("modchat", new ChannelConfig("modchat", "ModChat", "GREEN"));
        channels.put("adminchat", new ChannelConfig("adminchat", "AdminChat", "RED"));
    }

    public static class ChannelConfig {
        public String command;
        public String prefix;
        public String color; // Name of Formatting enum

        public ChannelConfig() {}

        public ChannelConfig(String command, String prefix, String color) {
            this.command = command;
            this.prefix = prefix;
            this.color = color;
        }

        public Formatting getFormatting() {
            Formatting f = Formatting.byName(color);
            return f != null ? f : Formatting.WHITE;
        }
    }

    public static class MessagesConfig {
        public String toggledOn = "Enabled %prefix% chat mode. All your chat messages will now go to this channel.";
        public String switchedChannel = "Switched chat mode from %old_prefix% to %new_prefix%.";
        public String toggledOff = "Disabled %prefix% chat mode.";
        public String consoleDeny = "Console cannot toggle chat channels. Use /%command% <message>";
        public String messageFormat = "[%prefix%] %luckperms_prefix%%player_name%: %message%";
    }
}
