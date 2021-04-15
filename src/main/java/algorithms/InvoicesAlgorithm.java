package algorithms;

import static algorithms.Test.separator;
import static algorithms.Test.users;
import datatypes.Invoices;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class InvoicesAlgorithm {

    public static Invoices[] analyse(
            String inputFile,
            String separator,
            String idColumn,
            String dateColumn,
            String dateFormat,
            String amountColumn,
            String users,
            String amount,
            String time,
            String timeframe
    ) throws Exception {
        int usersMore = Integer.parseInt(users);
        double amountWithin = Double.parseDouble(amount);
        int timeWithin = Integer.parseInt(time);

        Connection c = DriverManager.getConnection("jdbc:sqlite:db.db");
        c.setAutoCommit(false);
        Statement stmt = c.createStatement();

        String sql = "DROP TABLE IF EXISTS data;";
        stmt.executeUpdate(sql);

        sql = "CREATE TABLE data ("
                + "userid TEXT, "
                + "date REAL, "
                + "amount REAL"
                + ");";
        stmt.executeUpdate(sql);

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));

        String header[] = br.readLine().split(separator);

        Map<String, Integer> fieldsToWriteIndex = getFieldsToWriteIndex(header, idColumn, dateColumn, amountColumn);

        Map<String, String> data = new HashMap<>();

        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        Calendar cal = Calendar.getInstance();

        String line;
        while ((line = br.readLine()) != null) {
            String fields[] = line.split(separator);

            String userid = fields[fieldsToWriteIndex.get("userid")];
            String date = fields[fieldsToWriteIndex.get("date")];
            Double amountValue = Double.parseDouble(fields[fieldsToWriteIndex.get("amount")]);

            long millis = sdf.parse(date).getTime();

            sql = "INSERT INTO data (userid, date, amount) "
                    + "VALUES ("
                    + "\"" + userid + "\", "
                    + millis + ", "
                    + amountValue
                    + ");";
            stmt.executeUpdate(sql);

            data.put(millis + ";" + amountValue + ";" + date, "");
        }

        br.close();

        for (String points : data.keySet()) {
            long date = Long.parseLong(points.split(";")[0]);
            double amountValue = Double.parseDouble((points.split(";"))[1]);

            Date current = new Date(date);
            cal.setTime(current);

            long minDate = getMinDate(cal, timeWithin, timeframe);

            cal.setTime(current);

            long maxDate = getMaxDate(cal, timeWithin, timeframe);

            double minAmount = amountValue - amountWithin;
            double maxAmount = amountValue + amountWithin;

            sql = "SELECT DISTINCT userid "
                    + "FROM data "
                    + "WHERE "
                    + "date >= " + minDate + " AND date <= " + maxDate + " AND "
                    + "amount >= " + minAmount + " AND amount <= " + maxAmount + ";";
            ResultSet rs = stmt.executeQuery(sql);

            int count = 0;
            while (rs.next()) {
                count++;

                if (count > usersMore) {
                    break;
                }
            }
            rs.close();

            if (count > usersMore) {
                data.put(points, "green");
            } else {
                data.put(points, "red");
            }
        }

        stmt.close();
        c.commit();
        c.close();

        Invoices[] response = new Invoices[data.size()];

        int index = 0;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String key = entry.getKey();
            String safe = entry.getValue();
            
            long millis = Long.parseLong(key.split(";")[0]);
            double amountValue = Double.parseDouble(key.split(";")[1]);
            String date = key.split(";")[2];
            
            response[index++] = new Invoices(millis, amountValue, date, safe);
        }

        return response;
    }

    private static Map<String, Integer> getFieldsToWriteIndex(
            String header[],
            String idColumn,
            String dateColumn,
            String amountColumn
    ) {
        Map<String, Integer> fieldsToWriteIndex = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            if (header[i].equals(idColumn)) {
                fieldsToWriteIndex.put("userid", i);
            } else if (header[i].equals(dateColumn)) {
                fieldsToWriteIndex.put("date", i);
            } else if (header[i].equals(amountColumn)) {
                fieldsToWriteIndex.put("amount", i);
            }
        }

        return fieldsToWriteIndex;
    }

    private static long getMinDate(
            Calendar cal,
            int timeWithin,
            String timeframe
    ) {
        if (timeframe.equals("Years")) {
            cal.add(Calendar.YEAR, -timeWithin);
        } else if (timeframe.equals("Months")) {
            cal.add(Calendar.MONTH, -timeWithin);
        } else if (timeframe.equals("Weeks")) {
            cal.add(Calendar.WEEK_OF_YEAR, -timeWithin);
        } else if (timeframe.equals("Days")) {
            cal.add(Calendar.DAY_OF_MONTH, -timeWithin);
        } else if (timeframe.equals("Hours")) {
            cal.add(Calendar.HOUR_OF_DAY, -timeWithin);
        } else if (timeframe.equals("Minutes")) {
            cal.add(Calendar.MINUTE, -timeWithin);
        }

        return cal.getTimeInMillis();
    }

    private static long getMaxDate(
            Calendar cal,
            int timeWithin,
            String timeframe
    ) {
        if (timeframe.equals("Years")) {
            cal.add(Calendar.YEAR, timeWithin);
        } else if (timeframe.equals("Months")) {
            cal.add(Calendar.MONTH, timeWithin);
        } else if (timeframe.equals("Weeks")) {
            cal.add(Calendar.WEEK_OF_YEAR, timeWithin);
        } else if (timeframe.equals("Days")) {
            cal.add(Calendar.DAY_OF_MONTH, timeWithin);
        } else if (timeframe.equals("Hours")) {
            cal.add(Calendar.HOUR_OF_DAY, timeWithin);
        } else if (timeframe.equals("Minutes")) {
            cal.add(Calendar.MINUTE, timeWithin);
        }

        return cal.getTimeInMillis();
    }
}
