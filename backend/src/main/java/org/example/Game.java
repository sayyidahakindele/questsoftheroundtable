package org.example;

import java.util.ArrayList;
import java.util.Collections;

import org.springframework.web.bind.annotation.*;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;


@RestController
@CrossOrigin(origins = "http://127.0.0.1:8081")
public class Game {
    private int currentPlayer;
    private Deck adventureDeck;
    private Deck adventureDiscard;
    private Deck eventDeck;
    private Deck eventDiscard;
    private ArrayList<Player> players;
    private Quest currentQuest;

    public Game() {
        newGame();
    }

    @GetMapping("/start")
    private State newGame() {
        this.currentPlayer = 1;
        this.players = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            players.add(new Player(i));
        }

        this.adventureDeck = new Deck("adventure");
        this.adventureDiscard = new Deck("empty");
        this.eventDeck = new Deck("event");
        this.eventDiscard = new Deck("empty");
        this.currentQuest = null;
        DistributeCards(this.players, 12);
        return new State("Starting game...", "start", wrapInfo());
    }

    @GetMapping("/update")
    private Map<String, Object> wrapInfo() {
        Map<String, Object> gameInfo = new HashMap<>();

        gameInfo.put("currentPlayer", currentPlayer);
        gameInfo.put("adventureDeck", adventureDeck);
        gameInfo.put("adventureDiscard", adventureDiscard);
        gameInfo.put("eventDeck", eventDeck);
        gameInfo.put("eventDiscard", eventDiscard);
        gameInfo.put("players", players);
        gameInfo.put("currentQuest", currentQuest);

        return gameInfo;
    }

    private void DistributeCards(ArrayList<Player> players, int numCards) {
        for (Player player : players) {
            for (int i = 0; i < numCards; i++) {
                if (adventureDeck.IsDeckEmpty()) {
                    EmptyAdventureDiscard();
                }
                Card card = adventureDeck.DrawCard();
                player.AddCard(card);
            }
        }
    }

    private void EmptyAdventureDiscard() {
        adventureDeck.getCards().addAll(adventureDiscard.getCards());
        Collections.shuffle(adventureDeck.getCards());
        adventureDiscard.getCards().clear();
    }

    @GetMapping("/pickcard")
    private State PickEventCard() {
        Card card = eventDeck.DrawCard();
        eventDiscard.getCards().add(card);
        return new State("P" + currentPlayer + " picked " + card.PrintCard(), "pickcard", card);
    }

    @GetMapping("/gotevent")
    private State GotEventCard(@RequestParam("card") Integer card) {
        State state = new State("", "", null);
        switch (card) {
            case 2:
                GotPlague();
                state =  new State("Uh oh! You just picked a Plague card. You lose 2 shields.", "plague", wrapInfo());
                break;
            case 0:
                GotFavour();
                state = new State("Yay! You were granted with the Queen's Favour! You get 2 Adventure cards.", "queen", wrapInfo());
                break;
            case 1:
                GotProsperity();
                state = new State("Yay! You picked the Prosperity card! All players get 2 Adventure cards.", "prosperity", wrapInfo());
                break;
        }
        return state;
    }

    private void GotPlague() {
        if (players.get(currentPlayer - 1).getShields() < 3) {
            players.get(currentPlayer - 1).setShields(0);
        } else {
            players.get(currentPlayer - 1).setShields(players.get(currentPlayer - 1).getShields() - 2);
        }
    }

    private void GotFavour() {
        for (int i = 0; i < 2; i++) {
            if (adventureDeck.IsDeckEmpty()) {
                EmptyAdventureDiscard();
            }
            Card card = adventureDeck.DrawCard();
            players.get(currentPlayer-1).AddCard(card);
        }
    }

    private void GotProsperity() {
        DistributeCards(players, 2);
    }

    @PostMapping("/trimhand")
    private State TrimHand(@RequestBody Map<String, String> request) {
        int playerId = Integer.parseInt(request.get("id"));
        int index = Integer.parseInt(request.get("input"));
        Player player = players.get(playerId-1);
        Card card = player.PickCard(index);
        if (card != null) {
            adventureDiscard.getCards().add(card);
        }
        boolean isHandValid = player.IsHandValid();
        String context = isHandValid ? "complete" : "progress";

        return new State<>("", context, wrapInfo());
    }

    @PostMapping("/getsponsor")
    private State GetSponsor(@RequestBody Map<String, String> request) {
        int next = Integer.parseInt(request.get("next"));
        String answer = request.get("input");
        int stages = Integer.parseInt(request.get("stages"));

        int current = currentPlayer;
        for (int i=0; i<next; i++) {
            current = GetNextPlayer(current);
        }

        if (answer.equalsIgnoreCase("Y")) {
            ArrayList<Player> initialParticipants = new ArrayList<>(players);
            Player sponsor = initialParticipants.remove(current-1);
            Map<Integer, Deck> initial = new HashMap<>();
            for (int i=0; i<initialParticipants.size(); i++) {
                initial.put(initialParticipants.get(i).getId(), new Deck("empty"));
            }
            currentQuest = new Quest(sponsor, initial, stages);
            return new State<>("Player " + current + " decides to sponsor the quest", "complete", wrapInfo());
        }
        return new State<>("", "progress", wrapInfo());

    }

    private int GetNextPlayer(int current) {
        if (current < 4) {
            current++;
        } else {
            current= 1;
        }
        return current;
    }

    @PostMapping("/buildstage")
    private State AddToStage(@RequestBody Map<String, String> request) {
        int stage = Integer.parseInt(request.get("stageNum"));
        int index = Integer.parseInt(request.get("input"));
        Card card = players.get(currentQuest.GetSponsor()-1).PickCard(index);
        State state = currentQuest.AddCardToStage(stage, card);
        if (Objects.equals(state.context, "invalid")) {
            players.get(currentQuest.GetSponsor()-1).AddCard(card);
        }
        state.data = wrapInfo();
        return state;
    }

    @PostMapping("/endstage")
    private State EndStage(@RequestBody Map<String, String> request) {
        int stage = Integer.parseInt(request.get("stageNum"));
        return currentQuest.EndStage(stage);
    }

    @GetMapping("/completebuilding")
    private State CompleteBuilding() {
        return currentQuest.CompleteBuilding();
    }

    @PostMapping("/addcard")
    private State AddCard(@RequestBody Map<String, String> request) {
        int stage = Integer.parseInt(request.get("stageNum"));
        int index = Integer.parseInt(request.get("input"));
        Card card = currentQuest.GetDeck(stage).getCards().remove(index);
        players.get(currentQuest.GetSponsor()-1).AddCard(card);
        return new State("", "adding", wrapInfo());
    }

    @PostMapping("/leave")
    private State RemoveParticipant(@RequestBody Map<String, String> request) {
        int id = Integer.parseInt(request.get("id"));
        currentQuest.GetParticipants(id);
        return new State("", "removing", wrapInfo());
    }

    @PostMapping("/buildattack")
    private State BuildAttack(@RequestBody Map<String, String> request) {
        int id = Integer.parseInt(request.get("id"));
        int index = Integer.parseInt(request.get("input"));
        Card card = players.get(id-1).PickCard(index);
        State state = currentQuest.AddCardToAttack(id, card);
        if (Objects.equals(state.context, "invalid")) {
            players.get(id-1).AddCard(card);
        }
        state.data = wrapInfo();
        return state;
    }

    @PostMapping("/resolveattack")
    private State ResolveAttack(@RequestBody Map<String, String> request) {
        int id = Integer.parseInt(request.get("id"));
        int stage = Integer.parseInt(request.get("stage"));
        State state = currentQuest.ResolveAttack(id, stage);
        state.data = wrapInfo();
        return state;
    }

    @PostMapping("/returncard")
    private State ReturnCard(@RequestBody Map<String, String> request) {
        int id = Integer.parseInt(request.get("id"));
        int index = Integer.parseInt(request.get("input"));
        Card card = currentQuest.GetParticipant(id).getCards().remove(index);
        players.get(id-1).AddCard(card);
        return new State("", "adding", wrapInfo());
    }
}