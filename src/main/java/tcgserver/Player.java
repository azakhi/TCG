package tcgserver;

import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Player {
    public final static int START_HEALTH = 30;
    public final static int START_MANA_SLOT = 0;
    public final static int MAX_MANA_SLOT = 10;
    public final static int MAX_HAND_SIZE = 5;

    @Id
    private String userId;
    private int health;
    private int manaSlot;
    private int mana;
    private ArrayList<Card> deck;
    private ArrayList<Card> hand;

    public Player() {
        this("", Collections.emptyList(), Collections.emptyList());
    }

    public Player(List<Card> deck, List<Card> hand) {
        this("", deck, hand);
    }

    public Player(String userId, List<Card> deck, List<Card> hand) {
        this(userId, deck, hand, START_HEALTH, START_MANA_SLOT, START_MANA_SLOT);
    }

    public Player(List<Card> deck, List<Card> hand, int health, int manaSlot, int mana) {
        this("", deck, hand, health, manaSlot, mana);
    }

    public Player(String userId, List<Card> deck, List<Card> hand, int health, int manaSlot, int mana) {
        this.userId = userId;
        this.health = health;
        this.manaSlot = manaSlot;
        this.mana = mana;
        this.deck = new ArrayList<>();
        this.deck.addAll(deck);
        this.hand = new ArrayList<>();
        this.hand.addAll(hand);
    }

    public String getUserId() {
        return userId;
    }

    public int getHealth() {
        return health;
    }

    public int getManaSlot() {
        return manaSlot;
    }

    public int getMana() {
        return mana;
    }

    public List<Card> getDeck() {
        return Collections.unmodifiableList(deck);
    }

    public List<Card> getHand() {
        return Collections.unmodifiableList(hand);
    }

    public boolean drawRandomCard() {
        assert health > 0;
        assert deck != null;
        assert hand != null;

        if (deck.size() < 1) {
            return false;
        }

        Random random = new Random();
        int randomCardIndex = random.nextInt(deck.size());
        Card randomCard = deck.get(randomCardIndex);
        deck.remove(randomCardIndex);

        if (hand.size() < MAX_HAND_SIZE) {
            hand.add(randomCard);
        }

        return true;
    }

    public Card playCardAt(int index) {
        assert health > 0;

        if (index < hand.size() && index >= 0) {
            int manaCost = hand.get(index).getMana();
            if (manaCost > mana) {
                return null;
            }

            mana -= manaCost;
            return hand.remove(index);
        }

        return null;
    }

    public int dealDamage(int damage) {
        assert health > 0;
        assert damage >= 0;

        health -= damage;

        return health;
    }

    public void addManaSlot() {
        assert health > 0;

        if (manaSlot < MAX_MANA_SLOT) {
            manaSlot++;
        }
    }

    public void fillMana() {
        assert health > 0;

        mana = manaSlot;
    }

    public class PlayerSimple {
        public String userId;
        public int health;
        public int manaSlot;
        public int mana;
        public int deckSize;
        public int handSize;
    }

    public PlayerSimple getSimple() {
        PlayerSimple playerSimple = new PlayerSimple();
        playerSimple.userId = userId;
        playerSimple.health = health;
        playerSimple.manaSlot = manaSlot;
        playerSimple.mana = mana;
        playerSimple.deckSize = deck.size();
        playerSimple.handSize = hand.size();

        return playerSimple;
    }
}
