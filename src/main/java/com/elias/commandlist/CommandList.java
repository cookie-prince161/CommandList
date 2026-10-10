package com.elias.commandlist;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CommandList extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        getCommand("commands").setExecutor(this);
        getLogger().info("CommandList aktiviert.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
        Arrays.sort(plugins, Comparator.comparing(Plugin::getName, String.CASE_INSENSITIVE_ORDER));

        boolean any = false;
        for (Plugin plugin : plugins) {
            Map<String, Map<String, Object>> declared = plugin.getDescription().getCommands();
            if (declared == null || declared.isEmpty()) {
                continue;
            }

            List<String> lines = new ArrayList<>();
            for (Map.Entry<String, Map<String, Object>> entry : new TreeMap<>(declared).entrySet()) {
                String name = entry.getKey();
                Map<String, Object> info = entry.getValue();

                // Nur Befehle zeigen, die der Spieler benutzen darf
                Object permission = info.get("permission");
                if (permission instanceof String && !sender.hasPermission((String) permission)) {
                    continue;
                }

                String usage = "/" + name;
                Object usageObject = info.get("usage");
                if (usageObject instanceof String) {
                    String firstLine = ((String) usageObject).replace("<command>", name).split("\n")[0].trim();
                    if (firstLine.startsWith("/")) {
                        usage = firstLine;
                    }
                }

                String line = ChatColor.YELLOW + "  " + usage;
                Object description = info.get("description");
                if (description instanceof String && !((String) description).isBlank()) {
                    line += ChatColor.GRAY + " - " + description;
                }
                lines.add(line);
            }

            if (lines.isEmpty()) {
                continue;
            }
            any = true;
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + plugin.getName() + ":");
            for (String line : lines) {
                sender.sendMessage(line);
            }
        }

        if (!any) {
            sender.sendMessage(ChatColor.GRAY + "Keine Befehle gefunden.");
        }
        return true;
    }
}
