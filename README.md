# CubeSolverGUI

A Java desktop application for visualizing, scrambling, and solving a 3x3 Rubik's Cube.  
Features interactive GUI, step-by-step CFOP solving, optimal Kociemba solver, and simulation tools.

## Features

- **3D and 2D Cube Visualization:** See the cube in both 3D and unfolded 2D views.
- **Scramble & Custom Scramble:** Generate random scrambles or enter your own.
- **Step-by-Step Solution:** Solve using CFOP steps (Cross, F2L, OLL, PLL).
- **Optimal Solver:** Uses Kociemba’s two-phase algorithm for shortest solutions.
- **Move Statistics:** Displays move counts for each solving stage.
- **Simulation:** Run multiple solves to analyze best/worst cases.
- **Export:** Send scramble and solution to [alg.cubing.net](https://alg.cubing.net) for sharing.

## Requirements

- Java 8 or higher
- [FlatLaf](https://github.com/JFormDesigner/FlatLaf) (for dark theme)
- [Kociemba’s Two-Phase Solver](https://github.com/hkociemba/RubiksCube-Twophase) Java library

## How to Run

1. Clone the repository:
    ```
    git clone https://github.com/AndugalaPujitha/CubeSolverGUI.git
    cd CubeSolverGUI
    ```
2. Make sure dependencies (`FlatLaf`, `Kociemba`) are in your classpath.
3. Compile and run:
    ```
    javac CubeSolverGUI.java
    java CubeSolverGUI
    ```

## Usage

- **Scramble:** Click "Scramble" or enter a custom scramble.
- **Solve:** Click "Solve Cube" for optimal solution, or use step buttons for CFOP stages.
- **Rotate:** Use Y/Y' buttons to rotate the cube view.
- **Simulate:** Enter number of solves and click "Start Simulations" to analyze move counts.
- **Export:** Click "Export to alg.cubing.net" to view solution online.

## Notes

- Only supports 3x3 cubes.
- For other sizes (2x2, 4x4), significant code changes are needed.
- GUI uses Swing; tested on Windows.

## License

MIT License

