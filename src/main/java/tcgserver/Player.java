package tcgserver;

import java.util.List;

public class Player {
    private List<Card> deck;
    private List<Card> hand;

    public Player(List<Card> deck, List<Card> hand) {
        this.deck = deck;
        this.hand = hand;
    }

    public List<Card> getDeck() {
        return deck;
    }

    public List<Card> getHand() {
        return hand;
    }

    public boolean drawRandomCard() {
        return false;
    }
}
