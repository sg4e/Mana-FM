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

import java.util.stream.Stream;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.mockito.Mockito.*;
import org.testfx.framework.junit5.ApplicationExtension;

/**
 *
 * @author sg4e
 */
@ExtendWith(ApplicationExtension.class)
public class SettingsControllerTest {
    
    SettingsController c;
    
    @BeforeEach
    public void setup() {
        c = spy(SettingsController.class);
        //dummy out the ui method so the test doesn't hang
        doNothing().when(c).showError(anyString());
        c.setWindow(mock(Stage.class));
    }
    
    private void initFields(String field1, String field2) {
        c.searchStartField = new TextField(field1);
        c.searchEndField = new TextField(field2);
        c.validateAndClose();
    }

    @ParameterizedTest
    @MethodSource("provideBadSeedSearchInput")
    public void testValidationFails(String field1, String field2, String error) {
        initFields(field1, field2);
        assertEquals(SettingsController.DEFAULT_SEARCH_START, c.getSearchStart());
        assertEquals(SettingsController.DEFAULT_SEARCH_END, c.getSearchEnd());
        verify(c).showError(error);
    }
    
    @ParameterizedTest
    @MethodSource("provideGoodSeedSearchInput")
    public void testValidationSucceeds(String field1, String field2) {
        initFields(field1, field2);
        assertEquals(Integer.parseInt(field1), c.getSearchStart());
        assertEquals(Integer.parseInt(field2), c.getSearchEnd());
        verify(c, never()).showError(anyString());
    }
    
    @Test
    public void testReverseRangeValidation() {
        initFields("500", "400");
        assertEquals(400, c.getSearchStart());
        assertEquals(500, c.getSearchEnd());
        verify(c, never()).showError(anyString());
    }
    
    private static Stream<Arguments> provideBadSeedSearchInput() {
        return Stream.of(
                Arguments.of("Maika", "Sakuranomiya", SettingsController.ERROR_NOT_A_NUMBER),
                Arguments.of("", "", SettingsController.ERROR_NOT_A_NUMBER),
                Arguments.of("-1", "1", SettingsController.ERROR_NEGATIVE),
                Arguments.of("0", Long.toString(Long.valueOf(Integer.MAX_VALUE) + 1L), SettingsController.ERROR_NOT_A_32_BIT_INTEGER)
        );
    }
    
    private static Stream<Arguments> provideGoodSeedSearchInput() {
        return Stream.of(
                Arguments.of("1", "300"),
                Arguments.of("0", Integer.toString(Integer.MAX_VALUE))
        );
    }
    
}
