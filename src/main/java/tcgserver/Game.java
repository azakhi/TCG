package tcgserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Game {
    public enum GameState {
        INITIAL,
        ACTIVE,
        END
    }

    public final static int MAX_PLAYERS = 2;
    public final static int MIN_PLAYERS = 2;
    public final static int START_CARD_COUNT = 3;

    private GameState state;
    private int turn;
    private ArrayList<Player> players;
    private ArrayList<Card> initialDeck;

    public Game() {
        this(Arrays.asList(new Card(0), new Card(1), new Card(2), new Card(3), new Card(4)));
    }

    public Game(List<Card> initialDeck) {
        assert initialDeck.size() >= START_CARD_COUNT;

        state = GameState.INITIAL;
        turn = 0;
        players = new ArrayList<>();
        this.initialDeck = new ArrayList<>();
        this.initialDeck.addAll(initialDeck);
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public GameState getState() {
        return state;
    }

    public int getTurn() {
        return turn;
    }

    public Player addPlayer(User user) {
        if (players.size() < MAX_PLAYERS) {
            Player player = new Player(initialDeck, Collections.emptyList());
            players.add(player);
            return player;
        }

        return null;
    }

    public boolean start() {
        return false;
    }
}
