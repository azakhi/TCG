package tcgserver;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("cards")
public class Card {
    @Id
    private String id;
    private int mana;

    public Card() {
        mana = 0;
    }

    public Card(int mana) {
        this.mana = mana;
    }

    public String getId() {
        return id;
    }

    public int getMana() {
        return mana;
    }

    public class CardSimple {
        public String id;
        public int mana;
    }

    public CardSimple getSimple() {
        CardSimple cardSimple = new CardSimple();
        cardSimple.id = id;
        cardSimple.mana = mana;

        return cardSimple;
    }
}
