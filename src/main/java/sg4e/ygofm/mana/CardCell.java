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

import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.ListCell;
import moe.maika.ygofm.gamedata.Card;

/**
 *
 * @author sg4e
 */
public class CardCell extends ListCell<CardMetadata> {
    public static final String CSS_CLASS_SPECULATIVE = "speculative";
    
    private BooleanProperty speculative;
    private final ChangeListener<Boolean> speculativeListener = (observable, oldValue, newValue) -> {
        updateCardStyle(newValue);
    };
    
    @Override
    protected void updateItem(CardMetadata item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            if(speculative != null)
                speculative.removeListener(speculativeListener);
            speculative = null;
            setText(null);
            getStyleClass().removeAll(CSS_CLASS_SPECULATIVE);
        }
        else {
            setText(format(item.getCard()));
            speculative = item.speculativeProperty();
            speculative.addListener(speculativeListener);
            updateCardStyle(speculative.get());
        }
    }
    
    private void updateCardStyle(boolean speculative) {
        if(speculative) {
            getStyleClass().add(CSS_CLASS_SPECULATIVE);
        }
        else {
            getStyleClass().removeAll(CSS_CLASS_SPECULATIVE);
        }
    }
    
    public static String format(Card card) {
        return String.format("#%s %s", card.getId(), card.getName());
    }
}
