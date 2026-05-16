/* (C)2026 */
package de.hsbi.lockgame.model;

public final class Position {
    private final int x;
    private final int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    /**
     * Replacing the standard .equals() as it was not working correctly.
     *
     * @param o the second Position object you compare
     * @return Bool if the Position is the same between the objects
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position position)) return false;

        return x == position.x && y == position.y;
    }
}
