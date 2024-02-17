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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import moe.maika.ygofm.gamedata.Card;
import moe.maika.ygofm.gamedata.FMDB;

/**
 *
 * @author sg4e
 */
public class DeckWindowController implements Initializable {
    
    //SO: The field name must match the fx:id attribute string + "Controller"
    @FXML
    public CardCollectionController deckCardCollectionController;
    @FXML
    public Label cardCountLabel;
    @FXML
    public MenuItem clearDeckMenuItem;
    @FXML
    public MenuItem importMenuItem;
    @FXML
    public MenuItem exportMenuItem;
    
    private Stage parentStage;
    private FMDB fmdb;
    private static final FileChooser.ExtensionFilter DSV_EXT_FILTER = new FileChooser.ExtensionFilter("DSV files (*.dsv)", "*.dsv");
    
    private static final Logger LOG = LogManager.getLogger();
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        
    }
    
    public void initFmdb(FMDB fmdb) {
        this.fmdb = fmdb;
        deckCardCollectionController.initFmdb(fmdb);
        deckCardCollectionController.addOnChangeAction(cardCount -> cardCountLabel.setText("Deck Size: " + cardCount));
    }
    
    public void setStage(Stage stage) {
        parentStage = stage;
    }
    
    @FXML
    public void close() {
        parentStage.close();
    }
    
    @FXML
    public void clearDeck() {
        deckCardCollectionController.clear();
    }
    
    public CardCollectionController getCardCollectionController() {
        return deckCardCollectionController;
    }
    
    public void setDisable(boolean disable) {
        deckCardCollectionController.setDisable(disable);
        clearDeckMenuItem.setDisable(disable);
        importMenuItem.setDisable(disable);
    }
    
    @FXML
    public void importDeckAsDSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load deck");
        fileChooser.setInitialFileName("deck.dsv");
        fileChooser.getExtensionFilters().add(DSV_EXT_FILTER);
        File importFile = fileChooser.showOpenDialog(parentStage);
        if(importMenuItem.isDisable()) {
            LOG.warn("File chooser was open after import was disabled. Aborting import");
        }
        else {
            //spec: ids separated by whitespace
            try {
                String contents = Files.readString(importFile.toPath());
                List<Card> cards = Arrays.stream(contents.split("\\s"))
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Integer::parseInt)
                        .map(fmdb::getCard)
                        .collect(Collectors.toList());
                if(cards.stream().anyMatch(Objects::isNull)) {
                    LOG.error("Import file contains an invalid card id");
                }
                else if(cards.size() > 40) {
                    LOG.error("Import file contains more than 40 cards");
                }
                else if(cards.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).values().stream().anyMatch(value -> value > 3)) {
                    LOG.error("Import file contains more than 3 copies of a card");
                }
                else {
                    deckCardCollectionController.clear();
                    cards.forEach(deckCardCollectionController::addCardToDeck);
                }
            }
            catch(Exception ex) {
                LOG.error("Could not import deck", ex);
            }
        }
    }
    
    @FXML
    public void exportDeckAsDSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save deck");
        fileChooser.getExtensionFilters().add(DSV_EXT_FILTER);
        fileChooser.setInitialFileName("deck.dsv");
        File exportFile = fileChooser.showSaveDialog(parentStage);
        try {
            String filename = exportFile.getName();
            if(!filename.endsWith(".dsv")) {
                filename += ".dsv";
            }
            //note: if the user wants to save to a file called "deck" and not overwrite a file called "deck.csv", then sucks to be them
            try(BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
                List<Integer> ids = deckCardCollectionController.stream().map(Card::getId).collect(Collectors.toList());
                for(int id : ids) {
                    writer.write(id + "\r\n");
                }
            }
        }
        catch(Exception ex) {
            LOG.error("Could not export deck", ex);
        }
    }
    
}
