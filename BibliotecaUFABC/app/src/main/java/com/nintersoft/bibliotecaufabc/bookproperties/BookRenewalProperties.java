package com.nintersoft.bibliotecaufabc.bookproperties;

public class BookRenewalProperties {
    private String date;
    private String title;
    private String library;
    private String patrimony;
    private String renewalLink;

    public BookRenewalProperties(){
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPatrimony() {
        return patrimony;
    }

    public void setPatrimony(String patrimony) {
        this.patrimony = patrimony;
    }

    public String getLibrary() {
        return library;
    }

    public void setLibrary(String library) {
        this.library = library;
    }

    public String getRenewalLink() {
        return renewalLink;
    }

    public void setRenewalLink(String renewalLink) {
        this.renewalLink = renewalLink;
    }
}
