package bad;

public class ExchangeRateHolder {

    private static ExchangeRateHolder instance;

    private RateTable rateTable;

    private ExchangeRateHolder() {
        this.rateTable = loadFromDatabase();
    }

    public static ExchangeRateHolder getInstance() {
        if (instance == null) {
            synchronized (ExchangeRateHolder.class) {
                if (instance == null) {
                    instance = new ExchangeRateHolder();
                }
            }
        }
        return instance;
    }

    private RateTable loadFromDatabase() {
        RateTable table = new RateTable();
        table.load();
        return table;
    }

    public RateTable getRateTable() {
        return rateTable;
    }
}
