package fr.mathilde.moreVanillaAdvancements.model;

public enum ConditionType {
    BLOCK_BREAK,
    BLOCK_PLACE,
    ENTITY_KILL,

    // Statistiques de mouvement
    WALK,           // Distance marchée
    SPRINT,         // Distance sprintée
    CROUCH,         // Distance en sneak
    SWIM,           // Distance nagée
    FLY,            // Distance volée
    JUMP,           // Nombre de sauts

    // Interactions
    BLOCK_INTERACT,     // Clic droit sur un bloc
    ITEM_USE,           // Utiliser un item (clic droit)
    ITEM_CONSUME,       // Consommer un item (nourriture, potion)
    ENTITY_INTERACT,    // Interagir avec une entité (villageois, etc.)

    // Autres statistiques
    DAMAGE_DEALT,   // Dégâts infligés
    DAMAGE_TAKEN,   // Dégâts reçus
    DEATH,          // Nombre de morts
    ITEM_CRAFT,     // Items craftés
    ITEM_PICKUP,    // Items ramassés
    ITEM_DROP,      // Items jetés
    FISH_CAUGHT,    // Poissons pêchés

    // Exploration
    BIOME_VISIT,        // Visiter un biome
    DIMENSION_TRAVEL,   // Aller dans une dimension

    // Ressources
    ORE_MINE,           // Miner un minerai

    // Élevage & Agriculture
    ANIMAL_BREED,       // Reproduire des animaux
    CROP_HARVEST,       // Récolter
    ANIMAL_TAME,        // Apprivoiser des animaux

    // Économie
    FURNACE_SMELT,      // Fondre des items
    ENCHANT_ITEM,       // Enchanter des items

    // Défis spéciaux
    KILL_STREAK,        // Tuer X entités sans mourir

    // Autres
    PLAY_TIME           // Temps de jeu (en ticks)
}
