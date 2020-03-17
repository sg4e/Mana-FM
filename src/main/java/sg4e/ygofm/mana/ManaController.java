/*
 * Copyright (C) 2020 sg4e
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package sg4e.ygofm.mana;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.controlsfx.control.textfield.TextFields;
import sg4e.ygofm.gamedata.Card;
import sg4e.ygofm.gamedata.FMDB;

/**
 *
 * @author sg4e
 */
public class ManaController {
    
    public TextField deckCardEntry;
    public Label deckCardLabel;
    public Label cardCountLabel;
    public ListView<String> deckList;
    
    private final FMDB fmdb;
    private final Map<String,Card> cardNameMap;
    private int cardCount;
    
    public ManaController(FMDB fmdb) {
        this.fmdb = fmdb;
        cardNameMap = fmdb.getAllCards().stream().collect(Collectors.toMap(Card::getName, Function.identity()));
    }
    
    public void initComponents() {
        var settings = TextFields.bindAutoCompletion(deckCardEntry, cardNameMap.keySet());
        settings.setPrefWidth(deckCardEntry.getPrefWidth());
    }
    
    @FXML
    public void processDeckInput(KeyEvent keyEvent) {
        if(keyEvent.getCode() == KeyCode.ENTER)
            addCardToDeck(null);
    }
    
    @FXML
    public void addCardToDeck(Event e) {
        String input = deckCardEntry.getText().trim();
        Card card = null;
        try {
            int id = Integer.parseInt(input);
            card = fmdb.getCard(id);
        }
        catch(NumberFormatException ex) {
            //not at parsable number, so maybe it's the card name
            card = cardNameMap.keySet().stream().filter(input::equalsIgnoreCase).findAny().map(cardNameMap::get).orElse(null);
        }
        if(card != null) {
            //validation
            var items = deckList.getItems();
            var cardName = card.getName();
            if(items.size() >= 40) {
                setDeckErrorMessage("Deck is full");
                return;
            }
            if(items.stream().filter(item -> item.endsWith(cardName)).count() >= 3) {
                setDeckErrorMessage("Deck already has 3 copies of " + card.getName());
                return;
            }
            var text = "#" + card.getId() + " " + card.getName();
            deckList.getItems().add(text);
            deckList.scrollTo(items.size() - 1);
            deckCardLabel.setText(text);
            deckCardLabel.setStyle(null);
            deckCardEntry.setText("");
            cardCount++;
            updateCountLabel();
        }
        else {
            setDeckErrorMessage("Invalid card");
        }
    }
    
    private void setDeckErrorMessage(String message) {
        deckCardLabel.setText(message);
        deckCardLabel.setStyle("-fx-font-style: italic; -fx-text-fill: red;");
    }
    
    private void updateCountLabel() {
        cardCountLabel.setText("Deck Size: " + cardCount);
    }
    
    @FXML
    public void exit() {
        System.exit(0);
    }
    
}
