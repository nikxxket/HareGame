import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class HareGameGUI {
    private static final int FIELD_SIZE = 51;
    private static final int VISIBILITY_RANGE = 2;
    private static final char EMPTY = '.';
    private static final char HARE = 'H';
    private static final char WOLF = 'W';
    private static final char FLOWER = 'F';

    private char[][] field;
    private Hare[] hares;
    private Wolf[] wolves;
    private Flower[] flowers;
    private Random random;
    private int totalFlowersCollected;
    private int turn;

    // GUI компоненты
    private JFrame frame;
    private JPanel gridPanel;
    private JTextArea statsArea;
    private JButton nextTurnButton;
    private JLabel turnLabel;
    private JLabel[][] cellLabels;

    public HareGameGUI() {
        random = new Random();
        totalFlowersCollected = 0;
        turn = 1;
        initializeField();
        initializeGameObjects();
        createGUI();
    }

    private void initializeField() {
        field = new char[FIELD_SIZE][FIELD_SIZE];
        for (int i = 0; i < FIELD_SIZE; i++) {
            for (int j = 0; j < FIELD_SIZE; j++) {
                field[i][j] = EMPTY;
            }
        }
    }

    private void initializeGameObjects() {
        // Создаем 60 зайцев
        hares = new Hare[60];
        for (int i = 0; i < hares.length; i++) {
            int x, y;
            do {
                x = random.nextInt(FIELD_SIZE);
                y = random.nextInt(FIELD_SIZE);
            } while (field[y][x] != EMPTY);

            hares[i] = new Hare(x, y);
            field[y][x] = HARE;
        }

        // Создаем 30 волков
        wolves = new Wolf[30];
        for (int i = 0; i < wolves.length; i++) {
            int x, y;
            do {
                x = random.nextInt(FIELD_SIZE);
                y = random.nextInt(FIELD_SIZE);
            } while (field[y][x] != EMPTY);

            wolves[i] = new Wolf(x, y);
            field[y][x] = WOLF;
        }

        // Создаем 500 цветков
        flowers = new Flower[500];
        for (int i = 0; i < flowers.length; i++) {
            int x, y;
            do {
                x = random.nextInt(FIELD_SIZE);
                y = random.nextInt(FIELD_SIZE);
            } while (field[y][x] != EMPTY);

            flowers[i] = new Flower(x, y);
            field[y][x] = FLOWER;
        }
    }

    private void createGUI() {
        frame = new JFrame("Симуляция зайцев и волков");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        // Панель заголовка
        JPanel headerPanel = new JPanel();
        turnLabel = new JLabel("ХОД: 1");
        turnLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(turnLabel);

        // Основная панель с полем и статистикой
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

        // Поле игры в виде сетки
        JPanel fieldPanel = new JPanel(new BorderLayout());
        fieldPanel.setBorder(BorderFactory.createTitledBorder("Игровое поле (50x50)"));

        gridPanel = new JPanel(new GridLayout(FIELD_SIZE, FIELD_SIZE, 1, 1));
        gridPanel.setBackground(Color.BLACK);
        cellLabels = new JLabel[FIELD_SIZE][FIELD_SIZE];

        // Создаем ячейки сетки
        for (int y = 0; y < FIELD_SIZE; y++) {
            for (int x = 0; x < FIELD_SIZE; x++) {
                JLabel cell = new JLabel("", SwingConstants.CENTER);
                cell.setPreferredSize(new Dimension(15, 15));
                cell.setMinimumSize(new Dimension(15, 15));
                cell.setOpaque(true);
                cell.setBackground(Color.WHITE);
                cell.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
                cell.setFont(new Font("Arial", Font.BOLD, 10));

                cellLabels[y][x] = cell;
                gridPanel.add(cell);
            }
        }

        JScrollPane gridScroll = new JScrollPane(gridPanel);
        gridScroll.setPreferredSize(new Dimension(800, 600));
        fieldPanel.add(gridScroll, BorderLayout.CENTER);

        // Панель статистики
        JPanel statsPanel = new JPanel(new BorderLayout());
        statsPanel.setBorder(BorderFactory.createTitledBorder("Статистика"));
        statsPanel.setPreferredSize(new Dimension(300, 0));

        statsArea = new JTextArea(20, 25);
        statsArea.setFont(new Font("Arial", Font.PLAIN, 12));
        statsArea.setEditable(false);
        statsArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane statsScroll = new JScrollPane(statsArea);
        statsPanel.add(statsScroll, BorderLayout.CENTER);

        mainPanel.add(fieldPanel, BorderLayout.CENTER);
        mainPanel.add(statsPanel, BorderLayout.EAST);

        // Панель кнопок
        JPanel buttonPanel = new JPanel();
        nextTurnButton = new JButton("Следующий ход");
        nextTurnButton.setFont(new Font("Arial", Font.BOLD, 14));
        nextTurnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nextTurn();
            }
        });

        buttonPanel.add(nextTurnButton);

        frame.add(headerPanel, BorderLayout.NORTH);
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        updateDisplay();
    }

    private void nextTurn() {
        turnLabel.setText("ХОД: " + turn);

        // Двигаем всех зайцев
        moveHares();

        // Двигаем всех волков
        moveWolves();

        // Проверяем столкновения
        checkCollisions();

        // Обновляем отображение
        updateDisplay();

        // Проверяем окончание игры
        if (isGameOver()) {
            String message;
            if (areAllHaresDead()) {
                message = "Игра окончена! Все зайцы вымерли на ходе " + turn + ".";
            } else {
                message = "Игра окончена! Все волки вымерли на ходе " + turn + ".";
            }
            JOptionPane.showMessageDialog(frame, message);
            nextTurnButton.setEnabled(false);
        }

        turn++;
    }

    private void autoPlay(int turns) {
        nextTurnButton.setEnabled(false);

        new Thread(() -> {
            for (int i = 0; i < turns; i++) {
                if (isGameOver()) break;

                SwingUtilities.invokeLater(() -> {
                    turnLabel.setText("ХОД: " + turn);
                    moveHares();
                    moveWolves();
                    checkCollisions();
                    updateDisplay();
                    turn++;
                });

                try {
                    Thread.sleep(500); // Задержка для визуализации
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            SwingUtilities.invokeLater(() -> {
                nextTurnButton.setEnabled(true);
                if (isGameOver()) {
                    String message;
                    if (areAllHaresDead()) {
                        message = "Игра окончена! Все зайцы вымерли на ходе " + (turn-1) + ".";
                    } else {
                        message = "Игра окончена! Все волки вымерли на ходе " + (turn-1) + ".";
                    }
                    JOptionPane.showMessageDialog(frame, message);
                    nextTurnButton.setEnabled(false);
                }
            });
        }).start();
    }

    private void moveHares() {
        for (Hare hare : hares) {
            if (hare != null && hare.isAlive()) {
                int currentX = hare.getX();
                int currentY = hare.getY();

                // Ищем ближайшего волка в радиусе видимости
                Wolf nearestWolf = findNearestWolf(currentX, currentY);

                int newX = currentX;
                int newY = currentY;

                if (nearestWolf != null) {
                    // Убегаем от волка
                    if (currentX < nearestWolf.getX()) newX--;
                    else if (currentX > nearestWolf.getX()) newX++;

                    if (currentY < nearestWolf.getY()) newY--;
                    else if (currentY > nearestWolf.getY()) newY++;
                } else {
                    // Если волков нет поблизости, ищем ближайший цветок
                    Flower nearestFlower = findNearestFlower(currentX, currentY);

                    if (nearestFlower != null && !nearestFlower.isCollected()) {
                        // Двигаемся к цветку
                        if (currentX < nearestFlower.getX()) newX++;
                        else if (currentX > nearestFlower.getX()) newX--;

                        if (currentY < nearestFlower.getY()) newY++;
                        else if (currentY > nearestFlower.getY()) newY--;
                    } else {
                        // Случайное движение, если цветков тоже нет
                        int direction = random.nextInt(4);
                        switch (direction) {
                            case 0: newX++; break; // вправо
                            case 1: newX--; break; // влево
                            case 2: newY++; break; // вниз
                            case 3: newY--; break; // вверх
                        }
                    }
                }

                // Проверяем валидность хода
                if (isValidMove(newX, newY)) {
                    // Проверяем, есть ли на новой позиции цветок ДО движения
                    boolean wasFlower = field[newY][newX] == FLOWER;

                    // Двигаем зайца
                    moveAnimal(hare, newX, newY);

                    // Если на новой позиции был цветок - съедаем его
                    if (wasFlower) {
                        hare.eatFlower();
                        totalFlowersCollected++;
                        removeFlower(newX, newY);
                    }
                }

                // Тратим энергию
                if (!hare.useEnergy(1)) {
                    hare.die();
                    field[hare.getY()][hare.getX()] = EMPTY;
                }
            }
        }
    }

    private void moveWolves() {
        for (Wolf wolf : wolves) {
            if (wolf != null && wolf.isAlive()) {
                int currentX = wolf.getX();
                int currentY = wolf.getY();

                // Ищем ближайшего зайца в радиусе видимости
                Hare nearestHare = findNearestHare(currentX, currentY);

                int newX = currentX;
                int newY = currentY;

                if (nearestHare != null && nearestHare.isAlive()) {
                    // Преследуем зайца
                    if (currentX < nearestHare.getX()) newX++;
                    else if (currentX > nearestHare.getX()) newX--;

                    if (currentY < nearestHare.getY()) newY++;
                    else if (currentY > nearestHare.getY()) newY--;
                } else {
                    // Случайное движение, если зайцев нет поблизости
                    int direction = random.nextInt(4);
                    switch (direction) {
                        case 0: newX++; break;
                        case 1: newX--; break;
                        case 2: newY++; break;
                        case 3: newY--; break;
                    }
                }

                // Проверяем валидность хода и двигаем волка
                if (isValidMove(newX, newY) && (field[newY][newX] == EMPTY || field[newY][newX] == FLOWER || field[newY][newX] == HARE)) {
                    moveAnimal(wolf, newX, newY);
                }
                // Тратим энергию волка
                if (!wolf.useEnergy(1)) {
                    wolf.die();
                    field[wolf.getY()][wolf.getX()] = EMPTY;
                }
            }
        }
    }

    private Wolf findNearestWolf(int x, int y) {
        Wolf nearest = null;
        int minDistance = VISIBILITY_RANGE + 1;

        for (Wolf wolf : wolves) {
            if (wolf != null && wolf.isAlive()) {
                int distance = Math.abs(wolf.getX() - x) + Math.abs(wolf.getY() - y);
                if (distance <= VISIBILITY_RANGE && distance < minDistance) {
                    minDistance = distance;
                    nearest = wolf;
                }
            }
        }

        return nearest;
    }

    private Hare findNearestHare(int x, int y) {
        Hare nearest = null;
        int minDistance = VISIBILITY_RANGE + 1;

        for (Hare hare : hares) {
            if (hare != null && hare.isAlive()) {
                int distance = Math.abs(hare.getX() - x) + Math.abs(hare.getY() - y);
                if (distance <= VISIBILITY_RANGE && distance < minDistance) {
                    minDistance = distance;
                    nearest = hare;
                }
            }
        }

        return nearest;
    }

    private Flower findNearestFlower(int x, int y) {
        Flower nearest = null;
        int minDistance = VISIBILITY_RANGE + 1;

        for (Flower flower : flowers) {
            if (flower != null && !flower.isCollected()) {
                int distance = Math.abs(flower.getX() - x) + Math.abs(flower.getY() - y);
                if (distance <= VISIBILITY_RANGE && distance < minDistance) {
                    minDistance = distance;
                    nearest = flower;
                }
            }
        }

        return nearest;
    }

    private void moveAnimal(Object animal, int newX, int newY) {
        if (animal instanceof Hare) {
            Hare hare = (Hare) animal;
            field[hare.getY()][hare.getX()] = EMPTY;
            hare.move(newX, newY);
            field[newY][newX] = HARE;
        } else if (animal instanceof Wolf) {
            Wolf wolf = (Wolf) animal;
            field[wolf.getY()][wolf.getX()] = EMPTY;
            wolf.move(newX, newY);
            field[newY][newX] = WOLF;
        }
    }

    private void removeFlower(int x, int y) {
        for (Flower flower : flowers) {
            if (flower != null && !flower.isCollected() &&
                    flower.getX() == x && flower.getY() == y) {
                flower.collect();
                break;
            }
        }
    }

    private void checkCollisions() {
        // Проверяем, поймали ли волки зайцев
        for (Wolf wolf : wolves) {
            if (wolf != null && wolf.isAlive()) {
                for (Hare hare : hares) {
                    if (hare != null && hare.isAlive() &&
                            hare.getX() == wolf.getX() && hare.getY() == wolf.getY()) {
                        hare.die();
                        wolf.eatHare();
                        field[hare.getY()][hare.getX()] = WOLF;
                    }
                }
            }
        }
    }

    private boolean isGameOver() {
        return areAllHaresDead() || areAllWolvesDead();
    }

    private boolean areAllHaresDead() {
        for (Hare hare : hares) {
            if (hare != null && hare.isAlive()) {
                return false;
            }
        }
        return true;
    }

    private boolean areAllWolvesDead() {
        for (Wolf wolf : wolves) {
            if (wolf != null && wolf.isAlive()) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidMove(int x, int y) {
        return x >= 0 && x < FIELD_SIZE && y >= 0 && y < FIELD_SIZE;
    }

    private void updateDisplay() {
        updateGridDisplay();
        updateStatsDisplay();
    }

    private void updateGridDisplay() {
        for (int y = 0; y < FIELD_SIZE; y++) {
            for (int x = 0; x < FIELD_SIZE; x++) {
                JLabel cell = cellLabels[y][x];
                char cellContent = field[y][x];

                switch (cellContent) {
                    case HARE:
                        cell.setText("H");
                        cell.setBackground(new Color(200, 255, 200)); // Светло-зеленый для зайцев
                        cell.setForeground(Color.DARK_GRAY);
                        break;
                    case WOLF:
                        cell.setText("W");
                        cell.setBackground(new Color(255, 200, 200)); // Светло-красный для волков
                        cell.setForeground(Color.DARK_GRAY);
                        break;
                    case FLOWER:
                        cell.setText("F");
                        cell.setBackground(new Color(255, 255, 200)); // Светло-желтый для цветов
                        cell.setForeground(Color.DARK_GRAY);
                        break;
                    case EMPTY:
                        cell.setText("");
                        cell.setBackground(Color.WHITE);
                        break;
                }

                // Добавляем всплывающую подсказку с координатами
                cell.setToolTipText("Координаты: (" + x + ", " + y + ")");
            }
        }
    }

    private void updateStatsDisplay() {
        int aliveHares = 0;
        int aliveWolves = 0;
        int totalHareEnergy = 0;
        int totalWolfEnergy = 0;
        int remainingFlowers = 0;

        // Считаем живых зайцев и их энергию
        for (Hare hare : hares) {
            if (hare != null && hare.isAlive()) {
                aliveHares++;
                totalHareEnergy += hare.getEnergy();
            }
        }

        // Считаем живых волков и их энергию
        for (Wolf wolf : wolves) {
            if (wolf != null && wolf.isAlive()) {
                aliveWolves++;
                totalWolfEnergy += wolf.getEnergy();
            }
        }

        // Считаем оставшиеся цветы
        for (Flower flower : flowers) {
            if (flower != null && !flower.isCollected()) {
                remainingFlowers++;
            }
        }

        StringBuilder stats = new StringBuilder();
        stats.append("=== ХОД ").append(turn).append(" ===\n\n");

        stats.append("ОБЩАЯ СТАТИСТИКА:\n");
        stats.append("────────────────\n");
        stats.append("Живых зайцев: ").append(aliveHares).append("/").append(hares.length).append("\n");
        stats.append("Живых волков: ").append(aliveWolves).append("/").append(wolves.length).append("\n");
        stats.append("Всего цветов собрано: ").append(totalFlowersCollected).append("\n");
        stats.append("Цветов осталось: ").append(remainingFlowers).append("\n\n");

        stats.append("ЭНЕРГИЯ:\n");
        stats.append("────────────────\n");
        stats.append("Средняя энергия зайцев: ").append(aliveHares > 0 ? totalHareEnergy/aliveHares : 0).append("\n");
        stats.append("Средняя энергия волков: ").append(aliveWolves > 0 ? totalWolfEnergy/aliveWolves : 0).append("\n\n");

        stats.append("ЛЕГЕНДА:\n");
        stats.append("────────────────\n");
        stats.append("H - Заяц \n");
        stats.append("W - Волк \n");
        stats.append("F - Цветок\n");

        statsArea.setText(stats.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new HareGameGUI();
            }
        });
    }
}

class Hare {
    private int x;
    private int y;
    private int energy;
    private int score;
    private boolean alive;

    public Hare(int x, int y) {
        this.x = x;
        this.y = y;
        this.energy = 10;
        this.score = 0;
        this.alive = true;
    }

    public void move(int newX, int newY) {
        this.x = newX;
        this.y = newY;
    }

    public void eatFlower() {
        energy += 2;
        score++;
    }

    public boolean useEnergy(int amount) {
        if (energy >= amount) {
            energy -= amount;
            return true;
        }
        return false;
    }

    public void die() {
        alive = false;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getEnergy() { return energy; }
    public int getScore() { return score; }
    public boolean isAlive() { return alive; }
}

class Wolf {
    private int x;
    private int y;
    private int energy;
    private boolean alive;

    public Wolf(int x, int y) {
        this.x = x;
        this.y = y;
        this.energy = 10;
        this.alive = true;
    }

    public void move(int newX, int newY) {
        this.x = newX;
        this.y = newY;
    }

    public void eatHare() {
        energy += 2;
    }

    public boolean useEnergy(int amount) {
        if (energy >= amount) {
            energy -= amount;
            return true;
        }
        return false;
    }

    public void die() {
        alive = false;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getEnergy() { return energy; }
    public boolean isAlive() { return alive; }
}

class Flower {
    private int x;
    private int y;
    private boolean collected;

    public Flower(int x, int y) {
        this.x = x;
        this.y = y;
        this.collected = false;
    }

    public void collect() {
        collected = true;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isCollected() { return collected; }
}