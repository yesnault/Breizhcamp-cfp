package models;


public enum LinkType {
    TWITTER("Twitter","fa-twitter", "https://twitter.com/"),
    LINKEDIN("Linked-in","fa-linkedin", "http://fr.linkedin.com/in/"),
    GITHUB("Github","fa-github","https://github.com/"), GOOGLE_PLUS("Google Plus","fa-google-plus","https://plus.google.com/+"),
    SLIDESHARE("SlideShare","fa-slideshare","http://fr.slideshare.net/"), BLOG("Blog","fa-rss"),
    COMPAGNY("Société","fa-suitcase"), OTHER("Autre","fa-bookmark");

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
