# MoreVanillaAdvancements

Plugin Spigot/Paper 1.21.x pour des achievements (succès) 100% configurables et modulaires.

## 🎯 Fonctionnalités principales
- **110+ achievements vanilla** pré-configurés couvrant tous les modes de jeu
- **40+ types d'événements** trackables : blocs, combat, mouvement, craft, exploration, social, etc.
- **Système de catégories** optionnel avec 16 catégories (Extraction, Combat, Construction, etc.)
- **GUI joueur** listant la progression (complété/pas encore), consultable pour soi ou un autre joueur
- **GUI admin** pour basculer rapidement les réglages (broadcast chat, title privé)
- **Système de récompenses** optionnelles: XP, items, et/ou commande console
- **Annonces publiques** dans le chat avec nom de l'achievement **hoverable** et description
- **Title privé** au joueur (paramétrables)
- **Compatible serveurs crack** (offline-mode) via UUID hors-ligne
- **Validation automatique de configuration** avec erreurs bloquantes et avertissements
- **Logs console détaillés** : démarrage, completions, reloads
- **Commandes claires** avec tab-completion
- **Auto-export** dans server/plugins après build

## 📋 Commandes
- `/mva` (alias: `/achievements`, `/succes`)
  - `reload` – recharge et valide la configuration
  - `open [joueur]` – ouvre le GUI (pour soi par défaut, ou pour un joueur en ligne)
  - `view <joueur>` – ouvre le GUI de la progression d'un autre joueur (en ligne ou hors-ligne)
  - `list [page]` – liste les achievements avec pagination (10 par page) et navigation cliquable
  - `reset <joueur> [achievementId|all]` – remet à zéro la progression du joueur
  - `settings` – ouvre le GUI d'administration des réglages
  - `lang <langue>` – change la langue du plugin (fr, en, es)

## 🔐 Permissions
- `mva.use` (par défaut: true)
- `mva.reload` (par défaut: op)
- `mva.reset` (par défaut: op)

## 🎮 Types d'achievements supportés (40+)

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

### Autres basiques
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
- `FURNACE_SMELT` - Fondre des items dans un four

### Social & Serveur
- `PLAYER_JOIN` - Rejoindre le serveur pour la première fois (donné une seule fois)
- `PLAYER_CHAT` - Envoyer un message dans le chat
- `NIGHT_PLAY` - Jouer la nuit (entre 12000 et 24000 ticks)

### Futur
- `PLAY_TIME` - Temps de jeu (en ticks) [Déclaré, à implémenter]

## 📝 Configuration

### Structure de base
```yaml
settings:
  broadcastChat: true      # Annonce publique dans le chat
  showTitle: true          # Title privé au joueur
  chatFormat: "&b{player} &7a complété l'achievement &a{name}"

categories:
  "Extraction":
    icon: STONE_PICKAXE
    show: true
  # ... autres catégories

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
      give: "BREAD:2"  # Format: "MATERIAL:QUANTITY" ou "MAT1:QTY1,MAT2:QTY2,..."
      command: "say {player} a réussi!"
```

### Système de récompenses

Le plugin supporte trois types de récompenses configurables :

**1. XP**
```yaml
reward:
  xp: 100
```

**2. Items (format "give")**
- **Un seul item** : `give: "DIAMOND:8"` → donne 8 diamants
- **Plusieurs items** : `give: "DIAMOND:16,EMERALD:8,GOLD_INGOT:32"` → donne plusieurs items différents
- Format : `MATERIAL:QUANTITY` séparés par des virgules

```yaml
reward:
  xp: 100
  give: "DIAMOND:8,EMERALD:4"
```

**3. Commandes console**
```yaml
reward:
  xp: 100
  give: "DIAMOND:16"
  command: "say {player} a réussi !"
```
Le placeholder `{player}` est remplacé par le nom du joueur.

### Validation de configuration
Le plugin valide automatiquement la configuration :
- **Au démarrage** : Les erreurs bloquent le démarrage du plugin
- **Au reload** : Les erreurs annulent le reload, les avertissements s'affichent mais le reload continue

**Erreurs détectées** ❌ :
- Types invalides
- Champs critiques manquants (name, type, amount)
- Amount non entier ou <= 0

**Avertissements affichés** ⚠️ :
- Description manquante
- Format chatFormat incomplet ({name} ou {player})
- Catégories non définies ou non utilisées
- Icons/items invalides

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

#### Social
```yaml
first_join:
  name: Bienvenue
  description: Rejoindre le serveur pour la première fois
  icon: PAINTING
  type: PLAYER_JOIN
  target: "*"
  amount: 1
  category: "Serveur"
  reward:
    xp: 50
```

## 📊 Achievements pré-configurés (110+)

Le plugin inclut 110+ achievements vanilla pré-configurés couvrant :
- **Extraction** (6) : Minage, ressources minérales
- **Construction** (7) : Placement de blocs, bâtiments
- **Combat** (14) : Tuer mobs, dégâts, kills consécutifs, boss
- **Mouvement** (7) : Marche, sprint, nage, vol, sauts, parkour
- **Craft** (6) : Crafting d'items, outils, armures
- **Pêche** (2) : Pêche basique et avancée
- **Survie** (1) : Mort du joueur
- **Interactions** (8) : Clic droit, consommation, eau
- **Exploration** (8) : Biomes, dimensions (Nether, End)
- **Ressources** (7) : Types de minerais (fer, or, diamant, lapis, etc.)
- **Agriculture** (5) : Récolte de cultures
- **Élevage** (6) : Reproduction et apprivoisement d'animaux
- **Économie** (2) : Enchantement, enclume
- **Défis** (3) : Défis spéciaux (kills consécutifs, warrior ultime)
- **Aventure** (8) : Exploration avancée, trésors, donjons
- **Serveur** (4) : Rejoindre, chat (débutant et maître), jouer la nuit
- **Richesse** (3) : Accumulation de ressources
- **Finitions** (3) : Achievements ultimes (constructeur, mineur, guerrier)

## 🎨 Interface utilisateur

### GUI des achievements
Le GUI affiche pour chaque achievement :
- **Nom** (en or)
- **Description** (gris)
- **Progression** actuelle (ex: 42/100)
- **Statut** : ✓ COMPLÉTÉ en vert si terminé
- **Récompenses** (si présentes) :
  - ✦ XP
  - ✦ Items (classiques ou format "give")
  - ✦ Commande spéciale

**Exemple d'affichage dans le GUI :**
```
Diamant trouvé
Miner votre premier diamant

Progression: 1/1
✓ COMPLÉTÉ

Récompenses:
  ✦ 100 XP
  ✦ 8x diamond
```

### Commande /mva list
Affiche tous les achievements organisés par catégorie avec leurs récompenses :
```
===== Achievements disponibles (110) =====
Page 1/11

▸ Extraction (6)
  • Premier coup de pioche (first_break) → 25 XP, BREAD:2
  • Diamant trouvé (diamond_found) → 100 XP, DIAMOND:8
  • Maître mineur (ore_master) → 200 XP

▸ Construction (4)
  • Constructeur ultime (ultimate_builder) → 1000 XP, DIAMOND:16,EMERALD:8,GOLD_INGOT:32, Commande
  ...

« Précédent | Suivant »
Utilisez /mva open pour voir votre progression
```

**Navigation :**
- Boutons cliquables "« Précédent" et "Suivant »" dans le chat
- Hover pour voir le numéro de page
- 10 achievements par page pour éviter le spam
- Pour la console : `/mva list <page>` (ex: `/mva list 2`)

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

## 🌍 Système multi-langue

Le plugin supporte plusieurs langues pré-configurées :
- 🇬🇧 **Anglais** (`en`) - **Langue par défaut**
- 🇫🇷 **Français** (`fr`)
- 🇪🇸 **Espagnol** (`es`)

### Configuration de la langue

**Dans config.yml** :
```yaml
settings:
  language: "en"  # Available languages: fr, en, es (generated on first use)
```

La langue configurée sera chargée automatiquement au démarrage du serveur.

**Important** : Les fichiers de langue sont générés **automatiquement lors du premier changement de langue** via la commande `/mva lang <langue>`. Seul le fichier anglais (`lang_en.yml`) est créé par défaut au premier démarrage.

### Changer de langue

**Commande** : `/mva lang <langue>`

**Exemples** :
```
/mva lang en  → Switch to English (default)
/mva lang fr  → Passe en français
/mva lang es  → Cambiar a español
```

Lors du premier changement vers une langue (fr ou es), le fichier correspondant sera automatiquement créé dans le dossier du plugin.

**Important** : Le changement de langue via la commande est **sauvegardé automatiquement** dans config.yml et persistera après un redémarrage du serveur.

**Tab-completion** : La commande propose automatiquement les langues disponibles.

### Langues disponibles

**Commande** : `/mva lang` (sans argument)
Affiche la liste des langues disponibles.

### Fichiers de langue

Les fichiers de langue se trouvent dans `plugins/MoreVanillaAdvancements/` :
- `lang_en.yml` - English (créé automatiquement au premier démarrage)
- `lang_fr.yml` - Français (créé lors du premier `/mva lang fr`)
- `lang_es.yml` - Español (créé lors du premier `/mva lang es`)

**Génération automatique** : Les fichiers de langue sont créés automatiquement lors de leur première utilisation. Cela évite d'encombrer le dossier du plugin avec des fichiers inutilisés.

**Personnalisation** : Vous pouvez modifier ces fichiers pour personnaliser les messages dans chaque langue.

**Ajout d'une langue** : 
1. Créez un fichier `lang_XX.yml` (où XX est le code de langue)
2. Copiez le contenu d'un fichier existant
3. Traduisez tous les messages
4. Ajoutez le code de langue dans `LangManager.AVAILABLE_LANGUAGES`

### Messages traduits

100% des messages du plugin sont traduits :
- ✅ Commandes et leurs retours
- ✅ Menus GUI (titres et lores)
- ✅ Messages d'achievements (completion, broadcast)
- ✅ Validation de configuration (erreurs et warnings)
- ✅ Logs console
- ✅ Boutons et navigation

## 💾 Données
- **Configuration**: `plugins/MoreVanillaAdvancements/config.yml`
- **Progression**: `plugins/MoreVanillaAdvancements/progress.yml` (persistant, compatible offline)

## 📖 Notes techniques

### Unités de mesure
- **Distances**: centimètres (100 cm = 1 bloc)
- **Dégâts**: demi-cœurs × 10 (100 = 5 cœurs)
- **Temps**: ticks (20 ticks = 1 seconde)
- **Nuit**: entre 12000 et 24000 ticks

### Performance
- Les achievements complétés ne sont plus trackés (optimisation)
- Sauvegarde automatique à la complétion et au shutdown
- Caches pour biomes, dimensions et kill streaks
- PLAYER_JOIN donné une seule fois via HashSet

### Logging
- Logs au démarrage (nombre d'achievements, catégories, types)
- Logs à chaque completion (joueur, achievement ID et nom)
- Logs au reload (avec détails de ce qui a été chargé)
- Logs des erreurs de configuration

### Compatibilité
- Spigot & Paper 1.21.x
- Java 21
- Compatible serveurs offline (crack) via UUID
- Support des deux formats Deepslate (DEEPSLATE_*_ORE et *_DEEPSLATE_ORE)

## 🚀 Roadmap / Idées futures
- ✅ 40+ types d'achievements
- ✅ 110+ achievements vanilla
- ✅ Système de catégories avec icônes
- ✅ Validation automatique de configuration
- ✅ Nom de l'achievement hoverable avec description
- ✅ Achievements sociaux (join, chat, nuit)
- ✅ Logs console détaillés
- ⏳ Éditeur complet des achievements en GUI (création/édition/suppression)
- ⏳ Support PlaceholderAPI (%mva_progress_{id}%)
- ⏳ Multi-langue via messages.yml
- ⏳ Achievements avec paliers/étapes
- ⏳ Sons et messages personnalisables par achievement
- ⏳ PLAY_TIME tracker (temps de jeu)

## 👨‍💻 Développement
- **Auteurs**: Mathilde, GitHub Copilot
- **Licence**: [À définir]
- **API Version**: 1.21
- **Dernière mise à jour**: 17/11/2025

