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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import afester.javafx.svg.SvgLoader;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import moe.maika.ygofm.gamedata.Card;
import moe.maika.ygofm.gamedata.FMDB;

/**
 *
 * @author sg4e
 */
public class CardCollectionController implements Initializable {
    
    @FXML
    public TextField deckCardEntry;
    @FXML
    public Label deckCardLabel;
    @FXML
    public ListView<CardMetadata> deckList;
    @FXML
    public Button addButton;
    
    private FMDB fmdb;
    private TreeMap<String,Card> cardNameMap;
    private int cardCount;
    private List<Consumer<Integer>> onCardCollectionChangeListeners;
    private Set<Card> acceptableCards;
    private Map<Card,Long> cardCopies;
    private AutoCompletionBinding autocompletion;
    
    public void initFmdb(FMDB fmdb) {
        this.fmdb = fmdb;
        cardNameMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        cardNameMap.putAll(fmdb.getAllCards().stream().collect(Collectors.toMap(Card::getName, Function.identity())));
        acceptableCards = fmdb.getAllCards();
        cardCopies = new HashMap<>();
        updateAutocompletion(null);
    }
    
    private void updateAutocompletion(Collection<Card> suggestions) {
        if(autocompletion != null)
            autocompletion.dispose();
        autocompletion = TextFields.bindAutoCompletion(deckCardEntry, suggestions == null ? cardNameMap.keySet() : 
                suggestions.stream().map(Card::getName).distinct().collect(Collectors.toList()));
        autocompletion.setPrefWidth(deckCardEntry.getPrefWidth());
    }
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        onCardCollectionChangeListeners = new ArrayList<>();
        var svgLoad = new SvgLoader();
        Group dupGraphic = getGraphicFromSvg(svgLoad, "/glyphs/clone-regular.svg");
        Group deleteGraphic = getGraphicFromSvg(svgLoad, "/glyphs/trash-alt-regular.svg");
        deckList.setCellFactory(param -> {
            var cell = new CardCell();
            var contextMenu = new ContextMenu();
            var duplicateMenuItem = new MenuItem();
            duplicateMenuItem.setText("Duplicate");
            duplicateMenuItem.setGraphic(dupGraphic);
            duplicateMenuItem.setOnAction(event -> addCardToDeck(cell.getItem().getCard()));
            
            var deleteMenuItem = new MenuItem();
            deleteMenuItem.setText("Remove");
            deleteMenuItem.setGraphic(deleteGraphic);
            deleteMenuItem.setOnAction(event -> {
                //need to be careful for the case where multiple copies are in the deck
                deckList.getItems().remove(cell.getIndex());
                cardCount--;
                removeAllSpeculative();
                alertChangeListeners();
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
        Group dupIcon = svgLoad.loadSvg(CardCollectionController.class.getResourceAsStream(glyphResource));
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
    
    public void addCardToDeck(Card card) {
        addCardToDeck(card, false);
    }
    
    public void addCardToDeck(Card card, boolean speculative) {
        //validation: this is sufficient for private calls but external calls may need additional validation
        List<Card> items = getModelAsCards();
        if(!acceptableCards.contains(card)) {
            setDeckErrorMessage("Card not in deck");
            return;
        }
        if(cardCopies.containsKey(card)) {
            long currentCopies = deckList.getItems().stream().filter(card::equals).count();
            if(currentCopies >= cardCopies.get(card)) {
                setDeckErrorMessage("No additional copies");
                return;
            }
        }
        if(cardCount >= 40) {
            setDeckErrorMessage("Deck is full");
            return;
        }
        if(items.stream().filter(card::equals).count() >= 3) {
            setDeckErrorMessage("Deck already has 3 copies of " + card.getName());
            return;
        }
        //disallow multiple copies of Exodia pieces
        int id = card.getId();
        if(items.contains(card) && id >= 17 && id <= 21) {
            setDeckErrorMessage("Deck cannot have duplicates of Exodia pieces");
            return;
        }
        CardMetadata cardMeta = new CardMetadata(card);
        cardMeta.setSpeculative(speculative);
        if(!speculative) {
            var firstSpeculative = deckList.getItems().stream().filter(CardMetadata::isSpeculative).findFirst();
            if(firstSpeculative.isPresent()) {
                if(card != firstSpeculative.get().getCard()) {
                    //real draw conflicts with projected draw, so seed does not match. Clear all speculative draws
                    removeAllSpeculative();
                }
            }
        }
        if(!speculative && deckList.getItems().size() > cardCount && deckList.getItems().get(cardCount).getCard() == card) {
            //speculated card has been drawn; update style
            deckList.getItems().get(cardCount).setSpeculative(false);
        }
        else {
            deckList.getItems().add(cardMeta);
        }
        deckList.scrollTo(cardCount);
        deckCardLabel.setText(CardCell.format(card));
        deckCardLabel.setStyle(null);
        deckCardEntry.setText("");
        if(!speculative) {
            cardCount++;
        }
        alertChangeListeners();
    }
    
    public void removeAllSpeculative() {
        deckList.getItems().removeIf(CardMetadata::isSpeculative);
        cardCount = (int) deckList.getItems().stream().filter(cm -> !cm.isSpeculative()).count();
        alertChangeListeners();
    }
    
    public void addSpeculativeCards(List<Card> simulationResults) {
        removeAllSpeculative();
        simulationResults.subList(cardCount, simulationResults.size()).forEach(c -> addCardToDeck(c, true));
    }
    
    private List<Card> getModelAsCards() {
        return deckList.getItems().stream().map(CardMetadata::getCard).collect(Collectors.toList());
    }
    
    private void setDeckErrorMessage(String message) {
        deckCardLabel.setText(message);
        deckCardLabel.setStyle("-fx-font-style: italic; -fx-text-fill: red;");
    }
    
    private void alertChangeListeners() {
        onCardCollectionChangeListeners.forEach(l -> l.accept(cardCount));
    }
    
    public void addOnChangeAction(Consumer<Integer> cardCountConsumer) {
        onCardCollectionChangeListeners.add(cardCountConsumer);
    }
    
    public int getCardCount() {
        return cardCount;
    }
    
    public void setDisable(boolean disable) {
        addButton.setDisable(disable);
        deckList.setDisable(disable);
        deckCardEntry.setDisable(disable);
    }
    
    public void setCardSuggestionPool(Collection<Card> cards) {
        updateAutocompletion(cards);
        if(cards == null) {
            acceptableCards = fmdb.getAllCards();
            cardCopies = new HashMap<>();
        }
        else {
            acceptableCards = new HashSet<>(cards);
            cardCopies = cards.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        }
    }
    
    public void clear() {
        setCardSuggestionPool(null);
        deckList.getItems().clear();
        deckCardEntry.setText("");
        deckCardLabel.setText("");
        cardCount = 0;
        alertChangeListeners();
    }
    
    /**
     * Returns all non-speculative cards.
     * 
     * @return 
     */
    public List<Card> getDeck() {
        return deckList.getItems().stream().filter(cm -> !cm.isSpeculative()).map(CardMetadata::getCard).collect(Collectors.toList());
    }
    
    public Stream<Card> stream() {
        return deckList.getItems().stream().map(CardMetadata::getCard);
    }
    
}
