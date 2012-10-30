package models;

public enum StatusTalk {

    REJETE,
    ATTENTE,
    ACCEPTE;

    public static StatusTalk fromValue(String value) {
        for (StatusTalk status : StatusTalk.values()) {
            if (status.name().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
