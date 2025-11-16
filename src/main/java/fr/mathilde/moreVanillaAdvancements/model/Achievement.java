package fr.mathilde.moreVanillaAdvancements.model;

import org.bukkit.Material;

import java.util.Map;

public class Achievement {
    private final String id;
    private final String displayName;
    private final String description;
    private final Material icon;
    private final ConditionType type;
    private final String target;
    private final int amount;
    private final Reward reward;
    private final String category;

    public Achievement(String id, String displayName, String description, Material icon, ConditionType type, String target, int amount, Reward reward, String category) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.type = type;
        this.target = target;
        this.amount = amount;
        this.reward = reward;
        this.category = category;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public Material getIcon() { return icon; }
    public ConditionType getType() { return type; }
    public String getTarget() { return target; }
    public int getAmount() { return amount; }
    public Reward getReward() { return reward; }
    public String getCategory() { return category; }

    public Map<String, Object> toMap() {
        return Map.of(
                "id", id,
                "name", displayName,
                "description", description,
                "icon", icon.name(),
                "type", type.name(),
                "target", target,
                "amount", amount,
                "category", category != null ? category : "Général"
        );
    }
}
