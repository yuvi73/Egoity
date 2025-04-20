package com.simego.core;

public class Memory {
    private final Object content;
    private final long timestamp;
    private final MemoryType type;
    private final boolean isSelfGenerated;
    private final double valence;

    public Memory(Object content, long timestamp) {
        this(content, timestamp, false, calculateDefaultValence(content));
    }

    public Memory(Object content, long timestamp, boolean isSelfGenerated, double valence) {
        this.content = content;
        this.timestamp = timestamp;
        this.type = determineType(content);
        this.isSelfGenerated = isSelfGenerated;
        this.valence = Math.max(-1.0, Math.min(1.0, valence));
    }

    private static double calculateDefaultValence(Object content) {
        if (content instanceof Stimulus) {
            Stimulus stimulus = (Stimulus) content;
            switch (stimulus.getType()) {
                case FOOD:
                    return 0.8;
                case DANGER:
                    return -0.7;
                case OTHER_AGENT:
                    return stimulus.getSourceAction() != null ? 0.6 : 0.3;
                case OBSTACLE:
                    return -0.3;
                default:
                    return 0.0;
            }
        } else if (content instanceof Action) {
            Action action = (Action) content;
            switch (action.getType()) {
                case EAT:
                    return 0.7;
                case INTERACT:
                    return 0.6;
                case AVOID:
                    return 0.4;
                default:
                    return 0.1;
            }
        } else if (content instanceof Goal) {
            Goal goal = (Goal) content;
            switch (goal.getType()) {
                case FIND_FOOD:
                    return 0.7;
                case AVOID_DANGER:
                    return 0.6;
                case SOCIALIZE:
                    return 0.5;
                case EXPLORE:
                    return 0.3;
                default:
                    return 0.0;
            }
        }
        return 0.0;
    }

    private MemoryType determineType(Object content) {
        if (content instanceof Stimulus) {
            return MemoryType.STIMULUS;
        } else if (content instanceof Action) {
            return MemoryType.ACTION;
        } else if (content instanceof Goal) {
            return MemoryType.GOAL;
        }
        return MemoryType.OTHER;
    }

    public Object getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public MemoryType getType() {
        return type;
    }

    public double getValence() {
        return valence;
    }

    public boolean isSelfGenerated() {
        return isSelfGenerated;
    }

    public enum MemoryType {
        STIMULUS,
        ACTION,
        GOAL,
        OTHER,
        EXPERIENCE,
        SOCIAL,
        REFLECTION
    }
} 