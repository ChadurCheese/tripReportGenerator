package com.tripreport.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Table cell allowing selection of up to 2 duty codes, joined with "/" (e.g. "H/J").
 * Selection is done via a checkbox popup with a manual "Done" close button, since
 * a single selected code is a valid, complete value on its own.
 */
public class DutyCodeTableCell<S> extends TableCell<S, String> {

    private static final List<String> VALID_CODES = Arrays.asList(
            "A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "N", "S", "T"
    );
    private static final int MAX_SELECTED = 2;

    private Popup popup;
    private List<CheckBox> checkBoxes;

    @Override
    public void startEdit() {
        if (isEmpty()) {
            return;
        }
        super.startEdit();
        showPopup();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        hidePopup();
        setText(getItem());
        setGraphic(null);
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            setText(item);
            setGraphic(null);
        }
    }

    private void showPopup() {
        List<String> currentCodes = getItem() == null
                ? List.of()
                : Arrays.asList(getItem().split("/"));

        checkBoxes = new ArrayList<>();
        VBox box = new VBox(4);
        box.setPadding(new Insets(8));
        box.setStyle("-fx-background-color: white; -fx-border-color: #999999; -fx-border-width: 1;");

        for (String code : VALID_CODES) {
            CheckBox checkBox = new CheckBox(code);
            checkBox.setSelected(currentCodes.contains(code));
            checkBox.setOnAction(e -> updateDisabledState());
            checkBoxes.add(checkBox);
            box.getChildren().add(checkBox);
        }

        Button doneButton = new Button("Done");
        doneButton.setMaxWidth(Double.MAX_VALUE);
        doneButton.setOnAction(e -> commitSelection());
        box.getChildren().add(doneButton);

        updateDisabledState();

        // Let Tab on the first/last control commit and jump to the adjacent table cell,
        // instead of just cycling focus within the popup.
        box.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, (javafx.scene.input.KeyEvent event) -> {
            if (event.getCode() != javafx.scene.input.KeyCode.TAB) {
                return;
            }
            boolean forward = !event.isShiftDown();
            javafx.scene.Node focusOwner = getScene() != null ? getScene().getFocusOwner() : null;
            boolean atEdge = forward ? focusOwner == doneButton : focusOwner == checkBoxes.get(0);
            if (atEdge) {
                event.consume();
                int row = getIndex();
                TableColumn<S, String> column = getTableColumn();
                TableView<S> tableView = getTableView();
                commitSelection();
                TableNavigation.editAdjacentCell(tableView, row, column, forward);
            }
        });

        popup = new Popup();
        popup.setAutoHide(true);
        popup.setOnAutoHide(e -> commitSelection());
        popup.getContent().add(box);

        javafx.geometry.Bounds bounds = localToScreen(getBoundsInLocal());
        if (bounds != null) {
            popup.show(getScene().getWindow(), bounds.getMinX(), bounds.getMaxY());
        } else {
            popup.show(getScene().getWindow());
        }
    }

    private void updateDisabledState() {
        long selectedCount = checkBoxes.stream().filter(CheckBox::isSelected).count();
        for (CheckBox checkBox : checkBoxes) {
            checkBox.setDisable(!checkBox.isSelected() && selectedCount >= MAX_SELECTED);
        }
    }

    private void commitSelection() {
        ObservableList<String> selected = FXCollections.observableArrayList();
        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isSelected()) {
                selected.add(checkBox.getText());
            }
        }
        hidePopup();
        commitEdit(String.join("/", selected));
    }

    private void hidePopup() {
        if (popup != null) {
            popup.setOnAutoHide(null);
            popup.hide();
            popup = null;
        }
    }
}
