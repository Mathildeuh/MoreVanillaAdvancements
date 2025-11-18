# MoreVanillaAdvancements

**MoreVanillaAdvancements** is a plugin that enhances Minecraft's vanilla advancement system by offering a complete and fully customizable progression experience. Create your own achievements, manage rewards, and provide engaging and motivating gameplay for your players.

---

## ✦ Key Features ✦

### ✨ Custom Advancements
- Create unlimited advancements with customizable icons, descriptions, and objectives  
- Full progression support (counters, percentages, milestones, etc.)  

### 🎁 Reward System
- Grant experience points upon completion  
- Automatically give items as rewards  
- Execute custom commands when an advancement is completed  

### 📂 Category Organization
- Organize advancements into logical categories  
- Clean visual layout with customizable icons  
- Optional hideable categories  

### 🖥️ Intuitive User Interface
- Player\-friendly and easy\-to\-navigate GUI menus  
- Clear display of progress, completion state, and rewards  
- Intuitive back button and navigation controls  

### 🛡️ Anti\-Grief Protection
- Items cannot be removed from the GUI  
- Shift\-click, drag and most inventory interactions are blocked  
- Safe for public use on survival servers  

### 📊 Progress Viewing
- Open your own progress menu with `/mva` or `/mva open`  
- View another player’s advancements using `/mva open <player>`  
- Unified, consistent interface for all players  

### ⚙️ Flexible Configuration
- Multi\-language message system  
- Simple YAML\-based configuration files  
- Supports color codes and text formatting  

---

## ✦ Commands ✦

- **/mva \[open\]** – Open your own advancement menu  
- **/mva open \<player\>** – View another player's advancements  
- **/mva create** – Open the GUI to create a new advancement  
- **/mva edit** – Open the GUI to edit existing advancements  
- **/mva delete \<id\>** – Open a confirmation GUI to delete an advancement  
- **/mva settings** – Open the admin settings GUI  
- **/mva list \[page\]** – List all advancements with clickable pagination  
- **/mva view \<player\>** – Open another player's progression GUI (online or offline, if supported)  
- **/mva reset \<player\> \[achievementId\|all\]** – Reset a player's advancement progression  
- **/mva lang \[code\]** – Change the plugin language (`en`, `fr`, `es`, etc.)  
- **/mva reload** – Reload and validate configuration files  

---

## ✦ Configuration ✦

Everything is configured through YAML files:

- Advancements and their properties (name, description, type, amount, category, rewards, etc.)  
- Categories and their icons/visibility  
- Messages and displayed text  
- GUI titles and icons  

Typical files:

- `plugins/MoreVanillaAdvancements/config.yml` – General settings, language, GUI settings  
- `plugins/MoreVanillaAdvancements/achievements.yml` – All custom advancements  
- `plugins/MoreVanillaAdvancements/progress.yml` – Saved player progression (auto\-managed by the plugin)  

Example advancement (simplified):

```yaml
achievements:
  marathon:
    name: Marathon
    description: Walk 10 km in total
    icon: LEATHER_BOOTS
    type: WALK
    target: "*"
    amount: 1000000   # 10 km in centimeters
    category: "Movement"
    reward:
      xp: 100
      give: "BREAD:4"
      command: "say {player} just completed Marathon!"
```

Configuration validation:

- On startup and `/mva reload`, the plugin validates all achievements  
- Invalid fields (missing type, invalid amount, bad material names, etc.) are reported in console  
- Critical errors prevent loading; warnings are logged but the plugin continues to run  

---

## ✦ Permissions ✦

Player access:

- **morevanillaadvancements.view** – Access to the advancement menu (default: `true`)  
- **morevanillaadvancements.view.others** – View other players' advancements  

Core permissions (suggested):

- **mva.use** – General access to the `/mva` command (default: `true`)  
- **mva.editor** – Access to editor features (`/mva create`, `/mva edit`, `/mva delete`, `/mva settings`)  
- **mva.reset** – Use `/mva reset` to reset player progress  
- **mva.reload** – Use `/mva reload` to reload configuration  
- **mva.lang** – Change plugin language with `/mva lang`  

> Adjust permission names to match your `plugin.yml` if they differ.

---

## ✦ Supported Versions ✦

- Minecraft server software: **Paper/Spigot 1.21+**  
- Recommended Java version: **Java 21** or newer  

Older versions are not officially supported unless explicitly stated in the releases.

---

## ✦ User Interface ✦

### Player GUI
The main advancements GUI shows:

- Name and description of each advancement  
- Current progress (e.g. `42 / 100`, percentage, etc.)  
- Completion state (e.g. a green \`✔ Completed\` line when done)  
- Rewards summary (XP, items, and/or commands)  

Navigation:

- Clickable items to open categories or details  
- Back button to return to the previous menu  
- Protection against taking/moving items out of the GUI  

### Admin GUI
The admin/settings GUI allows you to:

- Quickly toggle broadcast messages in chat  
- Enable/disable private titles for completed advancements  
- Access editor tools to create, edit or delete advancements  

---

## ✦ Rewards System ✦

Each advancement can grant one or more rewards:

- **XP reward**  
  ```yaml
  reward:
    xp: 100
  ```

- **Item reward** (single or multiple items)  
  ```yaml
  reward:
    give: "DIAMOND:8,EMERALD:4"
  ```

- **Command reward** (executed from console)  
  ```yaml
  reward:
    command: "say {player} completed an epic challenge!"
  ```

Supported placeholders (in rewards/commands):

- `{player}` – The name of the player who completed the advancement  

---

## ✦ Multi\-Language Support ✦

The plugin ships with a simple localization system:

- Default language: `en`  
- Additional example languages: `fr`, `es` (if provided in your resources)  

Languages can be configured:

- In `config.yml` (e.g. `language: en`)  
- At runtime using `/mva lang <code>`  

Language files (examples):

- `lang_en.yml`  
- `lang_fr.yml`  
- `lang_es.yml`  

Each file contains all translatable messages (GUI titles, chat messages, errors, etc.).

---

## ✦ Data & Performance ✦

- Player progress is persisted in YAML and cached in memory  
- Completed advancements are no longer tracked to reduce overhead  
- Regular auto\-save and safe shutdown saving  
- Designed to be lightweight and suitable for survival and SMP servers  

Technical notes:

- Distances tracked in centimeters (100 cm = 1 block)  
- Damage tracked as half\-hearts × 10  
- Time tracked in ticks (20 ticks = 1 second)  

---

## ✦ Build & Installation ✦

### Build (Gradle)
From the project root:

```bash
./gradlew clean build
```

The built jar will be available in `build/libs/` and can optionally be auto\-copied to a `server/plugins/` folder if you configured it in your Gradle script.

### Installation
1. Place the jar in your server's `plugins/` folder  
2. Start the server once to generate default configuration files  
3. Edit `config.yml` and `achievements.yml` to match your server's needs  
4. Use `/mva reload` to apply changes, or restart the server  

---

## ✦ Roadmap ✦

Planned or potential future improvements:

- PlaceholderAPI support (e.g. `%mva_progress_{id}%`)  
- Multi\-stage / tiered advancements  
- Per\-advancement sounds and custom completion messages  
- Improved in\-game GUI editor (search, duplication, filters, etc.)  
- Additional trigger types (play time, custom stats, etc.)  

---

## ✦ Support ✦

If you need help:

- Check the documentation in this repository  
- Open an issue on GitHub with logs and your configuration snippets  
- Provide your server version, Java version, and plugin version when reporting bugs  

Thank you for using **MoreVanillaAdvancements**!
