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
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sg4e.ygofm.gamedata.FMDB;

/**
 *
 * @author sg4e
 */
public class ManaController implements Initializable {
    
    public static final int NUMBER_CARDS_IN_DECK = 40;
    
    @FXML
    public CardCollectionController handCardCollectionController;
    @FXML
    public Label remainingDrawsCount;
    @FXML
    public Button startDuelButton;
    @FXML
    public Button resetDuelButton;
    
    private DeckWindow deckWindow;
    private static final Logger LOG = LogManager.getLogger();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        FMDB fmdb = new FMDB.Builder().excludeDescrptions().build();
        handCardCollectionController.initFmdb(fmdb);
        handCardCollectionController.addOnChangeAction((value) -> {
            remainingDrawsCount.setText(Integer.toString(NUMBER_CARDS_IN_DECK - value));
        });
        handCardCollectionController.setDisable(true);
        startDuelButton.setDisable(true);
        resetDuelButton.setDisable(true);
        handCardCollectionController.setDisable(true);
        try {
            deckWindow = new DeckWindow();
            deckWindow.getController().initFmdb(fmdb);
        }
        catch(Exception ex) {
            LOG.fatal("Could not load deck window", ex);
            Platform.exit();
        }
        deckWindow.getController().getCardCollectionController().addOnChangeAction((deckSize) -> {
                startDuelButton.setDisable(deckSize != NUMBER_CARDS_IN_DECK);
        });
    }
    
    @FXML
    public void showDeckWindow() {
        deckWindow.show();
    }
    
    @FXML
    public void exit() {
        Platform.exit();
    }
    
    @FXML
    public void startDuel() {
        startDuelButton.setDisable(true);
        deckWindow.getController().setDisable(true);
        
        resetDuelButton.setDisable(false);
        handCardCollectionController.setDisable(false);
        
        handCardCollectionController.setCardSuggestionPool(deckWindow.getController().getCardCollectionController().getDeck());
    }
    
    @FXML
    public void resetDuel() {
        startDuelButton.setDisable(false);
        deckWindow.getController().setDisable(false);
        
        resetDuelButton.setDisable(true);
        handCardCollectionController.setDisable(true);
        
        handCardCollectionController.setCardSuggestionPool(null);
        handCardCollectionController.clear();
    }
    
}
