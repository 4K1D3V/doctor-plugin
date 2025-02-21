# Doctor Plugin

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.20.4-green.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)
![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)

**Doctor** is a feature-rich, medical-themed Minecraft plugin that adds a Doctor role, injury system, team mechanics, emergency events, and interactive GUIs to enhance survival gameplay. Built with industrial-grade code, it offers modularity, scalability, and a polished user experience.

## Features

- **Doctor Role**: Register as a Doctor with `/doctor register` (requires `doctor.use` permission).
- **Injury System**: Random injuries (Broken Leg, Infection, Bleeding) on damage, managed centrally with configurable durations and effects.
- **Healing Mechanics**: Use craftable items (Bandage, Antidote, Suture Kit, Splint) to heal or cure, with dynamic cooldowns influenced by team buffs.
- **Crafting Recipes**: Configurable recipes for all medical items (e.g., Stethoscope from Iron Ingots + Glass Pane).
- **Team System**: Join/lead teams, share XP, toggle team chat, and manage members via GUI or commands.
- **Emergency Events**: Periodic server-wide events (Plague, Mass Bleeding, Mass Fracture) with configurable chances and durations.
- **GUIs**: Intuitive interfaces for Medical Kit, Team Management, and Leaderboard with pagination and interactivity.
- **XP Progression**: Level up (max 5) by healing, with team XP pools and buffs.
- **Leaderboard**: Track top teams by XP pool and member XP, available as text or GUI.
- **Persistence**: All data (Doctors, teams, XP) saved in `doctors.yml`.

## Installation

1. **Prerequisites**:
   - Minecraft server running Spigot 1.20.4.
   - Java 17 or higher.

2. **Download**:
   - Grab the latest `Doctor.jar` from the [Releases](https://github.com/4K1D3V/doctor-plugin/releases) page.

3. **Setup**:
   - Place `Doctor.jar` in your server’s `plugins` folder.
   - Start the server to generate `config.yml` and `doctors.yml`.

4. **Build from Source** (optional):
   ```bash
   git clone https://github.com/4K1D3V/doctor-plugin.git
   cd doctor-plugin
   mvn clean package
   ```
   - Copy `target/Doctor-1.0.jar` to `plugins`.

5. **Dependencies**:
   - Add Spigot 1.20.4 to your `pom.xml`:
     ```xml
     <dependency>
         <groupId>org.spigotmc</groupId>
         <artifactId>spigot</artifactId>
         <version>1.20.4-R0.1-SNAPSHOT</version>
         <scope>provided</scope>
     </dependency>
     ```

## Usage

### Commands
- `/doctor register`: Become a Doctor (requires `doctor.use`).
- `/doctor diagnose <player>`: Check a player’s health and injuries (needs Stethoscope).
- `/doctor heal <player>`: Heal or cure a nearby player with an item.
- `/doctor stats`: View your Doctor level and XP.
- `/doctor kit`: Open the Medical Kit GUI.
- `/doctor team [join <name>|leave|gui|kick <player>|distribute]`: Manage teams.
- `/doctor leaderboard [gui <page>]`: Display team leaderboard (text or GUI).

### Tab Completion
- Suggests subcommands, player names, team members, and leaderboard pages dynamically.

### Permissions
- `doctor.use`: Grants access to all commands (default: op).

### GUI Interfaces
- **Medical Kit**: Click items to heal nearby players with sound effects.
- **Team GUI**: View/manage team members, distribute XP pool, toggle team chat.
- **Leaderboard GUI**: Paginated list of teams; click for detailed stats.

## Configuration

Edit `config.yml` in the `plugins/Doctor` folder:

- **Doctor Settings**: XP per heal, max level, cooldowns, team buffs.
- **Injury Settings**: Chance, tick rate, durations, damage.
- **Emergency Settings**: Interval, event chances (sum to 1.0).
- **Items**: Materials, names, lore, and crafting recipes.

Example tweak:
```yaml
doctor:
  team-xp-buff-multiplier: 0.0001  # Slower cooldown reduction
emergency:
  interval-minutes: 15  # More frequent events
```

## Development

### Building
```bash
mvn clean package
```

### Structure
- **Package**: `pro.akii.ks`
- **Main**: `DoctorPlugin.java`
- **Managers**: `DoctorManager`, `InjuryManager`, `EmergencyManager`
- **Utils**: `NMSUtil`, `ConfigUtil`
- **Data**: `DoctorData`
- **GUI**: `MedicalKitGUI`, `TeamGUI`, `LeaderboardGUI`
- **Commands**: `DoctorCommand`
- **Listeners**: `DoctorListener`

### NMS Usage
- Uses `v1_20_R3` for health, particles, and attribute manipulation.

## Contributing

1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/new-feature`).
3. Commit changes (`git commit -m "Add new feature"`).
4. Push to your fork (`git push origin feature/new-feature`).
5. Open a Pull Request.

### Guidelines
- Follow Java naming conventions.
- Add comments for complex logic.
- Test thoroughly before submitting.

## License

Licensed under the [MIT License](LICENSE). Feel free to use, modify, and distribute!

## Issues

Report bugs or suggest features on the [Issues](https://github.com/4K1D3V/doctor-plugin/issues) page.

## Credits

Developed by [Kit](https://github.com/4K1D3V). Inspired by medical-themed gameplay enhancements.
