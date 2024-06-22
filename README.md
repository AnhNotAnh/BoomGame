# BoomGame
# Mobile Game Development

## Assignment Team and Game Concept

### Team Members and Responsibilities

| Name               | Email                        | Responsibilities     |
| ------------------ | ---------------------------- | -------------------- |
| Nguyen Van Duy Phan| Phany031@mymail.unisa.edu.au | Player class         |
| Duy Quoc Anh Nguyen| Ngudy105@mymail.unisa.edu.au | Map class            |
| Phuc Vinh Duong    | Duopy006@mymail.unisa.edu.au | Enemy class          |

## Game Concept

Our game is a thrilling mobile game where players must use bombs to eliminate enemies while being chased around a map. Players can strategically place bombs to destroy breakable tiles, expanding their playable area and making it easier to evade enemies. The objective is to kill all enemies on the map to win, while avoiding getting caught by enemies or getting caught in the bomb's blast radius.

### Typical Play Session

1. **Game Start**:
    - The player starts the game with an initial map that has limited space.
    - Enemies are scattered around the map, ready to chase the player.

2. **Movement and Bomb Placement**:
    - The player uses touch controls (movement buttons) to move around the map.
    - The player can place bombs by tapping an attack button on the screen.
    - Bombs have a timed explosion and a specific blast radius.

3. **Gameplay Mechanics**:
    - Bombs can kill enemies and destroy breakable tiles on the map.
    - Destroying tiles expands the playable area, providing more room to evade enemies.
    - If a bomb explodes within range of an enemy, the enemy is killed.
    - If the player is caught in the blast radius or touched by an enemy, the player dies, and the game is over.

4. **Winning and Losing**:
    - The player wins by killing all enemies (or a certain amount) on the map.
    - The player loses if an enemy catches them or if they are caught in a bomb blast.

### Features

1. **Map Design**:
    - Basic map layout with destructible and indestructible tiles.
    - Tile-breaking mechanics to expand the playable area.

2. **Player Mechanics**:
    - Player movement and bomb placement functionality using touch controls.
    - Collision detection between the player, bombs, and tiles.

3. **Enemy AI**:
    - Simple AI for enemy movement and pathfinding.
    - Enemy behavior for chasing the player and avoiding obstacles.

4. **Bomb Mechanics**:
    - Bomb placement and timed explosion mechanics.
    - Blast radius effects on enemies and tiles.

5. **Level Progression**:
    - Multiple levels with different map designs and enemy configurations.
    - Level-up conditions and transitions.

### Audio Features

- **Background Music**: A continuous background music track to enhance the gaming experience.
- **Sound Effects**:
    - Movement sound when the player moves.
    - Bomb placement sound when a bomb is placed.
    - Explosion sound when a bomb explodes.
    - Button click sound for menu interactions.

### Game Screens

1. **Main Menu**:
    - Play Button: Starts the game.
    - Exit Button: Exits the game.

2. **Game Screen**:
    - Player movement using on-screen buttons.
    - Bomb placement using an on-screen button.
    - Enemies moving around the map.

3. **Game Over Screen**:
    - Retry Button: Restarts the game.
    - Menu Button: Returns to the main menu.

## How to Run

1. Clone the repository.
2. Open the project in Android Studio.
3. Ensure all dependencies are installed.
4. Replace the placeholder paths for the sounds and textures in the `MyGdxGame` class with the actual paths to your assets.
5. Run the project on an emulator or physical device.

## Assets

- **Textures**:
    - Player textures for different animations (idle, walking, etc.).
    - Enemy textures for different animations (moving, dying, etc.).
    - Map tiles for different types of terrain (grass, rock, sand, etc.).
    - Button textures for UI elements.

- **Audio**:
    - Background music.
    - Movement sound.
    - Bomb placement sound.
    - Explosion sound.
    - Button click sound.

## Future Enhancements

- Implement additional levels with increasing difficulty.
- Add more enemy types with unique behaviors.
- Introduce power-ups and special items for the player.
- Enhance the AI for more challenging enemy movement and tactics.
- Improve the graphics and animations for a more polished look.

## License

This project is for educational purposes only.
