package org.example.kpitelegrambot.DAO.entity;


public class PrinterStatistic {
    private String date;
    private  String prints_num;
    private  String defects_num;

    public PrinterStatistic(String date, String prints_num, String defects_num) {
        this.date = date;
        this.prints_num = prints_num;
        this.defects_num = defects_num;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setPrints_num(String prints_num) {
        this.prints_num = prints_num;
    }

    public void setDefects_num(String defects_num) {
        this.defects_num = defects_num;
    }

    public PrinterStatistic() {
    }

    public String getDate() {
        return date;
    }

    public String getPrints_num() {
        return prints_num;
    }

    public String getDefects_num() {
        return defects_num;
    }
}
