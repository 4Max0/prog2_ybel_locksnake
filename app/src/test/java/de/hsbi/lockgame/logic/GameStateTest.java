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
    public void testInteractionWall() {
        // given
        Level level = this.defaultLevel();
        assertNotNull(level);
        List<Position> positionSnake = new ArrayList<Position>();
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
    public void testInteractionEatingSnake() {
        // given
        Level level = this.defaultLevel();
        assertNotNull(level);
        List<Position> positionSnake = new ArrayList<Position>();
        positionSnake.add(level.snakeStart());
        Snake snake = new Snake(positionSnake);
        // when
        GameState state =
                new GameState(level, snake, level.pins(), GameState.Status.RUNNING, Direction.NONE);
        // let the snake eat itself by going in state circle in the next tick
        state = updateState(state, Direction.UP);
        state = state.tick();
        state = updateState(state, Direction.LEFT);
        state = state.tick();
        state = updateState(state, Direction.DOWN);
        state = state.tick();
        state = updateState(state, Direction.RIGHT);
        state = state.tick();
        ;
        // then
        // the game should be over
        assertEquals(GameState.Status.LOST_SELF_COLLISION, state.status());
    }

    @Test
    public void testInteractionLockPicked() {
        // given
        Level level = this.defaultLevel();
        assertNotNull(level);
        List<Position> positionSnake = new ArrayList<Position>();
        positionSnake.add(level.snakeStart());
        Snake snake = new Snake(positionSnake);
        // when
        GameState state =
                new GameState(level, snake, level.pins(), GameState.Status.RUNNING, Direction.NONE);
        // we get the pins and then navigate the snake to a pin and compare the pins before and
        // after
        List<Pin> pinsBefore = state.pins();
        state = updateState(state, Direction.LEFT);
        state = state.tick();
        state = updateState(state, Direction.LEFT);
        state = state.tick();
        state = updateState(state, Direction.LEFT);
        state = state.tick();
        List<Pin> pinsAfter = state.pins();
        // then
        // the list should not be equal because of a different pin
        assertNotEquals(pinsAfter, pinsBefore);
    }

    @Test
    public void testInteractionPinNotEntered() {
        // given
        Level level = this.defaultLevel();
        assertNotNull(level);
        List<Position> positionSnake = new ArrayList<Position>();
        positionSnake.add(level.snakeStart());
        Snake snake = new Snake(positionSnake);
        // when
        GameState state =
                new GameState(level, snake, level.pins(), GameState.Status.RUNNING, Direction.NONE);
        state = updateState(state, Direction.LEFT);
        state = state.tick();
        state = updateState(state, Direction.LEFT);
        state = state.tick();
        state = updateState(state, Direction.LEFT);
        state = state.tick();

        // then
        // The head of the snake should stop and not move onto (15, 4) where to pin lies
        Position positonNotAllowed = new Position(16, 4);
        Position positionSnakeHead = state.snake().head();
        assertEquals(positonNotAllowed, positionSnakeHead);
    }

    @Test
    public void testGameWon() {
        // given
        Level level = this.defaultLevel();
        assertNotNull(level);
        List<Position> positionSnake = new ArrayList<Position>();
        positionSnake.add(level.snakeStart());
        Snake snake = new Snake(positionSnake);
        // when
        // we simulate a game finish through getting the achievement of all pins being set
        List<Pin> pins = level.pins();
        List<Pin> pinsNew = new ArrayList<Pin>();
        for (Pin pin : pins) {
            pinsNew.add(pin.withState(Pin.State.HIGH));
        }
        // NOTE: we need to set direction to anything else than None because else the game doesn't recognize the win
        GameState state =
                new GameState(level, snake, pinsNew, GameState.Status.RUNNING, Direction.LEFT);
        state = state.tick();
        // then
        // The result of having all pins set should be a win
        assertEquals(GameState.Status.WON, state.status());
    }
}
