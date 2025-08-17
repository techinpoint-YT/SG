# SG_RLGL (Squid Game Red Light Green Light)

A Minecraft plugin implementing the Red Light Green Light game from Squid Game with multi-arena support and advanced features.

## Features

- **Multi-Arena Support**: Create and manage multiple game arenas
- **Customizable Timers**: Configure random duration ranges for Red and Green light phases
- **Flexible Elimination**: Choose between kill, kick, or teleport elimination methods
- **Spectator Mode**: Eliminated players can watch the game continue
- **Rewards System**: Execute custom commands when players win
- **Visual Effects**: Boss bars, action bars, titles, and sounds
- **Admin Controls**: Special items for game management
- **Configurable Messages**: Full message customization with placeholders

## Installation

1. Download the latest release JAR file
2. Place it in your server's `plugins` folder
3. Restart your server
4. Configure the plugin using `/rlgl` commands

## Building from Source

Requirements:
- JDK 17 or higher
- Gradle

```bash
# Clone the repository
git clone <repository-url>
cd SG_RLGL

# Build the plugin
./gradlew build

# The JAR file will be in build/libs/SG_RLGL-1.0.0.jar
```

## Commands

### Main Commands
- `/rlgl help` - Show help message
- `/rlgl start [arena]` - Start a game in the specified arena (default: "default")
- `/rlgl stop [arena]` - Stop a game in the specified arena
- `/rlgl reload` - Reload the plugin configuration

### Setup Commands
- `/rlgl set lobby` - Set the main game lobby location
- `/rlgl set guestlobby` - Set the guest lobby location
- `/rlgl set finish` - Get a special hoe to set the finish line
- `/rlgl set admin <player>` - Add a player as an admin
- `/rlgl set guest <player>` - Add a player as a guest
- `/rlgl set rules` - Apply world rules (disable day/night cycle, weather, etc.)

### Management Commands
- `/rlgl remove <admin|guest|winner> <player>` - Remove a player's role
- `/rlgl tp <guest|admin|player> <guestlobby|gamelobby>` - Teleport players
- `/rlgl arena create <name>` - Create a new arena
- `/rlgl arena list` - List all arenas

## Configuration

The plugin generates a comprehensive `config.yml` file with the following sections:

### Game Settings
```yaml
game:
  max-players: 20
  min-players: 2
```

### Timer Configuration
```yaml
timers:
  red-light:
    min: 3    # Minimum seconds
    max: 8    # Maximum seconds
  green-light:
    min: 5    # Minimum seconds
    max: 15   # Maximum seconds
```

### Elimination Options
```yaml
elimination:
  method: "KICK"  # KILL, KICK, or TELEPORT
  spectator-mode: true
```

### Effects and Sounds
```yaml
effects:
  sounds:
    enabled: true
    green-light: "BLOCK_NOTE_BLOCK_PLING"
    red-light: "BLOCK_NOTE_BLOCK_BASS"
  boss-bar:
    enabled: true
  action-bar:
    enabled: true
```

### Rewards System
```yaml
rewards:
  commands:
    - "give {player} diamond 5"
    - "broadcast {player} won in arena {arena}!"
```

## Placeholders

The following placeholders can be used in messages and commands:
- `{player}` - Player name
- `{arena}` - Arena name
- `{time}` - Time remaining
- `{x}`, `{y}`, `{z}` - Coordinates
- `{world}` - World name
- `{count}` - Count of players

## Permissions

- `sgrlgl.use` - Basic plugin usage (default: op)
- `sgrlgl.admin` - Full administrative access (default: op)

## Game Rules

1. **Green Light**: Players can move freely
2. **Red Light**: Players must stop completely (no movement, looking around, etc.)
3. **Elimination**: Players who move during Red Light are eliminated based on the configured method
4. **Winning**: First player to reach the finish line wins and receives rewards
5. **Immunity**: Admins, guests, and previous winners are immune to elimination

## Special Items

### Finish Setter Hoe
- Given with `/rlgl set finish`
- Right-click any block to set it as the finish line
- Only works for players with admin permissions

### Admin Control Dyes
- Given automatically when a game starts
- Green Dye: Manually trigger Green Light
- Red Dye: Manually trigger Red Light
- Only works for admin players during active games

## Multi-Arena Support

The plugin supports multiple simultaneous game arenas:
- Each arena operates independently
- Players can only be in one arena at a time
- Arenas have separate player lists and game states
- Commands can target specific arenas

## Version History

### 1.0.0
- Complete refactor of the original plugin
- Added multi-arena support
- Implemented customizable timers
- Added elimination options and spectator mode
- Introduced rewards system
- Enhanced visual and audio effects
- Improved configuration system
- Added comprehensive command system

## Support

For issues, feature requests, or contributions, please visit the project repository.

## License

This project is licensed under the MIT License - see the LICENSE file for details.