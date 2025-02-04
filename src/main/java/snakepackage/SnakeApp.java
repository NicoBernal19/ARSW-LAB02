package snakepackage;

import java.awt.*;

import javax.swing.*;

import enums.GridSize;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author jd-
 * @author Juan Pablo Daza Pereira
 */
public class SnakeApp {

    private static SnakeApp app;
    public static final int MAX_THREADS = 8;
    Snake[] snakes = new Snake[MAX_THREADS];
    private static final Cell[] spawn = {
            new Cell(1, (GridSize.GRID_HEIGHT / 2) / 2),
            new Cell(GridSize.GRID_WIDTH - 2, 3 * (GridSize.GRID_HEIGHT / 2) / 2),
            new Cell(3 * (GridSize.GRID_WIDTH / 2) / 2, 1),
            new Cell((GridSize.GRID_WIDTH / 2) / 2, GridSize.GRID_HEIGHT - 2),
            new Cell(1, 3 * (GridSize.GRID_HEIGHT / 2) / 2),
            new Cell(GridSize.GRID_WIDTH - 2, (GridSize.GRID_HEIGHT / 2) / 2),
            new Cell((GridSize.GRID_WIDTH / 2) / 2, 1),
            new Cell(3 * (GridSize.GRID_WIDTH / 2) / 2, GridSize.GRID_HEIGHT - 2)
    };

    // Componentes de la interfaz
    private JFrame frame;
    static Board board;
    private JButton startButton;
    private JButton pauseButton;
    private JButton resumeButton;
    private JLabel statusLabel;
    private JLabel longestSnakeLabel;
    private JLabel worstSnakeLabel;

    // Control del estado del juego
    private volatile boolean gameStarted = false;
    Thread[] thread = new Thread[MAX_THREADS];

    public SnakeApp() {
        initializeFrame();
        initializeGameControls();
        initializeStatusPanel();
    }

    private void initializeFrame() {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        frame = new JFrame("The Snake Race");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(GridSize.GRID_WIDTH * GridSize.WIDTH_BOX + 350,
                GridSize.GRID_HEIGHT * GridSize.HEIGH_BOX + 100);
        frame.setLocationRelativeTo(null);
        board = new Board();
        frame.add(board, BorderLayout.CENTER);
    }

    private void initializeGameControls() {
        JPanel controlPanel = new JPanel(new FlowLayout());

        startButton = new JButton("Iniciar");
        pauseButton = new JButton("Pausar");
        resumeButton = new JButton("Reanudar");

        pauseButton.setEnabled(false);
        resumeButton.setEnabled(false);

        startButton.addActionListener(e -> startGame());
        pauseButton.addActionListener(e -> pauseGame());
        resumeButton.addActionListener(e -> resumeGame());

        controlPanel.add(startButton);
        controlPanel.add(pauseButton);
        controlPanel.add(resumeButton);

        frame.add(controlPanel, BorderLayout.NORTH);
    }

    private void initializeStatusPanel() {
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        statusLabel = new JLabel("El juego no comenzó");
        longestSnakeLabel = new JLabel("Serpiente más larga: -");
        worstSnakeLabel = new JLabel("Peor serpiente: -");

        // Configurar fuentes para mejor legibilidad
        Font labelFont = new Font("Arial", Font.PLAIN, 14);
        statusLabel.setFont(labelFont);
        longestSnakeLabel.setFont(labelFont);
        worstSnakeLabel.setFont(labelFont);

        statusPanel.add(Box.createVerticalStrut(20));
        statusPanel.add(statusLabel);
        statusPanel.add(Box.createVerticalStrut(20));
        statusPanel.add(longestSnakeLabel);
        statusPanel.add(Box.createVerticalStrut(10));
        statusPanel.add(worstSnakeLabel);

        frame.add(statusPanel, BorderLayout.EAST);
    }

    // Control del inicio del juego
    private void startGame() {
        if (!gameStarted) {
            gameStarted = true;
            startButton.setEnabled(false);
            pauseButton.setEnabled(true);
            statusLabel.setText("Juego comenzado");

            init();

            // Iniciar timer para actualización de estado
            Timer statusTimer = new Timer(1000, e -> {
                if (gameStarted && !isPaused()) {
                    updateGameStatus();
                }
            });
            statusTimer.start();
        }
    }

    // Control de pausa del juego
    private synchronized void pauseGame() {
        pauseButton.setEnabled(false);
        resumeButton.setEnabled(true);
        statusLabel.setText("El juego se ha detenido");

        for (Snake snake : snakes) {
            if (snake != null) {
                snake.pause();
            }
        }

        updateGameStatus();
    }

    // Control de reanudación del juego
    private synchronized void resumeGame() {
        pauseButton.setEnabled(true);
        resumeButton.setEnabled(false);
        statusLabel.setText("El juego ha comenzado");

        for (Snake snake : snakes) {
            if (snake != null) {
                snake.resume();
            }
        }
    }

    private void updateGameStatus() {
        Snake longestSnake = null;
        Snake worstSnake = null;
        long earliestDeath = Long.MAX_VALUE;

        for (Snake snake : snakes) {
            if (snake != null) {
                // Buscar serpiente más larga entre las vivas
                if (!snake.isSnakeEnd() && (longestSnake == null ||
                        snake.getLength() > longestSnake.getLength())) {
                    longestSnake = snake;
                }

                // Buscar primera serpiente que murió
                if (snake.isSnakeEnd() && snake.getDeathTime() < earliestDeath) {
                    earliestDeath = snake.getDeathTime();
                    worstSnake = snake;
                }
            }
        }

        updateStatusLabels(longestSnake, worstSnake);
    }

    // Actualización de las etiquetas de estado
    private void updateStatusLabels(Snake longest, Snake worst) {
        if (longest != null) {
            longestSnakeLabel.setText(String.format("Serpiente más larga: serpiente %d (longitud: %d)",
                    longest.getIdt(), longest.getLength()));
        }

        if (worst != null) {
            worstSnakeLabel.setText(String.format("La peor serpiente: serpiente %d (murió primero)",
                    worst.getIdt()));
        }
    }

    private boolean isPaused() {
        return resumeButton.isEnabled();
    }

    private void init() {
        for (int i = 0; i < MAX_THREADS; i++) {
            snakes[i] = new Snake(i + 1, spawn[i], i + 1);
            snakes[i].addObserver(board);
            thread[i] = new Thread(snakes[i]);
            thread[i].start();
        }

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            app = new SnakeApp();
            app.frame.setVisible(true);
        });
    }

    public static SnakeApp getApp() {
        return app;
    }
}
