# Système de langue (lang_XX.yml)

## 📋 Description

Le plugin MoreVanillaAdvancements supporte **plusieurs langues** avec des fichiers pré-configurés. Tous les messages, titres, lores, et notifications peuvent être personnalisés pour chaque langue.

## 🌍 Langues disponibles

Le plugin inclut 3 langues pré-configurées :
- 🇫🇷 **Français** (`lang_fr.yml`) - Langue par défaut
- 🇬🇧 **Anglais** (`lang_en.yml`) - English
- 🇪🇸 **Espagnol** (`lang_es.yml`) - Español

### Changer de langue

**Commande** : `/mva lang <langue>`

**Exemples** :
```
/mva lang en  → Switch to English
/mva lang fr  → Passer en français
/mva lang es  → Cambiar a español
```

La langue sera chargée immédiatement et tous les messages seront affichés dans la nouvelle langue.

## 🎨 Codes couleur supportés

Le plugin supporte les codes couleur Minecraft standards :
- **Couleurs** : `&0-9`, `&a-f`
- **Formatage** : `&k` (obfusqué), `&l` (gras), `&m` (barré), `&n` (souligné), `&o` (italique)
- **Reset** : `&r` (réinitialiser le formatage)

## 📁 Structure du fichier

Le fichier est organisé en sections logiques :

### 1. Messages généraux (`general`)
- Préfixe du plugin
- Messages d'erreur communs (permissions, joueur introuvable, etc.)

### 2. Commandes (`commands`)
- Messages pour chaque commande : reload, open, view, list, reset
- Messages de succès, d'erreur et d'usage

### 3. Achievements (`achievements`)
- Messages de complétion (title, broadcast, log)
- Configuration des GUIs (catégories et achievements)
- Lores et descriptions

### 4. Validation de configuration (`validation`)
- En-têtes des erreurs et avertissements
- Tous les messages de validation
- Messages d'erreur et d'avertissement spécifiques

### 5. Logs console (`logs`)
- Messages de démarrage
- Messages de reload
- Logs d'achievements complétés

### 6. GUI Admin (`admin`)
- Titre et items du GUI des paramètres
- Descriptions des options

## 🔤 Placeholders disponibles

Les placeholders suivants peuvent être utilisés dans les messages :

| Placeholder | Description |
|------------|-------------|
| `{player}` | Nom du joueur |
| `{name}` | Nom de l'achievement |
| `{id}` | ID de l'achievement |
| `{description}` | Description de l'achievement |
| `{category}` | Catégorie de l'achievement |
| `{current}` | Progression actuelle |
| `{required}` | Progression requise |
| `{amount}` | Quantité |
| `{xp}` | Points d'expérience |
| `{item}` | Nom de l'item |
| `{count}` | Compteur générique |
| `{completed}` | Nombre d'achievements complétés |
| `{total}` | Nombre total d'achievements |
| `{admin}` | Administrateur effectuant l'action |
| `{target}` | Cible de l'action |
| `{icon}` | Icône |
| `{type}` | Type |
| `{valid_types}` | Types valides |
| `{section}` | Section de config |
| `{achievements}` | Nombre d'achievements |
| `{categories}` | Nombre de catégories |

## 📝 Exemples de personnalisation

### Changer le format de broadcast
```yaml
achievements:
  completion:
    broadcast-format: "&6[Succès] &e{player} &7vient de compléter &b{name} &7!"
```

### Personnaliser les titres
```yaml
achievements:
  completion:
    title-main: "&6🎉 BRAVO 🎉"
    title-sub: "&e{name}"
```

### Modifier les messages de commandes
```yaml
commands:
  reload:
    success: "&a✓ Configuration rechargée !"
  reset:
    all-success: "&aTous les succès de &e{player} &aont été réinitialisés"
```

### Personnaliser le GUI
```yaml
achievements:
  gui:
    achievements:
      item-lore:
        progress: "&6⚡ Progression: &e{current}&7/&e{required}"
        completed: "&a✓ TERMINÉ ✓"
```

## 🌍 Traduction

### Ajouter une nouvelle langue

Pour créer une traduction dans une autre langue :

1. **Copiez un fichier existant**
   ```powershell
   Copy-Item lang_fr.yml lang_de.yml
   ```

2. **Traduisez tous les messages**
   - Ouvrez `lang_de.yml`
   - Traduisez tous les messages (ne modifiez pas les placeholders `{...}`)
   - Conservez les codes couleur `&` ou modifiez-les selon vos préférences

3. **Ajoutez le code de langue dans le code**
   - Ouvrez `LangManager.java`
   - Ajoutez votre code de langue dans `AVAILABLE_LANGUAGES` :
   ```java
   private static final List<String> AVAILABLE_LANGUAGES = Arrays.asList("fr", "en", "es", "de");
   ```

4. **Testez la nouvelle langue**
   ```
   /mva lang de
   ```

**Note** : Les placeholders comme `{player}`, `{name}`, etc. doivent rester inchangés pour fonctionner correctement.

### Langues suggérées

Voici quelques codes de langue ISO 639-1 courants :
- `de` - Deutsch (Allemand)
- `it` - Italiano (Italien)
- `pt` - Português (Portugais)
- `ru` - Русский (Russe)
- `ja` - 日本語 (Japonais)
- `zh` - 中文 (Chinois)
- `ar` - العربية (Arabe)
- `nl` - Nederlands (Néerlandais)
- `pl` - Polski (Polonais)

## 🔧 Utilisation dans le code

Le LangManager charge automatiquement le fichier au démarrage. Pour recharger après modification :
```
/mva reload
```

## 📌 Notes importantes

1. **Ne supprimez pas de clés** : Tous les chemins (ex: `commands.reload.success`) doivent être présents
2. **Respectez les placeholders** : Ne traduisez pas `{player}` ou `{name}`, ils sont remplacés automatiquement
3. **Codes couleur** : Utilisez `&` et non `§`
4. **Listes** : Les lores du GUI supportent plusieurs lignes (format liste YAML)

## 🎯 Chemins importants

### Messages fréquents
- `general.no-permission` - Message de permission manquante
- `achievements.completion.broadcast-format` - Format d'annonce publique
- `achievements.gui.achievements.item-lore.*` - Lore des achievements dans le GUI

### Validation
- `validation.errors.*` - Messages d'erreur de config
- `validation.warnings.*` - Messages d'avertissement de config

### Logs
- `logs.achievement.completed` - Log console de complétion
- `logs.startup.*` - Messages de démarrage

## 💡 Conseils

- Gardez les messages courts pour le GUI (limitation de largeur Minecraft)
- Utilisez des couleurs cohérentes pour une meilleure lisibilité
- Testez vos modifications en jeu après chaque changement
- Créez une sauvegarde avant de modifier le fichier

