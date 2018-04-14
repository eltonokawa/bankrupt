package control;

import map.Property;
import model.GameModel;
import player.ImpulsivePlayerFactory;
import player.Player;
import player.PlayerFactory;
import utils.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by e11even on 13/04/18.
 */
public class GameControl {

    private GameModel gameModel = new GameModel();
    private GameStatistics gameStatistics = new GameStatistics();
    private List<PlayerFactory> playerFactories = new ArrayList<>(Arrays.asList(
            new ImpulsivePlayerFactory(),
            new ImpulsivePlayerFactory(),
            new ImpulsivePlayerFactory(),
            new ImpulsivePlayerFactory()
    ));

    public GameControl(String configName) throws IOException {
        init(configName);
    }

    public void init(String configName) throws IOException {
        gameModel.init();
        BufferedReader bf = Files.newBufferedReader(Paths.get(configName));
        String line;
        String[] arguments;
        while ((line = bf.readLine()) != null) {
            arguments = line.split(" ");
            gameModel.addProperty(new Property(Integer.valueOf(arguments[0]), Integer.valueOf(arguments[1])));
        }

        for (int i = 0; i < playerFactories.size(); i++) {
            gameModel.addPlayer(playerFactories.get(i).createPlayer());
        }
    }

    public void startGame() {
        int moves = 0;
        int round = 0;
        boolean finished = false;
        DecisionData decisionData = new DecisionData();
        Player player;
        Property property;
        while (round < GameConstants.MAX_NUMBER_OF_ROUNDS && !finished) {
            for (int playerNumber = 0; playerNumber < gameModel.getNumberOfPlayers(); playerNumber++) {
                player = gameModel.getPlayerAt(playerNumber);
                if (player.getCoins() >= 0) { //The player is still in the game
                    moves = SingletonRandom.getInstance().nextInt(GameConstants.MIN_MOVES, GameConstants.MAX_MOVES + 1);
                    int position = player.getPosition();
                    int newPosition = player.getPosition() + moves;

                    if (newPosition > gameModel.getMapSize()) {
                        player.addCoins(GameConstants.COINS_ROUND_MAP);
                    }
                    player.setPosition((moves + position) % gameModel.getMapSize()); //Makes a circle map

                    property = gameModel.getPropertyAt(player.getPosition());
                    if (property.hasOwner()) {
                        int rent = property.getRent();
                        player.addCoins(-rent); // Subtract rent
                        gameModel.getPlayerAt(property.getOwnerId()).addCoins(rent);

                        checkPlayerBankruptcy(player);
                    } else {
                        decisionData.setPrice(property.getPrice());
                        decisionData.setRent(property.getRent());

                        if (player.getCoins() >= property.getPrice() && player.decision(decisionData)) {
                            property.setOwnerId(player.getPlayerId());
                            player.addProperty(property);
                            player.addCoins(-(property.getPrice()));
                        }
                    }
                }
            }
            finished = (getPlayersPlaying() <= 1);
            round++;
        }

        gameStatistics.addNumberOfGames(1);
        gameStatistics.addTotalRounds(round);
        if (round == GameConstants.MAX_NUMBER_OF_ROUNDS) {
            gameStatistics.addTimeOutGames(1);
        }
        gameStatistics.addBehaviorWins(PlayerType.getPlayerByType(getWinnerIndex()));
    }

    private void checkPlayerBankruptcy(Player player) {
        if (player.getCoins() < 0) {
            List<Property> properties = player.getProperties();
            for (int i = 0; i < properties.size(); i++) {
                properties.get(i).setOwnerId(GameConstants.NO_OWNER);
            }
        }
    }

    private int getPlayersPlaying() {
        int playersPlaying = 0;
        for (int i = 0; i < gameModel.getNumberOfPlayers(); i++) {
            if (gameModel.getPlayerAt(i).getCoins() >= 0) {
                playersPlaying++;
            }
        }

        return playersPlaying;
    }

    private int getWinnerIndex() {
        for (int i = 0; i < gameModel.getNumberOfPlayers(); i++) {
            if (gameModel.getPlayerAt(i).getCoins() >= 0) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        return gameStatistics.toString();
    }

}