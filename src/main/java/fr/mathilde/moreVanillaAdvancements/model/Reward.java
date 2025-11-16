package fr.mathilde.moreVanillaAdvancements.model;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Reward {
    private int xp;
    private Material item;
    private int amount;
    private String command;

    public Reward(int xp, Material item, int amount, String command) {
        this.xp = xp;
        this.item = item;
        this.amount = amount;
        this.command = command;
    }

    public void give(Player p) {
        if (xp > 0) p.giveExp(xp);
        if (item != null && amount > 0) {
            p.getInventory().addItem(new ItemStack(item, amount));
        }
        if (command != null && !command.isEmpty()) {
            String toRun = command.replace("{player}", p.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), toRun);
        }
    }
}

