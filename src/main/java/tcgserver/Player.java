package tcgserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Player {
    private final int MAX_HAND_SIZE = 5;
    private ArrayList<Card> deck;
    private ArrayList<Card> hand;

    public Player(List<Card> deck, List<Card> hand) {
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
}
