package snakepackage;
/**
 *
 * @author Juan Pablo Daza Pereira
 */

import java.util.LinkedList;
import java.util.Observable;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;
import enums.Direction;
import enums.GridSize;

public class Snake extends Observable implements Runnable {

    private int idt;
    private Cell head;
    private Cell newCell;
    private final LinkedList<Cell> snakeBody = new LinkedList<Cell>();
    private Cell start = null;

    private volatile boolean snakeEnd = false;
    private volatile boolean paused = false;
    private volatile long deathTime = 0;
    private final ReentrantLock stateLock = new ReentrantLock();

    private int direction = Direction.NO_DIRECTION;
    private final int INIT_SIZE = 3;

    private volatile boolean hasTurbo = false;
    private int jumps = 0;
    private boolean isSelected = false;
    private int growing = 0;
    public boolean goal = false;

    public Snake(int idt, Cell head, int direction) {
        this.idt = idt;
        this.direction = direction;
        generateSnake(head);
    }

    public synchronized void pause() {
        paused = true;
    }

    public synchronized void resume() {
        paused = false;
        notifyAll();
    }

    private void generateSnake(Cell head) {
        start = head;
        snakeBody.add(head);
        growing = INIT_SIZE - 1;
    }

    @Override
    public void run() {
        while (!snakeEnd) {
            synchronized(this) {
                while(paused) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }

            snakeCalc();
            setChanged();
            notifyObservers();

            try {
                if (hasTurbo) {
                    Thread.sleep(500 / 3);
                } else {
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        // Registrar tiempo de muerte
        if (deathTime == 0) {
            deathTime = System.currentTimeMillis();
        }

        fixDirection(head);
    }

    private Cell fixDirection(Cell newCell) {
        // revert movement
        if (direction == Direction.LEFT && head.getX() + 1 < GridSize.GRID_WIDTH) {
            newCell = Board.gameboard[head.getX() + 1][head.getY()];
        } else if (direction == Direction.RIGHT && head.getX() - 1 >= 0) {
            newCell = Board.gameboard[head.getX() - 1][head.getY()];
        } else if (direction == Direction.UP && head.getY() + 1 < GridSize.GRID_HEIGHT) {
            newCell = Board.gameboard[head.getX()][head.getY() + 1];
        } else if (direction == Direction.DOWN && head.getY() - 1 >= 0) {
            newCell = Board.gameboard[head.getX()][head.getY() - 1];
        }

        randomMovement(newCell);
        return newCell;
    }

    public int getLength() {
        stateLock.lock();
        try {
            return snakeBody.size();
        } finally {
            stateLock.unlock();
        }
    }

    public long getDeathTime() {
        return deathTime;
    }

    private void snakeCalc() {
        stateLock.lock();
        try {
            head = snakeBody.peekFirst();
            newCell = head;
            newCell = changeDirection(newCell);
            randomMovement(newCell);

            checkIfFood(newCell);
            checkIfJumpPad(newCell);
            checkIfTurboBoost(newCell);
            checkIfBarrier(newCell);

            snakeBody.push(newCell);

            if (growing <= 0) {
                Cell tail = snakeBody.peekLast();
                snakeBody.remove(tail);
                Board.gameboard[tail.getX()][tail.getY()].freeCell();
            } else {
                growing--;
            }
        } finally {
            stateLock.unlock();
        }
    }

    private void checkIfBarrier(Cell newCell) {
        if (Board.gameboard[newCell.getX()][newCell.getY()].isBarrier()) {
            // crash
            System.out.println("[" + idt + "] " + "CRASHED AGAINST BARRIER "
                    + newCell.toString());
            snakeEnd=true;
        }
    }

    private void checkIfFood(Cell newCell) {
        if (Board.gameboard[newCell.getX()][newCell.getY()].isFood()) {
            growing += 3;
            System.out.println("[" + idt + "] " + "EATING " + newCell.toString());
            Board.relocateFood(newCell);
        }
    }

    private void checkIfJumpPad(Cell newCell) {
        if (Board.gameboard[newCell.getX()][newCell.getY()].isJump_pad()) {
            Board.removePowerUp(newCell, true);
            jumps++;
            System.out.println("[" + idt + "] " + "GETTING JUMP PAD " + newCell.toString());
        }
    }

    private void checkIfTurboBoost(Cell newCell) {
        if (Board.gameboard[newCell.getX()][newCell.getY()].isTurbo_boost()) {
            Board.removePowerUp(newCell, false);
            hasTurbo = true;
            System.out.println("[" + idt + "] " + "GETTING TURBO BOOST " + newCell.toString());
        }
    }

    private synchronized Cell changeDirection(Cell newCell) {
        handleBoundaries();
        try {
            switch (direction) {
                case Direction.UP:
                    if (head.getY() - 1 >= 0) {
                        newCell = Board.gameboard[head.getX()][head.getY() - 1];
                    }
                    break;
                case Direction.DOWN:
                    if (head.getY() + 1 < GridSize.GRID_HEIGHT) {
                        newCell = Board.gameboard[head.getX()][head.getY() + 1];
                    }
                    break;
                case Direction.LEFT:
                    if (head.getX() - 1 >= 0) {
                        newCell = Board.gameboard[head.getX() - 1][head.getY()];
                    }
                    break;
                case Direction.RIGHT:
                    if (head.getX() + 1 < GridSize.GRID_WIDTH) {
                        newCell = Board.gameboard[head.getX() + 1][head.getY()];
                    }
                    break;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            newCell = head;
            randomMovementAtBoundary();
        }
        return newCell;
    }

    private void handleBoundaries() {
        if (direction == Direction.UP && head.getY() <= 0) {
            randomMovementAtBoundary();
        }
        if (direction == Direction.DOWN && head.getY() >= GridSize.GRID_HEIGHT - 1) {
            randomMovementAtBoundary();
        }
        if (direction == Direction.LEFT && head.getX() <= 0) {
            randomMovementAtBoundary();
        }
        if (direction == Direction.RIGHT && head.getX() >= GridSize.GRID_WIDTH - 1) {
            randomMovementAtBoundary();
        }
    }

    private void randomMovementAtBoundary() {
        Random random = new Random();
        int newDirection;
        do {
            newDirection = random.nextInt(4) + 1;
        } while (!isValidDirection(newDirection));
        direction = newDirection;
    }

    private boolean isValidDirection(int newDirection) {
        if (direction == Direction.LEFT && newDirection == Direction.RIGHT) return false;
        if (direction == Direction.RIGHT && newDirection == Direction.LEFT) return false;
        if (direction == Direction.UP && newDirection == Direction.DOWN) return false;
        if (direction == Direction.DOWN && newDirection == Direction.UP) return false;

        switch (newDirection) {
            case Direction.UP:
                return head.getY() > 0;
            case Direction.DOWN:
                return head.getY() < GridSize.GRID_HEIGHT - 1;
            case Direction.LEFT:
                return head.getX() > 0;
            case Direction.RIGHT:
                return head.getX() < GridSize.GRID_WIDTH - 1;
            default:
                return false;
        }
    }

    private void randomMovement(Cell newCell) {
        Random random = new Random();
        int tmp = random.nextInt(4) + 1;
        if (isValidDirection(tmp)) {
            direction = tmp;
        }
    }

    public LinkedList<Cell> getBody() {
        stateLock.lock();
        try {
            return new LinkedList<>(snakeBody);
        } finally {
            stateLock.unlock();
        }
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public int getIdt() {
        return idt;
    }

    public boolean isSnakeEnd() {
        return snakeEnd;
    }
}
