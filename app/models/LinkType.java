package models;


public enum LinkType {
    TWITTER("Twitter","icon-twitter", "https://twitter.com/"),
    LINKEDIN("Linked-in","icon-linkedin", "http://fr.linkedin.com/in/"),
    GITHUB("Github","icon-github"), GOOGLE_PLUS("Google Plus","icon-google-plus"),
    SLIDESHARE("SlideShare","icon-slideshare"), BLOG("Blog","icon-rss"),
    COMPAGNY("Société","icon-suitcase"), OTHER("Autre","icon-bookmark");

    private String label;
    private String icon;
    private String url;

    LinkType(String label,String icon) {
        this.icon = icon;
        this.label = label;
    }

    LinkType(String label,String icon, String url) {
        this.icon = icon;
        this.url = url;
        this.label = label;
    }

    public String getIcon() {
        return icon;
    }

    public String getLabel() {
        return label;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "{" +
                "icon:" + icon +
                ", url:" + (url == null ? "" : url) +
                '}';
    }
}
