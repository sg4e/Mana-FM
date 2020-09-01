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
import javafx.util.Callback;
import lombok.AllArgsConstructor;

/**
 *
 * @author sg4e
 * @param <U>
 * @param <T>
 */
@AllArgsConstructor
public class GenericCellFactory<U, T> implements Callback<U, ListCell<T>> {
    
    private final Function<T,String> formatter;

    @Override
    public ListCell<T> call(U param) {
        return new GenericListCell<T>(formatter);
    }
    
}
