package tcgserver;

public class Card {
    private int mana;

    public Card() {
        mana = 0;
    }

    public Card(int mana) {
        this.mana = mana;
    }

    public int getMana() {
        return mana;
    }
}
