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

import afester.javafx.svg.SvgLoader;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sg4e.ygofm.gamedata.Card;

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
    private static final String SPECULATIVE_GRAPHIC_RESOURCE = "/glyphs/question-circle-regular.svg";
    private static final SvgLoader SVG_LOADER = new SvgLoader();
    private static final Logger LOG = LogManager.getLogger();
    private static byte[] iconBytes;
    
    @Override
    protected void updateItem(CardMetadata item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            if(speculative != null)
                speculative.removeListener(speculativeListener);
            speculative = null;
            setText(null);
            setGraphic(null);
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
            setGraphic(loadIcon());
        }
        else {
            getStyleClass().removeAll(CSS_CLASS_SPECULATIVE);
            setGraphic(null);
        }
    }
    
    private static Node loadIcon() {
        if(iconBytes == null) {
            try {
                iconBytes = Files.readAllBytes(Path.of(CardCell.class.getResource(SPECULATIVE_GRAPHIC_RESOURCE).toURI()));
            }
            catch(Exception ex) {
                LOG.error("Cannot open speculative-icon resource", ex);
            }
        }
        if(iconBytes == null)
            return null;
        else {
            return CardCollectionController.getGraphicFromSvg(SVG_LOADER, new ByteArrayInputStream(iconBytes));
        }
    }
    
    public static String format(Card card) {
        return String.format("#%s %s", card.getId(), card.getName());
    }
}
