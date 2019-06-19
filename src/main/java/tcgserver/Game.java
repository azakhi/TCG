package tcgserver;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Document("games")
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

    public static class Action {
        private int player;
        private ActionType type;
        private int index;

        public Action() {
            this(-1);
        }

        public Action(int player) {
            this(player, ActionType.SKIP);
        }

        public Action(int player, ActionType type) {
            this(player, type, -1);
        }

        public Action(int player, ActionType type, int index) {
            this.type = type;
            this.player = player;
            this.index = index;
        }

        public ActionType getType() {
            return type;
        }

        public int getPlayer() {
            return player;
        }

        public int getIndex() {
            return index;
        }
    }

    public final static int MAX_PLAYERS = 2;
    public final static int MIN_PLAYERS = 2;
    public final static int START_CARD_COUNT = 3;
    public final static int BLEED_OUT_DAMAGE = 1;
    public final static int MAX_CARD_DRAW_PER_TURN = 1;

    @Id
    private String id;
    private boolean isDrawCardAtTurnStart;
    private GameState state;
    private int turn;
    private int cardDrawnThisTurn;
    private ArrayList<Player> players;
    private ArrayList<Card> initialDeck;
    private ArrayList<Action> actions;

    public Game() {
        this(null);
    }

    public Game(List<Card> initialDeck) {
        this(initialDeck, true);
    }

    public Game(List<Card> initialDeck, boolean isDrawCardAtTurnStart) {
        if (initialDeck == null) {
            ArrayList<Card> cards = new ArrayList<>();
            for (int i = 0; i < START_CARD_COUNT; i++) {
                cards.add(new Card(i));
            }
            initialDeck = cards;
        }

        assert initialDeck.size() >= START_CARD_COUNT;

        this.isDrawCardAtTurnStart = isDrawCardAtTurnStart;
        state = GameState.INITIAL;
        turn = 0;
        cardDrawnThisTurn = 0;
        players = new ArrayList<>();
        this.initialDeck = new ArrayList<>();
        this.initialDeck.addAll(initialDeck);
        actions = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public boolean isDrawCardAtTurnStart() {
        return isDrawCardAtTurnStart;
    }

    public GameState getState() {
        return state;
    }

    public int getTurn() {
        return turn;
    }

    public int getCardDrawnThisTurn() {
        return cardDrawnThisTurn;
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public List<Card> getInitialDeck() {
        return Collections.unmodifiableList(initialDeck);
    }

    public List<Action> getActions() {
        return Collections.unmodifiableList(actions);
    }

    public int getActivePlayer() {
        return turn % players.size();
    }

    public int getPlayerIndex(String userId) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getUserId() == userId) {
                return i;
            }
        }

        return -1;
    }

    public int addPlayer(User user) {
        return addPlayer(new Player(user.getId(), initialDeck, Collections.emptyList()));
    }

    public int addPlayer(Player player) {
        int index = getPlayerIndex(player.getUserId());
        if (index >= 0) {
            return index;
        }

        if (players.size() < MAX_PLAYERS) {
            players.add(player);
            return players.size() - 1;
        }

        return -1;
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
            return startTurn();
        }

        return false;
    }

    public boolean addAction(Action action) {
        assert state == GameState.ACTIVE;

        if (action.getPlayer() != (turn % players.size())) {
            return false;
        }

        if (action.getType() == ActionType.DRAW_CARD) {
            if (cardDrawnThisTurn < MAX_CARD_DRAW_PER_TURN) {
                if (!players.get(action.getPlayer()).drawRandomCard()) {
                    players.get(action.getPlayer()).dealDamage(BLEED_OUT_DAMAGE);
                }

                cardDrawnThisTurn++;
                actions.add(action);
            }
            else {
                return false;
            }
        }
        else if (action.getType() == ActionType.PLAY_CARD) {
            Card playedCard = players.get(action.getPlayer()).playCardAt(action.getIndex());
            if (playedCard != null) {
                for (int i = 0; i < players.size(); i++) {
                    if (i != action.getPlayer()) {
                        players.get(i).dealDamage(playedCard.getMana());
                    }
                }

                actions.add(action);
            }
            else {
                return false;
            }
        }
        else if (action.getType() == ActionType.SKIP) {
            actions.add(action);
            turn++;
            return startTurn();
        }
        else {
            return false;
        }

        checkPlayerHealth();
        return true;
    }

    private boolean startTurn() {
        cardDrawnThisTurn = 0;
        if (players.size() < 1) {
            return true;
        }

        int activePlayer = turn % players.size();
        players.get(activePlayer).addManaSlot();
        players.get(activePlayer).fillMana();

        if (players.size() > 0 && isDrawCardAtTurnStart && !addAction(new Action(activePlayer, ActionType.DRAW_CARD))) {
            return false;
        }

        return true;
    }

    private void checkPlayerHealth() {
        for (Player p : players) {
            if (p.getHealth() <= 0) {
                state = GameState.END;
                break;
            }
        }
    }

    public class GameSimple {
        public String id;
        public boolean isDrawCardAtTurnStart;
        public GameState state;
        public int turn;
        public int cardDrawnThisTurn;
        public List<Player.PlayerSimple> players;
        public List<Card> initialDeck;
        public List<Action> actions;
    }

    public GameSimple getSimple() {
        GameSimple gameSimple = new GameSimple();
        gameSimple.id = id;
        gameSimple.isDrawCardAtTurnStart = isDrawCardAtTurnStart;
        gameSimple.state = state;
        gameSimple.turn = turn;
        gameSimple.cardDrawnThisTurn = cardDrawnThisTurn;
        gameSimple.initialDeck = initialDeck;
        gameSimple.actions = actions;

        ArrayList<Player.PlayerSimple> playerSimples = new ArrayList<>();
        for (Player p : players) {
            playerSimples.add(p.getSimple());
        }
        gameSimple.players = playerSimples;

        return gameSimple;
    }
}
