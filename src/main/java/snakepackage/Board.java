package snakepackage;
/**
 * @author Juan Pablo Daza Pereira
 */

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
import enums.GridSize;
import java.io.InputStream;

public class Board extends JLabel implements Observer {

	private static final long serialVersionUID = 1L;

	public static final int NR_BARRIERS = 5;
	public static final int NR_JUMP_PADS = 2;
	public static final int NR_TURBO_BOOSTS = 2;
	public static final int NR_FOOD = 5;

	static Cell[] food = new Cell[NR_FOOD];
	static Cell[] barriers = new Cell[NR_BARRIERS];
	static Cell[] jump_pads = new Cell[NR_JUMP_PADS];
	static Cell[] turbo_boosts = new Cell[NR_TURBO_BOOSTS];
	static int[] result = new int[SnakeApp.MAX_THREADS];

	Random random = new Random();
	static Cell[][] gameboard = new Cell[GridSize.GRID_WIDTH][GridSize.GRID_HEIGHT];

	public Board() {
		if ((NR_BARRIERS + NR_JUMP_PADS + NR_FOOD + NR_TURBO_BOOSTS) >
				GridSize.GRID_HEIGHT * GridSize.GRID_WIDTH){
			throw new IllegalArgumentException("Demasiados elementos para el tamaño de la cuadrícula");
		}
		initializeBoard();
	}

	private void initializeBoard() {
		GenerateBoard();
		GenerateFood();
		GenerateBarriers();
		GenerateJumpPads();
		GenerateTurboBoosts();
	}

	private void GenerateBoard() {
		for (int i = 0; i != GridSize.GRID_WIDTH; i++) {
			for (int j = 0; j != GridSize.GRID_HEIGHT; j++) {
				gameboard[i][j] = new Cell(i, j);
			}
		}
	}

	private void GenerateFood() {
		for (int i = 0; i < NR_FOOD; i++) {
			Cell cell = getRandomEmptyCell();
			food[i] = cell;
			food[i].setFood(true);
		}
	}

	private void GenerateBarriers() {
		for (int i = 0; i < NR_BARRIERS; i++) {
			Cell cell = getRandomEmptyCell();
			barriers[i] = cell;
			barriers[i].setBarrier(true);
		}
	}

	private void GenerateJumpPads() {
		for (int i = 0; i < NR_JUMP_PADS; i++) {
			Cell cell = getRandomEmptyCell();
			jump_pads[i] = cell;
			jump_pads[i].setJump_pad(true);
		}
	}

	private void GenerateTurboBoosts() {
		for (int i = 0; i < NR_TURBO_BOOSTS; i++) {
			Cell cell = getRandomEmptyCell();
			turbo_boosts[i] = cell;
			turbo_boosts[i].setTurbo_boost(true);
		}
	}

	private synchronized Cell getRandomEmptyCell() {
		Cell cell;
		do {
			cell = gameboard[random.nextInt(GridSize.GRID_WIDTH)]
					[random.nextInt(GridSize.GRID_HEIGHT)];
		}while (cell.hasElements());
		return cell;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawGrid(g);
		drawSnake(g);
		drawFood(g);
	}

	private void drawGrid(Graphics g) {
		g.setColor(new Color(255, 250, 250));
		g.fillRect(0, 0, GridSize.GRID_WIDTH * GridSize.WIDTH_BOX,
				GridSize.GRID_HEIGHT * GridSize.HEIGH_BOX);

		g.setColor(new Color(135, 135, 135));
		for (int i = 0; i <= GridSize.GRID_WIDTH; i++) {
			g.drawLine(i * GridSize.WIDTH_BOX, 0,
					i * GridSize.WIDTH_BOX, GridSize.GRID_HEIGHT * GridSize.HEIGH_BOX);
		}
		for (int i = 0; i <= GridSize.GRID_HEIGHT; i++) {
			g.drawLine(0, i * GridSize.HEIGH_BOX,
					GridSize.GRID_WIDTH * GridSize.WIDTH_BOX, i * GridSize.HEIGH_BOX);
		}
	}

	private synchronized void drawSnake(Graphics g) {
		for (int i = 0; i < SnakeApp.MAX_THREADS; i++) {
			Snake snake = SnakeApp.getApp().snakes[i];
			if (snake != null) {
				for (Cell cell : snake.getBody()) {
					if (cell.equals(snake.getBody().peekFirst())) {
						g.setColor(new Color(50 + (i * 10), 205, 150));
					} else {
						g.setColor(snake.isSelected() ?
								new Color(32, 178, 170) :
								new Color(34, 139, 34));
					}
					g.fillRect(cell.getX() * GridSize.WIDTH_BOX,
							cell.getY() * GridSize.HEIGH_BOX,
							GridSize.WIDTH_BOX, GridSize.HEIGH_BOX);
				}
			}
		}
	}

	private synchronized void drawElements(Graphics g) {
		drawFood(g);
		drawBarriers(g);
		drawJumpPads(g);
		drawTurboBoosts(g);
	}


	private void drawFood(Graphics g) {
		Image mouseImg = loadImage("Img/mouse.png");
		if (mouseImg != null) {
			synchronized(food) {
				for (Cell c : food) {
					g.drawImage(mouseImg,
							c.getX() * GridSize.WIDTH_BOX,
							c.getY() * GridSize.HEIGH_BOX, this);
				}
			}
		}
	}

	private void drawBarriers(Graphics g) {
		Image barrierImg = loadImage("Img/firewall.png");
		if (barrierImg != null) {
			synchronized(barriers) {
				for (Cell c : barriers) {
					g.drawImage(barrierImg,
							c.getX() * GridSize.WIDTH_BOX,
							c.getY() * GridSize.HEIGH_BOX, this);
				}
			}
		}
	}

	private void drawJumpPads(Graphics g) {
		Image jumpImg = loadImage("Img/up.png");
		if (jumpImg != null) {
			synchronized(jump_pads) {
				for (Cell c : jump_pads) {
					g.drawImage(jumpImg,
							c.getX() * GridSize.WIDTH_BOX,
							c.getY() * GridSize.HEIGH_BOX, this);
				}
			}
		}
	}

	private void drawTurboBoosts(Graphics g) {
		Image turboImg = loadImage("Img/lightning.png");
		if (turboImg != null) {
			synchronized(turbo_boosts) {
				for (Cell c : turbo_boosts) {
					g.drawImage(turboImg,
							c.getX() * GridSize.WIDTH_BOX,
							c.getY() * GridSize.HEIGH_BOX, this);
				}
			}
		}
	}

	private Image loadImage(String path) {
		try {
			InputStream resource = ClassLoader.getSystemResourceAsStream(path);
			if (resource != null) {
				return ImageIO.read(resource);
			}
		} catch (IOException e) {
			System.err.println("Error loading image: " + path);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void update(Observable o, Object arg) {
		repaint();
	}

	public synchronized static void relocateFood(Cell oldFoodCell) {
		for (int i = 0; i < NR_FOOD; i++) {
			if (food[i].equals(oldFoodCell)) {
				Cell newCell = SnakeApp.getApp().board.getRandomEmptyCell();
				food[i] = newCell;
				oldFoodCell.setFood(false);
				newCell.setFood(true);
				break;
			}
		}
	}

	public synchronized static void removePowerUp(Cell cell, boolean isJumpPad) {
		if (isJumpPad) {
			for (int i = 0; i < NR_JUMP_PADS; i++) {
				if (jump_pads[i].equals(cell)) {
					jump_pads[i].setJump_pad(false);
					jump_pads[i] = new Cell(-5, -5);
					break;
				}
			}
		} else {
			for (int i = 0; i < NR_TURBO_BOOSTS; i++) {
				if (turbo_boosts[i].equals(cell)) {
					turbo_boosts[i].setTurbo_boost(false);
					turbo_boosts[i] = new Cell(-5, -5);
					break;
				}
			}
		}
	}
}
