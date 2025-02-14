package org.example.postgresql.entity;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;


@Getter
@Setter
public class PackerStatistic {
    private final String date;
    private final String wb_mhc;
    private final String wb_signum;
    private final String wb_silicosha;
    private final String ozon;
    private final String yandex;
    private final String wb_printkid;
    private final String fbo;

    public PackerStatistic(@JsonProperty("date") String date, @JsonProperty("wbMhc") String wbMhc, @JsonProperty("wbSignum") String wbSignum, @JsonProperty("wbSilicosha") String wbSilicosha, @JsonProperty("ozon") String ozon, @JsonProperty("yandex") String yandex, @JsonProperty("wbPrintkid") String wbPrintkid, @JsonProperty("fbo") String fbo) {
        this.date = date;
        this.wb_mhc = wbMhc;
        this.wb_signum = wbSignum;
        this.wb_silicosha = wbSilicosha;
        this.ozon = ozon;
        this.yandex = yandex;
        this.wb_printkid = wbPrintkid;
        this.fbo = fbo;
    }

    @Override
    public String toString() {
        return "PackerStatistic{" +
                "date='" + date + '\'' +
                ", wb_mhc='" + wb_mhc + '\'' +
                ", wb_signum='" + wb_signum + '\'' +
                ", wb_silicosha='" + wb_silicosha + '\'' +
                ", ozon='" + ozon + '\'' +
                ", yandex='" + yandex + '\'' +
                ", wb_printkid='" + wb_printkid + '\'' +
                ", fbo='" + fbo + '\'' +
                '}';
    }
}
