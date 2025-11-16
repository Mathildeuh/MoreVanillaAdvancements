package fr.mathilde.moreVanillaAdvancements.listeners;

import fr.mathilde.moreVanillaAdvancements.model.ConditionType;
import fr.mathilde.moreVanillaAdvancements.service.AchievementService;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProgressListeners implements Listener {

    private final AchievementService service;
    // Cache pour détecter les sauts (track la position Y précédente)
    private final Map<UUID, Double> lastY = new HashMap<>();

    public ProgressListeners(AchievementService service) {
        this.service = service;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        String target = e.getBlock().getType().name();
        service.onEvent(ConditionType.BLOCK_BREAK, target, p.getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        String target = e.getBlock().getType().name();
        service.onEvent(ConditionType.BLOCK_PLACE, target, p.getUniqueId());
    }

    @EventHandler
    public void onKill(EntityDeathEvent e) {
        if (e.getEntity().getKiller() == null) return;
        Player p = e.getEntity().getKiller();
        EntityType type = e.getEntityType();
        service.onEvent(ConditionType.ENTITY_KILL, type.name(), p.getUniqueId());
    }

    // Mouvement: marcher et détecter les sauts
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getTo() == null) return;
        if (e.getFrom().getBlockX() == e.getTo().getBlockX()
            && e.getFrom().getBlockY() == e.getTo().getBlockY()
            && e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;

        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();

        // Détecter les sauts (mouvement vertical positif)
        double fromY = e.getFrom().getY();
        double toY = e.getTo().getY();
        Double prevY = lastY.get(uuid);

        // Saut détecté si Y augmente de plus de 0.4 bloc
        if (prevY != null && toY > fromY && (toY - fromY) > 0.4) {
            service.onEvent(ConditionType.JUMP, "*", uuid);
        }
        lastY.put(uuid, toY);

        // Calculer distance parcourue
        double distance = e.getFrom().distance(e.getTo());
        int distanceCm = (int) (distance * 100); // Convertir en centimètres

        if (p.isSprinting()) {
            service.onEventWithAmount(ConditionType.SPRINT, "*", p.getUniqueId(), distanceCm);
        } else if (p.isSneaking()) {
            service.onEventWithAmount(ConditionType.CROUCH, "*", p.getUniqueId(), distanceCm);
        } else if (p.isSwimming()) {
            service.onEventWithAmount(ConditionType.SWIM, "*", p.getUniqueId(), distanceCm);
        } else if (p.isFlying() || p.isGliding()) {
            service.onEventWithAmount(ConditionType.FLY, "*", p.getUniqueId(), distanceCm);
        } else {
            service.onEventWithAmount(ConditionType.WALK, "*", p.getUniqueId(), distanceCm);
        }
    }


    // Craft
    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        String target = e.getRecipe().getResult().getType().name();
        int amount = e.getRecipe().getResult().getAmount();
        service.onEventWithAmount(ConditionType.ITEM_CRAFT, target, p.getUniqueId(), amount);
        service.onEventWithAmount(ConditionType.ITEM_CRAFT, "*", p.getUniqueId(), amount);
    }

    // Ramasser un item
    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        String target = e.getItem().getItemStack().getType().name();
        int amount = e.getItem().getItemStack().getAmount();
        service.onEventWithAmount(ConditionType.ITEM_PICKUP, target, p.getUniqueId(), amount);
        service.onEventWithAmount(ConditionType.ITEM_PICKUP, "*", p.getUniqueId(), amount);
    }

    // Jeter un item
    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        String target = e.getItemDrop().getItemStack().getType().name();
        int amount = e.getItemDrop().getItemStack().getAmount();
        service.onEventWithAmount(ConditionType.ITEM_DROP, target, p.getUniqueId(), amount);
        service.onEventWithAmount(ConditionType.ITEM_DROP, "*", p.getUniqueId(), amount);
    }

    // Pêcher
    @EventHandler
    public void onFish(PlayerFishEvent e) {
        if (e.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        Player p = e.getPlayer();
        service.onEvent(ConditionType.FISH_CAUGHT, "*", p.getUniqueId());
    }

    // Dégâts infligés
    @EventHandler
    public void onDamageDealt(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        Player p = (Player) e.getDamager();
        int damage = (int) (e.getFinalDamage() * 10); // Convertir en demi-cœurs
        service.onEventWithAmount(ConditionType.DAMAGE_DEALT, "*", p.getUniqueId(), damage);
    }

    // Dégâts reçus
    @EventHandler
    public void onDamageTaken(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        int damage = (int) (e.getFinalDamage() * 10); // Convertir en demi-cœurs
        service.onEventWithAmount(ConditionType.DAMAGE_TAKEN, "*", p.getUniqueId(), damage);
    }

    // Mort
    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        service.onEvent(ConditionType.DEATH, "*", p.getUniqueId());
    }

    // Interagir avec un bloc (clic droit)
    @EventHandler
    public void onBlockInteract(PlayerInteractEvent e) {
        if (e.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        if (e.getClickedBlock() == null) return;
        Player p = e.getPlayer();
        String target = e.getClickedBlock().getType().name();
        service.onEvent(ConditionType.BLOCK_INTERACT, target, p.getUniqueId());
        service.onEvent(ConditionType.BLOCK_INTERACT, "*", p.getUniqueId());
    }

    // Utiliser un item (clic droit avec un item en main)
    @EventHandler
    public void onItemUse(PlayerInteractEvent e) {
        if (e.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR
            && e.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        if (e.getItem() == null) return;
        Player p = e.getPlayer();
        String target = e.getItem().getType().name();
        service.onEvent(ConditionType.ITEM_USE, target, p.getUniqueId());
        service.onEvent(ConditionType.ITEM_USE, "*", p.getUniqueId());
    }

    // Consommer un item (manger, boire)
    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent e) {
        Player p = e.getPlayer();
        String target = e.getItem().getType().name();
        service.onEvent(ConditionType.ITEM_CONSUME, target, p.getUniqueId());
        service.onEvent(ConditionType.ITEM_CONSUME, "*", p.getUniqueId());
    }

    // Interagir avec une entité (villageois, animaux, etc.)
    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        String target = e.getRightClicked().getType().name();
        service.onEvent(ConditionType.ENTITY_INTERACT, target, p.getUniqueId());
        service.onEvent(ConditionType.ENTITY_INTERACT, "*", p.getUniqueId());
    }

    // Miner un minerai (ORE_MINE)
    @EventHandler(ignoreCancelled = true)
    public void onOreMine(BlockBreakEvent e) {
        String blockType = e.getBlock().getType().name();
        // Détecte: IRON_ORE, DIAMOND_ORE, DEEPSLATE_IRON_ORE, IRON_DEEPSLATE_ORE, etc.
        if (!blockType.contains("_ORE") && !blockType.contains("DEEPSLATE")) return;
        if (!blockType.endsWith("_ORE") && !blockType.startsWith("DEEPSLATE")) return;
        Player p = e.getPlayer();
        service.onEvent(ConditionType.ORE_MINE, blockType, p.getUniqueId());
        service.onEvent(ConditionType.ORE_MINE, "*", p.getUniqueId());
    }

    // Récolter des cultures (CROP_HARVEST)
    @EventHandler(ignoreCancelled = true)
    public void onCropHarvest(BlockBreakEvent e) {
        String blockType = e.getBlock().getType().name();
        if (!blockType.matches("(WHEAT|CARROTS|POTATOES|BEETROOTS|NETHER_WART|TORCHFLOWER)")) return;
        Player p = e.getPlayer();
        service.onEvent(ConditionType.CROP_HARVEST, blockType, p.getUniqueId());
        service.onEvent(ConditionType.CROP_HARVEST, "*", p.getUniqueId());
    }

    // Reproduire des animaux (ANIMAL_BREED)
    @EventHandler
    public void onAnimalBreed(org.bukkit.event.entity.EntityBreedEvent e) {
        if (!(e.getBreeder() instanceof Player)) return;
        Player p = (Player) e.getBreeder();
        String target = e.getEntity().getType().name();
        service.onEvent(ConditionType.ANIMAL_BREED, target, p.getUniqueId());
        service.onEvent(ConditionType.ANIMAL_BREED, "*", p.getUniqueId());
    }

    // Apprivoiser des animaux (ANIMAL_TAME) - via EntityTameEvent
    @EventHandler
    public void onAnimalTame(org.bukkit.event.entity.EntityTameEvent e) {
        if (!(e.getOwner() instanceof Player)) return;
        Player p = (Player) e.getOwner();
        String target = e.getEntity().getType().name();
        service.onEvent(ConditionType.ANIMAL_TAME, target, p.getUniqueId());
        service.onEvent(ConditionType.ANIMAL_TAME, "*", p.getUniqueId());
    }

    // Fondre des items dans un four (FURNACE_SMELT)
    @EventHandler
    public void onFurnaceSmelt(org.bukkit.event.inventory.FurnaceSmeltEvent e) {
        // Pas d'événement direct pour le joueur, on va utiliser une approche alternative
        // Pour l'instant, on peut tracker via les items résultants
    }

    // Enchanter des items (ENCHANT_ITEM)
    @EventHandler
    public void onEnchant(org.bukkit.event.enchantment.EnchantItemEvent e) {
        Player p = e.getEnchanter();
        String target = e.getItem().getType().name();
        service.onEvent(ConditionType.ENCHANT_ITEM, target, p.getUniqueId());
        service.onEvent(ConditionType.ENCHANT_ITEM, "*", p.getUniqueId());
    }

    // Visiter un biome (BIOME_VISIT)
    private final Map<UUID, String> lastBiome = new HashMap<>();

    @EventHandler
    public void onBiomeVisit(PlayerMoveEvent e) {
        if (e.getTo() == null) return;
        if (e.getFrom().getBlockX() == e.getTo().getBlockX()
            && e.getFrom().getBlockY() == e.getTo().getBlockY()
            && e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;

        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        String currentBiome = e.getTo().getBlock().getBiome().getKey().getKey();
        String previous = lastBiome.get(uuid);

        if (previous == null || !previous.equals(currentBiome)) {
            lastBiome.put(uuid, currentBiome);
            service.onEvent(ConditionType.BIOME_VISIT, currentBiome, uuid);
            service.onEvent(ConditionType.BIOME_VISIT, "*", uuid);
        }
    }

    // Voyager dans une dimension (DIMENSION_TRAVEL)
    private final Map<UUID, String> lastDimension = new HashMap<>();

    @EventHandler
    public void onDimensionTravel(PlayerChangedWorldEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        String currentDimension = p.getWorld().getName();
        String previous = lastDimension.get(uuid);

        if (previous == null || !previous.equals(currentDimension)) {
            lastDimension.put(uuid, currentDimension);
            service.onEvent(ConditionType.DIMENSION_TRAVEL, currentDimension, uuid);
            service.onEvent(ConditionType.DIMENSION_TRAVEL, "*", uuid);
        }
    }

    // Kill streak - compter les kills consécutifs
    private final Map<UUID, Integer> killStreaks = new HashMap<>();

    @EventHandler
    public void onKillStreak(EntityDeathEvent e) {
        if (e.getEntity().getKiller() == null) return;
        Player p = e.getEntity().getKiller();
        UUID uuid = p.getUniqueId();
        int streak = killStreaks.getOrDefault(uuid, 0) + 1;
        killStreaks.put(uuid, streak);
        service.onEventWithAmount(ConditionType.KILL_STREAK, "*", uuid, 1);
    }

    @EventHandler
    public void onKillStreakBreak(PlayerDeathEvent e) {
        UUID uuid = e.getEntity().getUniqueId();
        killStreaks.remove(uuid);
    }
}
