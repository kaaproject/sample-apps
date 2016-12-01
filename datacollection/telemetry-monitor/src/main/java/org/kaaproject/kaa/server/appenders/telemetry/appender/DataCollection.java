package org.kaaproject.kaa.server.appenders.telemetry.appender;


public class DataCollection {

    public enum DataCollectionField {
        TEMPERATURE("temperature"),
        TIME_STAMP("timeStamp");

        private String name;

        DataCollectionField(String value) {
            this.name = value;
        }

        public String getName() {
            return name;
        }

    }

    private Integer temperature;
    private Long timeStamp;

    public static DataCollection of(Integer temperature, Long timeStamp) {
        return new DataCollection(temperature, timeStamp);
    }

    private DataCollection(Integer temperature, Long timeStamp) {
        this.temperature = temperature;
        this.timeStamp = timeStamp;
    }

    public Integer getTemperature() {
        return temperature;
    }

    public void setTemperature(Integer temperature) {
        this.temperature = temperature;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }
}