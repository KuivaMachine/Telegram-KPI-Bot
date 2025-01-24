package org.example.googlesheetservice.Data;

import lombok.Getter;

@Getter
public enum RowColumn {
    ROWS("ROWS"), COLUMNS("COLUMNS");

    private final String description;

    RowColumn(String description) {
        this.description = description;
    }
}
