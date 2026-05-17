/* (C)2026 */
package de.hsbi.lockgame.logic;

import static org.junit.jupiter.api.Assertions.*;

import de.hsbi.lockgame.io.LevelLoader;
import de.hsbi.lockgame.model.*;
import de.hsbi.lockgame.settings.LevelConstants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

    private GameState move(GameState s, Direction... dirs) {
        return Arrays.stream(dirs)
                .reduce(
                        s,
                        (st, d) ->
                                new GameState(st.level(), st.snake(), st.pins(), st.status(), d)
                                        .tick(),
                        (_, b) -> b);
    }

    @Test
    void testInitWorks() {
        // Given
        // We have the variables to create a GameState Object
        Level level = this.defaultLevel();
        assertNotNull(level);
        List<Position> positionSnake = new ArrayList<>();
        positionSnake.add(level.snakeStart());
        Snake snake = new Snake(positionSnake);
        List<Pin> pins = level.pins();
        GameState.Status status = GameState.Status.RUNNING;
        Direction direction = Direction.NONE;
        // when
        // we create a GameState object
        GameState state = new GameState(level, snake, pins, status, direction);

        // Then
        // We assume that the GameState variables did not get changed faultly
        assertNotNull(state);
        assertEquals(level, state.level());
        assertEquals(snake, state.snake());
        assertEquals(pins, state.pins());
        assertEquals(status, state.status());
        assertEquals(direction, state.pendingDirection());
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
        state = this.move(state, Direction.UP, Direction.LEFT, Direction.UP);
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
        state = this.move(state, Direction.UP, Direction.LEFT, Direction.DOWN, Direction.RIGHT);

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
        state = this.move(state, Direction.UP);

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
        state = this.move(state, Direction.LEFT, Direction.LEFT, Direction.LEFT);

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
        state = this.move(state, Direction.LEFT, Direction.LEFT, Direction.LEFT);
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
        state =
                this.move(
                        state,
                        Direction.UP,
                        Direction.UP,
                        Direction.UP,
                        Direction.LEFT,
                        Direction.LEFT,
                        Direction.LEFT,
                        Direction.DOWN);
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
        // lambda
        List<Pin> pins = level.pins();
        List<Pin> pinsNew = pins.stream().map(pin -> pin.withState(Pin.State.HIGH)).toList();
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
