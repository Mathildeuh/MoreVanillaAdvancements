# MoreVanillaAdvancements

Plugin Spigot/Paper 1.21.x pour des achievements (succès) 100% configurables et modulaires.

## 🎯 Fonctionnalités principales
- **Achievements configurables** via `config.yml` avec 35+ types d'événements
- **Système de catégories** optionnel avec navigation intuitive dans le GUI
- **GUI joueur** listant la progression (complété/pas encore), consultable pour soi ou pour un autre joueur
- **GUI admin** pour basculer rapidement les réglages (broadcast chat, title privé)
- **Système de récompenses** optionnelles: XP, items, et/ou commande console
- **Annonces publiques** dans le chat avec nom de l'achievement **hoverable** et description
- **Title privé** au joueur (paramétrables)
- **Compatible serveurs crack** (offline-mode) via UUID hors-ligne
- **Validation automatique de configuration** avec erreurs bloquantes et avertissements
- **Commandes claires** avec tab-completion
- **Auto-export** dans server/plugins après build

## 📋 Commandes
- `/mva` (alias: `/achievements`, `/succes`)
  - `reload` – recharge et valide la configuration
  - `open [joueur]` – ouvre le GUI (pour soi par défaut, ou pour un joueur en ligne)
  - `view <joueur>` – ouvre le GUI de la progression d'un autre joueur (en ligne ou hors-ligne)
  - `list` – liste les IDs d'achievements disponibles
  - `reset <joueur> [achievementId|all]` – remet à zéro la progression du joueur
  - `settings` – ouvre le GUI d'administration des réglages

## 🔐 Permissions
- `mva.use` (par défaut: true)
- `mva.reload` (par défaut: op)
- `mva.reset` (par défaut: op)

## 🎮 Types d'achievements supportés (35+)

### Blocs & Construction
- `BLOCK_BREAK` - Casser des blocs (target: `*` ou `Material`)
- `BLOCK_PLACE` - Placer des blocs (target: `*` ou `Material`)

### Combat
- `ENTITY_KILL` - Tuer des entités (target: `*` ou `EntityType`)
- `DAMAGE_DEALT` - Dégâts infligés (en demi-cœurs × 10)
- `DAMAGE_TAKEN` - Dégâts reçus (en demi-cœurs × 10)
- `DEATH` - Nombre de morts
- `KILL_STREAK` - Tuer X entités sans mourir (kills consécutifs)

### Mouvement
- `WALK` - Distance marchée (en centimètres)
- `SPRINT` - Distance sprintée (en centimètres)
- `CROUCH` - Distance en sneak (en centimètres)
- `SWIM` - Distance nagée (en centimètres)
- `FLY` - Distance volée/glide (en centimètres)
- `JUMP` - Nombre de sauts

### Items & Craft
- `ITEM_CRAFT` - Items craftés (target: `*` ou `Material`)
- `ITEM_PICKUP` - Items ramassés (target: `*` ou `Material`)
- `ITEM_DROP` - Items jetés (target: `*` ou `Material`)

### Interactions
- `BLOCK_INTERACT` - Clic droit sur un bloc (target: `*` ou `Material`)
- `ITEM_USE` - Utiliser un item (clic droit, target: `*` ou `Material`)
- `ITEM_CONSUME` - Consommer un item/nourriture (target: `*` ou `Material`)
- `ENTITY_INTERACT` - Interagir avec une entité (target: `*` ou `EntityType`)

### Autres
- `FISH_CAUGHT` - Poissons pêchés

### Exploration
- `BIOME_VISIT` - Visiter un biome (target: `*` ou `BiomeName`)
- `DIMENSION_TRAVEL` - Voyager entre dimensions (target: `*` ou `DimensionName`)

### Ressources & Minerais
- `ORE_MINE` - Miner un minerai (target: `*` ou `DIAMOND_ORE`, `IRON_ORE`, etc.)
  - Support Deepslate: `DEEPSLATE_IRON_ORE`, `IRON_DEEPSLATE_ORE`, etc.

### Élevage & Agriculture
- `ANIMAL_BREED` - Reproduire des animaux (target: `*` ou `COW`, `SHEEP`, etc.)
- `CROP_HARVEST` - Récolter des cultures (target: `*` ou `WHEAT`, `CARROTS`, etc.)
- `ANIMAL_TAME` - Apprivoiser des animaux (target: `*` ou `WOLF`, `HORSE`, etc.)

### Économie
- `ENCHANT_ITEM` - Enchanter des items (target: `*` ou `Material`)

## 📝 Configuration

### Structure de base
```yaml
settings:
  broadcastChat: true      # Annonce publique dans le chat
  showTitle: true          # Title privé au joueur
  chatFormat: "&b{player} &7a complété l'achievement &a{name}"

achievements:
  id_achievement:
    name: Nom de l'achievement
    description: Description
    icon: MATERIAL_NAME
    type: TYPE_ACHIEVEMENT
    target: "*"  # ou un matériau/entité spécifique
    amount: 100
    category: "Nom de la catégorie"  # optionnel
    reward:      # optionnel
      xp: 25
      item: BREAD
      amount: 2
      command: "say {player} a réussi!"
```

### Validation de configuration
Le plugin valide automatiquement la configuration :
- **Au démarrage** : Les erreurs bloquent le démarrage du plugin
- **Au reload** : Les erreurs annulent le reload, les avertissements s'affichent mais le reload continue

**Erreurs détectées** ❌ :
- Types invalides
- Champs critiques manquants (name, type, amount)
- Amount non entier ou <= 0
- Icons/items invalides

**Avertissements affichés** ⚠️ :
- Description manquante
- Format chatFormat incomplet ({name} ou {player})
- Reward.amount <= 0

### Exemples d'achievements

#### Mouvement
```yaml
marathon:
  name: Marathon
  description: Marcher 10 km
  icon: LEATHER_BOOTS
  type: WALK
  target: "*"
  amount: 1000000  # 10 km en centimètres
  category: "Mouvement"
```

#### Combat
```yaml
first_blood:
  name: Premier sang
  description: Infliger 100 points de dégâts
  icon: IRON_SWORD
  type: DAMAGE_DEALT
  target: "*"
  amount: 1000  # 50 cœurs (1000 demi-cœurs)
  category: "Combat"
```

#### Craft
```yaml
sword_master:
  name: Forgeron
  description: Crafter une épée en diamant
  icon: DIAMOND_SWORD
  type: ITEM_CRAFT
  target: DIAMOND_SWORD
  amount: 1
  category: "Craft"
```

#### Exploration
```yaml
explorer:
  name: Explorateur
  description: Visiter 5 biomes différents
  icon: COMPASS
  type: BIOME_VISIT
  target: "*"
  amount: 5
  category: "Exploration"
```

## 🔧 Build & Installation

### Build
```powershell
./gradlew.bat clean build
```
Le jar sera automatiquement copié dans `server/plugins/`

### Installation manuelle
1. Télécharger le jar depuis `build/libs/MoreVanillaAdvancements-1.0.0.jar`
2. Placer dans le dossier `plugins/` de votre serveur
3. Redémarrer le serveur
4. Configurer `plugins/MoreVanillaAdvancements/config.yml`

## 💾 Données
- **Configuration**: `plugins/MoreVanillaAdvancements/config.yml`
- **Progression**: `plugins/MoreVanillaAdvancements/progress.yml` (persistant, compatible offline)

## 📊 Exemple de GUI

**Menu des catégories** (si multiple) :
- Affiche les catégories en tant que livres cliquables
- Montre le nombre d'achievements complétés/total par catégorie

**Liste des achievements** :
- Affiche chaque achievement avec icône, nom, progression
- Statut "COMPLÉTÉ" en vert si fini
- Possibilité de cliquer sur le nom pour voir la description (hover)

**Affichage du chat** :
```
Mathilde a complété l'achievement Premier sang
                                       ↑
                    Au survol : "Infliger 100 points de dégâts"
```

## 🚀 Roadmap / Idées futures
- ✅ Système de catégories avec navigation GUI
- ✅ Types d'achievements variés (35+ types)
- ✅ Validation automatique de configuration
- ✅ Nom de l'achievement hoverable avec description
- ⏳ Éditeur complet des achievements en GUI (création/édition/suppression)
- ⏳ Support PlaceholderAPI (%mva_progress_{id}%)
- ⏳ Multi-langue via messages.yml
- ⏳ Achievements avec paliers/étapes
- ⏳ Sons et messages personnalisables par achievement

## 📖 Notes techniques

### Unités de mesure
- **Distances**: centimètres (100 cm = 1 bloc)
- **Dégâts**: demi-cœurs × 10 (100 = 5 cœurs)
- **Temps**: ticks (20 ticks = 1 seconde)

### Performance
- Les achievements complétés ne sont plus trackés (optimisation)
- Sauvegarde automatique à la complétion et au shutdown
- Caches pour biomes et dimensions visités
- Kill streaks avec réinitialisation à la mort

### Compatibilité
- Spigot & Paper 1.21.x
- Java 21
- Compatible serveurs offline (crack) via UUID
- Support des deux formats Deepslate (DEEPSLATE_*_ORE et *_DEEPSLATE_ORE)

## 👨‍💻 Développement
- **Auteurs**: Mathilde, GitHub Copilot
- **Licence**: [À définir]
- **API Version**: 1.21
- **Dernière mise à jour**: 17/11/2025

