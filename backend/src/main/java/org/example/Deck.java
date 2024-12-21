package org.example;

import java.util.ArrayList;
import java.util.Collections;

public class Deck {
    private ArrayList<Card> cards;

    public Deck(String type) {
        cards = new ArrayList<>();
        switch (type) {
            case "adventure":
                cards.addAll(InitializeFoes());
                cards.addAll(InitializeWeapons());
                Collections.shuffle(cards);
                break;
            case "event":
                cards.addAll(InitializeEvents());
                Collections.shuffle(cards);
                break;
            case "empty":
                break;
        }
    }

    private ArrayList<Card> InitializeFoes() {
        ArrayList<Card> foes = new ArrayList<>();
        for (int i=0; i<8; i++) {
            Card card = new Card(Type.FOE, "5");
            foes.add(card);
        }

        for (int i=0; i<7; i++) {
            Card card = new Card(Type.FOE, "10");
            foes.add(card);
        }

        for (int i=0; i<8; i++) {
            Card card = new Card(Type.FOE, "15");
            foes.add(card);
        }

        for (int i=0; i<7; i++) {
            Card card = new Card(Type.FOE, "20");
            foes.add(card);
        }

        for (int i=0; i<7; i++) {
            Card card = new Card(Type.FOE, "25");
            foes.add(card);
        }

        for (int i=0; i<4; i++) {
            Card card = new Card(Type.FOE, "30");
            foes.add(card);
        }

        for (int i=0; i<4; i++) {
            Card card = new Card(Type.FOE,  "35");
            foes.add(card);
        }

        for (int i=0; i<2; i++) {
            Card card = new Card(Type.FOE, "40");
            foes.add(card);
        }

        for (int i=0; i<2; i++) {
            Card card = new Card(Type.FOE, "50");
            foes.add(card);
        }

        Card card = new Card(Type.FOE, "70");
        foes.add(card);

        return foes;
    }

    private ArrayList<Card> InitializeWeapons() {
        ArrayList<Card> weapons = new ArrayList<>();
        for (int i=0; i<6; i++) {
            Card card = new Card(Type.WEAPON, "Dagger");
            weapons.add(card);
        }

        for (int i=0; i<16; i++) {
            Card card = new Card(Type.WEAPON, "Sword");
            weapons.add(card);
        }

        for (int i=0; i<12; i++) {
            Card card = new Card(Type.WEAPON, "Horse");
            weapons.add(card);
        }

        for (int i=0; i<8; i++) {
            Card card = new Card(Type.WEAPON, "Battle-axe");
            weapons.add(card);
        }

        for (int i=0; i<6; i++) {
            Card card = new Card(Type.WEAPON, "Lance");
            weapons.add(card);
        }

        for (int i=0; i<2; i++) {
            Card card = new Card(Type.WEAPON, "Excalibur");
            weapons.add(card);
        }

        return weapons;
    }

    private ArrayList<Card> InitializeEvents() {
        ArrayList<Card> e_deck = new ArrayList<>();
        for (int i=0; i<3; i++) {
            Card card = new Card(Type.QUEST,  "2");
            e_deck.add(card);
        }

        for (int i=0; i<4; i++) {
            Card card = new Card(Type.QUEST, "3");
            e_deck.add(card);
        }

        for (int i=0; i<3; i++) {
            Card card = new Card(Type.QUEST, "4");
            e_deck.add(card);
        }

        for (int i=0; i<2; i++) {
            Card card = new Card(Type.QUEST, "5");
            e_deck.add(card);
        }

        for (int i=0; i<2; i++) {
            Card card = new Card(Type.EVENT, "Queen's favour");
            e_deck.add(card);
        }

        for (int i=0; i<2; i++) {
            Card card = new Card(Type.EVENT, "Prosperity");
            e_deck.add(card);
        }

        Card card = new Card(Type.EVENT, "Plague");
        e_deck.add(card);
        return e_deck;
    }

    public int GetDeckSize() {
        return cards.size();
    }

    public Card DrawCard() {
        if (GetDeckSize() < 1) {
            return null;
        }
        return cards.remove(0);
    }

    public void AddCard(Card card) {
        if (card.getType() == Type.FOE) {
            int index = 0;
            while (index < GetDeckSize() && cards.get(index).getType() == Type.FOE && cards.get(index).getValue() < card.getValue()) {
                index++;
            }
            cards.add(index, card);
        } else if (card.getType() == Type.WEAPON) {
            int index = 0;
            while (index < GetDeckSize() && cards.get(index).getType() == Type.FOE) {
                index++;
            }
            while (index < GetDeckSize() && cards.get(index).getType() == Type.WEAPON) {
                if (cards.get(index).getValue() > card.getValue()) {
                    break;
                } else if (cards.get(index).getValue() == card.getValue()) {
                    if (cards.get(index).getCode().equals("H") && card.getCode().equals("S")) {
                        break;
                    }
                }
                index++;
            }
            cards.add(index, card);
        }
    }

    public boolean IsDeckEmpty() {
        return GetDeckSize() == 0;
    }

    public ArrayList<Card> getCards() {
        return cards;
    }
}
