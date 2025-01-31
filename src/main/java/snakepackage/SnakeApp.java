package snakepackage;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

import enums.GridSize;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * @author jd-
 *
 */
public class SnakeApp {

    private static SnakeApp app;
    public static final int MAX_THREADS = 8;
    Snake[] snakes = new Snake[MAX_THREADS];
    private static final Cell[] spawn = {
        new Cell(1, (GridSize.GRID_HEIGHT / 2) / 2),
        new Cell(GridSize.GRID_WIDTH - 2,
        3 * (GridSize.GRID_HEIGHT / 2) / 2),
        new Cell(3 * (GridSize.GRID_WIDTH / 2) / 2, 1),
        new Cell((GridSize.GRID_WIDTH / 2) / 2, GridSize.GRID_HEIGHT - 2),
        new Cell(1, 3 * (GridSize.GRID_HEIGHT / 2) / 2),
        new Cell(GridSize.GRID_WIDTH - 2, (GridSize.GRID_HEIGHT / 2) / 2),
        new Cell((GridSize.GRID_WIDTH / 2) / 2, 1),
        new Cell(3 * (GridSize.GRID_WIDTH / 2) / 2,
        GridSize.GRID_HEIGHT - 2)};
    private JFrame frame;
    private static Board board;
    int nr_selected = 0;
    Thread[] thread = new Thread[MAX_THREADS];

    private JButton resumeButton, startButton, pauseButton;
    private boolean running = false;
    private boolean started = false;
    private boolean paused = false;

    public SnakeApp() {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        frame = new JFrame("The Snake Race");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // frame.setSize(618, 640);
        frame.setSize(GridSize.GRID_WIDTH * GridSize.WIDTH_BOX + 17,
                GridSize.GRID_HEIGHT * GridSize.HEIGH_BOX + 40);
        frame.setLocation(dimension.width / 2 - frame.getWidth() / 2,
                dimension.height / 2 - frame.getHeight() / 2);
        board = new Board();
        
        
        frame.add(board,BorderLayout.CENTER);
        
//        JPanel actionsBPabel=new JPanel();
//        actionsBPabel.setLayout(new FlowLayout());
//        actionsBPabel.add(new JButton("Action "));
//        frame.add(actionsBPabel,BorderLayout.SOUTH);

        JPanel actionsPanel = new JPanel();
        actionsPanel.setLayout(new FlowLayout());

        startButton = new JButton("Iniciar");
        pauseButton = new JButton("Pausar");
        resumeButton = new JButton("Reanudar");

        actionsPanel.add(startButton);
        actionsPanel.add(pauseButton);
        actionsPanel.add(resumeButton);

        frame.add(actionsPanel,BorderLayout.SOUTH);

        startButton.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               if(!running){
                   init();
                   running = true;
               }
           }
        });

        pauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(!paused){
                    pause();
                    paused = true;
                }
            }
        });

        resumeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(paused){
                    resume();
                    paused = false;
                }
            }
        });

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        app = new SnakeApp();
        //app.init();
    }

    private void init() {
        for (int i = 0; i != MAX_THREADS; i++) {
            snakes[i] = new Snake(i + 1, spawn[i], i + 1);
            snakes[i].addObserver(board);
            thread[i] = new Thread(snakes[i]);
            thread[i].start();
        }

        new Thread(() -> {
            while (true) {
                int x = 0;
                for (int i = 0; i != MAX_THREADS; i++) {
                    if (snakes[i].isSnakeEnd() == true) {
                        x++;
                    }
                }
                if (x == MAX_THREADS) {
                    break;
                }
            }
            System.out.println("Thread (snake) status:");
            for (int i = 0; i != MAX_THREADS; i++) {
                System.out.println("[" + i + "] :" + thread[i].getState());
            }
        }).start();
    }

    private void pause() {
        for(Snake s: snakes){
            s.pause();
        }
    }

    private void resume() {
        for(Snake s: snakes){
            s.resume();
        }
    }

    public static SnakeApp getApp() {
        return app;
    }

}
