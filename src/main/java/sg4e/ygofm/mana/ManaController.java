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

import afester.javafx.svg.SvgLoader;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
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
    public ListView<Card> deckList;
    
    private final FMDB fmdb;
    private final TreeMap<String,Card> cardNameMap;
    private int cardCount;
    
    public ManaController(FMDB fmdb) {
        this.fmdb = fmdb;
        cardNameMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        cardNameMap.putAll(fmdb.getAllCards().stream().collect(Collectors.toMap(Card::getName, Function.identity())));
    }
    
    public void initComponents() {
        var settings = TextFields.bindAutoCompletion(deckCardEntry, cardNameMap.keySet());
        settings.setPrefWidth(deckCardEntry.getPrefWidth());
        var svgLoad = new SvgLoader();
        Group dupGraphic = getGraphicFromSvg(svgLoad, "/glyphs/clone-regular.svg");
        Group deleteGraphic = getGraphicFromSvg(svgLoad, "/glyphs/trash-alt-regular.svg");
        deckList.setCellFactory(param -> {
            var cell = new CardCell();
            var contextMenu = new ContextMenu();
            var duplicateMenuItem = new MenuItem();
            duplicateMenuItem.setText("Duplicate");
            duplicateMenuItem.setGraphic(dupGraphic);
            duplicateMenuItem.setOnAction(event -> addCardToDeck(cell.getItem()));
            
            var deleteMenuItem = new MenuItem();
            deleteMenuItem.setText("Remove");
            deleteMenuItem.setGraphic(deleteGraphic);
            deleteMenuItem.setOnAction(event -> {
                //need to be careful for the case where multiple copies are in the deck
                deckList.getItems().remove(cell.getIndex());
                cardCount--;
                updateCountLabel();
            });
            
            contextMenu.getItems().addAll(duplicateMenuItem, deleteMenuItem);
            
            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                }
            });
            
            return cell;
        });
    }
    
    private static Group getGraphicFromSvg(SvgLoader svgLoad, String glyphResource) {
        Group dupIcon = svgLoad.loadSvg(ManaController.class.getResourceAsStream(glyphResource));
        dupIcon.setScaleX(0.035);
        dupIcon.setScaleY(0.035);
        //need to encapsulate or sizing doesn't work
        return new Group(dupIcon);
    }
    
    @FXML
    public void processDeckInput(KeyEvent keyEvent) {
        if(keyEvent.getCode() == KeyCode.ENTER)
            processAddButtonAction(null);
    }
    
    @FXML
    public void processAddButtonAction(Event e) {
        String input = deckCardEntry.getText().trim();
        Card card;
        try {
            int id = Integer.parseInt(input);
            card = fmdb.getCard(id);
        }
        catch(NumberFormatException ex) {
            //not at parsable number, so maybe it's the card name
            card = cardNameMap.get(input);
        }
        if(card != null) {
            addCardToDeck(card);
        }
        else {
            setDeckErrorMessage("Invalid card");
        }
    }
    
    private void addCardToDeck(Card card) {
        //validation
        var items = deckList.getItems();
        if(items.size() >= 40) {
            setDeckErrorMessage("Deck is full");
            return;
        }
        if(items.stream().filter(card::equals).count() >= 3) {
            setDeckErrorMessage("Deck already has 3 copies of " + card.getName());
            return;
        }
        deckList.getItems().add(card);
        deckList.scrollTo(items.size() - 1);
        deckCardLabel.setText(CardCell.format(card));
        deckCardLabel.setStyle(null);
        deckCardEntry.setText("");
        cardCount++;
        updateCountLabel();
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
