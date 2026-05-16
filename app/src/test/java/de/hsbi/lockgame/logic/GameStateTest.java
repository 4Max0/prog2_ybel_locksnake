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
        state.tick();
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
        state.tick();
        state = updateState(state, Direction.LEFT);
        state.tick();
        state = updateState(state, Direction.DOWN);
        state.tick();
        state = updateState(state, Direction.RIGHT);
        state.tick();
        // then
        assertEquals(GameState.Status.LOST_SELF_COLLISION, state.status());
    }
}
