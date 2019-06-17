package tcgserver;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class PlayerTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void drawRandomCard_CardAvailableNoCardsOnHand_CardDrawn() {
        // Arrange
        List<Card> deck = Arrays.asList(new Card(), new Card(), new Card());
        Player player = new Player(deck, Collections.emptyList());

        // Act
        boolean isDrawn = player.drawRandomCard();

        // Assert
        assertEquals(true, isDrawn);
        assertEquals(2, player.getDeck().size());
        assertEquals(1, player.getHand().size());
    }

    @Test
    public void drawRandomCard_NoCardsOnDeck_NoCardDrawn() {
        // Arrange
        Player player = new Player(Collections.emptyList(), Collections.emptyList());

        // Act
        boolean isDrawn = player.drawRandomCard();

        // Assert
        assertEquals(false, isDrawn);
        assertEquals(0, player.getDeck().size());
        assertEquals(0, player.getHand().size());
    }

    @Test
    public void drawRandomCard_CardAvailableMaxCardsOnHand_CardDiscarded() {
        // Arrange
        List<Card> deck = Arrays.asList(new Card(), new Card(), new Card());
        List<Card> hand = Arrays.asList(new Card(), new Card(), new Card(), new Card(), new Card());
        Player player = new Player(deck, hand);

        // Act
        boolean isDrawn = player.drawRandomCard();

        // Assert
        assertEquals(true, isDrawn);
        assertEquals(2, player.getDeck().size());
        assertEquals(5, player.getHand().size());
    }
}