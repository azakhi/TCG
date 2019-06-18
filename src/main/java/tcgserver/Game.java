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
        this(Arrays.asList(new Card(0), new Card(1), new Card(2), new Card(3), new Card(4)));
    }

    public Game(List<Card> initialDeck) {
        this(initialDeck, true);
    }

    public Game(List<Card> initialDeck, boolean isDrawCardAtTurnStart) {
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

    public Player addPlayer(User user) {
        return addPlayer(new Player(user.getId(), initialDeck, Collections.emptyList()));
    }

    public Player addPlayer(Player player) {
        if (players.size() < MAX_PLAYERS) {
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
                DrawCardAction drawCardAction = (DrawCardAction)action;
                if (!players.get(drawCardAction.getPlayer()).drawRandomCard()) {
                    players.get(drawCardAction.getPlayer()).dealDamage(BLEED_OUT_DAMAGE);
                }

                cardDrawnThisTurn++;
                actions.add(drawCardAction);
            }
            else {
                return false;
            }
        }
        else if (action.getType() == ActionType.PLAY_CARD) {
            PlayCardAction playCardAction = (PlayCardAction)action;
            Card playedCard = players.get(playCardAction.getPlayer()).playCardAt(playCardAction.getIndex());
            if (playedCard != null) {
                for (int i = 0; i < players.size(); i++) {
                    if (i != playCardAction.getPlayer()) {
                        players.get(i).dealDamage(playedCard.getMana());
                    }
                }

                actions.add(playCardAction);
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

        if (players.size() > 0 && isDrawCardAtTurnStart && !addAction(new DrawCardAction(activePlayer))) {
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
