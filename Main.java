import java.applet.Applet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class Main extends Applet {
    private int n;
    private int boxSize;
    private JTextField[][] inputGrid;
    private JPanel gridPanel;
    private JButton solveButton;
    private JButton clearButton;

    @Override
    public void init() {
        setLayout(new BorderLayout());

        // Initial larger window size
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (frame != null) {
            frame.setSize(500, 500); // Larger initial window size
        }

        // Top panel for Sudoku size input
        JPanel sizePanel = new JPanel(new FlowLayout());
        Label sizeLabel = new Label("Enter Sudoku size (e.g., 3 for 9x9):");
        sizePanel.add(sizeLabel);
        TextField sizeInput = new TextField(5);
        sizePanel.add(sizeInput);
        Button sizeButton = new Button("Submit");
        sizePanel.add(sizeButton);
        add(sizePanel, BorderLayout.NORTH);

        // Action listener for size submission
        sizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    boxSize = Integer.parseInt(sizeInput.getText());
                    if (boxSize < 1)
                        throw new NumberFormatException();
                    n = boxSize * boxSize;
                    createGrid();
                } catch (NumberFormatException ex) {
                    showStatus("Invalid size. Please enter a positive integer.");
                }
            }
        });
    }

    private void createGrid() {
        // Remove existing panels if they exist
        if (gridPanel != null) {
            remove(gridPanel);
        }
        if (solveButton != null) {
            remove(solveButton);
        }
        if (clearButton != null) {
            remove(clearButton);
        }

        // Create grid panel
        gridPanel = new JPanel(new GridLayout(n, n));
        gridPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2)); // Border around the grid
        inputGrid = new JTextField[n][n];
        Font font = new Font("SansSerif", Font.BOLD, 30); // Font size
        int cellSize = font.getSize() + 10; // Cell size based on font

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                inputGrid[i][j] = new JTextField("0"); // Initially fill with zeroes
                inputGrid[i][j].setFont(font);
                inputGrid[i][j].setHorizontalAlignment(JTextField.CENTER);
                inputGrid[i][j].setPreferredSize(new Dimension(cellSize, cellSize));
                inputGrid[i][j].setBorder(BorderFactory.createLineBorder(Color.GRAY, 1)); // Light border for cells
                gridPanel.add(inputGrid[i][j]);
            }
        }

        // Create distinct borders for sub-grids
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int top = (i % boxSize == 0) ? 2 : 1;
                int left = (j % boxSize == 0) ? 2 : 1;
                int bottom = (i == n - 1) ? 2 : 1;
                int right = (j == n - 1) ? 2 : 1;

                inputGrid[i][j].setBorder(BorderFactory.createMatteBorder(top, left, bottom, right, Color.BLACK));
            }
        }

        // Solve button setup
        solveButton = new JButton("Solve");
        solveButton.setFont(font);
        solveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[][] sudoku = new int[n][n];
                try {
                    for (int i = 0; i < n; i++) {
                        for (int j = 0; j < n; j++) {
                            String text = inputGrid[i][j].getText();
                            sudoku[i][j] = text.isEmpty() ? 0 : Integer.parseInt(text);
                        }
                    }
                    if (solve(sudoku, n, boxSize)) {
                        showSolution(sudoku);
                    } else {
                        JOptionPane.showMessageDialog(gridPanel, "No solution exists for the given Sudoku.", "No Solution", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(gridPanel, "Invalid input. Please enter valid numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Clear button setup
        clearButton = new JButton("Clear");
        clearButton.setFont(font);
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearGrid();
            }
        });

        // Center panel for grid and buttons
        JPanel centerPanel = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.add(solveButton);
        buttonPanel.add(clearButton);

        // Add grid panel and button panel
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Display initial message after creating the grid
        JOptionPane.showMessageDialog(gridPanel,
                "Please fill in the already present cells with initial values (0 for empty cells).",
                "Fill Cells",
                JOptionPane.INFORMATION_MESSAGE);

        revalidate();
        repaint();
    }

    private void showSolution(int[][] sudoku) {
        // Display solution in the same grid with different colors
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (inputGrid[i][j].getText().isEmpty() || Integer.parseInt(inputGrid[i][j].getText()) == 0) {
                    inputGrid[i][j].setText(String.valueOf(sudoku[i][j]));
                    inputGrid[i][j].setForeground(Color.blue); // Solved cells in blue
                } else {
                    inputGrid[i][j].setForeground(Color.red); // Pre-filled cells in red
                }
                inputGrid[i][j].setEditable(false); // Disable editing after solving
            }
        }
        revalidate();
        repaint();
    }

    // Clear the Sudoku grid and reset for new input
    private void clearGrid() {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                inputGrid[i][j].setText("0"); // Reset to zero
                inputGrid[i][j].setForeground(Color.BLACK);
                inputGrid[i][j].setEditable(true);
            }
        }
        revalidate();
        repaint();
    }

    // Sudoku solving function (recursive backtracking)
    public static boolean solve(int[][] sudoku, int n, int boxSize) {
        int row = -1, col = -1;
        boolean isEmpty = true;

        // Find the first empty cell
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (sudoku[i][j] == 0) {
                    row = i;
                    col = j;
                    isEmpty = false;
                    break;
                }
            }
            if (!isEmpty) {
                break;
            }
        }

        // No empty cell found, Sudoku is solved
        if (isEmpty) {
            return true;
        }

        // Try filling the cell with valid numbers
        for (int num = 1; num <= n; num++) {
            if (isValid(sudoku, row, col, num, boxSize)) {
                sudoku[row][col] = num;
                if (solve(sudoku, n, boxSize)) {
                    return true;
                } else {
                    sudoku[row][col] = 0;
                }
            }
        }

        return false; // No valid number found, backtrack
    }

    // Check if placing num in sudoku[row][col] is valid
    public static boolean isValid(int[][] sudoku, int row, int col, int num, int boxSize) {
        for (int i = 0; i < sudoku.length; i++) {
            if (sudoku[row][i] == num || sudoku[i][col] == num) {
                return false;
            }
        }

        int boxRowStart = row - row % boxSize;
        int boxColStart = col - col % boxSize;
        for (int r = boxRowStart; r < boxRowStart + boxSize; r++) {
            for (int d = boxColStart; d < boxColStart + boxSize; d++) {
                if (sudoku[r][d] == num) {
                    return false;
                }
            }
        }

        return true;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Sudoku Solver Applet");
        Main applet = new Main();

        frame.setSize(500, 500); // Start with a larger initial window size
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(applet, BorderLayout.CENTER);

        applet.init();
        applet.start();

        frame.setVisible(true);
    }
}
