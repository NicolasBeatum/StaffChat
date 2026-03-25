package com.staffchat;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import com.staffchat.config.ConfigManager;
import com.staffchat.config.ModConfig;
import com.staffchat.util.LuckPermsHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StaffChatMod implements ModInitializer {

    // Tracks which channel a player is currently toggled into.
    private static final Map<UUID, ChatChannel> activeChannels = new HashMap<>();

    public enum ChatChannel {
        STAFF("staffchat", "staffchat.read", "staffchat.use"),
        MOD("modchat", "modchat.read", "modchat.use"),
        ADMIN("adminchat", "adminchat.read", "adminchat.use");

        public final String id;
        public final String readPerm;
        public final String usePerm;

        ChatChannel(String id, String readPerm, String usePerm) {
            this.id = id;
            this.readPerm = readPerm;
            this.usePerm = usePerm;
        }

        public ModConfig.ChannelConfig getConfig() {
            ModConfig.ChannelConfig cfg = ConfigManager.getConfig().channels.get(this.id);
            if (cfg == null) {
                // Return fallback if deleted from config
                return new ModConfig.ChannelConfig(this.id, "Chat", "WHITE");
            }
            return cfg;
        }
    }

    @Override
    public void onInitialize() {
        ConfigManager.loadConfig();

        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            for (ChatChannel channel : ChatChannel.values()) {
                registerChannelCommand(dispatcher, channel);
            }

            // Register reload command
            dispatcher.register(CommandManager.literal("staffchatreload")
                .requires(source -> Permissions.check(source, "staffchat.reload", 3))
                .executes(context -> {
                    ConfigManager.loadConfig();
                    context.getSource().sendFeedback(() -> Text.literal("StaffChat configuration reloaded!").formatted(Formatting.GREEN), true);
                    return 1;
                })
            );
        });

        // Intercept chat messages
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            UUID senderId = sender.getUuid();
            if (activeChannels.containsKey(senderId)) {
                ChatChannel channel = activeChannels.get(senderId);
                // Sender must still have permission even if toggled
                if (Permissions.check(sender, channel.usePerm, 2)) {
                    sendChannelMessage(sender.server, sender, message.getContent().getString(), channel);
                    // Cancel the default chat message
                    return false;
                } else {
                    // Lost permission, untoggle them
                    activeChannels.remove(senderId);
                }
            }
            return true;
        });
    }

    private void registerChannelCommand(CommandDispatcher<ServerCommandSource> dispatcher, ChatChannel channel) {
        dispatcher.register(CommandManager.literal(channel.getConfig().command)
            .requires(source -> Permissions.check(source, channel.usePerm, 2))
            .executes(context -> toggleChannel(context, channel))
            .then(CommandManager.argument("message", StringArgumentType.greedyString())
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    String message = StringArgumentType.getString(context, "message");
                    ServerPlayerEntity player = source.getPlayer();
                    if (player != null) {
                        sendChannelMessage(source.getServer(), player, message, channel);
                    } else {
                        // Console sending to channel (optional, but good to support)
                        sendConsoleMessage(source.getServer(), source.getName(), message, channel);
                    }
                    return 1;
                })
            )
        );
    }

    private int toggleChannel(CommandContext<ServerCommandSource> context, ChatChannel channel) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ModConfig.ChannelConfig cfg = channel.getConfig();
        ModConfig.MessagesConfig msgs = ConfigManager.getConfig().messages;

        if (player == null) {
            String msg = msgs.consoleDeny.replace("%command%", cfg.command);
            context.getSource().sendFeedback(() -> Text.literal(msg), false);
            return 0;
        }

        UUID playerId = player.getUuid();
        ChatChannel current = activeChannels.get(playerId);

        if (current == channel) {
            // Toggle off if clicking the same channel
            activeChannels.remove(playerId);
            String msg = msgs.toggledOff.replace("%prefix%", cfg.prefix).replace("%command%", cfg.command);
            player.sendMessage(Text.literal(msg).formatted(Formatting.YELLOW), false);
        } else {
            // Toggle on (replaces previous if active)
            activeChannels.put(playerId, channel);
            if (current != null) {
                ModConfig.ChannelConfig currentCfg = current.getConfig();
                String msg = msgs.switchedChannel.replace("%old_prefix%", currentCfg.prefix)
                        .replace("%new_prefix%", cfg.prefix);
                player.sendMessage(Text.literal(msg).formatted(Formatting.GREEN), false);
            } else {
                String msg = msgs.toggledOn.replace("%prefix%", cfg.prefix).replace("%command%", cfg.command);
                player.sendMessage(Text.literal(msg).formatted(Formatting.GREEN), false);
            }
        }

        return 1;
    }

    private void sendChannelMessage(MinecraftServer server, ServerPlayerEntity sender, String message, ChatChannel channel) {
        formatAndBroadcast(server, sender, sender.getName().getString(), message, channel, Formatting.WHITE);
    }

    private void sendConsoleMessage(MinecraftServer server, String senderName, String message, ChatChannel channel) {
        formatAndBroadcast(server, null, senderName, message, channel, Formatting.LIGHT_PURPLE);
    }

    private void formatAndBroadcast(MinecraftServer server, ServerPlayerEntity sender, String senderName, String message, ChatChannel channel, Formatting nameColor) {
        ModConfig.ChannelConfig cfg = channel.getConfig();
        String format = ConfigManager.getConfig().messages.messageFormat;
        
        Text prefixText = Text.literal(cfg.prefix).setStyle(cfg.getStyle());
        String lpStr = sender != null ? LuckPermsHelper.getPrefix(sender.getUuid()) : "";
        Text lpText = LuckPermsHelper.parseLegacy(lpStr);
        Text nameText = Text.literal(senderName).formatted(nameColor);
        Text msgText = Text.literal(message).setStyle(cfg.getStyle());

        MutableText formattedMessage = Text.empty();
        String[] parts = format.split("%", -1);
        for (int i = 0; i < parts.length; i++) {
            if (i % 2 == 1) { // It's a placeholder
                String p = parts[i];
                switch(p) {
                    case "prefix": formattedMessage.append(prefixText); break;
                    case "luckperms_prefix": formattedMessage.append(lpText); break;
                    case "player_name": formattedMessage.append(nameText); break;
                    case "message": formattedMessage.append(msgText); break;
                    default: formattedMessage.append(Text.literal("%" + p + "%").setStyle(cfg.getStyle()));
                }
            } else {
                if (!parts[i].isEmpty()) {
                    formattedMessage.append(Text.literal(parts[i]).setStyle(cfg.getStyle()));
                }
            }
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (Permissions.check(player, channel.readPerm, 2)) {
                player.sendMessage(formattedMessage, false);
            }
        }
        // Always log to console
        server.sendMessage(formattedMessage);
    }
}
