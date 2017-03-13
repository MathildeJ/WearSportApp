package com.example.mathilde.wearsportapp;

public class Sport {
    private String name;
    private String description;
    private int image;
    private String link;

    public Sport(String name, String description, int image, String link){
        this.name = name;
        this.description = description;
        this.image = image;
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public String getDescription(){
        return description;
    }

    public int getImage() {
        return image;
    }

    public String getLink() {
        return link;
    }

}
