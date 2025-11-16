package fr.mathilde.moreVanillaAdvancements.model;

import org.bukkit.Material;

public class Category {
    private final String name;
    private final Material icon;
    private final boolean show;

    public Category(String name, Material icon, boolean show) {
        this.name = name;
        this.icon = icon;
        this.show = show;
    }

    public String getName() {
        return name;
    }

    public Material getIcon() {
        return icon;
    }

    public boolean isShow() {
        return show;
    }
}

