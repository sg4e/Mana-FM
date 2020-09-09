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

import javafx.concurrent.Task;

/**
 * A Task whose progress isn't set to -1 on construction.
 * 
 * @author sg4e
 * @param <T>
 */
public abstract class NonnegativeProgressTask<T> extends Task<T> {
    public NonnegativeProgressTask() {
        super();
        updateProgress(0L, Long.MAX_VALUE);
    }
}
