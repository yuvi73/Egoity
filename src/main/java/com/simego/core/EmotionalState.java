package com.simego.core;

import java.util.Random;

public class EmotionalState {
    private double fear;
    private double joy;
    private double pride;
    private double stress;
    private Random random;
    private double learningRate;

    public EmotionalState() {
        this.fear = 0.0;
        this.joy = 0.0;
        this.pride = 0.0;
        this.stress = 0.0;
        this.random = new Random();
        this.learningRate = 0.1;
    }

    public void setFear(double fear) {
        this.fear = Math.max(0.0, Math.min(1.0, fear));
    }

    public void setJoy(double joy) {
        this.joy = Math.max(0.0, Math.min(1.0, joy));
    }

    public void setPride(double pride) {
        this.pride = Math.max(0.0, Math.min(1.0, pride));
    }

    public void setStress(double stress) {
        this.stress = Math.max(0.0, Math.min(1.0, stress));
    }

    public void update(Stimulus stimulus) {
        switch (stimulus.getType()) {
            case DANGER:
                fear = Math.min(1.0, fear + stimulus.getIntensity() * 0.2);
                stress = Math.min(1.0, stress + stimulus.getIntensity() * 0.1);
                break;
            case FOOD:
                joy = Math.min(1.0, joy + stimulus.getIntensity() * 0.2);
                stress = Math.max(0.0, stress - stimulus.getIntensity() * 0.1);
                pride = Math.min(1.0, pride + stimulus.getIntensity() * 0.1); // Add pride for finding food
                break;
            case OTHER_AGENT:
                stress = Math.min(1.0, stress + stimulus.getIntensity() * 0.1);
                if (pride > 0.5) {
                    joy = Math.min(1.0, joy + stimulus.getIntensity() * 0.1);
                }
                break;
            default:
                // Neutral stimuli have minimal effect
                break;
        }
    }

    public void updatePride(double amount) {
        // More dynamic pride updates
        double baseUpdate = learningRate * amount;
        double randomFactor = (random.nextDouble() - 0.5) * 0.1;
        pride = Math.min(1.0, Math.max(0.0, pride + baseUpdate + randomFactor));
        
        // Pride reduces stress and increases joy
        if (pride > 0.7) {
            stress = Math.max(0.0, stress - 0.1);
            joy = Math.min(1.0, joy + 0.05);
        }
    }

    public void updateFromAction(Action action) {
        // Update emotions based on actions
        switch (action.getType()) {
            case EAT:
                pride = Math.min(1.0, pride + 0.2);
                joy = Math.min(1.0, joy + 0.1);
                break;
            case INTERACT:
                if (random.nextDouble() < 0.7) { // 70% chance of positive interaction
                    pride = Math.min(1.0, pride + 0.1);
                    joy = Math.min(1.0, joy + 0.15);
                } else {
                    stress = Math.min(1.0, stress + 0.1);
                }
                break;
            case AVOID:
                pride = Math.min(1.0, pride + 0.1); // Pride from successful avoidance
                fear = Math.max(0.0, fear - 0.1);
                break;
        }
    }

    public void decay() {
        // Emotions naturally decay over time with some randomness
        double decayRate = 0.05;
        double randomFactor = (random.nextDouble() - 0.5) * 0.02;
        
        fear = Math.max(0.0, fear - decayRate + randomFactor);
        joy = Math.max(0.0, joy - decayRate + randomFactor);
        pride = Math.max(0.0, pride - (decayRate * 0.5) + randomFactor); // Pride decays more slowly
        stress = Math.max(0.0, stress - (decayRate * 0.7) + randomFactor);
    }

    public double getFear() {
        return fear;
    }

    public double getJoy() {
        return joy;
    }

    public double getPride() {
        return pride;
    }

    public double getStress() {
        return stress;
    }
} 