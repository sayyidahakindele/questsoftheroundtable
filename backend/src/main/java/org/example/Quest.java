package org.example;

import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Quest {
    @JsonProperty("sponsor")
    private Player sponsor;

    @JsonProperty("participants")
    private ArrayList<Player> participants;

    @JsonProperty("queststagecards")
    private ArrayList<Deck> questStageCards;

    public Quest(Player sponsor, ArrayList<Player> participants, int stages) {
        this.sponsor = sponsor;
        this.participants = participants;
        this.questStageCards = new ArrayList<>();
        for (int i=0; i<stages; i++) {
            Deck stage = new Deck("empty");
            questStageCards.add(stage);
        }
    }

    public State AddCardToStage(int stage, Card card) {
        Deck currentStage = questStageCards.get(stage-1);
        String context = "valid";
        String message = "Card added";
        for (int i = 0; i < currentStage.GetDeckSize(); i++) {
            if (card.getType() == Type.FOE && currentStage.getCards().get(i).getType() == Type.FOE) {
                context = "invalid";
                message = "Only one Foe card allowed";
            }
            if (card.getType() == Type.WEAPON && card.getCode().equals(currentStage.getCards().get(i).getCode())) {
                context = "invalid";
                message = "Only one " + card.getName() + " allowed";
            }
        }
        if (context.equals("valid")) {
            currentStage.AddCard(card);
        }
        return new State<>(message, context, null);
    }

    public State EndStage(int stage) {
        Deck currentStage = questStageCards.get(stage-1);
        String context = "invalid";
        String message = "You need a Foe card";
        for (int i = 0; i < currentStage.GetDeckSize(); i++) {
            if (currentStage.getCards().get(i).getType() == Type.FOE) {
                context = "valid";
                message = "";
            }
        }
        return new State<>(message, context, null);
    }

    public State CompleteBuilding() {
        for (int i = 0; i < questStageCards.size(); i++) {
            if (questStageCards.get(i).GetDeckSize() < 1) {
                return new State<>("All stages must have cards", "invalid", i+1);
            }
            if (i > 0 && CalculateStageValue(questStageCards.get(i)) < CalculateStageValue(questStageCards.get(i-1))) {
                return new State<>("Stage " + (i+1) + " must have a greater value than Stage " + i, "invalid", i+1);
            }
        }
        return new State<>("", "valid", null);
    }

    public int GetSponsor() {
        return sponsor.getId();
    }

    public Deck GetDeck(int deck) {
        return questStageCards.get(deck-1);
    }

    private int CalculateStageValue(Deck stageCards) {
        int total = 0;
        for (Card card: stageCards.getCards()) {
            total += card.getValue();
        }
        return total;
    }

}
