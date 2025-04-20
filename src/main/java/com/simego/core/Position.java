package com.simego.core;

public class Position {
    private final int x;
    private final int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Position move(Direction direction) {
        switch (direction) {
            case UP:
                return new Position(x, y - 1);
            case DOWN:
                return new Position(x, y + 1);
            case LEFT:
                return new Position(x - 1, y);
            case RIGHT:
                return new Position(x + 1, y);
            default:
                return this;
        }
    }

    public double distanceTo(Position other) {
        int dx = this.x - other.x;
        int dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }
} 