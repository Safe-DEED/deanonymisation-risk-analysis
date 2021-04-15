package datatypes;

public class Aggregated {

    private String event;
    private int count;

    public Aggregated(String event, int count) {
        this.event = event;
        this.count = count;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}
