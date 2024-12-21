package org.example;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"id", "shields", "cards"})
public class Player {

    @JsonProperty("id")
    private final int id;

    @JsonProperty("shields")
    private int shields;

    @JsonProperty("cards")
    private Deck hand;

    public Player(int id) {
        this.id = id;
        this.shields = 0;
        this.hand = new Deck("empty");
    }

    public boolean IsWinner() {
        return shields >=7;
    }

    public boolean IsHandValid() {
        return hand.GetDeckSize() <=12;
    }

    public int GetHandSize() {
        return hand.GetDeckSize();
    }

    public Card PickCard(int index) {
        if (index < 0 || index > GetHandSize()-1) {
            return null;
        }
        return hand.getCards().remove(index);
    }

    public int getId() {
        return id;
    }

    public Deck getPlayerHand() {
        return hand;
    }

    public int getShields() {
        return shields;
    }

    public void setShields(int num) {
        shields = num;
    }

    public void AddCard(Card card) {
        hand.AddCard(card);
    }
}
