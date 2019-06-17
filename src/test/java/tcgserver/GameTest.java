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
}