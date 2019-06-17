package tcgserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Player {
    private final static int START_HEALTH = 30;
    private final static int START_MANA_SLOT = 0;
    private final static int MAX_HAND_SIZE = 5;

    private int health;
    private int manaSlot;
    private int mana;
    private ArrayList<Card> deck;
    private ArrayList<Card> hand;

    public Player(List<Card> deck, List<Card> hand) {
        this(deck, hand, START_HEALTH, START_MANA_SLOT, START_MANA_SLOT);
    }

    public Player(List<Card> deck, List<Card> hand, int health, int manaSlot, int mana) {
        this.health = health;
        this.manaSlot = manaSlot;
        this.mana = mana;
        this.deck = new ArrayList<>();
        this.deck.addAll(deck);
        this.hand = new ArrayList<>();
        this.hand.addAll(hand);
    }

    public List<Card> getDeck() {
        return Collections.unmodifiableList(deck);
    }

    public List<Card> getHand() {
        return Collections.unmodifiableList(hand);
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

    public boolean drawRandomCard() {
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
}
