package datatypes;

public class Invoices {

    private long millis;
    private double amount;
    private String date;
    private String safe;

    public Invoices(long millis, double amount, String date, String safe) {
        this.millis = millis;
        this.amount = amount;
        this.date = date;
        this.safe = safe;
    }

    public long getMillis() {
        return millis;
    }

    public void setMillis(long millis) {
        this.millis = millis;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSafe() {
        return safe;
    }

    public void setSafe(String safe) {
        this.safe = safe;
    }

}
