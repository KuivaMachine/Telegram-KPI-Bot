package org.example.googlesheetservice.postgresql.entity;



import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PrinterStatistic {
    private final String date;
    private final String prints_num;
    private final String defects_num;

    @JsonProperty("fio")
    private String fio;

    public PrinterStatistic(@JsonProperty("date") String date, @JsonProperty("prints_num") String prints_num, @JsonProperty("defects_num") String defects_num, @JsonProperty("fio") String fio) {
        this.date = date;
        this.prints_num = prints_num;
        this.defects_num = defects_num;
        this.fio = fio;
    }

    @Override
    public String toString() {
        return "PrinterStatistic{" +
                "date='" + date + '\'' +
                ", prints_num='" + prints_num + '\'' +
                ", defects_num='" + defects_num + '\'' +
                ", fio='" + fio + '\'' +
                '}';
    }
}
