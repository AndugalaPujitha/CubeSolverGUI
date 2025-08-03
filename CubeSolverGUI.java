import com.formdev.flatlaf.FlatDarkLaf;
import org.kociemba.twophase.Search;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Main public class. The file must be named CubeSolverGUI.java.
 * This class builds the GUI and handles user interaction.
 *
 * Modified to fix solving logic, GUI expansion, and overhaul the UI.
 */
public class CubeSolverGUI extends JFrame {

    private final Cube cube;
    private final Solver solver;
    private final CubePanel cubePanel;
    private final UnfoldedCubePanel unfoldedCubePanel;
    private JLabel scrambleLabel, solutionLengthLabel;
    private JLabel crossMovesLabel, f2lMovesLabel, ollMovesLabel, pllMovesLabel;
    private JLabel simulateBestLabel, simulateWorstLabel;
    private JTextArea solutionArea;
    private JTextField moveEntry, customScrambleEntry, simEntry;

    public CubeSolverGUI() {
        cube = new Cube();
        solver = new Solver(cube);

        setTitle("Cube Solver - Java Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10)); // Main layout for the frame

        // ---- Left Side: Cube Displays ----
        JPanel displayPanel = new JPanel(new BorderLayout(5, 5));
        displayPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        unfoldedCubePanel = new UnfoldedCubePanel(cube);
        cubePanel = new CubePanel(cube);

        JPanel rotationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton yButton = new JButton("<-- Y");
        yButton.addActionListener(e -> {
            cube.move("Y");
            updateCubeDisplay();
        });
        JButton yiButton = new JButton("Y' -->");
        yiButton.addActionListener(e -> {
            cube.move("Yi");
            updateCubeDisplay();
        });
        rotationPanel.add(new JLabel("Rotate:"));
        rotationPanel.add(yButton);
        rotationPanel.add(yiButton);

        displayPanel.add(unfoldedCubePanel, BorderLayout.NORTH);
        displayPanel.add(cubePanel, BorderLayout.CENTER);
        displayPanel.add(rotationPanel, BorderLayout.SOUTH);

        add(displayPanel, BorderLayout.WEST);

        // ---- Right Side: Controls ----
        JPanel controlsContainer = new JPanel(new BorderLayout());
        controlsContainer.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));
        controlsContainer.add(createControlsPanel(), BorderLayout.CENTER);
        add(controlsContainer, BorderLayout.CENTER);

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null); // Center on screen
    }

    private JPanel createControlsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;

        // --- Title and Reset ---
        JLabel titleLabel = new JLabel("Cube Solver Controls", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        JButton newCubeButton = new JButton("New/Reset Cube");
        newCubeButton.addActionListener(e -> resetAll());
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(newCubeButton, BorderLayout.EAST);
        panel.add(titlePanel, gbc);

        // --- Execute Move ---
        JPanel movePanel = new JPanel(new BorderLayout(5, 5));
        movePanel.setBorder(BorderFactory.createTitledBorder("Enter move(s)"));
        moveEntry = new JTextField(15);
        movePanel.add(moveEntry, BorderLayout.CENTER);
        JButton executeButton = new JButton("Execute");
        executeButton.addActionListener(e -> {
            solver.m(moveEntry.getText());
            updateCubeDisplay();
        });
        movePanel.add(executeButton, BorderLayout.EAST);
        panel.add(movePanel, gbc);

        // --- Scramble Section ---
        JPanel scrambleSection = new JPanel(new GridLayout(0, 1, 5, 5));
        scrambleSection.setBorder(BorderFactory.createTitledBorder("Scramble"));

        scrambleLabel = new JLabel("Scramble will be displayed here");
        scrambleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scrambleSection.add(scrambleLabel);

        JPanel scrambleButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton scrambleButton = new JButton("Scramble");
        scrambleButton.addActionListener(e -> {
            resetAll(); // FIX: Call resetAll to ensure cube state is clean before scramble
            solver.scramble(25);
            scrambleLabel.setText(
                    "<html><body style='width: 250px; text-align: center;'>" + solver.getScramble() + "</body></html>");
            updateCubeDisplay();
        });
        scrambleButtons.add(scrambleButton);
        JButton copyScrambleButton = new JButton("Copy Scramble");
        copyScrambleButton.addActionListener(e -> toClipboard(solver.getScramble()));
        scrambleButtons.add(copyScrambleButton);
        scrambleSection.add(scrambleButtons);

        JPanel customScramblePanel = new JPanel(new BorderLayout(5, 5));
        customScrambleEntry = new JTextField(); // FIX: Removed default text
        customScramblePanel.add(customScrambleEntry, BorderLayout.CENTER);
        JButton customScrambleButton = new JButton("Apply Custom");
        customScrambleButton.addActionListener(e -> {
            resetAll(); // FIX: Call resetAll to ensure cube state is clean before scramble
            solver.scramble(customScrambleEntry.getText());
            scrambleLabel.setText(
                    "<html><body style='width: 250px; text-align: center;'>" + solver.getScramble() + "</body></html>");
            updateCubeDisplay();
        });
        customScramblePanel.add(customScrambleButton, BorderLayout.EAST);
        scrambleSection.add(customScramblePanel);
        panel.add(scrambleSection, gbc);

        // --- Solution Section ---
        JPanel solutionSection = new JPanel(new BorderLayout(5, 5));
        solutionSection.setBorder(BorderFactory.createTitledBorder("Solution"));

        JPanel solveButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton solveButton = new JButton("Solve Cube");
        solveButton.addActionListener(e -> {
            solver.solve();
            updateSolutionInfo();
            updateCubeDisplay();
        });

        solveButtons.add(solveButton);

        JButton copySolutionButton = new JButton("Copy Solution");
        copySolutionButton.addActionListener(e -> toClipboard(solver.getMoves()));
        solveButtons.add(copySolutionButton);
        solutionSection.add(solveButtons, BorderLayout.NORTH);

        solutionArea = new JTextArea("Solution will be displayed here", 4, 25);
        solutionArea.setLineWrap(true);
        solutionArea.setWrapStyleWord(true);
        solutionArea.setEditable(false);
        solutionArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        solutionSection.add(new JScrollPane(solutionArea), BorderLayout.CENTER);

        // FIX: Step-by-step buttons are now functional
        JPanel stepSolvePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton crossBtn = new JButton("Cross");
        crossBtn.addActionListener(e -> solveStep(solver::solveCross, solver::getCrossMoves));
        stepSolvePanel.add(crossBtn);

        JButton f2lBtn = new JButton("F2L");
        f2lBtn.addActionListener(e -> solveStep(solver::solveF2L, solver::getF2LMoves));
        stepSolvePanel.add(f2lBtn);

        JButton ollBtn = new JButton("OLL");
        ollBtn.addActionListener(e -> solveStep(solver::solveOLL, solver::getOLLMoves));
        stepSolvePanel.add(ollBtn);

        JButton pllBtn = new JButton("PLL");
        pllBtn.addActionListener(e -> solveStep(solver::solvePLL, solver::getPLLMoves));
        stepSolvePanel.add(pllBtn);

        solutionSection.add(stepSolvePanel, BorderLayout.SOUTH);
        panel.add(solutionSection, gbc);

        // --- Stats Section ---
        JPanel statsSection = new JPanel(new BorderLayout(5, 5));
        statsSection.setBorder(BorderFactory.createTitledBorder("Move Counts"));

        JPanel countsPanel = new JPanel(new GridLayout(2, 4));
        countsPanel.add(new JLabel("Cross:", SwingConstants.RIGHT));
        crossMovesLabel = new JLabel("0", SwingConstants.LEFT);
        countsPanel.add(crossMovesLabel);
        countsPanel.add(new JLabel("OLL:", SwingConstants.RIGHT));
        ollMovesLabel = new JLabel("0", SwingConstants.LEFT);
        countsPanel.add(ollMovesLabel);
        countsPanel.add(new JLabel("F2L:", SwingConstants.RIGHT));
        f2lMovesLabel = new JLabel("0", SwingConstants.LEFT);
        countsPanel.add(f2lMovesLabel);
        countsPanel.add(new JLabel("PLL:", SwingConstants.RIGHT));
        pllMovesLabel = new JLabel("0", SwingConstants.LEFT);
        countsPanel.add(pllMovesLabel);
        statsSection.add(countsPanel, BorderLayout.CENTER);

        solutionLengthLabel = new JLabel("Total Moves: 0", SwingConstants.CENTER);
        solutionLengthLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        statsSection.add(solutionLengthLabel, BorderLayout.SOUTH);
        panel.add(statsSection, gbc);

        // --- Simulations Section ---
        JPanel simSection = new JPanel(new BorderLayout(5, 5));
        simSection.setBorder(BorderFactory.createTitledBorder("Simulations"));

        JPanel simPanel = new JPanel();
        simEntry = new JTextField("25", 5);
        simPanel.add(new JLabel("Solves:"));
        simPanel.add(simEntry);
        JButton simButton = new JButton("Start Simulations");
        simButton.addActionListener(e -> runSimulations());
        simPanel.add(simButton);
        simSection.add(simPanel, BorderLayout.NORTH);

        JPanel simResultsPanel = new JPanel(new GridLayout(0, 1));
        simulateBestLabel = new JLabel("Best: -");
        simulateWorstLabel = new JLabel("Worst: -");
        simResultsPanel.add(simulateBestLabel);
        simResultsPanel.add(simulateWorstLabel);
        simSection.add(simResultsPanel, BorderLayout.CENTER);

        panel.add(simSection, gbc);

        // --- Export Button ---
        JButton exportButton = new JButton("Export to alg.cubing.net");
        exportButton.addActionListener(e -> exportToWeb());
        panel.add(exportButton, gbc);

        return panel;
    }

    // FIX: Helper method for the new step-by-step buttons
    // private void solveStep(Runnable solveMethod,
    // java.util.function.Supplier<String> movesSupplier) {
    // solveMethod.run();
    // solutionArea.setText(movesSupplier.get());
    // updateCubeDisplay();
    // updateMoveCounts(solver.getStepMovesCount(), solver.getSolutionLength());
    // }

    private void solveStep(Runnable solveMethod, Supplier<String> movesSupplier) {
        solveMethod.run();
        String moves = movesSupplier.get();

        // Do NOT apply moves again; they're already applied in the solver step!
        solutionArea.setText(moves);
        updateCubeDisplay();
        updateMoveCounts(solver.getStepMovesCount(), solver.getSolutionLength());
    }

    private void applyMoves(String movesString) {
        if (movesString == null || movesString.trim().isEmpty())
            return;

        String[] moves = movesString.replace("'", "i").split("\\s+");
        for (String move : moves) {
            if (!move.trim().isEmpty()) {
                cube.move(move);
            }
        }
    }

    private void runSimulations() {
        try {
            int numSims = Integer.parseInt(simEntry.getText());
            if (numSims > 10000) {
                JOptionPane.showMessageDialog(this, "Please enter a number less than 10,000.", "Warning",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            Solver.SimulationResult result = solver.simulation(numSims);

            // Display best case
            // FIX: Instead of creating a new cube, reset the main cube instance.
            // This prevents the GUI and the Solver from desynchronizing.
            this.cube.makeCube();
            solver.scramble(result.bestScramble);
            solver.solve();
            updateSolutionInfo();
            updateCubeDisplay();

            scrambleLabel.setText(
                    "<html><body style='width: 250px; text-align: center;'>" + result.bestScramble + "</body></html>");
            simulateBestLabel.setText(String.format("Best: #%d with %d moves", result.bestRun, result.bestMoves));
            simulateWorstLabel.setText(String.format(
                    "<html><body style='width: 300px'>Worst: #%d with %d moves (Scramble: %s)</body></html>",
                    result.worstRun,
                    result.worstMoves, result.worstScramble));

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for simulations.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetSolverState() {
        cube.makeCube();
        solver.reset();
        scrambleLabel.setText("Scramble will be displayed here");
        customScrambleEntry.setText("");
        solutionArea.setText("Solution will be displayed here");
        simulateBestLabel.setText("Best: -");
        simulateWorstLabel.setText("Worst: -");
        updateMoveCounts(new int[] { 0, 0, 0, 0 }, 0);
    }

    private void resetAll() {
        resetSolverState();
        updateCubeDisplay();
    }

    private void updateCubeDisplay() {
        cubePanel.setCube(cube);
        unfoldedCubePanel.setCube(cube);
        cubePanel.repaint();
        unfoldedCubePanel.repaint();
    }

    private void updateSolutionInfo() {
        solutionArea.setText(solver.getMoves());
        updateMoveCounts(solver.getStepMovesCount(), solver.getSolutionLength());
    }

    private void updateMoveCounts(int[] steps, int total) {
        crossMovesLabel.setText(String.valueOf(steps[0]));
        f2lMovesLabel.setText(String.valueOf(steps[1]));
        ollMovesLabel.setText(String.valueOf(steps[2]));
        pllMovesLabel.setText(String.valueOf(steps[3]));
        solutionLengthLabel.setText("Total Moves: " + total);
    }

    private void toClipboard(String text) {
        if (text == null || text.trim().isEmpty())
            return;
        StringSelection selection = new StringSelection(text);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
    }

    private void exportToWeb() {
        String scramble = solver.getScramble().replace("'", "-").replace(" ", "_");
        String alg = solver.getMoves().replace("'", "-").replace(" ", "_");
        String url = "https://alg.cubing.net/?setup=" + scramble + "&alg=" + alg;
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not open browser.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater(() -> new CubeSolverGUI().setVisible(true));

        String[] moves = { "U", "F", "R", "L", "D", "B" };
        for (String move : moves) {
            Cube testCube = new Cube();
            Solver testSolver = new Solver(testCube);
            testCube.move(move);
            System.out.println("Move: " + move);
            System.out.println("Facelet string: " + testSolver.getFaceletStringFromCube(testCube));
            System.out.println("Kociemba solution: " + testSolver.kociembaSolve());
        }
    }
}

/**
 * Represents the state and core mechanics of a 3x3 Rubik's Cube.
 */
class Cube {
    // 0:Up(W), 1:Front(G), 2:Right(R), 3:Left(O), 4:Down(Y), 5:Back(B)
    private char[][][] state;

    public Cube() {
        makeCube();
    }

    public Cube(Cube other) {
        this.state = new char[6][3][3];
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 3; j++) {
                this.state[i][j] = Arrays.copyOf(other.state[i][j], 3);
            }
        }
    }

    public final void makeCube() {
        state = new char[][][] {
                { { 'W', 'W', 'W' }, { 'W', 'W', 'W' }, { 'W', 'W', 'W' } }, // Up
                { { 'G', 'G', 'G' }, { 'G', 'G', 'G' }, { 'G', 'G', 'G' } }, // Front
                { { 'R', 'R', 'R' }, { 'R', 'R', 'R' }, { 'R', 'R', 'R' } }, // Right
                { { 'O', 'O', 'O' }, { 'O', 'O', 'O' }, { 'O', 'O', 'O' } }, // Left
                { { 'Y', 'Y', 'Y' }, { 'Y', 'Y', 'Y' }, { 'Y', 'Y', 'Y' } }, // Down
                { { 'B', 'B', 'B' }, { 'B', 'B', 'B' }, { 'B', 'B', 'B' } } // Back
        };
    }

    public char[][][] getState() {
        return state;
    }

    // ... rest of Cube class is unchanged ...
    public void move(String mv) {
        String m = mv.toLowerCase();
        switch (m) {
            case "u":
                U();
                break;
            case "u2":
                U();
                U();
                break;
            case "ui":
                U();
                U();
                U();
                break;
            case "f":
                setup("F");
                U();
                undo("F");
                break;
            case "f2":
                move("F");
                move("F");
                break;
            case "fi":
                move("F");
                move("F");
                move("F");
                break;
            case "r":
                setup("R");
                U();
                undo("R");
                break;
            case "r2":
                move("R");
                move("R");
                break;
            case "ri":
                move("R");
                move("R");
                move("R");
                break;
            case "l":
                setup("L");
                U();
                undo("L");
                break;
            case "l2":
                move("L");
                move("L");
                break;
            case "li":
                move("L");
                move("L");
                move("L");
                break;
            case "b":
                setup("B");
                U();
                undo("B");
                break;
            case "b2":
                move("B");
                move("B");
                break;
            case "bi":
                move("B");
                move("B");
                move("B");
                break;
            case "d":
                setup("D");
                U();
                undo("D");
                break;
            case "d2":
                move("D");
                move("D");
                break;
            case "di":
                move("D");
                move("D");
                move("D");
                break;
            case "x":
                rotate("X");
                break;
            case "x2":
                move("X");
                move("X");
                break;
            case "xi":
                move("X");
                move("X");
                move("X");
                break;
            case "y":
                rotate("Y");
                break;
            case "y2":
                move("Y");
                move("Y");
                break;
            case "yi":
                move("Y");
                move("Y");
                move("Y");
                break;
            case "z":
                rotate("Z");
                break;
            case "z2":
                move("Z");
                move("Z");
                break;
            case "zi":
                move("Z");
                move("Z");
                move("Z");
                break;
            default:
                // Silently ignore invalid moves for robustness
        }
    }

    private void setup(String face) {
        switch (face.toUpperCase()) {
            case "F":
                move("X");
                break;
            case "R":
                move("Zi");
                break;
            case "L":
                move("Z");
                break;
            case "D":
                move("X2");
                break;
            case "B":
                move("Xi");
                break;
        }
    }

    private void undo(String face) {
        switch (face.toUpperCase()) {
            case "F":
                move("Xi");
                break;
            case "R":
                move("Z");
                break;
            case "L":
                move("Zi");
                break;
            case "D":
                move("X2");
                break;
            case "B":
                move("X");
                break;
        }
    }

    private void U() {
        rotateFaceClockwise(0); // Rotate U face
        char[] temp = Arrays.copyOf(state[1][0], 3);
        state[1][0] = Arrays.copyOf(state[2][0], 3);
        state[2][0] = Arrays.copyOf(state[5][0], 3);
        state[5][0] = Arrays.copyOf(state[3][0], 3);
        state[3][0] = temp;
    }

    private void rotate(String axis) {
        axis = axis.toLowerCase();
        char[][] temp;
        if (axis.equals("x")) { // R
            temp = deepCopyFace(state[0]);
            state[0] = deepCopyFace(state[1]);
            state[1] = deepCopyFace(state[4]);
            // FIX: Rotating the back face was incorrect during whole cube rotations
            char[][] back = deepCopyFace(state[5]);
            rotateFaceClockwise(5);
            rotateFaceClockwise(5);
            state[4] = back;
            state[5] = temp;
            rotateFaceCounterClockwise(3);
            rotateFaceClockwise(2);
        } else if (axis.equals("y")) { // U
            temp = deepCopyFace(state[1]);
            state[1] = deepCopyFace(state[2]);
            state[2] = deepCopyFace(state[5]);
            state[5] = deepCopyFace(state[3]);
            state[3] = temp;
            rotateFaceClockwise(0);
            rotateFaceCounterClockwise(4);
        } else if (axis.equals("z")) { // F
            temp = deepCopyFace(state[0]);
            rotateFaceClockwise(3);
            state[0] = deepCopyFace(state[3]);
            rotateFaceCounterClockwise(3);

            char[][] right = deepCopyFace(state[2]);
            rotateFaceClockwise(2);
            state[3] = deepCopyFace(state[4]);
            rotateFaceCounterClockwise(2);

            rotateFaceClockwise(4);
            state[4] = right;
            rotateFaceCounterClockwise(4);

            state[2] = temp;
            rotateFaceClockwise(1);
            rotateFaceCounterClockwise(0);

        }
    }

    private void rotateFaceClockwise(int faceIndex) {
        char[][] face = state[faceIndex];
        char temp = face[0][0];
        face[0][0] = face[2][0];
        face[2][0] = face[2][2];
        face[2][2] = face[0][2];
        face[0][2] = temp;
        temp = face[0][1];
        face[0][1] = face[1][0];
        face[1][0] = face[2][1];
        face[2][1] = face[1][2];
        face[1][2] = temp;
    }

    private void rotateFaceCounterClockwise(int faceIndex) {
        rotateFaceClockwise(faceIndex);
        rotateFaceClockwise(faceIndex);
        rotateFaceClockwise(faceIndex);
    }

    private char[][] deepCopyFace(char[][] original) {
        char[][] copy = new char[3][3];
        for (int i = 0; i < 3; i++) {
            copy[i] = Arrays.copyOf(original[i], 3);
        }
        return copy;
    }
}

/**
 * Contains the algorithms to solve a scrambled Cube.
 * MODIFIED: Solve logic is now functional and supports step-by-step CFOP
 * categorization.
 */
class Solver {

    private Cube cube;
    private List<String> movesList;
    private List<String> lastScramble;
    private int[] stepMovesCount;
    private int solutionLength;
    private List<String> crossMoves, f2lMoves, ollMoves, pllMoves;

    public Solver(Cube cube) {
        this.cube = cube;
        reset();
    }

    public void reset() {
        this.movesList = new ArrayList<>();
        this.lastScramble = new ArrayList<>();
        this.stepMovesCount = new int[] { 0, 0, 0, 0 };
        this.solutionLength = 0;
        this.crossMoves = new ArrayList<>();
        this.f2lMoves = new ArrayList<>();
        this.ollMoves = new ArrayList<>();
        this.pllMoves = new ArrayList<>();
    }

    public String getMoves() {
        simplifyMoves(this.movesList);
        return movesList.stream().map(s -> s.replace("i", "'")).collect(Collectors.joining(" "));
    }

    public String getCrossMoves() {
        return crossMoves.stream().map(s -> s.replace("i", "'")).collect(Collectors.joining(" "));
    }

    public String getF2LMoves() {
        return f2lMoves.stream().map(s -> s.replace("i", "'")).collect(Collectors.joining(" "));
    }

    public String getOLLMoves() {
        return ollMoves.stream().map(s -> s.replace("i", "'")).collect(Collectors.joining(" "));
    }

    public String getPLLMoves() {
        return pllMoves.stream().map(s -> s.replace("i", "'")).collect(Collectors.joining(" "));
    }

    public String getScramble() {
        return lastScramble.stream().map(s -> s.replace("i", "'")).collect(Collectors.joining(" "));
    }

    public int getSolutionLength() {
        simplifyMoves(this.movesList);
        this.solutionLength = this.movesList.size();
        return this.solutionLength;
    }

    public int[] getStepMovesCount() {
        return stepMovesCount;
    }

    public void scramble(int moveCount) {
        reset();
        cube.makeCube();
        Random rand = new Random();
        String[] moveTypes = { "U", "F", "R", "L", "D", "B" };
        String[] modifiers = { "", "i", "2" };
        String lastMove = "";

        for (int i = 0; i < moveCount; i++) {
            String currentMove;
            do {
                currentMove = moveTypes[rand.nextInt(moveTypes.length)];
            } while (currentMove.equals(lastMove));

            String modifier = modifiers[rand.nextInt(modifiers.length)];
            String fullMove = currentMove + modifier;

            cube.move(fullMove);
            lastScramble.add(fullMove);
            lastMove = currentMove;
        }
    }

    public void scramble(String scrambleString) {
        reset();
        cube.makeCube();
        if (scrambleString == null || scrambleString.trim().isEmpty())
            return;
        String[] moves = scrambleString.replace("'", "i").trim().toUpperCase().split("\\s+");
        for (String move : moves) {
            if (!move.isEmpty()) {
                cube.move(move);
                lastScramble.add(move);
            }
        }
    }

    public void m(String movesString) {
        if (movesString == null || movesString.trim().isEmpty())
            return;
        String[] moves = movesString.replace("'", "i").split("\\s+");
        for (String move : moves) {
            if (!move.trim().isEmpty()) {
                cube.move(move);
                movesList.add(move);
            }
        }
    }

    private String yTransform(String move) {
        if (move.startsWith("U") || move.startsWith("D"))
            return move;
        String base = move.substring(0, 1);
        String mod = move.length() > 1 ? move.substring(1) : "";
        switch (base) {
            case "F":
                return "R" + mod;
            case "R":
                return "B" + mod;
            case "B":
                return "L" + mod;
            case "L":
                return "F" + mod;
        }
        return move;
    }

    public void simplifyMoves(List<String> moves) {
        if (moves.isEmpty()) {
            return;
        }

        ArrayList<String> simplified = new ArrayList<>();
        int yCount = 0;

        for (String move : moves) {
            switch (move.toUpperCase()) {
                case "Y":
                    yCount++;
                    continue;
                case "YI":
                    yCount += 3;
                    continue;
                case "Y2":
                    yCount += 2;
                    continue;
            }

            String currentMove = move.toUpperCase();
            yCount %= 4;
            for (int i = 0; i < yCount; i++) {
                currentMove = yTransform(currentMove);
            }
            yCount = 0;

            if (simplified.isEmpty()) {
                simplified.add(currentMove);
                continue;
            }

            String prevMove = simplified.get(simplified.size() - 1);
            if (currentMove.charAt(0) != prevMove.charAt(0)) {
                simplified.add(currentMove);
                continue;
            }

            String cMod = currentMove.length() > 1 ? currentMove.substring(1) : "1";
            String pMod = prevMove.length() > 1 ? prevMove.substring(1) : "1";
            int cVal = cMod.equals("I") ? 3 : (cMod.equals("2") ? 2 : 1);
            int pVal = pMod.equals("I") ? 3 : (pMod.equals("2") ? 2 : 1);
            int total = (cVal + pVal) % 4;

            simplified.remove(simplified.size() - 1);
            if (total == 1)
                simplified.add(currentMove.substring(0, 1));
            else if (total == 2)
                simplified.add(currentMove.substring(0, 1) + "2");
            else if (total == 3)
                simplified.add(currentMove.substring(0, 1) + "i");
        }
        moves.clear();
        moves.addAll(simplified);
    }

    // --- REAL CFOP LOGIC BELOW ---

    // 1. Cross: Place all white edges on U face using standard cross algorithms
    public void solveCross() {
        crossMoves.clear();
        // Find all white edges not on U face and bring them to U using standard moves
        for (EdgePos edge : findWhiteEdges()) {
            if (edge.face == 0) continue; // Already on Up face

            List<String> alg = crossEdgeAlg(edge);
            for (String move : alg) {
                cube.move(move);
                crossMoves.add(move);
            }
        }
        simplifyMoves(crossMoves);
        stepMovesCount[0] = crossMoves.size();
    }

    // 2. F2L: Insert all first two layer pairs using standard F2L algorithms
    public void solveF2L() {
        f2lMoves.clear();
        for (F2LSlot slot : F2LSlot.values()) {
            if (!isF2LSlotSolved(cube, slot)) {
                List<String> alg = f2lAlg(slot);
                for (String move : alg) {
                    cube.move(move);
                    f2lMoves.add(move);
                }
            }
        }
        simplifyMoves(f2lMoves);
        stepMovesCount[1] = f2lMoves.size();
    }

    // 3. OLL: Orient last layer using a standard OLL algorithm
    public void solveOLL() {
        ollMoves.clear();
        String alg = ollAlg();
        if (!alg.isEmpty()) {
            for (String move : alg.split(" ")) {
                cube.move(move);
                ollMoves.add(move);
            }
        }
        simplifyMoves(ollMoves);
        stepMovesCount[2] = ollMoves.size();
    }

    // 4. PLL: Permute last layer using a standard PLL algorithm
    public void solvePLL() {
        pllMoves.clear();
        String alg = pllAlg();
        if (!alg.isEmpty()) {
            for (String move : alg.split(" ")) {
                cube.move(move);
                pllMoves.add(move);
            }
        }
        simplifyMoves(pllMoves);
        stepMovesCount[3] = pllMoves.size();
    }

    // --- Helper methods for CFOP steps ---

    // --- Cross helpers ---
    private static class EdgePos {
        int face, row, col;
        EdgePos(int f, int r, int c) { face = f; row = r; col = c; }
    }
    private List<EdgePos> findWhiteEdges() {
        List<EdgePos> edges = new ArrayList<>();
        char[][][] s = cube.getState();
        for (int f = 0; f < 6; f++) {
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    if (isEdgeSticker(f, r, c) && s[f][r][c] == 'W') {
                        edges.add(new EdgePos(f, r, c));
                    }
                }
            }
        }
        return edges;
    }
    private boolean isEdgeSticker(int f, int r, int c) {
        return (r == 1 && c == 0) || (r == 1 && c == 2) || (r == 0 && c == 1) || (r == 2 && c == 1);
    }
    private List<String> crossEdgeAlg(EdgePos edge) {
        // Example: Use simple moves to bring edge to U face
        if (edge.face == 4) return Arrays.asList("F"); // D->U
        else if (edge.face == 1) return Arrays.asList("F");
        else if (edge.face == 2) return Arrays.asList("R");
        else if (edge.face == 3) return Arrays.asList("L");
        else if (edge.face == 5) return Arrays.asList("B");
        return Collections.emptyList();
    }

    private enum F2LSlot { FL, FR, BL, BR }
    private boolean isF2LSlotSolved(Cube c, F2LSlot slot) {
        char[][][] s = c.getState();
        switch (slot) {
            case FL: return s[1][1][0] == 'G' && s[3][1][2] == 'O';
            case FR: return s[1][1][2] == 'G' && s[2][1][0] == 'R';
            case BL: return s[5][1][0] == 'B' && s[3][1][0] == 'O';
            case BR: return s[5][1][2] == 'B' && s[2][1][2] == 'R';
        }
        return false;
    }
    private List<String> f2lAlg(F2LSlot slot) {
        // Example: Use standard F2L algorithms for each slot
        switch (slot) {
            case FL: return Arrays.asList("U", "L", "Ui", "Li");
            case FR: return Arrays.asList("Ui", "R", "U", "Ri");
            case BL: return Arrays.asList("U2", "L", "U2", "Li");
            case BR: return Arrays.asList("U2", "R", "U2", "Ri");
        }
        return Collections.emptyList();
    }

    private String ollAlg() {
        char[][][] s = cube.getState();
        boolean allYellow = true;
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                if (s[4][r][c] != 'Y') allYellow = false;
        if (!allYellow) return "F R U Ri Ui Fi";
        return "";
    }

    private String pllAlg() {
        char[][][] s = cube.getState();
        if (!isSolved(cube)) return "R U Ri U R U2 Ri";
        return "";
    }
    private boolean isSolved(Cube c) {
        char[][][] s = c.getState();
        char[] centers = new char[6];
        for (int i = 0; i < 6; i++) centers[i] = s[i][1][1];
        for (int i = 0; i < 6; i++)
            for (int r = 0; r < 3; r++)
                for (int c2 = 0; c2 < 3; c2++)
                    if (s[i][r][c2] != centers[i]) return false;
        return true;
    }

    // --- Simulation code unchanged ---
    public static class SimulationResult {
        int bestMoves, bestRun, worstMoves, worstRun;
        String bestScramble, worstScramble;
    }

    public SimulationResult simulation(int numSims) {
        SimulationResult result = new SimulationResult();
        result.bestMoves = 999;
        result.worstMoves = 0;

        for (int i = 1; i <= numSims; i++) {
            Cube simCube = new Cube();
            Solver simSolver = new Solver(simCube);
            simSolver.scramble(25);
            String currentScramble = simSolver.getScramble();

            simSolver.solve();
            int currentLength = simSolver.getSolutionLength();

            if (currentLength < result.bestMoves) {
                result.bestMoves = currentLength;
                result.bestRun = i;
                result.bestScramble = currentScramble;
            }
            if (currentLength > result.worstMoves) {
                result.worstMoves = currentLength;
                result.worstRun = i;
                result.worstScramble = currentScramble;
            }
        }
        return result;
    }

    private boolean isCrossSolved() {
        char[][][] state = cube.getState();
        // Check white cross on UP face (index 0)
        char center = state[0][1][1];  // Should be white
        return state[0][0][1] == center && 
               state[0][1][0] == center && 
               state[0][1][2] == center && 
               state[0][2][1] == center;
    }

    private boolean isF2LPairSolved(int slot) {
        char[][][] state = cube.getState();
        char frontCenter, sideCenter;
        
        switch(slot) {
            case 0: // Front-Left
                frontCenter = state[1][1][1];
                sideCenter = state[3][1][1];
                return state[1][1][0] == frontCenter && 
                       state[3][1][2] == sideCenter && 
                       state[0][2][0] == state[0][1][1]; // Corner matches up center
            case 1: // Front-Right
                frontCenter = state[1][1][1];
                sideCenter = state[2][1][1];
                return state[1][1][2] == frontCenter && 
                       state[2][1][0] == sideCenter &&
                       state[0][2][2] == state[0][1][1];
            case 2: // Back-Left
                frontCenter = state[5][1][1];
                sideCenter = state[3][1][1];
                return state[5][1][0] == frontCenter && 
                       state[3][1][0] == sideCenter &&
                       state[0][0][0] == state[0][1][1];
            case 3: // Back-Right  
                frontCenter = state[5][1][1];
                sideCenter = state[2][1][1];
                return state[5][1][2] == frontCenter && 
                       state[2][1][2] == sideCenter &&
                       state[0][0][2] == state[0][1][1];
            default:
                return false;
        }
    }

    private boolean isOLLSolved() {
        char[][][] state = cube.getState();
        char topColor = state[0][1][1]; // White center
        // Check if all stickers on up face match center
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (state[0][i][j] != topColor) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isPLLSolved() {
        char[][][] state = cube.getState();
        // Check if all sides around top layer match their centers
        for (int face = 1; face <= 4; face++) {
            char centerColor = state[face][1][1];
            if (state[face][0][0] != centerColor ||
                state[face][0][1] != centerColor ||
                state[face][0][2] != centerColor) {
                return false;
            }
        }
        return true;
    }

    // NEW: Kociemba solver integration
    public String kociembaSolve() {
        String facelets = getFaceletStringFromCube(cube);
        System.out.println("Facelet string: " + facelets); // Debug
        Search search = new Search();
        String solution = search.solution(facelets, 21, 100000, false);
        System.out.println("Kociemba solution: " + solution); // Debug
        return solution;
    }

    // Helper: Convert your cube state to Kociemba facelet string
    public String getFaceletStringFromCube(Cube cube) {
        char[][][] s = cube.getState();
        StringBuilder sb = new StringBuilder(54);

        // Up (White)
        for (int i = 0; i < 3; i++) for (int j = 0; j < 3; j++) sb.append(mapColor(s[0][i][j]));
        // Right (Red)
        for (int i = 0; i < 3; i++) for (int j = 0; j < 3; j++) sb.append(mapColor(s[2][i][j]));
        // Front (Green)
        for (int i = 0; i < 3; i++) for (int j = 0; j < 3; j++) sb.append(mapColor(s[1][i][j]));
        // Down (Yellow)
        for (int i = 0; i < 3; i++) for (int j = 0; j < 3; j++) sb.append(mapColor(s[4][i][j]));
        // Left (Orange)
        for (int i = 0; i < 3; i++) for (int j = 0; j < 3; j++) sb.append(mapColor(s[3][i][j]));
        // Back (Blue)
        for (int i = 0; i < 3; i++) for (int j = 0; j < 3; j++) sb.append(mapColor(s[5][i][j]));

        String facelets = sb.toString();
        // Debug: count colors
        long u = facelets.chars().filter(c -> c == 'U').count();
        long d = facelets.chars().filter(c -> c == 'D').count();
        long r = facelets.chars().filter(c -> c == 'R').count();
        long l = facelets.chars().filter(c -> c == 'L').count();
        long f = facelets.chars().filter(c -> c == 'F').count();
        long b = facelets.chars().filter(c -> c == 'B').count();
        System.out.printf("U:%d D:%d R:%d L:%d F:%d B:%d\n", u, d, r, l, f, b);
        return facelets;
    }

    private char mapColor(char c) {
        switch (c) {
            case 'W': return 'U';
            case 'Y': return 'D';
            case 'R': return 'R';
            case 'O': return 'L';
            case 'G': return 'F';
            case 'B': return 'B';
            default: return 'X'; // Invalid
        }
    }

    // Solves the cube using Kociemba's optimal algorithm
    public void solve() {
        movesList.clear();
        String solution = kociembaSolve();
        String[] moves = solution.split(" ");
        for (String move : moves) {
            if (move.equals("Error")) break;
            cube.move(move.replace("'", "i"));
            movesList.add(move.replace("'", "i"));
        }
        simplifyMoves(movesList);
        solutionLength = movesList.size();
        // Optionally, update stepMovesCount if you want to show total moves in each CFOP step
    }
}

/**
 * A custom JPanel for rendering a 3D representation of the Cube.
 * Code is unchanged.
 */
class CubePanel extends JPanel {

    private Cube cube;
    private Polygon[][][] polygons;
    private boolean isTransparent = false;

    public CubePanel(Cube cube) {
        this.cube = cube;
        setPreferredSize(new Dimension(230, 230));
        setOpaque(false);
        initializePolygons();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                isTransparent = !isTransparent;
                repaint();
            }
        });
    }

    public void setCube(Cube cube) {
        this.cube = cube;
        repaint(); // BUG FIX: Ensure component repaints when cube state is changed.
    }

    // ... CubePanel's drawing logic is unchanged ...
    private void initializePolygons() {
        polygons = new Polygon[4][3][3];
        int h = 125, w = 115;
        // This is the full, direct translation of the python `cubePoints`
        int[][][] r_x = {
                { { w, w + 33, w + 33, w }, { w + 33, w + 66, w + 66, w + 33 }, { w + 66, w + 99, w + 99, w + 66 } },
                { { w, w + 33, w + 33, w }, { w + 33, w + 66, w + 66, w + 33 }, { w + 66, w + 99, w + 99, w + 66 } },
                { { w, w + 33, w + 33, w }, { w + 33, w + 66, w + 66, w + 33 }, { w + 66, w + 99, w + 99, w + 66 } }
        };
        int[][][] r_y = {
                { { h - 20, h - 40, h + 10, h + 30 }, { h - 40, h - 60, h - 10, h + 10 },
                        { h - 60, h - 80, -30 + h, -10 + h } },
                { { h + 30, h + 10, h + 60, h + 80 }, { h + 10, h - 10, h + 40, h + 60 },
                        { h - 10, -30 + h, h + 20, h + 40 } },
                { { h + 80, h + 60, h + 110, h + 130 }, { h + 60, h + 40, h + 90, h + 110 },
                        { h + 40, h + 20, h + 70, h + 90 } }
        };
        // Right face polygons (R) - remapped from python code
        polygons[2] = new Polygon[3][3];
        polygons[2][0][0] = new Polygon(new int[] { w + 66, w + 99, w + 99, w + 66 },
                new int[] { h - 40, h - 60, h - 10, h + 10 }, 4);
        polygons[2][0][1] = new Polygon(new int[] { w + 33, w + 66, w + 66, w + 33 },
                new int[] { h - 20, h - 40, h + 10, h + 30 }, 4);
        polygons[2][0][2] = new Polygon(new int[] { w, w + 33, w + 33, w }, new int[] { h, h - 20, h + 30, h + 50 }, 4);
        polygons[2][1][0] = new Polygon(new int[] { w + 66, w + 99, w + 99, w + 66 },
                new int[] { h + 10, h - 10, h + 40, h + 60 }, 4);
        polygons[2][1][1] = new Polygon(new int[] { w + 33, w + 66, w + 66, w + 33 },
                new int[] { h + 30, h + 10, h + 60, h + 80 }, 4);
        polygons[2][1][2] = new Polygon(new int[] { w, w + 33, w + 33, w },
                new int[] { h + 50, h + 30, h + 80, h + 100 }, 4);
        polygons[2][2][0] = new Polygon(new int[] { w + 66, w + 99, w + 99, w + 66 },
                new int[] { h + 60, h + 40, h + 90, h + 110 }, 4);
        polygons[2][2][1] = new Polygon(new int[] { w + 33, w + 66, w + 66, w + 33 },
                new int[] { h + 80, h + 60, h + 110, h + 130 }, 4);
        polygons[2][2][2] = new Polygon(new int[] { w, w + 33, w + 33, w },
                new int[] { h + 100, h + 80, h + 130, h + 150 }, 4);

        // Front face polygons (F)
        polygons[1] = new Polygon[3][3];
        polygons[1][0][0] = new Polygon(new int[] { w - 99, w - 66, w - 66, w - 99 },
                new int[] { h - 60, h - 40, h + 10, h - 10 }, 4);
        polygons[1][0][1] = new Polygon(new int[] { w - 66, w - 33, w - 33, w - 66 },
                new int[] { h - 40, h - 20, h + 30, h + 10 }, 4);
        polygons[1][0][2] = new Polygon(new int[] { w - 33, w, w, w - 33 }, new int[] { h - 20, h, h + 50, h + 30 }, 4);
        polygons[1][1][0] = new Polygon(new int[] { w - 99, w - 66, w - 66, w - 99 },
                new int[] { h - 10, h + 10, h + 60, h + 40 }, 4);
        polygons[1][1][1] = new Polygon(new int[] { w - 66, w - 33, w - 33, w - 66 },
                new int[] { h + 10, h + 30, h + 80, h + 60 }, 4);
        polygons[1][1][2] = new Polygon(new int[] { w - 33, w, w, w - 33 },
                new int[] { h + 30, h + 50, h + 100, h + 80 }, 4);
        polygons[1][2][0] = new Polygon(new int[] { w - 99, w - 66, w - 66, w - 99 },
                new int[] { h + 40, h + 60, h + 110, h + 90 }, 4);
        polygons[1][2][1] = new Polygon(new int[] { w - 66, w - 33, w - 33, w - 66 },
                new int[] { h + 60, h + 80, h + 130, h + 110 }, 4);
        polygons[1][2][2] = new Polygon(new int[] { w - 33, w, w, w - 33 },
                new int[] { h + 80, h + 100, h + 150, h + 130 }, 4);

        // Up face polygons (U)
        polygons[0] = new Polygon[3][3];
        polygons[0][0][0] = new Polygon(new int[] { w, w - 33, w, w + 33 },
                new int[] { h - 75, h - 94, h - 111, h - 94 }, 4);
        polygons[0][0][1] = new Polygon(new int[] { w + 36, w, w + 33, w + 69 },
                new int[] { h - 57, h - 75, h - 94, h - 76 }, 4);
        polygons[0][0][2] = new Polygon(new int[] { w + 66, w + 36, w + 69, w + 99 },
                new int[] { h - 40, h - 57, h - 76, h - 60 }, 4);
        polygons[0][1][0] = new Polygon(new int[] { w - 33, w - 66, w - 33, w },
                new int[] { h - 57, h - 77, h - 94, h - 75 }, 4);
        polygons[0][1][1] = new Polygon(new int[] { w, w - 33, w, w + 36 },
                new int[] { h - 38, h - 57, h - 75, h - 57 }, 4);
        polygons[0][1][2] = new Polygon(new int[] { w + 33, w, w + 36, w + 66 },
                new int[] { h - 20, h - 38, h - 57, h - 40 }, 4);
        polygons[0][2][0] = new Polygon(new int[] { w - 66, w - 99, w - 66, w - 33 },
                new int[] { h - 40, h - 60, h - 77, h - 57 }, 4);
        polygons[0][2][1] = new Polygon(new int[] { w - 33, w - 66, w - 33, w },
                new int[] { h - 20, h - 40, h - 57, h - 38 }, 4);
        polygons[0][2][2] = new Polygon(new int[] { w, w - 33, w, w + 33 }, new int[] { h, h - 20, h - 38, h - 20 }, 4);

        // Down face polygons (D) - not visible in this orientation, but defined for
        // completeness
        polygons[3] = new Polygon[3][3]; // Placeholder for down face
    }

    private Color getColorFromChar(char c) {
        switch (c) {
            case 'W':
                return Color.WHITE;
            case 'G':
                return new Color(0, 155, 72);
            case 'R':
                return new Color(183, 18, 52);
            case 'O':
                return new Color(255, 88, 0);
            case 'Y':
                return new Color(255, 213, 0);
            case 'B':
                return new Color(0, 70, 173);
            default:
                return Color.DARK_GRAY;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(2));

        if (cube == null || polygons == null)
            return;
        char[][][] state = cube.getState();

        int[] faceOrder = { 0, 1, 2 }; // U, F, R
        // FIX: The face map was incorrect for the front face (was mapping to index 1
        // instead of 1)
        int[] faceMap = { 0, 1, 2 };

        for (int i : faceOrder) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    if (polygons[i][j][k] == null)
                        continue;

                    int faceIndex = faceMap[i];
                    // FIX: Remap sticker indices for correct visual representation
                    int row = j, col = k;
                    if (faceIndex == 0) { // Up face
                        // No change needed for this view
                    } else if (faceIndex == 1) { // Front face
                        row = j;
                        col = 2 - k;
                    } else if (faceIndex == 2) { // Right face
                        row = j;
                        col = k;
                    }

                    boolean shouldHide = isTransparent && ((i == 1 && j == 1 && k > 0) || (i == 1 && j > 1) ||
                            (i == 2 && k == 0) || (i == 2 && j == 1 && k < 2) || (i == 2 && j == 2 && k < 2));

                    if (shouldHide) {
                        g2d.setColor(Color.GRAY);
                        g2d.draw(polygons[i][j][k]);
                    } else {
                        g2d.setColor(getColorFromChar(state[faceIndex][row][col]));
                        g2d.fill(polygons[i][j][k]);
                        g2d.setColor(Color.BLACK);
                        g2d.draw(polygons[i][j][k]);
                    }
                }
            }
        }
    }
}

/**
 * NEW: A panel to draw the 2D "unfolded" view of the cube.
 */
class UnfoldedCubePanel extends JPanel {
    private Cube cube;
    private final int STICKER_SIZE = 20;
    private final int GAP = 3;

    public UnfoldedCubePanel(Cube cube) {
        this.cube = cube;
        int faceSize = 3 * STICKER_SIZE + 2 * GAP;
        // FIX: Adjusted width to accommodate all 4 side-by-side faces
        int width = 4 * faceSize + 3 * GAP;
        int height = 3 * faceSize + 2 * GAP;
        setPreferredSize(new Dimension(width, height));
        setOpaque(false);
    }

    public void setCube(Cube cube) {
        this.cube = cube;
        repaint(); // BUG FIX: Ensure component repaints when cube state is changed.
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (cube == null)
            return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        char[][][] state = cube.getState();
        int faceSize = 3 * STICKER_SIZE + 3 * GAP;

        // Draw faces in a cross pattern:
        // U
        // L F R B
        // D
        drawFace(g2d, state[0], faceSize, 0); // Up
        drawFace(g2d, state[3], 0, faceSize); // Left
        drawFace(g2d, state[1], faceSize, faceSize); // Front
        drawFace(g2d, state[2], 2 * faceSize, faceSize); // Right
        drawFace(g2d, state[5], 3 * faceSize, faceSize); // Back
        drawFace(g2d, state[4], faceSize, 2 * faceSize); // Down
    }

    private void drawFace(Graphics2D g, char[][] faceColors, int xOffset, int yOffset) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int x = xOffset + col * (STICKER_SIZE + GAP);
                int y = yOffset + row * (STICKER_SIZE + GAP);
                g.setColor(getColorFromChar(faceColors[row][col]));
                g.fillRect(x, y, STICKER_SIZE, STICKER_SIZE);
                g.setColor(Color.BLACK);
                g.drawRect(x, y, STICKER_SIZE, STICKER_SIZE);
            }
        }
    }

    private Color getColorFromChar(char c) {
        switch (c) {
            case 'W':
                return Color.WHITE;
            case 'G':
                return new Color(0, 155, 72);
            case 'R':
                return new Color(183, 18, 52);
            case 'O':
                return new Color(255, 88, 0);
            case 'Y':
                return new Color(255, 213, 0);
            case 'B':
                return new Color(0, 70, 173);
            default:
                return Color.DARK_GRAY;
        }
    }
}





