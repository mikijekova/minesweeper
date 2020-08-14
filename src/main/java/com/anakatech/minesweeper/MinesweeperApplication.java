package com.anakatech.minesweeper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.*;

/*
    This class aims to recreate Minesweeper game as a Spring Boot application
 */
@SpringBootApplication
public class MinesweeperApplication {

    public static void main(String[] args) {
        SpringApplication.run(MinesweeperApplication.class, args);
        Scanner sc = new Scanner(System.in);

        int difficulty = getDifficulty(sc); //Validate entered difficulty level
        int mines = getMinesToAdd(difficulty); //Calculate the number of mines

        //Resizing the boards based on difficulty
        String[][] playerBoard = resizeBoard(difficulty); //Board which is used by user to make moves and "open" cells
        String[][] mineMaskBoard = resizeBoard(difficulty); //Board which keeps where the mines are

        clearBoard(playerBoard);
        clearBoard(mineMaskBoard);

        boolean passedFirstMove = false; //Making sure that the fist move of the user is not a Mine
        while (true) { //Makes moves until Mine or Win
            if (countEmptySpaces(playerBoard) == mines) {
                showMines(playerBoard, mineMaskBoard);
                System.out.println("\nCongratulation! You Won!");
                break;
            }

            System.out.print("Enter your move: {row} {col} -> ");
            String[] moveOnCell = sc.nextLine().split(" ");
            int row, col;
            try {
                row = Integer.parseInt(moveOnCell[0]);
                col = Integer.parseInt(moveOnCell[1]);
            } catch (NumberFormatException ex){
                System.out.printf("\nThe row and col should be between 0 and %d!\n", playerBoard.length - 1);
                continue;
            }

            if (!isInBoard(row, col, mineMaskBoard)) {
                System.out.printf("\nThe row and col should be between 0 and %d!\n", playerBoard.length - 1);
                continue;
            }

            if (!passedFirstMove) {
                //Plant mines randomly
                setMines(mines, mineMaskBoard, row, col);
                passedFirstMove = true;

                //Uncomment to see where the mines are
                printBoard(mineMaskBoard);
            }

            //If user stepped on Mine
            if (isMine(row, col, mineMaskBoard)) {
                System.out.println("\nBOOM! You Lost.");
                showMines(playerBoard, mineMaskBoard);
                printBoard(playerBoard);
                System.out.println("The End.");
                break;
            }

            Set<List<Integer>> visited = new HashSet<>();
            if (playerBoard[row][col].equals("-")) {
                countSurroundingMines(row, col, playerBoard, mineMaskBoard);

                //If the cell has no adjacent mines, recursively steps on all the safe adjacent cells
                if (playerBoard[row][col].equals("0")) {
                    if (!move(row, col, visited, playerBoard, mineMaskBoard)) {
                        break;
                    }
                }
            }
            printBoard(playerBoard);
        }
    }

    private static void showMines(String[][] playerBoard, String[][] mineMaskBoard) {
        for (int i = 0; i < mineMaskBoard.length; i++) {
            for (int j = 0; j < mineMaskBoard[i].length; j++) {
                if (mineMaskBoard[i][j].equals("*")) {
                    playerBoard[i][j] = "*";
                }
            }
        }
    }

    private static boolean move(int row, int col, Set<List<Integer>> visited, String[][] playerBoard, String[][] mineMaskBoard) {
        if (!isInBoard(row, col, mineMaskBoard)) {
            return false;
        }

        countSurroundingMines(row, col, playerBoard, mineMaskBoard);

        if (!playerBoard[row][col].equals("0")) {
            return false;
        }

        if (playerBoard[row][col].equals("0")) {
            List<Integer> currentPair = new ArrayList<>();
            currentPair.add(row);
            currentPair.add(col);

            if (!visited.contains(currentPair)) {
                visited.add(currentPair);
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (isInBoard(row + i, col + j, mineMaskBoard)) {
                            move(row + i, col + j, visited, playerBoard, mineMaskBoard);
                        }
                    }
                }
            }
        }
        return true;
    }

    private static void countSurroundingMines(int row, int col, String[][] playerBoard, String[][] minesMaskBoard) {
        int counter = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (isInBoard(row + i, col + j, minesMaskBoard) && minesMaskBoard[row + i][col + j].equals("*")) {
                    counter++;
                }
            }
        }
        playerBoard[row][col] = String.valueOf(counter);
    }

    private static int countEmptySpaces(String[][] playerBoard) {
        int counter = 0;
        for (String[] rows : playerBoard) {
            for (String col : rows) {
                if (col.equals("-")) {
                    counter++;
                }
            }
        }
        return counter;
    }

    private static boolean isInBoard(int row, int col, String[][] mineMaskBoard) {
        return row < mineMaskBoard.length && row >= 0 && col < mineMaskBoard[0].length && col >= 0;
    }

    private static void setMines(int minesToAdd, String[][] mineMaskBoard, int firstMoveRow, int firstMoveCol) {
        Random random = new Random();
        while (minesToAdd > 0) {
            int row = random.nextInt(mineMaskBoard.length);
            int col = random.nextInt(mineMaskBoard.length);
            if (row != firstMoveRow && col != firstMoveCol && !isMine(row, col, mineMaskBoard)) {
                mineMaskBoard[row][col] = "*";
                minesToAdd--;
            }
        }
    }

    private static boolean isMine(int row, int col, String[][] board) {
        try {
            return board[row][col].equals("*");
        } catch (Exception ex) {
            System.out.println("Exception: " + ex + "\nrow: " + row + " col: " + col);
            return false;
        }
    }

    private static int getDifficulty(Scanner sc) {
        int difficulty;
        do {
            System.out.println("Enter the difficulty level:\n" +
                    "Press 0 for BEGINNER (9 * 9 Board and 10 Mines)\n" +
                    "Press 1 INTERMEDIATE (16 * 16 Board and 40 Mines)\n" +
                    "Press 2 ADVANCED (24 * 24 Board and 99 Mines)");
            difficulty = Integer.parseInt(sc.nextLine());
        } while (difficulty < 0 || difficulty > 2);
        return difficulty;
    }

    private static int getMinesToAdd(int difficulty) {
        switch (difficulty) {
            case 0:
                return 10;
            case 1:
                return 40;
            case 2:
                return 99;
            default:
                throw new IllegalStateException("Unexpected value: " + difficulty);
        }
    }

    private static String[][] resizeBoard(int difficulty) {
        String[][] board;
        switch (difficulty) {
            case 0:
                board = new String[9][9];
                break;
            case 1:
                board = new String[16][16];
                break;
            case 2:
                board = new String[24][24];
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + difficulty);
        }
        return board;
    }

    private static void clearBoard(String[][] board) {
        for (String[] strings : board) {
            Arrays.fill(strings, "-");
        }
    }

    private static void printBoard(String[][] board) {
        int rowIndex = 0;
        String formatter;

        System.out.print("Current Status of Board :" + "\n\t");

        //Prints the the border row with indexes
        for (int colIndex = 0; colIndex < board.length; colIndex++) {
            formatter = String.format("%1$-3s", colIndex);
            System.out.print(formatter);
        }
        System.out.println();

        for (String[] row : board) {
            for (int col = 0; col < row.length; col++) {
                //Prints the border colon with indexes
                if (col == 0) {
                    formatter = String.format("%1$-4s", rowIndex);
                    System.out.print(formatter);
                    rowIndex++;
                }

                //Prints the board
                formatter = String.format("%1$-3s", row[col]);
                System.out.print(formatter);
            }
            System.out.println();
        }
    }
}

