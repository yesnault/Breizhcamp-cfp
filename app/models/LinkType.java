package models;


public enum LinkType {
    TWITTER("icon-twitter", "https://twitter.com/{userId}"), LINKEDIN("icon-linkedin", "http://fr.linkedin.com/in/{userId}"), GITHUB("icon-github"), GOOGLE_PLUS("icon-google-plus"),
    SLIDESHARE("icon-slideshare"), BLOG("icon-rss"), COMPAGNY("icon-suitcase"), OTHER("icon-bookmark");

    private String icon;

    private String url;

    LinkType(String icon) {
        this.icon = icon;
    }

    LinkType(String icon, String url) {
        this.icon = icon;
        this.url = url;
    }

    public String getIcon() {
        return icon;
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
