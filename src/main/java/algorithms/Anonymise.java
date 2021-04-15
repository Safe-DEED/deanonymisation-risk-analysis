package algorithms;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import prioprivacy.PrioPrivacy;

public class Anonymise {

    public static Map<Integer, String> anonymise(
            String input,
            String separator,
            List<String> QIs,
            int k
    ) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF8"));

        String header[] = br.readLine().split(separator);

        List<Integer> fieldsToWrite = getFieldsToWrite(QIs, header);

        Map<String, Integer> QIsToFieldIndex = getQIsToFieldIndex(QIs, header);

        Map<Integer, String> dataset = new HashMap<>();
        Map<String, List<Integer>> patternsToRowIDs = new HashMap<>();

        String line;
        int index = 0;
        while ((line = br.readLine()) != null) {
            if (line.endsWith(separator)) {
                line += "NULL";
            }

            String fields[] = line.split(separator);

            String lineToWrite = "";
            for (int field : fieldsToWrite) {
                if (fields[field].equals("")) {
                    fields[field] = "NULL";
                }

                lineToWrite += fields[field] + separator;
            }
            lineToWrite = lineToWrite.substring(0, lineToWrite.length() - 1);

            List<Integer> rowIDs;
            if (patternsToRowIDs.containsKey(lineToWrite)) {
                rowIDs = patternsToRowIDs.get(lineToWrite);
            } else {
                rowIDs = new ArrayList<>();
            }
            rowIDs.add(index);

            patternsToRowIDs.put(lineToWrite, rowIDs);

            dataset.put(index++, lineToWrite);
        }

        br.close();
        
        Map<Integer, List<String>> QIsDomainsMap = new HashMap<>();
        

        for (String QI : QIs) {
            QIsDomainsMap.put(QIsToFieldIndex.get(QI), new ArrayList<String>());
        }

        for (String row : dataset.values()) {
            String fields[] = row.split(separator);

            for (int i = 0; i < fields.length; i++) {
                if (!QIsDomainsMap.get(i).contains(fields[i])) {
                    List<String> list = QIsDomainsMap.get(i);
                    list.add(fields[i]);
                    QIsDomainsMap.put(i, list);
                }
            }
        }
        
        return PrioPrivacy.Algorithm(dataset, k, QIsDomainsMap, QIs, QIsToFieldIndex, separator);               
    }

    private static List<Integer> getFieldsToWrite(
            List<String> QIs,
            String header[]
    ) {
        List<Integer> fieldsToWrite = new ArrayList<>();

        for (int i = 0; i < header.length; i++) {
            if (QIs.contains(header[i])) {
                fieldsToWrite.add(i);
            }
        }

        return fieldsToWrite;
    }

    private static Map<String, Integer> getQIsToFieldIndex(
            List<String> QIs,
            String header[]
    ) {
        Map<String, Integer> QIsToFieldIndex = new HashMap<>();

        int index = 0;
        for (String field : header) {
            if (QIs.contains(field)) {
                QIsToFieldIndex.put(field, index++);
            }
        }

        return QIsToFieldIndex;
    }
}
