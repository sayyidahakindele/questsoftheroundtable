package org.example;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public class Quest {
    @JsonProperty("sponsor")
    private Player sponsor;

    @JsonProperty("participants")
    private Map<Integer, Deck> participants;

    @JsonProperty("queststagecards")
    private ArrayList<Deck> questStageCards;

    public Quest(Player sponsor, Map<Integer, Deck> participants, int stages) {
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
            if (i > 0 && CalculateStageValue(questStageCards.get(i)) <= CalculateStageValue(questStageCards.get(i-1))) {
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

    public Map<Integer, Deck> GetParticipants(int remove) {
        participants.remove(remove);
        return participants;
    }

    public Deck GetParticipant(int id) {
        return participants.get(id);
    }

    private int CalculateStageValue(Deck stageCards) {
        int total = 0;
        for (Card card: stageCards.getCards()) {
            total += card.getValue();
        }
        return total;
    }

    public State AddCardToAttack(int id, Card card) {
        Deck currentStage = participants.get(id);
        String context = "valid";
        String message = "Card added";
        if (card.getType() == Type.FOE) {
            context = "invalid";
            message = "No Foe cards allowed";
            return new State<>(message, context, null);
        }
        for (int i = 0; i < currentStage.GetDeckSize(); i++) {
            if (card.getCode().equals(currentStage.getCards().get(i).getCode())) {
                context = "invalid";
                message = "Only one " + card.getName() + " allowed";
            }
        }
        if (context.equals("valid")) {
            currentStage.AddCard(card);
        }
        return new State<>(message, context, null);
    }

    public State ResolveAttack(int id, int stage) {
        Deck currentStage = participants.get(id);
        if (CalculateStageValue(currentStage) < CalculateStageValue(questStageCards.get(stage - 1))) {
            participants.remove(id);
            return new State<>("Uh oh! You lost", "lost", null);
        }
        participants.put(id, new Deck("empty"));
        return new State<>("Yay! You won!", "won", null);
    }

}
