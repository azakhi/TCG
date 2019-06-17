package tcgserver;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
        Player player = new Player(deck, Collections.emptyList(), 30, 0, 0);

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
        Player player = new Player(Collections.emptyList(), Collections.emptyList(), 30, 0, 0);

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
        Player player = new Player(deck, hand, 30, 0, 0);

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
        Player player = new Player(Collections.emptyList(), Collections.emptyList(), 30, 0, 0);

        // Act
        Card card = player.playCardAt(0);

        // Assert
        assertNull(card);
    }

    @Test
    public void playCardAt_InvalidCardIndex_NullReturned() {
        // Arrange
        List<Card> hand = Arrays.asList(new Card(), new Card(), new Card(), new Card(), new Card());
        Player player = new Player(Collections.emptyList(), hand, 30, 0, 0);

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
        assertEquals(true, player.getHand().contains(cardToPlay));
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
        assertEquals(false, player.getHand().contains(cardToPlay));
        assertEquals(9, player.getMana());
    }

    @Test
    public void dealDamage_HasHealth0Damage_NoDamageDealt() {
        // Arrange
        Player player = new Player(Collections.emptyList(), Collections.emptyList(), 10, 0, 0);

        // Act
        int remainingHealth = player.dealDamage(0);

        // Assert
        assertEquals(10, remainingHealth);
        assertEquals(10, player.getHealth());
    }

    @Test
    public void dealDamage_HasHealthNegativeDamage_ExceptionThrown() {
        // Arrange
        Player player = new Player(Collections.emptyList(), Collections.emptyList(), 10, 0, 0);

        // Act
        // Assert
        assertThrows(AssertionError.class, () -> {
            int remainingHealth = player.dealDamage(-1);
        });
    }

    @Test
    public void dealDamage_HasHealthPositiveDamage_DamageDealt() {
        // Arrange
        Player player = new Player(Collections.emptyList(), Collections.emptyList(), 10, 0, 0);

        // Act
        int remainingHealth = player.dealDamage(5);

        // Assert
        assertEquals(5, remainingHealth);
        assertEquals(5, player.getHealth());
    }

    @Test
    public void addManaSlot_LessThanMaxSlot_SlotAdded() {
        // Arrange
        Player player = new Player(Collections.emptyList(), Collections.emptyList(), 10, 0, 0);

        // Act
        player.addManaSlot();

        // Assert
        assertEquals(1, player.getManaSlot());
    }

    @Test
    public void addManaSlot_MaxManaSlot_NoSlotAdded() {
        // Arrange
        Player player = new Player(Collections.emptyList(), Collections.emptyList(), 10, Player.MAX_MANA_SLOT, 0);

        // Act
        player.addManaSlot();

        // Assert
        assertEquals(Player.MAX_MANA_SLOT, player.getManaSlot());
    }

    @Test
    public void fillMana_NoManaSlot_SameMana() {
        // Arrange
        Player player = new Player(Collections.emptyList(), Collections.emptyList(), 10, 0, 0);

        // Act
        player.fillMana();

        // Assert
        assertEquals(0, player.getMana());
    }

    @Test
    public void fillMana_LessThanSlotMana_ManaFilled() {
        // Arrange
        Player player = new Player(Collections.emptyList(), Collections.emptyList(), 10, 10, 0);

        // Act
        player.fillMana();

        // Assert
        assertEquals(10, player.getMana());
    }

    @Test
    public void AllMethods_HasNoHealth_ExceptionThrown() {
        // Arrange
        List<Card> deck = Arrays.asList(new Card(), new Card(), new Card());
        List<Card> hand = Arrays.asList(new Card(), new Card(), new Card(), new Card(), new Card());
        Player player = new Player(deck, hand, -1, 0, 0);

        // Act
        // Assert
        assertThrows(AssertionError.class, () -> {
            int remainingHealth = player.dealDamage(1);
        });
        assertThrows(AssertionError.class, () -> {
            player.drawRandomCard();
        });
        assertThrows(AssertionError.class, () -> {
            player.playCardAt(0);
        });
        assertThrows(AssertionError.class, () -> {
            player.addManaSlot();
        });
        assertThrows(AssertionError.class, () -> {
            player.fillMana();
        });
    }
}