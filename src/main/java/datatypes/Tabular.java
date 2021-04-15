package datatypes;

public class Tabular {

    private String combination;
    private int QIs;
    private double risk;

    public Tabular(String combination, int QIs, double risk) {
        this.combination = combination;
        this.QIs = QIs;
        this.risk = risk;
    }

    public String getCombination() {
        return combination;
    }

    public void setCombination(String combination) {
        this.combination = combination;
    }

    public int getQIs() {
        return QIs;
    }

    public void setQIs(int QIs) {
        this.QIs = QIs;
    }

    public double getRisk() {
        return risk;
    }

    public void setRisk(double risk) {
        this.risk = risk;
    }

}
