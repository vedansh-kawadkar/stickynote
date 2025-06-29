package com.stickynote.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class PrimaryController {

    private String fileName;
    private Stage stage;
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private TextArea noteArea;
    @FXML
    private HBox topBar;
    @FXML
    private Label noteTitleLabel;
    @FXML
    private TextField noteTitleField;
    @FXML
    private Button notesListButton;
    @FXML
    private BorderPane borderPane;

    private static int untitledCount = 1;
    private static int untitledCounter = 1;

    private static final Set<Stage> openWindows = new HashSet<>();
    private static final Map<String, Stage> openNoteMap = new HashMap<>();
    private static final String NOTE_DIR = System.getProperty("user.home") + File.separator + ".stickynotes";

    public void initialize() {
        topBar.setOnMousePressed(e -> {
            xOffset = e.getSceneX();
            yOffset = e.getSceneY();
        });
        topBar.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - xOffset);
            stage.setY(e.getScreenY() - yOffset);
        });

        if (fileName == null || fileName.isEmpty()) {
            String title = "Untitled " + untitledCount++;
            noteTitleLabel.setText(title);
            noteTitleField.setText(title);
            fileName = new File(NOTE_DIR, title.replaceAll("\\s+", "_") + ".txt").getAbsolutePath();
        }

        noteTitleLabel.setOnMouseClicked(e -> {
            noteTitleLabel.setVisible(false);
            noteTitleField.setVisible(true);
            noteTitleField.requestFocus();
        });

        noteTitleField.setOnAction(e -> finishEditingTitle());
        noteTitleField.focusedProperty().addListener((obs, old, isFocused) -> {
            if (!isFocused) {
                finishEditingTitle();
            }
        });
    }

    public void setFileName(String inputNameOrPath) {
        File dir = new File(NOTE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file;
        String displayTitle;

        if (inputNameOrPath == null || inputNameOrPath.isBlank()) {
            // For new notes
            displayTitle = "Untitled " + untitledCounter++;
            file = new File(NOTE_DIR, displayTitle.replaceAll("\\s+", "_") + ".txt");
        } else {
            file = new File(inputNameOrPath);
            displayTitle = file.getName().replace(".txt", "").replace("_", " ");
        }

        this.fileName = file.getAbsolutePath();
        noteTitleLabel.setText(displayTitle);
        noteTitleField.setText(displayTitle);

        if (stage != null) {
            stage.setTitle(displayTitle);
        }

        noteArea.setText(loadNotes());
        noteArea.textProperty().addListener((obs, oldText, newText) -> saveNotes(newText));
    }

    private String ensureUniqueTitle(String baseTitle) {
        int i = 1;
        String candidate = baseTitle;
        while (new File(NOTE_DIR, candidate.replaceAll("\\s+", "_") + ".txt").exists()) {
            candidate = baseTitle + " (" + i++ + ")";
        }
        return candidate;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        openWindows.add(stage);
        if (this.fileName != null) {
            openNoteMap.put(this.fileName, stage);
        }

        stage.setOnCloseRequest(e -> {
            openWindows.remove(stage);
            if (this.fileName != null) {
                openNoteMap.remove(this.fileName);
            }
            if (openWindows.isEmpty()) {
                Platform.exit();
            }
        });
    }

    @FXML
    private void handleMinimize() {
        if (stage != null) {
            stage.setIconified(true);

        }
    }

    @FXML
    private void handleClose() {
        if (stage != null) {
            if (this.fileName != null) {
                openNoteMap.remove(this.fileName);
            }
            stage.close();
            openWindows.remove(stage);
            if (openWindows.isEmpty()) {
                Platform.exit();
            }
        }
    }

    private String loadNotes() {
        File file = new File(fileName);
        if (!file.exists()) {
            return "";
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void saveNotes(String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNewNote() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("primary.fxml"));
            Scene scene = new Scene(loader.load(), 500, 400);
            scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

            PrimaryController controller = loader.getController();
            controller.setStage(new Stage());
            controller.setFileName(null);

            Stage newStage = controller.stage;
            newStage.initStyle(StageStyle.UNDECORATED);
            newStage.setScene(scene);
            newStage.setTitle("Sticky Note");
            newStage.setAlwaysOnTop(false);
            newStage.setResizable(true);
            newStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void finishEditingTitle() {
        String newTitle = noteTitleField.getText().trim();
        if (newTitle.isEmpty()) {
            noteTitleLabel.setVisible(true);
            noteTitleField.setVisible(false);
            return;
        }

        String baseName = newTitle.replaceAll("\\s+", "_");
        String newFileName = baseName + ".txt";
        File newFile = new File(NOTE_DIR, newFileName);

        // If the new name conflicts and is not the current file, append suffixes
        if (!newFile.getAbsolutePath().equals(fileName) && newFile.exists()) {
            int count = 2;
            do {
                newFileName = baseName + "_(" + count + ").txt";
                newFile = new File(NOTE_DIR, newFileName);
                count++;
            } while (newFile.exists());
        }

        // Rename file if changed
        File oldFile = new File(fileName);
        if (!oldFile.getAbsolutePath().equals(newFile.getAbsolutePath())) {
            if (oldFile.exists()) {
                oldFile.renameTo(newFile);
            }
            fileName = newFile.getAbsolutePath();
        }

        noteTitleLabel.setText(newTitle);
        noteTitleField.setText(newTitle);
        if (stage != null) {
            stage.setTitle(newTitle);
            openNoteMap.put(fileName, stage);
        }

        noteTitleLabel.setVisible(true);
        noteTitleField.setVisible(false);
    }

    @FXML
    private void handleNotesList() {
        File noteDir = new File(NOTE_DIR);
        if (!noteDir.exists()) {
            return;
        }

        File[] files = noteDir.listFiles((dir, name) -> name.endsWith(".txt"));
        if (files == null || files.length == 0) {
            return;
        }

        ContextMenu menu = new ContextMenu();

        for (File file : files) {
            String fileNameDisplay = file.getName().replace(".txt", "").replace("_", " ");
            String rawFileName = file.getName();
            String fullPath = new File(NOTE_DIR, rawFileName).getAbsolutePath();

            Label titleLabel = new Label(fileNameDisplay);
            titleLabel.getStyleClass().add("note-title-label");
            titleLabel.setOnMouseClicked(e -> {
                menu.hide();
                Stage existingStage = openNoteMap.get(fullPath);
                if (existingStage != null) {
                    existingStage.toFront();
                    existingStage.requestFocus();
                } else {
                    openNote(rawFileName);
                }
            });

            ImageView binIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/bin.png")));
            binIcon.setFitWidth(14);
            binIcon.setFitHeight(14);
            Button deleteButton = new Button();
            deleteButton.setGraphic(binIcon);
            deleteButton.setTooltip(new Tooltip("Delete note"));
            deleteButton.getStyleClass().add("note-delete-button");
            deleteButton.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete note: " + fileNameDisplay + "?", ButtonType.YES, ButtonType.NO);
                confirm.setHeaderText(null);
                Optional<ButtonType> result = confirm.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.YES) {
                    if (file.delete()) {
                        // Remove from screen if open
                        Stage openStage = openNoteMap.remove(fullPath);
                        if (openStage != null) {
                            openWindows.remove(openStage);
                            openStage.close();
                        }

                        handleNotesList(); // Refresh the list
                    }
                }

            });

            HBox row = new HBox(titleLabel, deleteButton);
            row.setSpacing(10);
            row.getStyleClass().add("note-list-item");

            CustomMenuItem item = new CustomMenuItem(row);
            item.setHideOnClick(false);
            menu.getItems().add(item);
        }

        if (menu.getItems().isEmpty()) {
            MenuItem empty = new MenuItem("No notes found");
            empty.setDisable(true);
            menu.getItems().add(empty);
        }

        menu.show(notesListButton, Side.BOTTOM, 0, 0);
    }

    private void openNote(String fileNameWithExtension) {
        File file = new File(NOTE_DIR, fileNameWithExtension);
        String fullPath = file.getAbsolutePath();

        Stage existingStage = openNoteMap.get(fullPath);
        if (existingStage != null) {
            existingStage.toFront();
            existingStage.requestFocus();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("primary.fxml"));
            Scene scene = new Scene(loader.load(), 500, 400);
            scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

            PrimaryController controller = loader.getController();

            Stage newStage = new Stage();
            newStage.initStyle(StageStyle.UNDECORATED);
            newStage.setAlwaysOnTop(false);
            newStage.setResizable(true);
            newStage.setScene(scene);

            controller.setStage(newStage);
            controller.setFileName(fullPath); // ✅ pass full path here

            newStage.setTitle(file.getName());
            newStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteNote() {
        if (fileName == null) {
            return;
        }

        File file = new File(fileName);
        if (file.exists()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this note?", ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText(null);
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.YES) {
                return;
            }
            if (!file.delete()) {
                System.out.println("Could not delete: " + fileName);
            }
        }

        if (stage != null) {
            openWindows.remove(stage);
            if (this.fileName != null) {
                openNoteMap.remove(this.fileName);
            }
            stage.close();
            if (openWindows.isEmpty()) {
                Platform.exit();
            }
        }
    }

}
