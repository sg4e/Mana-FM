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

import java.math.BigInteger;
import java.util.Locale;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import moe.maika.ygofm.gamedata.SeedSearch;

/**
 *
 * @author sg4e
 */
public class SettingsController {
    
    public static final int DEFAULT_SEARCH_START = 0;
    public static final int DEFAULT_SEARCH_END = SeedSearch.DEFAULT_SEARCH_SPACE;
    static final String ERROR_NOT_A_NUMBER = "Seed search space must be a number";
    static final String ERROR_NEGATIVE = "Seed search space must be non-negative";
    static final String ERROR_NOT_A_32_BIT_INTEGER = String.format(Locale.getDefault(), "Seed search space value cannot exceed %,d", Integer.MAX_VALUE);
    
    @FXML
    public TextField searchStartField;
    @FXML
    public TextField searchEndField;
    
    @Setter
    Stage window;
    @Getter
    private int searchStart = DEFAULT_SEARCH_START;
    @Getter
    private int searchEnd = DEFAULT_SEARCH_END;
    
    public void show() {
        //set to current settings
        searchStartField.setText(Integer.toString(searchStart));
        searchEndField.setText(Integer.toString(searchEnd));
        
        window.show();
        window.toFront();
    }
    
    public void cancel() {
        window.hide();
    }
    
    @FXML
    public void validateAndClose() {
        BigInteger checkStart, checkEnd;
        try {
            checkStart = parseNumberInput(searchStartField.getText());
            checkEnd = parseNumberInput(searchEndField.getText());
        }
        catch(Exception ex) {
            showError(ERROR_NOT_A_NUMBER);
            return;
        }
        if(checkStart.signum() == -1 || checkEnd.signum() == -1) {
            showError(ERROR_NEGATIVE);
            return;
        }
        BigInteger max = BigInteger.valueOf(Integer.MAX_VALUE);
        if(checkStart.compareTo(max) == 1 || checkEnd.compareTo(max) == 1) {
            showError(ERROR_NOT_A_32_BIT_INTEGER);
            return;
        }
        //this one HAS to be last because it switches the values
        if(checkStart.compareTo(checkEnd) == 1) {
            //it's valid to give the values in reverse order, but the backend will evaluate from lower seed -> higher seed anyway
            var temp1 = checkStart;
            var temp2 = checkEnd;
            checkStart = temp2;
            checkEnd = temp1;
        }
        searchStart = checkStart.intValueExact();
        searchEnd = checkEnd.intValueExact();
        
        window.hide();
    }
    
    private BigInteger parseNumberInput(String input) {
        return new BigInteger(input.replaceAll("\\D", input));
    }
    
    void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }
    
}
