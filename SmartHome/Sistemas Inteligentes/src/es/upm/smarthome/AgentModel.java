package es.upm.smarthome;

public enum AgentModel {
    TEMPERATURE_SENSOR("TemperatureSensor"),
    HUMIDITY_SENSOR("HumiditySensor"),
    WIND_SENSOR("WindSensor"),
    RAIN_SENSOR("RainSensor"),
    DECISION("Decision"),
    UI("UI"),
    DESCONOCIDO("Desconocido");

    private final String value;

    AgentModel(String value) { this.value = value; }

    public String getValue() { return value; }

    public static AgentModel getEnum(String value) {
        switch (value) {
            case "TemperatureSensor": return TEMPERATURE_SENSOR;
            case "HumiditySensor": return HUMIDITY_SENSOR;
            case "WindSensor": return WIND_SENSOR;
            case "RainSensor": return RAIN_SENSOR;
            case "Decision": return DECISION;
            case "UI": return UI;
            default: return DESCONOCIDO;
        }
    }
}