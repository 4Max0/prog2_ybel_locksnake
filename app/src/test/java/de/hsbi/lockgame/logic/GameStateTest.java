/* (C)2026 */
package de.hsbi.lockgame.logic;

import static org.junit.jupiter.api.Assertions.*;

import de.hsbi.lockgame.io.LevelLoader;
import de.hsbi.lockgame.model.*;
import de.hsbi.lockgame.settings.LevelConstants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class GameStateTest {
    private Level defaultLevel() {
        try {
            return LevelLoader.loadLevelFromResource(LevelConstants.defaultLevel());
        } catch (IOException e) {
            // Fallback: leeres Level oder Fehlermeldung
            System.err.println("Konnte Level nicht laden: " + e.getMessage());
            return null;
        }
    }

    private GameState updateState(GameState state, Direction dir) {
        return new GameState(state.level(), state.snake(), state.pins(), state.status(), dir);
    }

    @Test
    void testInitWorks() {
        // Given
        // We have the variables to create a GameState Object
        Level level = this.defaultLevel();
        assertNotNull(level);
        List<Position> positionSnake = new ArrayList<>();
        positionSnake.add(level.snakeStart());
        // when
        // we create a GameState object and move it a couple of times
        GameState state =
                new GameState(
                        level,
                        new Snake(positionSnake),
                        level.pins(),
                        GameState.Status.RUNNING,
                        Direction.UP);
        state = state.tick();
        state = this.updateState(state, Direction.LEFT);
        state = state.tick();
        state = this.updateState(state, Direction.UP);
        state = state.tick();

        // Then
        // We assume that the head of the snake is at it's correct plays
        assertEquals(new Position(17, 2), state.snake().head());
    }

    @Test
    public void testWalkingWorks() {
        // given
        // We have the variables to create a GameState Object
        Level level = this.defaultLevel();
        assertNotNull(level);
        List<Position> positionSnake = new ArrayList<>();
        positionSnake.add(level.snakeStart());
        // when
        // we move the snake a couple of times
        GameState state =
            new GameState(
                level,
                new Snake(positionSnake),
                level.pins(),
                GameState.Status.RUNNING,
                Direction.NONE);
        state = state.tick();
        state = this.updateState(state, Direction.UP);
        state = state.tick();
        state = this.updateState(state, Direction.LEFT);
        state = state.tick();
        state = this.updateState(state, Direction.UP);
        state = state.tick();
        // then
        // the snake position is at the right place
        assertEquals(new Position(17, 2), state.snake().head());
    }

    @Test
    public void testInteractionWall() {
        // given
        // We have the variables to create a GameState Object
        Level level = this.defaultLevel();
        assertNotNull(level);
        List<Position> positionSnake = new ArrayList<>();
        positionSnake.add(level.snakeStart());
        // when
        GameState state =
                new GameState(
                        level,
                        new Snake(positionSnake),
                        level.pins(),
                        GameState.Status.RUNNING,
                        Direction.RIGHT);
        state = state.tick();
        // then
        assertEquals(Direction.NONE, state.pendingDirection());
    }

    @Test
    public void testLostSnakeEatingSelf() {
        // given
        // We have the variables to create a GameState Object
        Level level = this.defaultLevel();
        assertNotNull(level);
        List<Position> positionSnake = new ArrayList<>();
        positionSnake.add(level.snakeStart());
        Snake snake = new Snake(positionSnake);
        // when
        GameState state =
                new GameState(level, snake, level.pins(), GameState.Status.RUNNING, Direction.NONE);
        // let the snake eat itself by going in state circle in the next tick
        state = this.updateState(state, Direction.UP);
        state = state.tick();
        state = this.updateState(state, Direction.LEFT);
        state = state.tick();
        state = this.updateState(state, Direction.DOWN);
        state = state.tick();
        state = this.updateState(state, Direction.RIGHT);
        state = state.tick();

        // then
        // the game should be over
        assertEquals(GameState.Status.LOST_SELF_COLLISION, state.status());
    }

    @Test
    public void testLOSTOOB() {
        // given
        // We have the variables to create a GameState Object
        Level level = this.defaultLevel();
        assertNotNull(level);
        List<Position> positionSnake = new ArrayList<>();
        positionSnake.add(new Position(-1, -1));
        Snake snake = new Snake(positionSnake);
        // when
        // the position of the given snake is out of bounds
        GameState state =
            new GameState(level, snake, level.pins(), GameState.Status.RUNNING, Direction.NONE);
        state = this.updateState(state, Direction.UP);
        state = state.tick();

        // then
        // the game should be lost with an OOB
        assertEquals(GameState.Status.LOST_OUT_OF_BOUNDS, state.status());
    }

    @Test
    public void testInteractionPinNotEntered() {
        // given
        // We have the variables to create a GameState Object
        Level level = this.defaultLevel();
        assertNotNull(level);
        List<Position> positionSnake = new ArrayList<>();
        positionSnake.add(level.snakeStart());
        Snake snake = new Snake(positionSnake);
        // when
        GameState state =
                new GameState(level, snake, level.pins(), GameState.Status.RUNNING, Direction.NONE);
        state = this.updateState(state, Direction.LEFT);
        state = state.tick();
        state = this.updateState(state, Direction.LEFT);
        state = state.tick();
        state = this.updateState(state, Direction.LEFT);
        state = state.tick();

        // then
        // The head of the snake should stop and not move onto (15, 4) where to pin lies
        Position positonNotAllowed = new Position(16, 4);
        Position positionSnakeHead = state.snake().head();
        assertEquals(positonNotAllowed, positionSnakeHead);
    }

    @Test
    public void testInteractionPinPicked() {
        // given
        // We have the variables to create a GameState Object
        Level level = this.defaultLevel();
        assertNotNull(level);
        List<Position> positionSnake = new ArrayList<>();
        positionSnake.add(level.snakeStart());
        Snake snake = new Snake(positionSnake);
        // when
        GameState state =
                new GameState(level, snake, level.pins(), GameState.Status.RUNNING, Direction.NONE);
        // we get the pins and then navigate the snake to a pin and compare the pins before and
        // after
        List<Pin> pinsBefore = state.pins();
        state = this.updateState(state, Direction.LEFT);
        state = state.tick();
        state = this.updateState(state, Direction.LEFT);
        state = state.tick();
        state = this.updateState(state, Direction.LEFT);
        state = state.tick();
        List<Pin> pinsAfter = state.pins();
        // then
        // the list should not be equal because of a different pin has been set
        assertNotEquals(pinsAfter, pinsBefore);
    }

    @Test
    public void testInteractionPinNotPickedWrongDirection() {
        // given
        // We have the variables to create a GameState Object
        Level level = this.defaultLevel();
        assertNotNull(level);
        List<Position> positionSnake = new ArrayList<>();
        positionSnake.add(level.snakeStart());
        Snake snake = new Snake(positionSnake);
        // when
        GameState state =
                new GameState(level, snake, level.pins(), GameState.Status.RUNNING, Direction.NONE);
        // we get the pins and then navigate the snake to a pin and compare the pins before and
        // after but the result should be same, because we are not coming from the activation
        // direction
        List<Pin> pinsBefore = state.pins();
        for (int i = 0; i < 3; i++) {
            state = this.updateState(state, Direction.UP);
            state = state.tick();
        }
        for (int i = 0; i < 3; i++) {
            state = this.updateState(state, Direction.LEFT);
            state = state.tick();
        }
        state = this.updateState(state, Direction.DOWN);
        state = state.tick();
        List<Pin> pinsAfter = state.pins();
        // then
        // the list should be equal because the pins state did not change
        assertEquals(pinsAfter, pinsBefore);
    }

    @Test
    public void testGameWon() {
        // given
        // We have the variables to create a GameState Object
        Level level = this.defaultLevel();
        assertNotNull(level);
        List<Position> positionSnake = new ArrayList<>();
        positionSnake.add(level.snakeStart());
        Snake snake = new Snake(positionSnake);
        // when
        // we simulate a game finish through getting the achievement of all pins being set
        List<Pin> pins = level.pins();
        List<Pin> pinsNew = new ArrayList<>();
        for (Pin pin : pins) {
            pinsNew.add(pin.withState(Pin.State.HIGH));
        }
        // NOTE: we need to set direction to anything else than None because else the tick ends
        // without doing anything
        GameState state =
                new GameState(level, snake, pinsNew, GameState.Status.RUNNING, Direction.LEFT);
        state = state.tick();
        // then
        // The result of having all pins set should be a win
        assertEquals(GameState.Status.WON, state.status());
    }

    @Test
    public void testtestGameGameNotWonNotAllPinSet() {
        // given
        // We have the variables to create a GameState Object
        Level level = this.defaultLevel();
        assertNotNull(level);
        List<Position> positionSnake = new ArrayList<>();
        positionSnake.add(level.snakeStart());
        Snake snake = new Snake(positionSnake);
        // when
        // we simulate two pins being set
        List<Pin> pins = level.pins();
        List<Pin> pinsNew = new ArrayList<>();
        for (Pin pin : pins) {
            if (pin != pins.getLast()) {
                pinsNew.add(pin.withState(Pin.State.HIGH));
            } else {
                pinsNew.add(pin);
            }
        }
        // NOTE: we need to set direction to anything else than None because else the tick ends
        // without doing anything
        GameState state =
                new GameState(level, snake, pinsNew, GameState.Status.RUNNING, Direction.LEFT);
        state = state.tick();
        // then
        // The result of having all pins set should be a win
        assertEquals(GameState.Status.RUNNING, state.status());
    }
}
