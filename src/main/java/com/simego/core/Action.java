package com.simego.core;

public class Action {
    private final ActionType type;
    private final Agent agent;
    private final Direction direction;

    public Action(ActionType type, Agent agent, Direction direction) {
        this.type = type;
        this.agent = agent;
        this.direction = direction;
    }

    public ActionType getType() {
        return type;
    }

    public Agent getAgent() {
        return agent;
    }

    public Direction getDirection() {
        return direction;
    }

    public enum ActionType {
        MOVE_UP,
        MOVE_DOWN,
        MOVE_LEFT,
        MOVE_RIGHT,
        EAT,
        AVOID,
        INTERACT
    }
} 