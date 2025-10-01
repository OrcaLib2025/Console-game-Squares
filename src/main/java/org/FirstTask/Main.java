package org.FirstTask;

import java.util.*;

class App {
    public static void main(String[] args) {
        new SquareGame().run();
    }
}

class SquareGame {
    private int boardSize;
    private char[][] board;
    private Player[] players;
    private int currentPlayerIndex;
    private boolean gameStarted;
    private boolean gameFinished;
    private Set<String> freeCells;

    class Player {
        String type;
        char color;
        Player(String type, char color) { this.type = type; this.color = color; }
    }

    public void run() {
        System.out.println("=== SQUARE GAME ===");
        System.out.println("Type HELP for commands");
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("> ");
            String command = scanner.nextLine().trim();

            if (command.equalsIgnoreCase("exit")) {
                break;
            } else if (command.equalsIgnoreCase("help")) {
                showHelp();
            } else if (command.toUpperCase().startsWith("GAME")) {
                parseGameCommand(command);
            } else if (command.toUpperCase().startsWith("MOVE")) {
                parseMoveCommand(command);
            } else if (command.equalsIgnoreCase("BOARD")) {
                printBoard();
            } else if (!command.isEmpty()) {
                System.out.println("Incorrect command");
            }
        }
        scanner.close();
    }

    private void showHelp() {
        System.out.println("=== COMMANDS ===");
        System.out.println("GAME N, U1, U2 - Start new game");
        System.out.println("  N - board size (integer > 2)");
        System.out.println("  U1, U2 - players in format 'TYPE C'");
        System.out.println("  TYPE: 'user' or 'comp'");
        System.out.println("  C: 'W' (white) or 'B' (black)");
        System.out.println("Examples:");
        System.out.println("  GAME 5, user W, comp B");
        System.out.println("  GAME 6, comp W, user B");
        System.out.println("MOVE X, Y - Make a move at coordinates X, Y");
        System.out.println("BOARD - Show current board");
        System.out.println("EXIT - Exit program");
        System.out.println("HELP - Show this help");
    }

    private void parseGameCommand(String command) {
        String[] parts = command.split("\\s+");
        if (parts.length < 4) {
            System.out.println("Incorrect command format");
            return;
        }

        try {
            int n = Integer.parseInt(parts[1].replace(",", ""));
            if (n <= 2) {
                System.out.println("Board size must be greater than 2");
                return;
            }

            String rest = command.substring(command.indexOf(parts[1]) + parts[1].length()).trim();
            String[] playerParts = rest.split(",");

            if (playerParts.length < 2) {
                System.out.println("Incorrect players format");
                return;
            }

            Player player1 = parsePlayer(playerParts[0].trim());
            Player player2 = parsePlayer(playerParts[1].trim());

            if (player1.color == player2.color) {
                System.out.println("Players must have different colors");
                return;
            }

            startNewGame(n, new Player[]{player1, player2});

        } catch (Exception e) {
            System.out.println("Incorrect command");
        }
    }

    private Player parsePlayer(String playerStr) {
        String[] parts = playerStr.split("\\s+");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid player format: " + playerStr);
        }

        String type = parts[0].toLowerCase();
        char color = parts[1].toUpperCase().charAt(0);

        if (!type.equals("user") && !type.equals("comp")) {
            throw new IllegalArgumentException("Unknown player type: " + type);
        }
        if (color != 'W' && color != 'B') {
            throw new IllegalArgumentException("Color must be W or B");
        }

        return new Player(type, color);
    }

    private void startNewGame(int n, Player[] players) {
        this.boardSize = n;
        this.players = players;
        this.currentPlayerIndex = 0;
        this.gameStarted = true;
        this.gameFinished = false;

        this.board = new char[n][n];
        this.freeCells = new HashSet<>();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                board[i][j] = '.';
                freeCells.add(j + "," + i);
            }
        }

        System.out.println("New game started");
        printBoard();

        if (players[0].type.equals("comp")) {
            makeComputerMove();
        }
    }

    private void printBoard() {
        if (!gameStarted) {
            System.out.println("Game not started");
            return;
        }

        System.out.println("\n=== BOARD " + boardSize + "x" + boardSize + " ===");

        int cellWidth = String.valueOf(boardSize).length();
        String numberFormat = "%" + cellWidth + "d";
        String emptySpace = " ".repeat(cellWidth);

        System.out.print(emptySpace + " ");
        for (int i = 1; i <= boardSize; i++) {
            System.out.print(" " + String.format(numberFormat, i));
        }
        System.out.println();

        // Верхняя граница
        int borderLength = boardSize * (cellWidth + 1) + 1;
        System.out.print(emptySpace + " ┌");
        for (int i = 0; i < borderLength; i++) {
            System.out.print("─");
        }
        System.out.println("┐");

        // Строки доски
        for (int i = 0; i < boardSize; i++) {
            System.out.printf("%" + cellWidth + "d │", i + 1);

            // Ячейки доски
            for (int j = 0; j < boardSize; j++) {
                char cell = board[i][j];
                String symbol = ".";
                if (cell == 'W') symbol = "○";
                if (cell == 'B') symbol = "●";

                String formattedSymbol = " " + symbol;
                if (cellWidth > 1) {
                    formattedSymbol = " " + symbol + " ".repeat(cellWidth - 1);
                }
                System.out.print(formattedSymbol);
            }

            // Номер строки справа
            System.out.println(" │" + String.format(numberFormat, i + 1));
        }

        // Нижняя граница
        System.out.print(emptySpace + " └");
        for (int i = 0; i < borderLength; i++) {
            System.out.print("─");
        }
        System.out.println("┘");

        // Нижние номера столбцов
        System.out.print(emptySpace + " ");
        for (int i = 1; i <= boardSize; i++) {
            System.out.print(" " + String.format(numberFormat, i));
        }
        System.out.println();

        // Легенда
        System.out.println("Legend: ○ = White (W), ● = Black (B), . = Empty");

        // Текущий игрок
        if (!gameFinished) {
            Player current = players[currentPlayerIndex];
            System.out.println("Current turn: " + current.color + " (" + current.type + ")");
        }
        System.out.println();
    }

    private void parseMoveCommand(String command) {
        if (!gameStarted) {
            System.out.println("Game not started");
            return;
        }
        if (gameFinished) {
            System.out.println("Game already finished");
            return;
        }

        try {
            String[] parts = command.split("\\s+");
            int x = Integer.parseInt(parts[1].replace(",", ""));
            int y = Integer.parseInt(parts[2]);
            makeUserMove(x, y);
        } catch (Exception e) {
            System.out.println("Incorrect command");
        }
    }

    private void makeUserMove(int x, int y) {
        Player current = players[currentPlayerIndex];
        if (!current.type.equals("user")) {
            System.out.println("Not user's turn");
            return;
        }
        if (!isValidMove(x, y)) {
            System.out.println("Invalid move");
            return;
        }
        makeMove((x - 1), (y - 1), current.color);
    }

    private void makeComputerMove() {
        if (!gameStarted || gameFinished) return;

        Player current = players[currentPlayerIndex];
        if (!current.type.equals("comp")) return;

        if (!freeCells.isEmpty()) {
            String cell = freeCells.iterator().next();
            String[] coords = cell.split(",");
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            makeMove(x, y, current.color);
            System.out.println(current.color + " (" + (x + 1) + ", " + (y + 1) + ")");
        }
    }

    private void makeMove(int x, int y, char color) {
        board[y][x] = color;
        freeCells.remove(x + "," + y);

        System.out.println("Move made: " + color + " at (" + (x + 1) + ", " + (y + 1) + ")");
        printBoard();

        if (checkWin(color)) {
            gameFinished = true;
            System.out.println("🎉 Game finished. " + color + " wins! 🎉");
            return;
        }

        if (freeCells.isEmpty()) {
            gameFinished = true;
            System.out.println("Game finished. Draw");
            return;
        }

        currentPlayerIndex = (currentPlayerIndex + 1) % 2;

        if (!gameFinished && players[currentPlayerIndex].type.equals("comp")) {
            makeComputerMove();
        }
    }

    private boolean isValidMove(int x, int y) {
        return x >= 1 && x <= boardSize && y >= 1 && y <= boardSize && board[y-1][x-1] == '.';
    }

    private boolean checkWin(char color) {
        List<int[]> pieces = new ArrayList<>();
        for (int y = 0; y < boardSize; y++) {
            for (int x = 0; x < boardSize; x++) {
                if (board[y][x] == color) {
                    pieces.add(new int[]{x, y});
                }
            }
        }
        if (pieces.size() < 4) return false;

        for (int i = 0; i < pieces.size(); i++) {
            for (int j = i + 1; j < pieces.size(); j++) {
                for (int k = j + 1; k < pieces.size(); k++) {
                    for (int l = k + 1; l < pieces.size(); l++) {
                        if (isSquare(pieces.get(i), pieces.get(j), pieces.get(k), pieces.get(l))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isSquare(int[] p1, int[] p2, int[] p3, int[] p4) {
        List<Long> distances = new ArrayList<>();
        int[][] points = {p1, p2, p3, p4};

        for (int i = 0; i < 4; i++) {
            for (int j = i + 1; j < 4; j++) {
                long dx = points[i][0] - points[j][0];
                long dy = points[i][1] - points[j][1];
                distances.add(dx * dx + dy * dy);
            }
        }

        Collections.sort(distances);
        return distances.get(0) > 0 &&
                distances.get(0).equals(distances.get(1)) &&
                distances.get(1).equals(distances.get(2)) &&
                distances.get(2).equals(distances.get(3)) &&
                distances.get(4).equals(distances.get(5));
    }
}