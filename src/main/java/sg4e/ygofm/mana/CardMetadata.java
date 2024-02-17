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
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import moe.maika.ygofm.gamedata.Card;

/**
 *
 * @author sg4e
 */
@RequiredArgsConstructor
public class CardMetadata {
    @Getter
    private final Card card;
    private final BooleanProperty speculative = new SimpleBooleanProperty(false);
    
    public BooleanProperty speculativeProperty() {
        return speculative;
    }
    
    public void setSpeculative(boolean speculative) {
        speculativeProperty().set(speculative);
    }
    
    public boolean isSpeculative() {
        return speculativeProperty().get();
    }
}
