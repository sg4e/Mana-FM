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

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author sg4e
 */
public class DeckWindow {
    private final Stage stage;
    private final DeckWindowController controller;
    
    public DeckWindow() throws IOException {
        stage = new Stage();
        stage.setTitle("Deck");
        
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/deck.fxml"));
        VBox vbox = loader.<VBox>load();
        controller = loader.getController();
        controller.setStage(stage);
        Scene mainScene = new Scene(vbox);

        stage.setScene(mainScene);
    }
    
    public void show() {
        if(!stage.isShowing())
            stage.show();
    }
    
    public DeckWindowController getController() {
        return controller;
    }
}
