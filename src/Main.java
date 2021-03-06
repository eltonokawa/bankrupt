import control.GameControl;
import utils.GameConstants;

import java.io.IOException;

/**
 * Created by e11even on 13/04/18.
 */
public class Main {

    public static void main (String args[]) throws IOException {
        GameControl gameControl = new GameControl(GameConstants.GAME_CONFIG_FILE_NAME);

        for (int i = 0; i < GameConstants.NUMBER_OF_GAMES; i++) {
//        for (int i = 0; i < 2; i++){
            gameControl.startGame();
            gameControl.init(GameConstants.GAME_CONFIG_FILE_NAME);
        }

        System.out.println(gameControl);
    }
}
