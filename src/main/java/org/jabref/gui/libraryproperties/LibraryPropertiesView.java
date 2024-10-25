package org.jabref.gui.libraryproperties;

import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.application.Platform;
import javafx.scene.control.TextField;

import org.jabref.gui.theme.ThemeManager;
import org.jabref.gui.util.BaseDialog;
import org.jabref.gui.util.ControlHelper;
import org.jabref.logic.l10n.Localization;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.gui.util.IconValidationDecorator;
import de.saxsys.mvvmfx.utils.validation.visualization.ControlsFxVisualizer;

import com.airhacks.afterburner.views.ViewLoader;
import jakarta.inject.Inject;

import java.nio.file.Path;
import java.nio.file.Paths;

public class LibraryPropertiesView extends BaseDialog<LibraryPropertiesViewModel> {

    @FXML private TabPane tabPane;
    @FXML private ButtonType saveButton;

    @Inject private ThemeManager themeManager;

    @FXML private TextField filePathField; // Field for file path input

    private final BibDatabaseContext databaseContext;
    private LibraryPropertiesViewModel viewModel;

    private final ControlsFxVisualizer visualizer = new ControlsFxVisualizer();

    public LibraryPropertiesView(BibDatabaseContext databaseContext) {
        this.databaseContext = databaseContext;

        ViewLoader.view(this)
                  .load()
                  .setAsDialogPane(this);

        ControlHelper.setAction(saveButton, getDialogPane(), event -> savePreferencesAndCloseDialog());

        if (databaseContext.getDatabasePath().isPresent()) {
            setTitle(Localization.lang("%0 - Library properties", databaseContext.getDatabasePath().get().getFileName()));
        } else {
            setTitle(Localization.lang("Library properties"));
        }

        themeManager.updateFontStyle(getDialogPane().getScene());
    }

    @FXML
    private void initialize() {
        visualizer.setDecoration(new IconValidationDecorator());

        viewModel = new LibraryPropertiesViewModel(databaseContext);

        for (PropertiesTab pane : viewModel.getPropertiesTabs()) {
            ScrollPane scrollPane = new ScrollPane(pane.getBuilder());
            scrollPane.setFitToHeight(true);
            scrollPane.setFitToWidth(true);
            tabPane.getTabs().add(new Tab(pane.getTabName(), scrollPane));
            if (pane instanceof AbstractPropertiesTabView<?> propertiesTab) {
                propertiesTab.prefHeightProperty().bind(tabPane.tabMaxHeightProperty());
                propertiesTab.prefWidthProperty().bind(tabPane.widthProperty());
                propertiesTab.getStyleClass().add("propertiesTab");
            }
        }

        viewModel.setValues();

        // Bind the filePathField to the ViewModel's filePathProperty
        filePathField.textProperty().bindBidirectional(viewModel.filePathProperty());

        // Add the file path validation and visualization
        Platform.runLater(() -> {
            // Validate file path using the visualizer
            visualizer.initVisualization(viewModel.filePathValidation(), filePathField, true);

            // Add a listener to update the ViewModel whenever the TextField changes
            filePathField.textProperty().addListener((observable, oldValue, newValue) -> {
                viewModel.setFilePath(newValue.isEmpty() ? Path.of("") : Path.of(newValue));
            });


        });
    }

    private void savePreferencesAndCloseDialog() {
        viewModel.storeAllSettings();
        close();
    }
}
