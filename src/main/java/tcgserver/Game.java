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

    public enum ActionType {
        DRAW_CARD,
        PLAY_CARD,
        SKIP
    }

    public class Action {
        private int player;
        protected ActionType type;

        public Action(int player) {
            type = ActionType.SKIP;
            this.player = player;
        }

        public ActionType getType() {
            return type;
        }

        public int getPlayer() {
            return player;
        }
    }

    public class PlayCardAction extends Action {
        private int index;

        public PlayCardAction(int player, int index) {
            super(player);
            type = ActionType.PLAY_CARD;
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    public class DrawCardAction extends Action {
        public DrawCardAction(int player) {
            super(player);
            type = ActionType.DRAW_CARD;
        }
    }

    public final static int MAX_PLAYERS = 2;
    public final static int MIN_PLAYERS = 2;
    public final static int START_CARD_COUNT = 3;
    public final static int BLEED_OUT_DAMAGE = 1;

    private boolean drawCardAtTurnStart;
    private GameState state;
    private int turn;
    private ArrayList<Player> players;
    private ArrayList<Card> initialDeck;
    private ArrayList<Action> actions;

    public Game() {
        this(Arrays.asList(new Card(0), new Card(1), new Card(2), new Card(3), new Card(4)));
    }

    public Game(List<Card> initialDeck) {
        this(initialDeck, true);
    }

    public Game(List<Card> initialDeck, boolean drawCardAtTurnStart) {
        assert initialDeck.size() >= START_CARD_COUNT;

        this.drawCardAtTurnStart = drawCardAtTurnStart;
        state = GameState.INITIAL;
        turn = 0;
        players = new ArrayList<>();
        this.initialDeck = new ArrayList<>();
        this.initialDeck.addAll(initialDeck);
        actions = new ArrayList<>();
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public List<Action> getActions() {
        return Collections.unmodifiableList(actions);
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
        if (players.size() >= MIN_PLAYERS && state == GameState.INITIAL) {
            for (Player p : players) {
                for (int i = 0; i < START_CARD_COUNT; i++) {
                    if(!p.drawRandomCard()) {
                        return false;
                    }
                }
            }

            state = GameState.ACTIVE;
            turn = 0;
            return true;
        }

        return false;
    }

    public boolean addAction(Action action) {
        return false;
    }
}
