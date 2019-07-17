import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;

/**
 * Created by seanyeo on 2019-06-21.
 */
public class Gomoku extends Canvas implements MouseListener {
    public static void main(String[] args) {
        JFrame game = new JFrame("Gomoku");
        game.setSize(1000, 1000);
        game.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        game.add(new Gomoku());
        game.setVisible(true);
    }

    private GameBoard gb;
    private int[][] pieces;
    private boolean firstMove = true;

    //boolean gameOver = false;
    boolean blackWins = false;
    boolean whiteWins = false;

    private final int NONE = 0;
    private final int BLACK = 1;
    private final int WHITE = 2;

    private int player = WHITE;
    private int other = BLACK;

    private Queue<TileCoordinate> whiteCoords = new LinkedList<>();
    private Queue<TileCoordinate> blackCoords = new LinkedList<>();

    private boolean whiteJustPlayed;
    private boolean blackJustPlayed;

    private int numBlackPiecesOnBoard;
    private int numWhitePiecesOnBoard;
    private boolean tie = false;

    private TileCoordinate ultimateCoordinate = new TileCoordinate();
    private HashMap<Integer[][], Integer> memoize = new HashMap<>(1000);//store Gameboard values for DP

    public Gomoku() {
        addMouseListener(this);
        gb = new GameBoard(10);
        pieces = new int[10][10];

        //pieces[0][0] = 2;

    }

    public void paint(Graphics g) {
        g.setColor(new Color(139, 69, 19));
        g.fillRect(0, 0, 1000, 1000);
        g.setColor(Color.BLACK);
        Graphics2D g2 = (Graphics2D) g;
        g.setFont(new Font("Arial", Font.BOLD, 48));

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                g2.draw(gb.getSpace(i, j));
            }
        }

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (pieces[i][j] > 0) {
                    if (pieces[i][j] == BLACK) {
                        g.setColor(Color.BLACK);
                    } else if (pieces[i][j] == WHITE) {
                        g.setColor(Color.WHITE);
                    }

                    Rectangle temp_space = gb.getSpace(i, j);
                    g.fillOval(temp_space.x + 8, temp_space.y + 8, temp_space.width - 16, temp_space.height - 16);
                }
            }
        }


        if (firstMove) {
            getAIMove();
            repaint();
            firstMove = false;
        }
        if (!firstMove && whiteJustPlayed && !gameOver(pieces)) {
            getAIMove();
            //repaint();
        }
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (pieces[i][j] > 0) {
                    if (pieces[i][j] == BLACK) {
                        g.setColor(Color.BLACK);
                    } else if (pieces[i][j] == WHITE) {
                        g.setColor(Color.WHITE);
                    }

                    Rectangle temp_space = gb.getSpace(i, j);
                    g.fillOval(temp_space.x + 8, temp_space.y + 8, temp_space.width - 16, temp_space.height - 16);
                }
            }
        }


        numBlackPiecesOnBoard = 0;
        numWhitePiecesOnBoard = 0;
        //isGameOver();
        if (gameOver(pieces) && !tie) {
            if (blackWins) {
                g.setColor(Color.BLACK);
                g.drawString("Game Over", 675, 300);
                g.drawString("winner is black", 635, 400);
                g.setFont(new Font("Arial", Font.BOLD, 24));
                g.drawString("Click anywhere to play again", 635, 500);
            } else if (whiteWins) {
                g.setColor(Color.white);
                g.drawString("Game Over", 675, 300);
                g.drawString("winner is white", 635, 400);
                g.setFont(new Font("Arial", Font.BOLD, 24));
                g.drawString("Click anywhere to play again", 635, 500);
            }
        }
        numBlackPiecesOnBoard = 0;
        numWhitePiecesOnBoard = 0;
        if (gameOver(pieces) && tie) {
            g.setColor(Color.gray);
            g.drawString("Tie Game", 675, 300);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Click anywhere to play again", 635, 500);
        }





//        numBlackPiecesOnBoard = 0;
//        numWhitePiecesOnBoard = 0;
//        //isGameOver();
//        if (gameOver(pieces) && !tie) {
//            if (blackWins) {
//                g.setColor(Color.BLACK);
//                g.drawString("Game Over", 675, 300);
//                g.drawString("winner is black", 635, 400);
//                g.setFont(new Font("Arial", Font.BOLD, 24));
//                g.drawString("Click anywhere to play again", 635, 500);
//            } else if (whiteWins) {
//                g.setColor(Color.white);
//                g.drawString("Game Over", 675, 300);
//                g.drawString("winner is white", 635, 400);
//                g.setFont(new Font("Arial", Font.BOLD, 24));
//                g.drawString("Click anywhere to play again", 635, 500);
//            }
//        }
//        numBlackPiecesOnBoard = 0;
//        numWhitePiecesOnBoard = 0;
//        if (gameOver(pieces) && tie) {
//            g.setColor(Color.gray);
//            g.drawString("Tie Game", 675, 300);
//            g.setFont(new Font("Arial", Font.BOLD, 24));
//            g.drawString("Click anywhere to play again", 635, 500);
//        }

//        if (!firstMove && whiteJustPlayed && !gameOver(pieces)) {
//            getAIMove();
//            repaint();
//        }

//        if (gameOver) {
//            System.out.println("game is over?");
//        }
    }

    public void mouseReleased(MouseEvent event) {
        int x = event.getX();
        int y = event.getY();

        if (gameOver(pieces)) {
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    pieces[i][j] = NONE;
                }
            }
            //gameOver = false;
            whiteWins = false;
            blackWins = false;
            firstMove = true;
            repaint();
            return;
        }

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
//                if (gb.getSpace(i, j).contains(event.getPoint()) && firstMove) {
//                    if (pieces[i][j] == 0) {
//                        pieces[i][j] = BLACK;
//                        TileCoordinate tc = new TileCoordinate(i, j);
//                        whiteCoords.add(tc);//no need to check for first move if a win detected
//                        player = BLACK;
//                        other = WHITE;
//                        firstMove = false;
//                        whiteJustPlayed = false;
//                        blackJustPlayed = true;
//                        repaint();
//                        return;
//                    }
                //} /*else*/
                if (gb.getSpace(i, j).contains(event.getPoint()) && !firstMove && blackJustPlayed) {
                    if (pieces[i][j] == 0) {
                        pieces[i][j] = WHITE;
                        TileCoordinate tc = new TileCoordinate(i, j);
                        whiteCoords.add(tc);
                        whiteJustPlayed = true;
                        blackJustPlayed = false;
                        //other = player;
                        //player = (player == WHITE) ? BLACK : WHITE;
                        //other = BLACK;
                        //player = WHITE;
                        repaint();
                        return;
                    }
                }
            }
        }
    }

    public void getAIMove() {// use this to
        for (int i = 0 ; i < pieces.length ; i++) {
            for (int j = 0 ; j < pieces.length ; j++) {
                if (validCoordinates(pieces).size() > 0) {
//                    TileCoordinate bestCoordinate = bestMove(BLACK);
                    int bestScore = minimax(pieces, 3, Integer.MIN_VALUE, Integer.MAX_VALUE, true);//4 possible, but too slow to play. 3 should be good
                    System.out.println(bestScore);
                    pieces[ultimateCoordinate.getX()][ultimateCoordinate.getY()] = BLACK;
                    TileCoordinate copy = new TileCoordinate(ultimateCoordinate.getX(), ultimateCoordinate.getY());
                    //System.out.println(bestScore + ", " + copy.getX() + ", " + copy.getY() + "****************************");
                    blackCoords.add(copy);
                    whiteJustPlayed = false;
                    blackJustPlayed = true;
                    other = player;
                    player = (player == WHITE) ? BLACK : WHITE;
                    return;
                }
            }
        }

    }

    public int getWindowScore(int[] window, int color) {

        //if placing in the coordinate gives 2, score += 10
        //if placing in the coordinate gives 3, score += 100
        //if placing in the coordinate gives 4, score += 1000
        //if placing in the coordinate gives 5, score += 10000
        //if opponent has 4 in a row and an empty window available, score -= 5000

        int score = 0;
        int opponentPiece = player;
        if (color == player) {
            opponentPiece = other;
        }

        int colorCount = 0;
        int oppCount = 0;
        int noneCount = 0;

        for (int i = 0 ; i < window.length ; i++) {
            if (window[i] == color) colorCount++;
            if (window[i] == opponentPiece) oppCount++;
            if (window[i] == NONE) noneCount++;
        }

        //advantage AI
        if (colorCount == 2 && noneCount == 3) {
            score += 1;
        } else if (colorCount == 3 && noneCount == 2) {
            score += 2;
        } else if (colorCount == 4 && noneCount == 1) {
            score += 5;
        }
        else if (colorCount == 5) {
            score += 25;
        }

        //advantage opposition
        if (oppCount == 2 && noneCount == 3) {
            score -= 1;
        } else if (oppCount == 3 && noneCount == 2) {
            score -= 2;
        } else if (oppCount == 4 && noneCount == 1) {
            score -= 5;
        }
        else if (oppCount == 5) {
            score -= 25;
        }

        //mixed
//        if (colorCount == 1 && oppCount == 1 && noneCount == 3) {
//            score += 1;
//        } else if (colorCount == 2 && oppCount == 1 && noneCount == 2) {
//            score -= 5;
//        } else if (colorCount == 3 && oppCount == 1 && noneCount == 2) {
//            score -= 500;
//        } else if (colorCount == 4 && oppCount == 1) {
//            score -= 5000;
//        }

        return score;

    }

    public int totalCoordinateScore(int[][] board, int color) {
        int score = 0;

        //if in 4x4 centre grid, add 1 by default
        for (int i = 3 ; i < 7 ; i++) {
            for (int j = 3 ; j < 7 ; j++) {
                if (board[i][j] == color) {
                    score += 1;//same as p=1, opp=1, none=3
                }
            }
        }

        //evaluate rows
        for (int i = 0 ; i < board.length ; i++) {
            for (int j = 0 ; j < board.length-4 ; j++) {
                int[] window = Arrays.copyOfRange(board[i], j, j+5);//exclusive
                score += getWindowScore(window, color);
            }
        }

        //evaluate all columns (going downward)
        for (int j = 0 ; j < board.length ; j++) {
            for (int i = 0 ; i < board.length-5 ; i++) {
                int[] window = new int[5];
                for (int r = i, c = 0; r < i+5 ; r++, c++) {
                    window[c] = board[r][j];
                }
                score += getWindowScore(window, color);
            }
        }

        //evaluate all downward right diagonals
        for (int i = 0 ; i < board.length-4; i++) {
            for (int j = 0 ; j < board.length-4 ; j++) {
                int[] window = new int[5];
                for (int r = i, c = j, count = 0 ; r < i+5 && c < j+5 ; r++, c++, count++) {
                    window[count] = board[r][c];
                }
                score += getWindowScore(window, color);
            }
        }

        //evaluate all downward left diagonals
        for (int i = 0 ; i < board.length-4 ; i++) {
            for (int j = board.length-1 ; j >= board.length-6 ; j--) {
                int[] window = new int[5];
                for (int r = i, c = j, count = 0 ; r < i+5 && c > j-5 ; r++, c--, count++) {
                    window[count] = board[r][c];
                }
                score += getWindowScore(window, color);
            }
        }

        return score;
    }

    public List<TileCoordinate> validCoordinates(int[][] board) {
        List<TileCoordinate> validCoordinates = new ArrayList<>();
        for (int i = 0 ; i < pieces.length ; i++) {
            for (int j = 0 ; j < pieces.length ; j++) {
                if (board[i][j] == 0) {
                    TileCoordinate tc = new TileCoordinate(i, j);
                    validCoordinates.add(tc);
                }
            }
        }
        return validCoordinates;
    }

//    public TileCoordinate bestMove(int color) {
//        List<TileCoordinate> validLocations = validCoordinates(pieces);
//        int bestScore = 0;
//        Random rand = new Random();
//        TileCoordinate bestCoordinate = validLocations.get(rand.nextInt(validLocations.size()));// place holder spot
//
//        for (TileCoordinate tc : validLocations) {
//            //copy the current board and modify to check valid spaces
//            int[][] tempBoard = new int[pieces.length][];
//            for (int i = 0 ; i < pieces.length ; i++) {
//                tempBoard[i] = pieces[i].clone();
//            }
//            tempBoard[tc.getX()][tc.getY()] = color;//put piece into the board copy
//            int score = totalCoordinateScore(tempBoard, color);
//            if (score > bestScore) {
//                bestScore = score;
//                bestCoordinate = tc;
//            }
//        }
//
//        return bestCoordinate;
//
//    }

//    public int consecutivePieceCheck(int i, int j) {
//        int player_count = 0;
//        int score = 0;
//        int piece_color = pieces[i][j];
//        int other_color = (pieces[i][j] == WHITE) ? BLACK : WHITE;
//        int other_count = 0;
//        int none_count = 0;
//
//        if (j + 5 < pieces.length) { //check 4 to right
//            for (int c = j ; c < j+4 ; c++) {
//                if (pieces[i][c] == piece_color) {
//                    player_count++;
//                } else if (pieces[i][c] == other_color) {
//                    other_count++;
//                } else if (pieces[i][c] == 0) {
//                    none_count++;
//                }
//            }
//        }
//        score += getScore(player_count, other_count, none_count);
//        player_count = other_count = none_count = 0; //reset values
//
//
//        return score;
//    }

    public boolean fiveInARow(int i, int j, int[][] board) {//TODO: revise so it takes in a boardCopy instance instead of using pieces
        int piece_color = board[i][j];
        if (piece_color == NONE) return false;

        if (j + 4 < board.length && //to right
                piece_color == board[i][j + 1] &&
                piece_color == board[i][j + 2] &&
                piece_color == board[i][j + 3] &&
                piece_color == board[i][j + 4]) {
            if (piece_color == BLACK && board == pieces) {
                blackWins = true;
            } else {
                whiteWins = true;
            }
            return true;
        }
        if (j - 4 >= 0 && //to left
                piece_color == board[i][j - 1] &&
                piece_color == board[i][j - 2] &&
                piece_color == board[i][j - 3] &&
                piece_color == board[i][j - 4]) {
            if (piece_color == BLACK && board == pieces) {
                blackWins = true;
            } else {
                whiteWins = true;
            }
            return true;
        }
        if (i - 4 >= 0) {//above
            if (piece_color == board[i - 1][j] && //above
                    piece_color == board[i - 2][j] &&
                    piece_color == board[i - 3][j] &&
                    piece_color == board[i - 4][j]) {
                if (piece_color == BLACK && board == pieces) {
                    blackWins = true;
                } else {
                    whiteWins = true;
                }
                return true;
            }
            if (j + 4 < board.length &&
                    piece_color == board[i - 1][j + 1] && //above and to right
                    piece_color == board[i - 2][j + 2] &&
                    piece_color == board[i - 3][j + 3] &&
                    piece_color == board[i - 4][j + 4]) {
                if (piece_color == BLACK && board == pieces) {
                    blackWins = true;
                } else {
                    whiteWins = true;
                }
                return true;
            }
            if (j - 4 >= 0 &&
                    piece_color == board[i - 1][j - 1] && // above and to left
                    piece_color == board[i - 2][j - 2] &&
                    piece_color == board[i - 3][j - 3] &&
                    piece_color == board[i - 4][j - 4]) {
                if (piece_color == BLACK && board == pieces) {
                    blackWins = true;
                } else {
                    whiteWins = true;
                }
                return true;
            }
        }
        if (i + 4 < board.length) { //below
            if (piece_color == board[i + 1][j] && //below
                    piece_color == board[i + 2][j] &&
                    piece_color == board[i + 3][j] &&
                    piece_color == board[i + 4][j]) {
                if (piece_color == BLACK && board == pieces) {
                    blackWins = true;
                } else {
                    whiteWins = true;
                }
                return true;
            }
            if (j + 4 < board.length &&
                    piece_color == board[i + 1][j + 1] && //below and to right
                    piece_color == board[i + 2][j + 2] &&
                    piece_color == board[i + 3][j + 3] &&
                    piece_color == board[i + 4][j + 4]) {
                if (piece_color == BLACK && board == pieces) {
                    blackWins = true;
                } else {
                    whiteWins = true;
                }
                return true;
            }
            if (j - 4 >= 0 &&
                    piece_color == board[i + 1][j - 1] && //below and to left
                    piece_color == board[i + 2][j - 2] &&
                    piece_color == board[i + 3][j - 3] &&
                    piece_color == board[i + 4][j - 4]) {
                if (piece_color == BLACK && board == pieces) {
                    blackWins = true;
                } else {
                    whiteWins = true;
                }
                return true;
            }
        }
        return false;
    }

    public int minimax(int[][] board, int depth, int alpha, int beta, boolean maximizingPlayer) {
        List<TileCoordinate> validCoordinates = validCoordinates(board);
        boolean isTerminal = gameOver(board);//rewrite so gameOver takes in a board copy instance
        if (depth == 0 || isTerminal) {
            if (isTerminal) {
                for (TileCoordinate tc : blackCoords) {
                    if (fiveInARow(tc.getX(), tc.getY(), board)) {
                        return 50;
                    }
                }
                for (TileCoordinate tc : whiteCoords) {
                    if (fiveInARow(tc.getX(), tc.getY(), board)) {
                        return -50;
                    }
                }
                return 0;
            } else {
//                if (!memoize.containsKey(board)) {
//                    Integer[][] b = new Integer[10][10];
//                    b = board;
//                    int score = totalCoordinateScore(board, BLACK);
//                    memoize.put(board, score);
//                    return score;
//                } else {
//                    return memoize.get(board);
//                }
                return totalCoordinateScore(board, BLACK);
            }
        }
        if (maximizingPlayer) {// the AI
            int value = Integer.MIN_VALUE;
            Random rand = new Random();
            TileCoordinate coord = validCoordinates.get(rand.nextInt(validCoordinates.size()));
            //ultimateCoordinate = coord;//place holder spot
            for (TileCoordinate tc : validCoordinates) {
                int[][] tempBoard = new int[board.length][];
                for (int i = 0 ; i < board.length ; i++) {
                    tempBoard[i] = board[i].clone();
                }
                tempBoard[tc.getX()][tc.getY()] = BLACK;//put piece into the board copy-->not good, should be variable so can do different "starting colour"
//                if (color == WHITE) color = BLACK;
//                if (color == BLACK) color = WHITE;
                //value = Math.max(value, minimax(childBoard, depth-1, false, color));
                int newScore = minimax(tempBoard, depth-1, alpha, beta, false);
                if (newScore > value) {
                    value = newScore;
                    coord = tc;
                }
                alpha = Math.max(alpha, value);
                if (alpha >= beta) break;
            }
            //MinimaxData md = new MinimaxData(coord, value);
            ultimateCoordinate = coord;
            //System.out.println(value + ", " + coord.getX() + ", " + coord.getY() + "MAX++++++++++++++++++++++");
            return value;
        } else {//minimizing player, the user
            int value = Integer.MAX_VALUE;
            Random rand = new Random();
            TileCoordinate coord = validCoordinates.get(rand.nextInt(validCoordinates.size()));
            //ultimateCoordinate = coord;//place holder spot
            for (TileCoordinate tc : validCoordinates) {
                int[][] tempBoard = new int[board.length][];
                for (int i = 0 ; i < board.length ; i++) {
                    tempBoard[i] = board[i].clone();
                }
                tempBoard[tc.getX()][tc.getY()] = WHITE;//put piece into the board copy-->not good, should be variable so can do different "starting colour"
//                if (color == WHITE) color = BLACK;
//                if (color == BLACK) color = WHITE;
                int newScore = minimax(tempBoard, depth-1, alpha, beta, true);
                if (newScore < value) {
                    value = newScore;
                    coord = tc;
                }
                beta = Math.min(beta, value);
                if (alpha >= beta) break;
            }
            //MinimaxData md = new MinimaxData(coord, value);
            ultimateCoordinate = coord;
            //System.out.println(value + ", " + coord.getX() + ", " + coord.getY() + "MIN______________________");
            return value;
        }
    }


    public boolean gameOver(int[][] board) {// given position of a black/white piece, see if a 5-in-a-row is present

        //check for a 5-in-a-row
        if (whiteJustPlayed) {
            for (TileCoordinate tc : whiteCoords) {
                if (fiveInARow(tc.getX(), tc.getY(), board)) {
                    return true;
                }
            }
        } else if (blackJustPlayed) {
            for (TileCoordinate tc : blackCoords) {
                if (fiveInARow(tc.getX(), tc.getY(), board)) {
                    return true;
                }
            }
        }
        //check for a tie if no 5-in-a-row detected
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (board[i][j] == WHITE) {
                    numWhitePiecesOnBoard++;
                } else if (board[i][j] == BLACK) {
                    numBlackPiecesOnBoard++;
                }
            }
        }
        if (numWhitePiecesOnBoard == 50 && numBlackPiecesOnBoard == 50) {
            tie = true;
            return true;
        }
        //game isn't over
        return false;
    }


    public void mouseClicked(MouseEvent event) {
    }

    public void mousePressed(MouseEvent evt) {
    }

    public void mouseEntered(MouseEvent evt) {
    }

    public void mouseExited(MouseEvent evt) {
    }


}


