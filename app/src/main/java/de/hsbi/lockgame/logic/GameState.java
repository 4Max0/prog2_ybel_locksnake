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
        Level gsNewLevel = this.level();
        Snake gsNewSnake = this.snake();
        List<Pin> gsNewPins = new java.util.ArrayList<>(pins);
        Status gsNewStatus = this.status();
        Direction gsNewPendingDirecton = this.pendingDirection();

        // TODO: early exit: wenn das Spiel nicht läuft oder keine Blickrichtung gesetzt ist: kein
        // Änderung
        if (!gsNewStatus.isRunning() || gsNewPendingDirecton == Direction.NONE) {
            return this;
        }

        // TODO: prüfe die folgenden Bedingungen:
        // (a) Schlange würde das Spielfeld verlassen: Spiel verloren
        // (b) Schlange würde in ein Wandelement gehen: Blockiert (keine Bewegung, Blickrichtung
        // "none")
        // (c) Schlange beisst sich: Spiel verloren
        // (d) Schlange würde auf einen Pin gehen (Pin bereits gesetzt oder Schlange kommt nicht in
        // der Aktivierungsrichtung): Blockiert (keine Bewegung, Blickrichtung "none")
        Position nextPosition = gsNewSnake.nextHead(pendingDirection);
        // lambda
        Pin nextPositionPin =
                gsNewPins.stream()
                        .filter(pin -> nextPosition.equals(pin.position()))
                        .findFirst()
                        .orElse(null);

        boolean occupies = gsNewSnake.occupies(nextPosition);
        if (!gsNewLevel.isInside(nextPosition)) {
            gsNewStatus = Status.LOST_OUT_OF_BOUNDS;
        } else if (gsNewLevel.cellAt(nextPosition) == CellType.WALL) {
            gsNewPendingDirecton = Direction.NONE;
        } else if (occupies) {
            gsNewStatus = Status.LOST_SELF_COLLISION;
        } else if (nextPositionPin != null) {
            gsNewPendingDirecton = Direction.NONE;
        }
        // TODO: aktiviere einen noch nicht gesetzten Pin, wenn die Schlange in der richtigen
        // Richtung auf den Pin gehen würde (die Schlange darf dabei aber nicht auf den Pin gehen)
        boolean canActivatePin =
                nextPositionPin != null
                        && !nextPositionPin.state().isSet()
                        && this.pendingDirection == nextPositionPin.activationDirection();
        if (canActivatePin) {
            int idx = gsNewPins.indexOf(nextPositionPin);
            if (idx >= 0) {
                Pin pinNew = nextPositionPin.withState(Pin.State.HIGH);
                gsNewPins.set(idx, pinNew);
            }
        }

        // lambda
        // Check if all pins are set else keep running the game
        boolean allSet = gsNewPins.stream().allMatch(pin -> pin.state().isSet());

        if (gsNewStatus.isRunning() && allSet) {
            gsNewStatus = Status.WON;
        }

        // TODO: anderenfalls: bewege die Schlange um einen Schritt in Blickrichtung (falls gesetzt)
        if (gsNewPendingDirecton != Direction.NONE) {
            gsNewSnake = gsNewSnake.grow(gsNewPendingDirecton);
        }
        return new GameState(gsNewLevel, gsNewSnake, gsNewPins, gsNewStatus, gsNewPendingDirecton);
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
