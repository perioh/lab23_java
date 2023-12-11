import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.util.concurrent.*;


class GameWithGUI extends JFrame {
    private static final int SIZE = 10;
    private static final char EMPTY_CELL = ' ';
    private static final char WOLF = 'W';
    private static final char TOURIST = 'T';
    private static final char SAFE_ZONE = 'S';

    private boolean safeZoneReached=false;
    private ImageIcon wolfIcon;
    private ImageIcon touristIcon;
    private char[][] board;
    private int wolfX, wolfY, touristX, touristY, safeZoneX, safeZoneY;

    private JButton[][] buttons;

    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    public GameWithGUI() {
        initializeBoard();
        placeObjects();
        createUI();
        startGame();
    }
    private void startGame() {
        executor.submit(this::moveWolfTask);
        executor.submit(this::moveTouristTask);
    }

    private void moveWolfTask() {
        while (true) {
            moveWolf();
            updateUI();

            if (wolfX == touristX && wolfY == touristY) {
                JOptionPane.showMessageDialog(GameWithGUI.this, "Вовк впіймав туриста! Гра закінчена.");
                break;
            }
            if (safeZoneReached){
                return;
            }



            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private void moveTouristTask() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_UP) {
                    moveTourist(Direction.UP);
                } else if (keyCode == KeyEvent.VK_DOWN) {
                    moveTourist(Direction.DOWN);
                } else if (keyCode == KeyEvent.VK_LEFT) {
                    moveTourist(Direction.LEFT);
                } else if (keyCode == KeyEvent.VK_RIGHT) {
                    moveTourist(Direction.RIGHT);
                }
                updateUI();
                if (touristX == safeZoneX && touristY == safeZoneY) {
                    JOptionPane.showMessageDialog(GameWithGUI.this, "Турист дійшов до безпечної зони! Ви виграли!");
                    safeZoneReached=true;
                    return;
                }
            }
        });
    }

    private void initializeBoard() {
        board = new char[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                board[i][j] = EMPTY_CELL;
            }
        }
    }

    private void placeObjects() {
        Random random = new Random();

        touristX = random.nextInt(SIZE);
        touristY = random.nextInt(SIZE);
        board[touristX][touristY] = TOURIST;

        do {
            wolfX = random.nextInt(SIZE);
            wolfY = random.nextInt(SIZE);
        } while (board[wolfX][wolfY] != EMPTY_CELL);
        board[wolfX][wolfY] = WOLF;

        do {
            safeZoneX = random.nextInt(SIZE);
            safeZoneY = random.nextInt(SIZE);
        } while (board[safeZoneX][safeZoneY] != EMPTY_CELL);
        board[safeZoneX][safeZoneY] = SAFE_ZONE;
    }

    private void createUI() {
        setTitle("Гра");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400);

        setLayout(new GridLayout(SIZE, SIZE));
        buttons = new JButton[SIZE][SIZE];

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                var button = new JButton(String.valueOf(EMPTY_CELL));
                button.setEnabled(false);
                buttons[i][j] = button;
                add(buttons[i][j]);
            }
        }

        var localWolfIcon = new ImageIcon("wolf.png");
        var localTouristIcon = new ImageIcon("tourist.png");

        Image scaledWolfImage = localWolfIcon.getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH);
        Image scaledTouristImage = localTouristIcon.getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH);

        wolfIcon = new ImageIcon(scaledWolfImage);
        touristIcon = new ImageIcon(scaledTouristImage);

        buttons[touristX][touristY].setIcon(touristIcon);
        buttons[wolfX][wolfY].setIcon(wolfIcon);
        buttons[safeZoneX][safeZoneY].setText(String.valueOf(SAFE_ZONE));

        setFocusable(true);
    }

    private void updateUI() {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < SIZE; i++) {
                for (int j = 0; j < SIZE; j++) {
                    buttons[i][j].setText(String.valueOf(board[i][j]));
                }
            }
        });
    }


    private void moveWolf() {
            int newWolfX = wolfX;
            int newWolfY = wolfY;

            if (touristX > wolfX) {
                newWolfX++;
            } else if (touristX < wolfX) {
                newWolfX--;
            } else if (touristY > wolfY) {
                newWolfY++;
            } else if (touristY < wolfY) {
                newWolfY--;
            }

            if (newWolfX >= 0 && newWolfX < SIZE && newWolfY >= 0 && newWolfY < SIZE) {
                buttons[wolfX][wolfY].setIcon(null);
                board[wolfX][wolfY] = EMPTY_CELL;

                wolfX = newWolfX;
                wolfY = newWolfY;

                board[wolfX][wolfY] = WOLF;
                buttons[wolfX][wolfY].setIcon(wolfIcon);
        }
    }

    private void moveTourist(Direction direction) {
        int newTouristX = touristX;
        int newTouristY = touristY;

        switch (direction) {
            case UP:
                newTouristX--;
                break;
            case DOWN:
                newTouristX++;
                break;
            case LEFT:
                newTouristY--;
                break;
            case RIGHT:
                newTouristY++;
                break;
            
        }

        if (isValidMove(newTouristX, newTouristY)) {
            buttons[touristX][touristY].setIcon(null);
            board[touristX][touristY] = EMPTY_CELL;

            touristX = newTouristX;
            touristY = newTouristY;

            board[touristX][touristY] = TOURIST;
            buttons[touristX][touristY].setIcon(touristIcon);
        }
    }

    private boolean isValidMove(int x, int y) {
        return x >= 0 && x < SIZE && y >= 0 && y < SIZE && board[x][y] != WOLF;
    }

    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameWithGUI().setVisible(true));
    }
}
