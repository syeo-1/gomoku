import javax.swing.*;
import java.awt.*;

/**
 * Created by seanyeo on 2019-06-21.
 */
public class GameBoard extends JPanel{
    //Tile[] board = new Tile[100];
    private Rectangle[][] board;

    public GameBoard(int size) {
        board = new Rectangle[size][size];
        //System.out.println(size);
        for (int i = 0 ; i < size ; i++) {
            for (int j = 0 ; j < size ; j++) {
                board[i][j] = new Rectangle(30+j*60,50+i*60,60,60);
            }
        }
    }

    public Rectangle getSpace(int i, int j) {
        return board[i][j];
    }


}
