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

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import sg4e.ygofm.gamedata.FMDB;

/**
 *
 * @author sg4e
 */
public class Mana extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FMDB fmdb = new FMDB.Builder().excludeDescrptions().build();
        
        primaryStage.setTitle("Mana");
        
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/main.fxml"));
        var controller = new ManaController(fmdb);
        loader.setController(controller);
        VBox vbox = loader.<VBox>load();
        Scene mainScene = new Scene(vbox);
        controller.initComponents();

        primaryStage.setScene(mainScene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch();
    }
}
