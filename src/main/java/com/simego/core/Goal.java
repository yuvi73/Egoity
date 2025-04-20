package com.simego.core;

public class Goal {
    private final GoalType type;
    private final Position targetPosition;
    private double priority;

    public Goal(GoalType type, Position targetPosition, double priority) {
        this.type = type;
        this.targetPosition = targetPosition;
        this.priority = Math.max(0.0, Math.min(1.0, priority));
    }

    public GoalType getType() {
        return type;
    }

    public Position getTargetPosition() {
        return targetPosition;
    }

    public double getPriority() {
        return priority;
    }

    public void setPriority(double priority) {
        this.priority = Math.max(0.0, Math.min(1.0, priority));
    }

    public enum GoalType {
        FIND_FOOD,
        AVOID_DANGER,
        EXPLORE,
        REST,
        SOCIALIZE
    }
} 