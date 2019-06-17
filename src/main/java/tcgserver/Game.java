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

    private GameState state;
    private ArrayList<Player> players;
    private ArrayList<Card> initialDeck;

    public Game() {
        this(Arrays.asList(new Card(0), new Card(1), new Card(2), new Card(3), new Card(4)));
    }

    public Game(List<Card> initialDeck) {
        state = GameState.INITIAL;
        players = new ArrayList<>();
        this.initialDeck = new ArrayList<>();
        this.initialDeck.addAll(initialDeck);
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public Player addPlayer(User user) {
        return new Player(Collections.emptyList(), Collections.emptyList());
    }
}
