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

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sg4e.ygofm.gamedata.Card;
import sg4e.ygofm.gamedata.Deck;
import sg4e.ygofm.gamedata.Duelist;
import sg4e.ygofm.gamedata.FMDB;
import sg4e.ygofm.gamedata.RNG;
import sg4e.ygofm.gamedata.SeedSearch;

/**
 *
 * @author sg4e
 */
public class ManaController implements Initializable {
    
    public static final int NUMBER_CARDS_IN_DECK = 40;
    
    @FXML
    public CardCollectionController handCardCollectionController;
    @FXML
    public Label remainingDrawsCount;
    @FXML
    public Button startDuelButton;
    @FXML
    public Button resetDuelButton;
    @FXML
    public Button searchSeedsButton;
    @FXML
    public ComboBox<Duelist> duelistComboBox;
    @FXML
    public ComboBox<Comparator<Card>> deckSortComboBox;
    @FXML
    public ListView<RNG> seedList;
    @FXML
    public Label statusLabel;
    @FXML
    public ListView<CardMetadata> aiDeckList;
    @FXML
    public ProgressBar searchProgressBar;
    
    private DeckWindow deckWindow;
    private FMDB fmdb;
    private static final Logger LOG = LogManager.getLogger();
    private boolean isSearching = false;
    private final Executor executor = Executors.newFixedThreadPool(1, (Runnable r) -> {
        Thread t = Executors.defaultThreadFactory().newThread(r);
        t.setDaemon(true);
        return t;
    });
    //this field is initialized much later
    private Stage parent;
    private VBox settingsVBox;
    private Stage settingsStage;
    private SettingsController settingsController;

    private Task<Void> currentTask;
    private volatile SeedSearch seedSearch = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fmdb = new FMDB.Builder().excludeDescrptions().build();
        handCardCollectionController.initFmdb(fmdb);
        handCardCollectionController.addOnChangeAction((value) -> {
            remainingDrawsCount.setText(Integer.toString(NUMBER_CARDS_IN_DECK - value));
        });
        handCardCollectionController.setDisable(true);
        startDuelButton.setDisable(true);
        resetDuelButton.setDisable(true);
        searchSeedsButton.setDisable(true);
        handCardCollectionController.setDisable(true);
        try {
            deckWindow = new DeckWindow();
            deckWindow.getController().initFmdb(fmdb);
        }
        catch(Exception ex) {
            LOG.fatal("Could not load deck window", ex);
            Platform.exit();
        }
        deckWindow.getController().getCardCollectionController().addOnChangeAction((deckSize) -> {
            checkAndEnableStartDuelButton();
        });
        //initialize duelists
        List<Duelist> duelists = Arrays.stream(Duelist.Name.values()).map(fmdb::getDuelist).collect(Collectors.toList());
        duelistComboBox.getItems().addAll(duelists);
        duelistComboBox.getSelectionModel().select(fmdb.getDuelist(Duelist.Name.HEISHIN_2));
        //initialize decksorts
        deckSortComboBox.setCellFactory(new GenericCellFactory<>(c -> c.toString().replace("Japanese", "JP")));
        deckSortComboBox.getItems().addAll(Deck.getAllSorts());
        deckSortComboBox.getSelectionModel().select(Deck.CARD_ID_ORDER);
        
        seedList.setCellFactory(new GenericCellFactory<>((rng) -> Integer.toString(rng.getDelta())));
        seedList.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends RNG> ov, RNG oldSelection, RNG newSelection) -> {
            aiDeckList.getItems().clear();
            if(newSelection != null) {
                RNG copyRNG = new RNG(newSelection);
                //simulate the whole process
                Deck playersDeck = new Deck(deckWindow.getController().getCardCollectionController().getDeck());
                playersDeck.shuffle(copyRNG, deckSortComboBox.getValue());
                handCardCollectionController.addSpeculativeCards(playersDeck.toList());
                Deck aiDeck = Deck.createDuelistDeck(duelistComboBox.getSelectionModel().getSelectedItem(), copyRNG);
                aiDeck.shuffle(copyRNG);
                aiDeckList.setItems(FXCollections.observableList(aiDeck.toList().stream().map(CardMetadata::new).collect(Collectors.toList())));
            }
        });
        
        aiDeckList.setCellFactory((param) -> new CardCell());
        
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/settings.fxml"));
            settingsVBox = loader.<VBox>load();
            settingsController = loader.getController();
            settingsStage = new Stage();
            settingsStage.setTitle("Settings");
            settingsStage.setScene(new Scene(settingsVBox));
            settingsController.setWindow(settingsStage);
            settingsStage.setResizable(false);
        }
        catch(IOException ex) {
            LOG.error("Could not load settings window", ex);
        }
    }
    
    @FXML
    public void showSettings() {
        if(settingsController != null) {
            settingsController.show();
        }
        else {
            LOG.error("Settings window is null");
        }
    }
    
    @FXML
    public void checkAndEnableStartDuelButton() {
        int deckSize = deckWindow.getController().getCardCollectionController().getCardCount();
        if(deckSize == NUMBER_CARDS_IN_DECK && duelistComboBox.getSelectionModel().getSelectedItem() != null
                && deckSortComboBox.getSelectionModel().getSelectedItem() != null)
            startDuelButton.setDisable(false);
        else
            startDuelButton.setDisable(true);
    }
    
    @FXML
    public void showDeckWindow() {
        deckWindow.show();
    }
    
    @FXML
    public void showAboutDialog() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/about.fxml"));
            VBox dialog = loader.<VBox>load();
            Stage stage = new Stage();
            stage.setTitle("About Mana");
            //moving this into initialize will cause parent below to be null
            //initializing the parent doesn't seem to matter though
            stage.initOwner(parent);
            stage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(dialog);
            stage.setScene(scene);
            stage.showAndWait();
        }
        catch(Exception ex) {
            LOG.error("Failure to open the About dialog", ex);
        }
    }
    
    @FXML
    public void exit() {
        Platform.exit();
    }
    
    @FXML
    public void startDuel() {
        startDuelButton.setDisable(true);
        deckWindow.getController().setDisable(true);
        duelistComboBox.setDisable(true);
        deckSortComboBox.setDisable(true);
        
        resetDuelButton.setDisable(false);
        searchSeedsButton.setDisable(false);
        handCardCollectionController.setDisable(false);
        
        handCardCollectionController.setCardSuggestionPool(deckWindow.getController().getCardCollectionController().getDeck());
    }
    
    @FXML
    public void resetDuel() {
        startDuelButton.setDisable(false);
        deckWindow.getController().setDisable(false);
        duelistComboBox.setDisable(false);
        deckSortComboBox.setDisable(false);
        
        resetDuelButton.setDisable(true);
        searchSeedsButton.setDisable(true);
        handCardCollectionController.setDisable(true);
        
        handCardCollectionController.setCardSuggestionPool(null);
        handCardCollectionController.clear();
        seedList.getItems().clear();
        aiDeckList.getItems().clear();
    }
    
    @FXML
    public void searchSeeds() {
        if(isSearching) {
            //cancel
            if(seedSearch != null) { // guard against a multi-threaded memory inconsistency
                searchSeedsButton.setDisable(true);
                statusLabel.setText("Cancelling...");
                seedSearch.cancel();
            }
        }
        else {
            //start searching
            isSearching = true;
            searchSeedsButton.setText("Stop Searching");
            statusLabel.setText("Simulating...");
            resetDuelButton.setDisable(true);
            
            ObservableList<RNG> hitSeeds = FXCollections.observableArrayList();
            seedList.setItems(new SortedList<>(hitSeeds, (r1, r2) -> r1.getDelta() - r2.getDelta()));
            
            Deck playerDeck = new Deck(deckWindow.getController().getCardCollectionController().getDeck());
            Comparator<Card> sort = deckSortComboBox.getSelectionModel().getSelectedItem();
            List<Card> drawnCards = handCardCollectionController.getDeck();
            final int start = settingsController.getSearchStart();
            final int end = settingsController.getSearchEnd();
            
            currentTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    AtomicLong progress = new AtomicLong();
                    seedSearch = new SeedSearch.Builder(playerDeck, drawnCards)
                        .withSpace(start, end)
                        .withSort(sort)
                        .withCallbackAfterEachIteration(() -> {
                                long p = progress.incrementAndGet();
                                updateProgress(p, end - start);
                        })
                        .withCallbackAfterEachHit(rng -> Platform.runLater(() -> hitSeeds.add(rng)))
                        .build();
                    seedSearch.search();
                    return null;
                }
            };
            currentTask.setOnSucceeded(event -> {
                resetUiAfterSearch();
            });
            searchProgressBar.progressProperty().bind(currentTask.progressProperty());
            executor.execute(currentTask);
        }
    }
    
    private void resetUiAfterSearch() {
        isSearching = false;
        searchSeedsButton.setText("Search Seeds");
        searchSeedsButton.setDisable(false);
        statusLabel.setText("Ready");
        resetDuelButton.setDisable(false);
    }
    
    public void setParent(Stage parent) {
        this.parent = parent;
    }
    
}
