package com.example.epiture;

public class ImageItem {
    private static final String CARACTERE_PARSE = "²";
    private final String id;
    private final String link;
    private Boolean isFavorite;
    private String type;

    /**
     * @param id         id de l'image
     * @param link       lien de l'image
     * @param isFavorite true si elle est en favori, false sinon
     */
    public ImageItem(String id, String link, Boolean isFavorite) {
        this.id = id;
        this.link = link;
        this.isFavorite = isFavorite;
    }

    /**
     * @param lucasString le string de lucas
     */
    public ImageItem(String lucasString) {
        String[] liste = lucasString.split(CARACTERE_PARSE);
        if (liste.length == 4) {
            this.id = liste[0];
            this.link = liste[1];
            this.isFavorite = (liste[2].equals("1"));
            this.type = liste[3];
        } else {
            this.id = "";
            this.link = "";
            this.isFavorite = false;
            this.type = "";
        }

    }

    /**
     * @return renvoi l'id de l'image
     */
    public String getId() {
        return id;
    }

    /**
     * @return renvoi le lien de l'image
     */
    public String getLink() {
        return link;
    }

    /**
     * @return renvoi true si l'image est en favori, false sinon
     */
    public Boolean isFavorite() {
        return isFavorite;
    }

    /**
     * @param favorite true si l'image est en favori, false sinon
     */
    public void setFavorite(Boolean favorite) {
        isFavorite = favorite;
    }

    /**
     * @return renvoi le string de lucas
     */
    public String getString() {
        String favoriteOn = "0";

        if (isFavorite) favoriteOn = "1";

        return id + CARACTERE_PARSE + link + CARACTERE_PARSE + favoriteOn + CARACTERE_PARSE + type;
    }
}
