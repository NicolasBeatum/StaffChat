package com.staffchat.util;

import net.fabricmc.loader.api.FabricLoader;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.UUID;

public class LuckPermsHelper {

    private static boolean isLuckPermsLoaded() {
        return FabricLoader.getInstance().isModLoaded("luckperms");
    }

    public static String getPrefix(UUID uuid) {
        if (isLuckPermsLoaded()) {
            return fetchPrefix(uuid);
        }
        return "";
    }

    private static String fetchPrefix(UUID uuid) {
        try {
            LuckPerms api = LuckPermsProvider.get();
            User user = api.getUserManager().getUser(uuid);
            if (user != null) {
                String prefix = user.getCachedData().getMetaData().getPrefix();
                return prefix != null ? prefix : "";
            }
        } catch (Exception e) {
            // Ignore
        }
        return "";
    }

    public static MutableText parseLegacy(String text) {
        if (text == null || text.isEmpty()) return Text.empty();
        MutableText finalResult = Text.empty();
        StringBuilder builder = new StringBuilder();
        Formatting currentFormat = Formatting.WHITE;

        text = text.replace('&', '\u00A7');

        if (!text.contains("\u00A7")) {
            return Text.literal(text).formatted(currentFormat);
        }

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\u00A7' && i + 1 < text.length()) {
                if (builder.length() > 0) {
                    finalResult.append(Text.literal(builder.toString()).formatted(currentFormat));
                    builder.setLength(0);
                }
                char code = text.charAt(i + 1);
                Formatting f = Formatting.byCode(code);
                if (f != null) {
                    currentFormat = f;
                }
                i++;
            } else {
                builder.append(c);
            }
        }
        if (builder.length() > 0) {
            finalResult.append(Text.literal(builder.toString()).formatted(currentFormat));
        }
        return finalResult;
    }
}
