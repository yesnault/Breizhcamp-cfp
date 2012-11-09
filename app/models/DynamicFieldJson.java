package models;


public class DynamicFieldJson {
    private Long idValue;
    private Long idField;
    private String name;
    private String value;

    public Long getIdValue() {
        return idValue;
    }

    public void setIdValue(Long idValue) {
        this.idValue = idValue;
    }

    public Long getIdField() {
        return idField;
    }

    public void setIdField(Long idField) {
        this.idField = idField;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static DynamicFieldJson toDynamicFieldJson(DynamicField field, DynamicFieldValue value) {
        DynamicFieldJson json = new DynamicFieldJson();
        json.setIdField(field.getId());
        json.setName(field.getName());
        if (value != null) {
            json.setIdValue(value.getId());
            json.setValue(value.getValue());
        }
        return json;
    }
}
