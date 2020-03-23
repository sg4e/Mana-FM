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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import sg4e.ygofm.gamedata.FMDB;

/**
 *
 * @author sg4e
 */
public class ManaController implements Initializable {
    
    //SO: The field name must match the fx:id attribute string + "Controller"
    @FXML
    public CardCollectionController deckCardCollectionController;
    
    @FXML
    public Label cardCountLabel;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        FMDB fmdb = new FMDB.Builder().excludeDescrptions().build();
        deckCardCollectionController.initFmdb(fmdb);
        deckCardCollectionController.setOnChangeAction(cardCount -> cardCountLabel.setText("Deck Size: " + cardCount));
    }
    
    @FXML
    public void exit() {
        System.exit(0);
    }
    
}
