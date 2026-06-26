package com.tripreport.ui;

import javafx.application.Platform;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

/**
 * Shared helper for Excel-style Tab / Shift+Tab navigation between table cells:
 * commits the current cell, then opens edit mode on the next (or previous) editable
 * cell, wrapping from the last column of a row into the first column of the next row.
 */
public final class TableNavigation {

    private TableNavigation() {
    }

    public static <S> void editAdjacentCell(TableView<S> table, int row, TableColumn<S, ?> column, boolean forward) {
        List<TableColumn<S, ?>> columns = table.getColumns();
        int colIndex = columns.indexOf(column);
        int nextCol = colIndex;
        int nextRow = row;

        for (int attempts = 0; attempts < columns.size() * 2; attempts++) {
            nextCol += forward ? 1 : -1;
            if (nextCol >= columns.size()) {
                nextCol = 0;
                nextRow++;
            } else if (nextCol < 0) {
                nextCol = columns.size() - 1;
                nextRow--;
            }

            if (nextRow < 0 || nextRow >= table.getItems().size()) {
                return;
            }

            TableColumn<S, ?> candidate = columns.get(nextCol);
            if (candidate.isEditable()) {
                int targetRow = nextRow;
                table.getSelectionModel().clearAndSelect(targetRow, candidate);
                table.scrollTo(targetRow);
                Platform.runLater(() -> table.edit(targetRow, candidate));
                return;
            }
        }
    }
}
