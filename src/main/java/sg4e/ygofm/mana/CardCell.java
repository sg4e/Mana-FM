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

import javafx.scene.control.ListCell;
import sg4e.ygofm.gamedata.Card;

/**
 *
 * @author sg4e
 */
public class CardCell extends ListCell<Card> {
    @Override
    protected void updateItem(Card item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
        }
        else {
            setText(format(item));
        }
    }
    
    public static String format(Card card) {
        return String.format("#%s %s", card.getId(), card.getName());
    }
}
