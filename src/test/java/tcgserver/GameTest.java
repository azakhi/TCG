package tcgserver;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class GameTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void addPlayer_LessThanMaxPlayers_PlayerAdded() {
        Assume.assumeTrue(Game.MAX_PLAYERS > 0);

        // Arrange
        Game game = new Game(Arrays.asList(new Card(0), new Card(1), new Card(2), new Card(3), new Card(4)));

        // Act
        Player player = game.addPlayer(new User());

        // Assert
        assertEquals(1, game.getPlayers().size());
        assertEquals(player, game.getPlayers().get(0));
        assertEquals(5, player.getDeck().size());
    }

    @Test
    public void addPlayer_MaxPlayers_PlayerNotAdded() {
        // Arrange
        Game game = new Game();
        for (int i = 0; i < Game.MAX_PLAYERS; i++) game.addPlayer(new User());
        Assume.assumeTrue(game.getPlayers().size() == Game.MAX_PLAYERS);

        // Act
        Player player = game.addPlayer(new User());

        // Assert
        assertEquals(Game.MAX_PLAYERS, game.getPlayers().size());
        assertNull(player);
    }

    @Test
    public void start_NotEnoughPlayers_FalseReturned() {
        // Arrange
        Game game = new Game(Arrays.asList(new Card(0), new Card(1), new Card(2), new Card(3), new Card(4)));

        // Act
        boolean isStarted = game.start();

        // Assert
        assertEquals(false, isStarted);
        assertEquals(Game.GameState.INITIAL, game.getState());
    }

    @Test
    public void start_EnoughPlayers_GameStarted() {
        Assume.assumeTrue(Game.MAX_PLAYERS >= Game.MIN_PLAYERS);

        // Arrange
        Game game = new Game(Arrays.asList(new Card(0), new Card(1), new Card(2), new Card(3), new Card(4)), true);
        for (int i = 0; i < Game.MAX_PLAYERS; i++) game.addPlayer(new User());
        Assume.assumeTrue(game.getPlayers().size() == Game.MAX_PLAYERS);

        // Act
        boolean isStarted = game.start();

        // Assert
        assertEquals(true, isStarted);
        assertEquals(Game.GameState.ACTIVE, game.getState());
        assertEquals(0, game.getTurn());
        Assume.assumeTrue(game.getPlayers().size() > 0);
        assertEquals(Game.START_CARD_COUNT + 1, game.getPlayers().get(0).getHand().size());
        assertEquals(1, game.getActions().size());
        for (int i = 1; i < Game.MAX_PLAYERS; i++) assertEquals(Game.START_CARD_COUNT, game.getPlayers().get(i).getHand().size());
    }

    @Test
    public void start_AlreadyRunningGame_FalseReturned() {
        // Arrange
        Game game = new Game(Arrays.asList(new Card(0), new Card(1), new Card(2), new Card(3), new Card(4)));
        for (int i = 0; i < Game.MAX_PLAYERS; i++) game.addPlayer(new User());
        Assume.assumeTrue(game.getPlayers().size() == Game.MAX_PLAYERS);
        game.start();
        Assume.assumeTrue(game.getState() == Game.GameState.ACTIVE);

        // Act
        boolean isStarted = game.start();

        // Assert
        assertEquals(false, isStarted);
    }

    @Test
    public void addAction_DrawCard_GameStart_CardDrawn() {
        // Arrange
        Assume.assumeTrue(Game.MAX_PLAYERS > 0);
        ArrayList<Card> initialDeck = new ArrayList<>();
        for (int i = 0; i <= Game.START_CARD_COUNT; i++) initialDeck.add(new Card(i));
        Game game = new Game(initialDeck, false);
        for (int i = 0; i < Game.MAX_PLAYERS; i++) game.addPlayer(new User());
        Assume.assumeTrue(game.getPlayers().size() == Game.MAX_PLAYERS);
        game.start();
        Assume.assumeTrue(game.getState() == Game.GameState.ACTIVE);

        // Act
        int activePlayer = game.getTurn() % game.getPlayers().size();
        boolean isAdded = game.addAction(game.new DrawCardAction(activePlayer));

        // Assert
        assertEquals(true, isAdded);
        assertEquals(Game.START_CARD_COUNT + 1, game.getPlayers().get(activePlayer).getHand().size());
    }

    @Test
    public void addAction_DrawCard_ActivePlayerAlreadyDrawn_CardNotDrawn() {
        // Arrange
        Assume.assumeTrue(Game.MAX_PLAYERS > 0);
        ArrayList<Card> initialDeck = new ArrayList<>();
        for (int i = 0; i < (Game.START_CARD_COUNT + 2); i++) initialDeck.add(new Card(i));
        Game game = new Game(initialDeck, false);
        for (int i = 0; i < Game.MAX_PLAYERS; i++) game.addPlayer(new User());
        Assume.assumeTrue(game.getPlayers().size() == Game.MAX_PLAYERS);
        game.start();
        Assume.assumeTrue(game.getState() == Game.GameState.ACTIVE);
        int activePlayer = game.getTurn() % game.getPlayers().size();
        Assume.assumeTrue(game.addAction(game.new DrawCardAction(activePlayer)));

        // Act
        boolean isAdded = game.addAction(game.new DrawCardAction(activePlayer));

        // Assert
        assertEquals(false, isAdded);
    }

    @Test
    public void addAction_DrawCard_GameStartNoCardOnDeck_BleedOutDamageDealt() {
        // Arrange
        Assume.assumeTrue(Game.MAX_PLAYERS > 0);
        Assume.assumeTrue(Player.START_HEALTH > 0);
        ArrayList<Card> initialDeck = new ArrayList<>();
        for (int i = 0; i < Game.START_CARD_COUNT; i++) initialDeck.add(new Card(i));
        Game game = new Game(initialDeck, false);
        for (int i = 0; i < Game.MAX_PLAYERS; i++) game.addPlayer(new User());
        Assume.assumeTrue(game.getPlayers().size() == Game.MAX_PLAYERS);
        game.start();
        Assume.assumeTrue(game.getState() == Game.GameState.ACTIVE);
        int activePlayer = game.getTurn() % game.getPlayers().size();
        Assume.assumeTrue(game.getPlayers().get(activePlayer).getDeck().size() == 0);

        // Act
        boolean isAdded = game.addAction(game.new DrawCardAction(activePlayer));

        // Assert
        assertEquals(true, isAdded);
        assertEquals(Player.START_HEALTH - Game.BLEED_OUT_DAMAGE, game.getPlayers().get(activePlayer).getHealth());
    }

    @Test
    public void addAction_PlayCard_HasCards_CardPlayedDamageDealt() {
        // Arrange
        Assume.assumeTrue(Game.MAX_PLAYERS > 0);
        Assume.assumeTrue(Player.START_HEALTH > 0);
        ArrayList<Card> initialDeck = new ArrayList<>();
        for (int i = 0; i < (Game.START_CARD_COUNT + 2); i++) initialDeck.add(new Card(i));
        Game game = new Game(initialDeck, true);
        for (int i = 0; i < Game.MAX_PLAYERS; i++) game.addPlayer(new User());
        Assume.assumeTrue(game.getPlayers().size() == Game.MAX_PLAYERS);
        game.start();
        Assume.assumeTrue(game.getState() == Game.GameState.ACTIVE);
        int activePlayer = game.getTurn() % game.getPlayers().size();
        Assume.assumeTrue(game.getPlayers().get(activePlayer).getHand().size() > 0);

        // Act
        Card playedCard = game.getPlayers().get(activePlayer).getHand().get(0);
        boolean isAdded = game.addAction(game.new PlayCardAction(activePlayer, 0));

        // Assert
        assertEquals(true, isAdded);
        assertEquals(false, game.getPlayers().get(activePlayer).getHand().contains(playedCard));
        for (int i = 0; i < game.getPlayers().size(); i++) {
            if (i != activePlayer) {
                assertEquals(Player.START_HEALTH - playedCard.getMana(), game.getPlayers().get(i).getHealth());
            }
        }
    }

    @Test
    public void addAction_PlayCard_InvalidCard_FalseReturned() {
        // Arrange
        Assume.assumeTrue(Game.MAX_PLAYERS > 0);
        Assume.assumeTrue(Player.START_HEALTH > 0);
        ArrayList<Card> initialDeck = new ArrayList<>();
        for (int i = 0; i < Game.START_CARD_COUNT; i++) initialDeck.add(new Card(i));
        Game game = new Game(initialDeck, true);
        for (int i = 0; i < Game.MAX_PLAYERS; i++) game.addPlayer(new User());
        Assume.assumeTrue(game.getPlayers().size() == Game.MAX_PLAYERS);
        game.start();
        Assume.assumeTrue(game.getState() == Game.GameState.ACTIVE);

        // Act
        int activePlayer = game.getTurn() % game.getPlayers().size();
        boolean isAdded = game.addAction(game.new PlayCardAction(activePlayer, game.getPlayers().get(activePlayer).getHand().size()));

        // Assert
        assertEquals(false, isAdded);
    }

    @Test
    public void addAction_PlayCard_CardManaMoreThanPlayerHealth_CardPlayedGameEnded() {
        // Arrange
        Assume.assumeTrue(Game.MAX_PLAYERS > 0);
        Assume.assumeTrue(Player.START_HEALTH > 0);
        ArrayList<Card> initialDeck = new ArrayList<>();
        for (int i = 0; i < Game.START_CARD_COUNT; i++) initialDeck.add(new Card(Player.START_HEALTH + 1));
        Game game = new Game(initialDeck, true);
        for (int i = 0; i < Game.MAX_PLAYERS; i++) game.addPlayer(new User());
        Assume.assumeTrue(game.getPlayers().size() == Game.MAX_PLAYERS);
        game.start();
        Assume.assumeTrue(game.getState() == Game.GameState.ACTIVE);
        int activePlayer = game.getTurn() % game.getPlayers().size();
        Assume.assumeTrue(game.getPlayers().get(activePlayer).getHand().size() > 0);

        // Act
        boolean isAdded = game.addAction(game.new PlayCardAction(activePlayer, 0));

        // Assert
        assertEquals(false, isAdded);
        assertEquals(Game.GameState.END, game.getState());
        for (int i = 0; i < game.getPlayers().size(); i++) {
            if (i != activePlayer) {
                assertEquals(true, game.getPlayers().get(i).getHealth() < 0);
            }
        }
    }

    @Test
    public void addAction_Skip_CanSkipTurn_TurnChanges() {
        // Arrange
        Assume.assumeTrue(Game.MAX_PLAYERS > 0);
        ArrayList<Card> initialDeck = new ArrayList<>();
        for (int i = 0; i < (Game.START_CARD_COUNT + 2); i++) initialDeck.add(new Card(0));
        Game game = new Game(initialDeck, false);
        for (int i = 0; i < Game.MAX_PLAYERS; i++) game.addPlayer(new User());
        Assume.assumeTrue(game.getPlayers().size() == Game.MAX_PLAYERS);
        game.start();
        Assume.assumeTrue(game.getState() == Game.GameState.ACTIVE);

        // Act
        int currentTurn = game.getTurn();
        int activePlayer = game.getTurn() % game.getPlayers().size();
        boolean isAdded = game.addAction(game.new Action(activePlayer));

        // Assert
        assertEquals(true, isAdded);
        assertEquals(currentTurn + 1, game.getTurn());
    }

    @Test
    public void addAction_All_GameRunningNotActivePlayer_FalseReturned() {
        // Arrange
        Assume.assumeTrue(Game.MAX_PLAYERS > 1);
        ArrayList<Card> initialDeck = new ArrayList<>();
        for (int i = 0; i <= Game.START_CARD_COUNT; i++) initialDeck.add(new Card(i));
        Game game = new Game(initialDeck, false);
        for (int i = 0; i < Game.MAX_PLAYERS; i++) game.addPlayer(new User());
        Assume.assumeTrue(game.getPlayers().size() == Game.MAX_PLAYERS);
        game.start();
        Assume.assumeTrue(game.getState() == Game.GameState.ACTIVE);

        // Act
        int otherPlayer = (game.getTurn() + 1) % game.getPlayers().size();
        boolean isDrawCardAdded = game.addAction(game.new DrawCardAction(otherPlayer));
        boolean isPlayCardAdded = game.addAction(game.new PlayCardAction(otherPlayer, 0));
        boolean isActionAdded = game.addAction(game.new Action(otherPlayer));

        // Assert
        assertEquals(false, isDrawCardAdded);
        assertEquals(false, isPlayCardAdded);
        assertEquals(false, isActionAdded);
    }

    @Test
    public void addAction_All_GameNotRunning_AssertionThrown() {
        // Arrange
        Assume.assumeTrue(Game.MAX_PLAYERS > 0);
        ArrayList<Card> initialDeck = new ArrayList<>();
        for (int i = 0; i <= Game.START_CARD_COUNT; i++) initialDeck.add(new Card(i));
        Game game = new Game(initialDeck, false);
        for (int i = 0; i < Game.MAX_PLAYERS; i++) game.addPlayer(new User());
        Assume.assumeTrue(game.getPlayers().size() == Game.MAX_PLAYERS);
        Assume.assumeTrue(game.getState() != Game.GameState.ACTIVE);

        // Act
        // Assert
        assertThrows(AssertionError.class, () -> {
            game.addAction(game.new DrawCardAction(0));
        });
        assertThrows(AssertionError.class, () -> {
            game.addAction(game.new PlayCardAction(0, 0));
        });
        assertThrows(AssertionError.class, () -> {
            game.addAction(game.new Action(0));
        });
    }
}