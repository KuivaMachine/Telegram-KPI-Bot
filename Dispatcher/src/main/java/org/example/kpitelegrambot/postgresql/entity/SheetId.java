package org.example.postgresql.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Entity
@Table(name = "sheet_id")
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SheetId {
    @Id
    @Column(name = "sheet_id")
   private int sheetId;
    @Column(name = "title")
    String title;

    @Override
    public String toString() {
        return "SheetId{" +
                "sheetId=" + sheetId +
                ", title='" + title + '\'' +
                '}';
    }
}
