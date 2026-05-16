/* (C)2026 */
package de.hsbi.lockgame.logic;

import de.hsbi.lockgame.model.*;
import java.util.List;

public final class GameState {
    private Level level;
    private Snake snake;
    private List<Pin> pins;
    private Status status;
    private Direction pendingDirection;

    public GameState(
            Level level, Snake snake, List<Pin> pins, Status status, Direction pendingDirection) {
        // TODO: lege einen neuen GameState mit den übergebenen Informationen an
        this.level = level;
        this.snake = snake;
        this.pins = pins;
        this.status = status;
        this.pendingDirection = pendingDirection;
        // throw new UnsupportedOperationException("method not implemented yet");
    }

    public Level level() {
        // TODO: Getter
        return this.level;
        // throw new UnsupportedOperationException("method not implemented yet");
    }

    public Snake snake() {
        // TODO: Getter
        return this.snake;
        // throw new UnsupportedOperationException("method not implemented yet");
    }

    public List<Pin> pins() {
        // TODO: Getter
        return this.pins;
        // throw new UnsupportedOperationException("method not implemented yet");
    }

    public Status status() {
        // TODO: Getter
        return this.status;
        // throw new UnsupportedOperationException("method not implemented yet");
    }

    public Direction pendingDirection() {
        // TODO: Getter
        return this.pendingDirection;
        // throw new UnsupportedOperationException("method not implemented yet");
    }

    public GameState tick() {
        // TODO: diese Methode lässt das Spiel einen Schritt laufen (berechnet den Spielzustand im
        // nächsten Schritt)

        // TODO: early exit: wenn das Spiel nicht läuft oder keine Blickrichtung gesetzt ist: kein
        // Änderung
        if (!this.status.isRunning() || this.pendingDirection == Direction.NONE) {
            return this;
        }

        // TODO: prüfe die folgenden Bedingungen:
        // (a) Schlange würde das Spielfeld verlassen: Spiel verloren
        // (b) Schlange würde in ein Wandelement gehen: Blockiert (keine Bewegung, Blickrichtung
        // "none")
        // (c) Schlange beisst sich: Spiel verloren
        // (d) Schlange würde auf einen Pin gehen (Pin bereits gesetzt oder Schlange kommt nicht in
        // der Aktivierungsrichtung): Blockiert (keine Bewegung, Blickrichtung "none")
        Position nextPosition = this.snake.nextHead(pendingDirection);
        Pin nextPositionPin = null;
        for (Pin pin : this.pins) {
            if (pin.position() == nextPosition) {
                nextPositionPin = pin;
                break;
            }
        }
        boolean occupies = this.snake.occupies(nextPosition);
        if (!level.isInside(nextPosition)) {
            this.status = Status.LOST_OUT_OF_BOUNDS;
        } else if (level.cellAt(nextPosition) == CellType.WALL) {
            this.pendingDirection = Direction.NONE;
        } else if (occupies) {
            this.status = Status.LOST_SELF_COLLISION;
        } else if (nextPositionPin != null
                && (nextPositionPin.state().isSet()
                        || this.pendingDirection != nextPositionPin.activationDirection())) {
            this.pendingDirection = Direction.NONE;
        }
        // TODO: aktiviere einen noch nicht gesetzten Pin, wenn die Schlange in der richtigen
        // Richtung auf den Pin gehen würde (die Schlange darf dabei aber nicht auf den Pin gehen)
        boolean canActivatePin =
                nextPositionPin != null
                        && !nextPositionPin.state().isSet()
                        && this.pendingDirection == nextPositionPin.activationDirection();
        if (canActivatePin) {
            this.pins.remove(nextPositionPin);
            nextPositionPin = nextPositionPin.withState(Pin.State.HIGH);
            this.pins.add(nextPositionPin);
        }

        if (this.status.isRunning()) {
            // Check if all pins are set else keep running the game
            for (Pin pin : this.pins) {
                this.status = Status.WON;
                if (!pin.state().isSet()) {
                    this.status = Status.RUNNING;
                    break;
                }
            }
        }

        // TODO: anderenfalls: bewege die Schlange um einen Schritt in Blickrichtung (falls gesetzt)
        if (this.pendingDirection != Direction.NONE) {
            this.snake = this.snake.grow(pendingDirection);
        }
        return this;
        // throw new UnsupportedOperationException("method not implemented yet");
    }

    public enum Status {
        RUNNING,
        WON,
        LOST_SELF_COLLISION,
        LOST_OUT_OF_BOUNDS;

        public boolean isRunning() {
            return this == RUNNING;
        }
    }
}
