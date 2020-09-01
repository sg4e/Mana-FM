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

import java.util.function.Function;
import javafx.scene.control.ListCell;
import lombok.AllArgsConstructor;

/**
 *
 * @author sg4e
 * @param <T>
 */
@AllArgsConstructor
public class GenericListCell<T> extends ListCell<T> {
    
    private final Function<T,String> formatter;
    
    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
        }
        else {
            setText(formatter.apply(item));
        }
    }
}
