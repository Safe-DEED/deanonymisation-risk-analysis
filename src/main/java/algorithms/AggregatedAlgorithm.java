package algorithms;

import datatypes.Aggregated;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AggregatedAlgorithm {

    private static String thousandSeparator = ".";

    public static Map<String, List<Aggregated>> analyse(
            String QIs,
            String separator,
            String inputFile
    ) throws Exception {
        Map<String, List<Aggregated>> response = new HashMap<String, List<Aggregated>>();

        Map<Integer, String> dataset = new HashMap<Integer, String>();
        Map<String, Integer> QIsIndexMap = new HashMap<String, Integer>();

        List<String> QIsList = Arrays.asList(QIs.split(separator));

        for (String QI : QIsList) {
            response.put(QI, new ArrayList<Aggregated>());
        }

        createDataset(inputFile, separator, QIsList, dataset, QIsIndexMap);

        for (int id : dataset.keySet()) {
            String fields[] = dataset.get(id).split(separator);

            String event = fields[1] + " " + fields[0];

            for (String QI : QIsList) {
                int count = Integer.parseInt(fields[QIsIndexMap.get(QI)].replaceAll("\\" + thousandSeparator, ""));

                List<Aggregated> temp = response.get(QI);
                temp.add(new Aggregated(event, count));

                response.put(QI, temp);
            }
        }

        return response;
    }

    private static void createDataset(String input, String separator, List<String> QIs, Map<Integer, String> dataset,
            Map<String, Integer> QIsIndexMap) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF8"));

        String line = br.readLine();

        String fields[] = line.split(separator);

        for (int i = 0; i < fields.length; i++) {
            if (QIs.contains(fields[i])) {
                QIsIndexMap.put(fields[i], i);
            }
        }

        int row_index = 0;

        while ((line = br.readLine()) != null) {
            if (line.endsWith(";")) {
                line += "NULL";
            }
            dataset.put(row_index++, line);
        }

        br.close();
    }
}
