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

    @Test
    public void playCardAt_NoCardToPlay_NullReturned() {
        // Arrange
        Player player = new Player(Collections.emptyList(), Collections.emptyList());

        // Act
        Card card = player.playCardAt(0);

        // Assert
        assertNull(card);
    }

    @Test
    public void playCardAt_InvalidCardIndex_NullReturned() {
        // Arrange
        List<Card> hand = Arrays.asList(new Card(), new Card(), new Card(), new Card(), new Card());
        Player player = new Player(Collections.emptyList(), hand);

        // Act
        Card card = player.playCardAt(-1);
        Card card2 = player.playCardAt(hand.size());

        // Assert
        assertNull(card);
        assertNull(card2);
    }

    @Test
    public void playCardAt_ValidCardNoMana_NullReturned() {
        // Arrange
        Card cardToPlay = new Card(1);
        List<Card> hand = Arrays.asList(cardToPlay, new Card(), new Card(), new Card(), new Card());
        Player player = new Player(Collections.emptyList(), hand, 30, 0, 0);

        // Act
        Card card = player.playCardAt(0);

        // Assert
        assertNull(card);
    }

    @Test
    public void playCardAt_ValidCardEnoughMana_CardPlayed() {
        // Arrange
        Card cardToPlay = new Card(1);
        List<Card> hand = Arrays.asList(cardToPlay, new Card(), new Card(), new Card(), new Card());
        Player player = new Player(Collections.emptyList(), hand, 30, 10, 10);

        // Act
        Card card = player.playCardAt(0);

        // Assert
        assertEquals(4, player.getHand().size());
        assertEquals(cardToPlay, card);
        assertEquals(9, player.getMana());
    }
}