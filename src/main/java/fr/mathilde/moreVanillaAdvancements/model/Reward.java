package fr.mathilde.moreVanillaAdvancements.model;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Reward {
    private final int xp;
    private final String command;
    private final String giveItems;  // Format: "MATERIAL:QUANTITY,MATERIAL:QUANTITY"

    public Reward(int xp, String command, String giveItems) {
        this.xp = xp;
        this.command = command;
        this.giveItems = giveItems;
    }

    public void give(Player p) {
        if (xp > 0) p.giveExp(xp);

        // Traiter les items du format "give"
        if (giveItems != null && !giveItems.isEmpty()) {
            String[] items = giveItems.split(",");
            for (String itemStr : items) {
                itemStr = itemStr.trim();
                if (itemStr.contains(":")) {
                    String[] parts = itemStr.split(":");
                    String materialName = parts[0].trim();
                    try {
                        int qty = Integer.parseInt(parts[1].trim());
                        Material mat = Material.matchMaterial(materialName);
                        if (mat != null && qty > 0) {
                            p.getInventory().addItem(new ItemStack(mat, qty));
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        if (command != null && !command.isEmpty()) {
            String toRun = command.replace("{player}", p.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), toRun);
        }
    }

    // Getters pour affichage
    public int getXp() {
        return xp;
    }

    public String getCommand() {
        return command;
    }

    public String getGiveItems() {
        return giveItems;
    }

    public boolean hasReward() {
        return xp > 0 || (giveItems != null && !giveItems.isEmpty()) || (command != null && !command.isEmpty());
    }
}



