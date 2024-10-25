package org.jabref.gui.libraryproperties;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

import javafx.beans.property.StringProperty;
import org.jabref.gui.libraryproperties.constants.ConstantsPropertiesView;
import org.jabref.gui.libraryproperties.contentselectors.ContentSelectorView;
import org.jabref.gui.libraryproperties.general.GeneralPropertiesView;
import org.jabref.gui.libraryproperties.keypattern.KeyPatternPropertiesView;
import org.jabref.gui.libraryproperties.preamble.PreamblePropertiesView;
import org.jabref.gui.libraryproperties.saving.SavingPropertiesView;
import org.jabref.logic.l10n.Localization;
import org.jabref.model.database.BibDatabaseContext;
import de.saxsys.mvvmfx.utils.validation.CompositeValidator;
import de.saxsys.mvvmfx.utils.validation.FunctionBasedValidator;
import de.saxsys.mvvmfx.utils.validation.ValidationMessage;
import de.saxsys.mvvmfx.utils.validation.ValidationStatus;
import de.saxsys.mvvmfx.utils.validation.Validator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.binding.Bindings;

public class LibraryPropertiesViewModel {

    private final StringProperty filePath = new SimpleStringProperty("");
    private final Validator filePathValidator; // Validator for file path

    private final List<PropertiesTab> propertiesTabs;

    public LibraryPropertiesViewModel(BibDatabaseContext databaseContext) {
        propertiesTabs = List.of(
                new GeneralPropertiesView(databaseContext),
                new SavingPropertiesView(databaseContext),
                new KeyPatternPropertiesView(databaseContext),
                new ConstantsPropertiesView(databaseContext),
                new ContentSelectorView(databaseContext),
                new PreamblePropertiesView(databaseContext)
        );


        Predicate<String> filePathExists = input -> {
            if (input == null || input.isEmpty()) return false; // Avoid null pointer exception
            Path path = Path.of(input);
            return Files.exists(path);
        };

        filePathValidator = new FunctionBasedValidator<>(filePath, filePathExists, ValidationMessage.error(Localization.lang("File path does not exist!")));
    }

    public void setValues() {
        for (PropertiesTab propertiesTab : propertiesTabs) {
            propertiesTab.setValues();
        }
    }

    // Setter to change the filePath value
    public void setFilePath(Path path) {
        this.filePath.set(path != null ? path.toString() : "");
    }

    public void storeAllSettings() {
        for (PropertiesTab propertiesTab : propertiesTabs) {
            propertiesTab.storeSettings();
        }
    }

    public List<PropertiesTab> getPropertiesTabs() {
        return propertiesTabs;
    }

    public StringProperty filePathProperty() {
        return filePath;
    }

    public ValidationStatus filePathValidation() {
        return filePathValidator.getValidationStatus();
    }

}
