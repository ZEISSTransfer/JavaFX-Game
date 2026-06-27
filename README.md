# Auto Battler JavaFX Game

## Overview

Auto Battler is a graphical strategy game built with Java and JavaFX. The player buys units from a shop, places them on a board, equips items, and starts automatic battles against enemy teams. The objective is to survive all rounds while managing gold, unit placement, upgrades, and equipment.

## Game Objective

The player starts with health, gold, and a small army capacity. Each round has a preparation phase followed by a battle phase. During preparation, the player can buy units, refresh the shop, level up, equip items, and arrange units on the board. During battle, units fight automatically. The player wins the game by surviving until the final round and loses if their health reaches zero.

## Core Rules

- The board has 4 rows and 8 columns.
- The player can place units only on the lower player-side rows.
- Enemy units are generated automatically on the enemy-side rows each round.
- Units attack automatically during battle based on their class behavior.
- Every third action triggers a unit skill.
- Winning a battle grants bonus gold.
- Losing a battle reduces player health.
- Win and loss streaks can provide extra gold.
- Units are restored after battle, so the player's formation persists between rounds.

## Units

- **Warrior**: A melee fighter that targets the closest enemy and has an area skill.
- **Archer**: A long-range attacker that prefers distant enemies and can hit enemies in the same row or column.
- **Mage**: A caster that targets low-health enemies and deals high skill damage.
- **Tank**: A durable front-line unit that targets strong enemies and can taunt nearby opponents.

## Skills

Every unit casts its skill on each third action during battle (skill animations play with a longer pause so they stand out).

| Unit | Skill | Effect |
|------|-------|--------|
| Warrior | Whirlwind | Deals 1.2x ATK to all adjacent enemies (area damage). |
| Archer | Pierce Shot | Hits every enemy in the same row or column for full ATK; otherwise a normal attack. |
| Mage | Fireball | Deals 1.8x ATK to a single target. |
| Tank | Taunt | Deals no damage; forces nearby enemies to attack the Tank for 2 turns. |

## Items

Items cost gold and can be equipped to player units:

- **Sword**: Increases attack.
- **Shield**: Increases defense and health.
- **Ring**: Increases speed and range.

Each unit can hold one item.

## Controls

- **Start**: Begin a new game from the main menu.
- **Buy**: Purchase a unit from the shop. Bought units are placed automatically on the player side if space is available.
- **Refresh**: Spend gold to refresh shop offerings.
- **Level Up**: Spend gold to increase player level and unit capacity.
- **Ready**: Start the battle phase for the current round.
- **Drag and Drop**: Rearrange player units on the player-side board cells.
- **Item Buttons**: Equip Sword, Shield, or Ring to a selected unit from the equipment panel.

## Game Flow

1. Start the game from the main menu.
2. Use the preparation phase to buy units, equip items, level up, and arrange formation.
3. Click **Ready** to start automatic combat.
4. After battle, receive rewards or take damage.
5. Continue through later rounds with stronger enemies.
6. Win by surviving the final round or lose when player health reaches zero.

## Project Structure

- `src/com/autobattler/app`: JavaFX application entry point.
- `src/com/autobattler/model`: Unit and item classes.
- `src/com/autobattler/logic`: Board and battle logic.
- `src/com/autobattler/view`: JavaFX screens and UI components.
- `src/com/autobattler/controller`: Drag-and-drop and animation control.
- `src/com/autobattler/shop`: Player economy and shop system.
- `src/com/autobattler/util`: Game state, constants, round flow, and enemy generation.

## OOP Design Notes

The project uses encapsulation by keeping most game data private and exposing behavior through methods. `ChessPiece` defines the common interface for all units, while `Warrior`, `Archer`, `Mage`, and `Tank` use inheritance and polymorphism to implement different attacks, skills, and targeting behavior. Separate classes manage the board, battle system, shop, player state, UI views, and round flow.

## How to Run

Open the project in an IDE with JavaFX configured, then run:

`com.autobattler.app.GameApp`

The project is built with JDK 26 and JavaFX SDK 26. Add the JavaFX `lib` folder to the module path with `--add-modules javafx.controls,javafx.fxml,javafx.media` when compiling/running.
