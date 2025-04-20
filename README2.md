# Self-Recognition Simulation

A Java-based simulation exploring the development of self-recognition in artificial agents through social interactions and mirroring behavior.

## Overview

This project simulates how artificial agents can develop self-recognition through:
- Social interactions
- Mirroring behavior
- Emotional state development
- Memory accumulation
- Self-model formation

## Key Components

- **Agents**: Autonomous entities with emotional states and self-models
- **Environment**: Grid-based world where agents interact
- **SelfModel**: Tracks self-consistency, agency, and self-recognition
- **EmotionalState**: Manages pride, joy, stress, and fear
- **Memory System**: Stores and processes experiences
- **Mirroring**: Agents learn by observing and imitating others

## Running the Simulation

```bash
cd src/main/java
javac com/simego/experiments/SelfRecognitionExperiment.java
java com.simego.experiments.SelfRecognitionExperiment
```

The simulation runs 10 experiments with:
- 6 agents in an 8x8 grid
- 2000 steps per simulation
- Metrics collected every 5 steps
- Results saved to `experiment_results.csv`

## Results

The simulation generates data on:
- Self-recognition development
- Mirroring behavior evolution
- Emotional state changes
- Social interaction patterns
- Memory accumulation
