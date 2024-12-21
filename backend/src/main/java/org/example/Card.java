package org.example;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import static org.example.Type.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"type", "name", "code", "value"})
public class Card {
    @JsonProperty("type")
    private Type type;

    @JsonProperty("name")
    private String name;

    @JsonProperty("code")
    private String code;

    @JsonProperty("value")
    private int value;

    public Card(Type type, String info) {
        this.type = type;
        this.name = "";
        this.code = ""; //
        this.value = -1;
        if (type == FOE) {
            this.value = Integer.parseInt(info);
        } else if (type == WEAPON) {
            this.name = info;
            if (info.equalsIgnoreCase("Dagger")) {
                this.code = "D";
                this.value = 5;
            } else if (info.equalsIgnoreCase("Horse")) {
                this.code = "H";
                this.value = 10;
            } else if (info.equalsIgnoreCase("Sword")) {
                this.code = "S";
                this.value = 10;
            } else if (info.equalsIgnoreCase("Battle-axe")) {
                this.code = "B";
                this.value = 15;
            } else if (info.equalsIgnoreCase("Lance")) {
                this.code = "L";
                this.value = 20;
            } else if (info.equalsIgnoreCase("Excalibur")) {
                this.code = "E";
                this.value = 30;
            }
        } else if (type == EVENT) {
            if (info.equalsIgnoreCase("Queen's Favour")) {
                this.value = 0;
            } else if (info.equalsIgnoreCase("Prosperity")) {
                this.value = 1;
            } else if (info.equalsIgnoreCase("Plague")) {
                this.value = 2;
            }
            this.name = info;
        } else if (type == QUEST) {
            this.value = Integer.parseInt(info);
        }
    }

    public String PrintCard() {
        String message = "";
        if (type == Type.QUEST) {
            message = "a quest of " + value + " stages";
        } else if (type == Type.EVENT) {
            message = name;
        }
        return message;
    }

    public String getCode() {
        return code;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }
}