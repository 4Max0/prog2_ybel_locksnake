/* (C)2026 */
package de.hsbi.lockgame.logic;

import de.hsbi.lockgame.model.Direction;
import de.hsbi.lockgame.model.Level;
import de.hsbi.lockgame.model.Position;
import de.hsbi.lockgame.model.Snake;
import de.hsbi.lockgame.ui.GamePanel;
import java.util.ArrayList;
import java.util.List;

// TODO: Die GameEngine verwaltet den GameState.

// TODO: Die GameEngine wird durch den Timer im main() getriggert ("tick") und lässt den GameState
// daraufhin einen Schritt ausführen. Dann müssen alle für den GameState registrierten Observer
// benachrichtigt werden, damit das Spielfeld neu gezeichnet werden kann o.ä.

// TODO: Die GameEngine beobachtet die Tastatureingaben (gesetzt in GamePanel.setupKeyBindings()),
// die in Direction übersetzt und an GameEngine.update() übergeben werden. Wenn es eine neue Eingabe
// gibt, wird die "update"-Methode von GameEngine aufgerufen, und die GameEngine muss die
// Blickrichtung der Schlange aktualisieren und diese GameState-Änderung den für den GameState
// registrierten Observer mitteilen.

// TODO: Die GameEngine ist ein Observer für Direction: GameEngine.update(Direction)
// TODO: Die GameEngine ist ein Observable für GameState: GamePanel.update(GameState)
public final class GameEngine {
    Level level;
    GamePanel panel;
    GameState state;

    public GameEngine(Level level) {
        // TODO: lege eine neue GameEngine mit den übergebenen Informationen an
        this.level = level;

        List<Position> positionSnake = new ArrayList<Position>();
        positionSnake.add(level.snakeStart());

        this.state =
                new GameState(
                        level,
                        new Snake(positionSnake),
                        level.pins(),
                        GameState.Status.RUNNING,
                        Direction.NONE);
        // throw new UnsupportedOperationException("method not implemented yet");
    }

    public GameState state() {
        // TODO: gebe den aktuellen Spielzustand zurück
        return this.state;
        // throw new UnsupportedOperationException("method not implemented yet");
    }

    public void setGamePanel(GamePanel panel) {
        // TODO: Setter
        this.panel = panel;
        // throw new UnsupportedOperationException("method not implemented yet");
    }

    public void update(Direction d) {
        // TODO: aktualisiere den Blickwinkel der Schlange (GameState)
        // TODO: benachrichtige alle Observer und gibt den neuen Spielzustand mit (Neuzeichnen der
        // Spielfläche)
        // Observer benachrichtigen
        this.state =
                new GameState(
                        this.state.level(),
                        this.state.snake(),
                        this.state.pins(),
                        this.state.status(),
                        d);
        if (this.panel != null) {
            this.panel.update(this.state);
        }
        // throw new UnsupportedOperationException("method not implemented yet");
    }

    public void tick() {
        // TODO: lass das Spiel (den GameState) einen Schritt ("tick") machen
        // TODO: benachrichtige alle Observer und gibt den neuen Spielzustand mit (Neuzeichnen der
        // Spielfläche)
        // GameState einen Schritt weiter
        this.state = this.state.tick();

        // Observer benachrichtigen
        if (this.panel != null) {
            this.panel.update(this.state);
        }
    }
}
