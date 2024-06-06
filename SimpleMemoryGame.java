import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;

public class SimpleMemoryGame extends JPanel implements ActionListener, MouseListener, MouseMotionListener {

    private static final int FPS = 30;
    private static final int WINDOWWIDTH = 900;
    private static final int WINDOWHEIGHT = 650;
    private static final int REVEALSPEED = 5;
    private static final int BOXSIZE = 40;
    private static final int GAPSIZE = 10;

    private Timer timer;
    private int currentLevel = 0;
    private boolean[][] revealedBoxes;
    private List<ColorShapePair> mainBoard;
    private int mousex = 0;
    private int mousey = 0;
    private Point firstSelection = null;

    private final Color GRAY = new Color(100, 100, 100);
    private final Color NAVYBLUE = new Color(60, 60, 100);
    private final Color WHITE = Color.WHITE;
    private final Color RED = Color.RED;
    private final Color GREEN = Color.GREEN;
    private final Color BLUE = Color.BLUE;
    private final Color YELLOW = Color.YELLOW;
    private final Color ORANGE = new Color(255, 128, 0);
    private final Color PURPLE = new Color(255, 0, 255);
    private final Color CYAN = Color.CYAN;
    private final Color BLACK = Color.BLACK;

    private final Color BGCOLOR = BLACK;
    private final Color LIGHTBGCOLOR = GRAY;
    private final Color BOXCOLOR = WHITE;
    private final Color HIGHLIGHTCOLOR = BLUE;

    private final List<Color> ALLCOLORS = List.of(RED, GREEN, BLUE, YELLOW, ORANGE, PURPLE, CYAN);
    private final List<String> ALLSHAPES = List.of("donut", "square", "diamond", "lines", "oval");

    private static final Level[] LEVELS = {
        new Level(2, 2, "Easy"),
        new Level(3, 2, "Easy"),
        new Level(4, 3, "Easy"),
        new Level(5, 4, "Easy"),
        new Level(6, 5, "Easy"),
        new Level(7, 6, "Medium"),
        new Level(8, 7, "Medium"),
        new Level(10, 7, "Medium"),
        new Level(10, 8, "Medium"),
        new Level(10, 9, "Medium"),
        new Level(10, 10, "Hard"),
        new Level(11, 10, "Hard"),
        new Level(12, 10, "Hard"),
        new Level(13, 10, "Hard"),
        new Level(14, 10, "Hard"),
        new Level(15, 10, "Expert"),
        new Level(16, 10, "Expert"),
        new Level(17, 10, "Expert"),
        new Level(18, 10, "Expert"),
        new Level(19, 10, "Master"),
    };

    public SimpleMemoryGame() {
        setPreferredSize(new Dimension(WINDOWWIDTH, WINDOWHEIGHT));
        setBackground(BGCOLOR);
        addMouseListener(this);
        addMouseMotionListener(this);

        timer = new Timer(1000 / FPS, this);
        timer.start();

        startNewGame();
   
    }

    private void startNewGame() {
        int boardWidth = LEVELS[currentLevel].width;
        int boardHeight = LEVELS[currentLevel].height;

        revealedBoxes = new boolean[boardWidth][boardHeight];
        mainBoard = getRandomizedBoard(boardWidth, boardHeight);

        repaint();
    }

    private List<ColorShapePair> getRandomizedBoard(int boardWidth, int boardHeight) {
        List<ColorShapePair> icons = new ArrayList<>();
        for (Color color : ALLCOLORS) {
            for (String shape : ALLSHAPES) {
                icons.add(new ColorShapePair(shape, color));
            }
        }

        Collections.shuffle(icons);
        int numIconsUsed = (boardWidth * boardHeight) / 2;
        List<ColorShapePair> iconsUsed = new ArrayList<>(icons.subList(0, numIconsUsed));
        iconsUsed.addAll(iconsUsed);  // duplicate for pairs
        Collections.shuffle(iconsUsed);

        List<ColorShapePair> board = new ArrayList<>();
        for (int i = 0; i < boardWidth * boardHeight; i++) {
            board.add(iconsUsed.get(i));
        }
        return board;
    }

    private Point getBoxAtPixel(int x, int y) {
        int boardWidth = LEVELS[currentLevel].width;
        int boardHeight = LEVELS[currentLevel].height;
        int xMargin = (WINDOWWIDTH - (boardWidth * (BOXSIZE + GAPSIZE))) / 2;
        int yMargin = (WINDOWHEIGHT - (boardHeight * (BOXSIZE + GAPSIZE))) / 2;

        for (int boxx = 0; boxx < boardWidth; boxx++) {
            for (int boxy = 0; boxy < boardHeight; boxy++) {
                int left = boxx * (BOXSIZE + GAPSIZE) + xMargin;
                int top = boxy * (BOXSIZE + GAPSIZE) + yMargin;
                Rectangle boxRect = new Rectangle(left, top, BOXSIZE, BOXSIZE);
                if (boxRect.contains(x, y)) {
                    return new Point(boxx, boxy);
                }
            }
        }
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBoard(g);
        drawLevel(g, currentLevel + 1, LEVELS[currentLevel].category);
    }

    private void drawBoard(Graphics g) {
        int boardWidth = LEVELS[currentLevel].width;
        int boardHeight = LEVELS[currentLevel].height;
        int xMargin = (WINDOWWIDTH - (boardWidth * (BOXSIZE + GAPSIZE))) / 2;
        int yMargin = (WINDOWHEIGHT - (boardHeight * (BOXSIZE + GAPSIZE))) / 2;

        for (int boxx = 0; boxx < boardWidth; boxx++) {
            for (int boxy = 0; boxy < boardHeight; boxy++) {
                int left = boxx * (BOXSIZE + GAPSIZE) + xMargin;
                int top = boxy * (BOXSIZE + GAPSIZE) + yMargin;
                if (!revealedBoxes[boxx][boxy]) {
                    g.setColor(BOXCOLOR);
                    g.fillRect(left, top, BOXSIZE, BOXSIZE);
                } else {
                    ColorShapePair pair = mainBoard.get(boxx + boxy * boardWidth);
                    drawIcon(g, pair.shape, pair.color, left, top);
                }
            }
        }
    }

    private void drawIcon(Graphics g, String shape, Color color, int left, int top) {
        int quarter = BOXSIZE / 4;
        int half = BOXSIZE / 2;

        g.setColor(color);
        switch (shape) {
            case "donut":
                g.fillOval(left, top, BOXSIZE, BOXSIZE);
                g.setColor(BGCOLOR);
                g.fillOval(left + quarter, top + quarter, half, half);
                break;
            case "square":
                g.fillRect(left + quarter, top + quarter, half, half);
                break;
            case "diamond":
                Polygon diamond = new Polygon();
                diamond.addPoint(left + half, top);
                diamond.addPoint(left + BOXSIZE, top + half);
                diamond.addPoint(left + half, top + BOXSIZE);
                diamond.addPoint(left, top + half);
                g.fillPolygon(diamond);
                break;
            case "lines":
                for (int i = 0; i < BOXSIZE; i += 4) {
                    g.drawLine(left, top + i, left + i, top);
                    g.drawLine(left + i, top + BOXSIZE, left + BOXSIZE, top + i);
                }
                break;
            case "oval":
                g.fillOval(left, top + quarter, BOXSIZE, half);
                break;
        }
    }

    private void drawLevel(Graphics g, int level, String category) {
        g.setColor(WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.drawString("Level: " + level + " (" + category + ")", 10, 30);
    }

    private boolean hasWon() {
        for (boolean[] row : revealedBoxes) {
            for (boolean box : row) {
                if (!box) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Point box = getBoxAtPixel(e.getX(), e.getY());
        if (box != null && !revealedBoxes[box.x][box.y]) {
            if (firstSelection == null) {
                firstSelection = box;
                revealedBoxes[box.x][box.y] = true;
            } else {
                revealedBoxes[box.x][box.y] = true;
                ColorShapePair icon1 = mainBoard.get(firstSelection.x + firstSelection.y * LEVELS[currentLevel].width);
                ColorShapePair icon2 = mainBoard.get(box.x + box.y * LEVELS[currentLevel].width);
                if (!icon1.equals(icon2)) {
                    Timer delay = new Timer(1000, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent evt) {
                            revealedBoxes[firstSelection.x][firstSelection.y] = false;
                            revealedBoxes[box.x][box.y] = false;
                            firstSelection = null;
                            repaint();
                        }
                    });
                    delay.setRepeats(false);
                    delay.start();
                } else {
                    firstSelection = null;
                    if (hasWon()) {
                        Timer delay = new Timer(2000, new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent evt) {
                                currentLevel++;
                                if (currentLevel >= LEVELS.length) {
                                    currentLevel = 0;
                                }
                                startNewGame();
                            }
                        });
                        delay.setRepeats(false);
                        delay.start();
                    }
                }
            }
        }
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        mousex = e.getX();
        mousey = e.getY();
        repaint();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Simple Memory Game");
        SimpleMemoryGame gamePanel = new SimpleMemoryGame();
        frame.add(gamePanel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static class ColorShapePair {
        String shape;
        Color color;

        ColorShapePair(String shape, Color color) {
            this.shape = shape;
            this.color = color;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            ColorShapePair that = (ColorShapePair) obj;
            return shape.equals(that.shape) && color.equals(that.color);
        }
    }

    private static class Level {
        int width, height;
        String category;

        Level(int width, int height, String category) {
            this.width = width;
            this.height = height;
            this.category = category;
        }
    }
}
