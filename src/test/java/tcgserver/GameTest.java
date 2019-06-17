package tcgserver;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

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
        Game game = new Game(Arrays.asList(new Card(0), new Card(1), new Card(2), new Card(3), new Card(4)));
        for (int i = 0; i < Game.MAX_PLAYERS; i++) game.addPlayer(new User());
        Assume.assumeTrue(game.getPlayers().size() == Game.MAX_PLAYERS);

        // Act
        boolean isStarted = game.start();

        // Assert
        assertEquals(true, isStarted);
        assertEquals(Game.GameState.ACTIVE, game.getState());
        assertEquals(0, game.getTurn());
        for (int i = 0; i < Game.MAX_PLAYERS; i++) assertEquals(Game.START_CARD_COUNT, game.getPlayers().get(i).getHand().size());
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
}