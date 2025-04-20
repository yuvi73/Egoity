package com.simego.core;

public class Stimulus {
    private final Position position;
    private final StimulusType type;
    private final double intensity;
    private final Action sourceAction;

    public Stimulus(Position position, StimulusType type, double intensity) {
        this(position, type, intensity, null);
    }

    public Stimulus(Position position, StimulusType type, double intensity, Action sourceAction) {
        this.position = position;
        this.type = type;
        this.sourceAction = sourceAction;
        this.intensity = Math.max(0.0, Math.min(1.0, intensity));
    }

    public Position getPosition() {
        return position;
    }

    public StimulusType getType() {
        return type;
    }

    public double getIntensity() {
        return intensity;
    }

    public Action getSourceAction() {
        return sourceAction;
    }

    public enum StimulusType {
        FOOD,
        DANGER,
        OTHER_AGENT,
        OBSTACLE,
        NEUTRAL
    }
} 