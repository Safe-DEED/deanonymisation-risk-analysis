package algorithms;

import datatypes.Tabular;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

public class TabularAlgorithm {

    private static int k = 2;

    private static NumberFormat formatter = new DecimalFormat("#0.00");

    public static Tabular[] analyse(
            String input,
            String separator,
            List<String> QIs
    ) throws Exception {
        if (!Files.exists(Paths.get("temp"))) {
            File file = new File("temp");
            file.mkdir();
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF8"));
        
        application.Controller.header = br.readLine();
        
        String header[] = application.Controller.header.split(separator);

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

        int datasetSize = dataset.size();

        for (Map.Entry<String, List<Integer>> entry : patternsToRowIDs.entrySet()) {
            List<Integer> rowIDs = entry.getValue();

            if (rowIDs.size() >= k) {
                dataset.keySet().removeAll(rowIDs);
            }
        }

        List<String> QIsCombinations = createQIsCombinations(QIs, separator);

        Tabular[] response = new Tabular[QIsCombinations.size()];

        Map<Integer, List<String>> numberOfQIsQIsCombinations = getNumberOfQIsQIsCombinations(QIsCombinations, separator);

        Map<String, List<String>> combinationsChildren = new HashMap<>();
        for (int i = 1; i < numberOfQIsQIsCombinations.size() - 1; i++) {
            for (String combination : numberOfQIsQIsCombinations.get(i)) {
                combinationsChildren.put(combination, new ArrayList<>());
                for (String superComb : numberOfQIsQIsCombinations.get(i + 1)) {
                    if (isSubsetOf(Arrays.asList(combination.split(separator)), Arrays.asList(superComb.split(separator)))) {
                        List<String> temp = combinationsChildren.get(combination);
                        temp.add(superComb);
                        combinationsChildren.put(combination, temp);
                    }
                }
            }
        }

        Map<String, Double> QIsCombinationRisks = new HashMap<>();

        QIsCombinationRisks.put(numberOfQIsQIsCombinations.get(QIs.size()).get(0), (dataset.size() / (double) datasetSize) * 100);

        for (int i = 1; i < numberOfQIsQIsCombinations.size(); i++) {
            List<String> combinationsToBeTested = numberOfQIsQIsCombinations.get(i);
            IntStream.range(0, combinationsToBeTested.size()).parallel().forEach(combinationsIndex -> {
                try {
                    String combinationToBeTested = combinationsToBeTested.get(combinationsIndex);

                    Set<Integer> inheriretedUnsafe = getInheritedUnsafe(combinationToBeTested);

                    Map<String, List<Integer>> rowPatternRowIDs = createRowPatternRowIDs(dataset, combinationToBeTested,
                            QIsToFieldIndex, separator, inheriretedUnsafe);

                    List<Integer> unsafeRows = getUnsafeRows(rowPatternRowIDs, k);
                    inheriretedUnsafe.addAll(unsafeRows);

                    writeUnsafe(combinationToBeTested, inheriretedUnsafe);

                    Double risk = ((double) inheriretedUnsafe.size() / datasetSize) * 100;

                    QIsCombinationRisks.put(combinationToBeTested, risk);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            for (String combinationToBeTested : numberOfQIsQIsCombinations.get(i)) {
                if (combinationsChildren.get(combinationToBeTested) != null) {
                    Set<Integer> inheriretedUnsafe = getInheritedUnsafe(combinationToBeTested);
                    for (String child : combinationsChildren.get(combinationToBeTested)) {
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("temp/" + child, true), StandardCharsets.UTF_8));

                        for (int id : inheriretedUnsafe) {
                            bw.write(id + "\n");
                        }

                        bw.close();
                    }
                }

                Files.delete(Paths.get("temp/" + combinationToBeTested));
            }
        }

        index = 0;
        for (Map.Entry<String, Double> entry : QIsCombinationRisks.entrySet()) {
            String value = formatter.format(entry.getValue()).replaceAll(",", ".");
            double doubleValue = Double.parseDouble(value);

            response[index++] = new Tabular(entry.getKey(), entry.getKey().split(separator).length, doubleValue);
        }

        return response;
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

    private static List<String> createQIsCombinations(
            List<String> QIs,
            String separator
    ) {
        String sequence[] = new String[QIs.size()];
        for (int i = 0; i < QIs.size(); i++) {
            sequence[i] = QIs.get(i);
        }

        List<String> combinations = new ArrayList<>();

        String[] data = new String[QIs.size()];

        for (int r = 0; r < sequence.length; r++) {
            combinations(sequence, data, 0, QIs.size() - 1, 0, r, combinations, separator);
        }

        String all = "";
        all = QIs.stream().map((QI) -> QI + separator).reduce(all, String::concat);
        all = all.substring(0, all.length() - 1);

        combinations.add(all);

        return combinations;
    }

    private static void combinations(
            String[] sequence,
            String[] data,
            int start,
            int end,
            int index,
            int r,
            List<String> combinations,
            String separator
    ) {
        if (index == r) {
            String combination = "";

            for (int j = 0; j < r; j++) {
                combination += data[j] + separator;
            }

            if (!combination.equals("")) {
                combination = combination.substring(0, combination.length() - 1);
                combinations.add(combination);
            }
        }

        for (int i = start; i <= end && ((end - i + 1) >= (r - index)); i++) {
            data[index] = sequence[i];
            combinations(sequence, data, i + 1, end, index + 1, r, combinations, separator);
        }
    }

    private static Map<Integer, List<String>> getNumberOfQIsQIsCombinations(
            List<String> QIsCombinations,
            String separator
    ) {
        Map<Integer, List<String>> numberOfQIsQIsCombinations = new HashMap<>();

        QIsCombinations.forEach((QIsCombination) -> {
            int numberOfQIs = QIsCombination.split(separator).length;

            List<String> temp;
            if (numberOfQIsQIsCombinations.containsKey(numberOfQIs)) {
                temp = numberOfQIsQIsCombinations.get(numberOfQIs);
            } else {
                temp = new ArrayList<>();
            }
            temp.add(QIsCombination);

            numberOfQIsQIsCombinations.put(numberOfQIs, temp);
        });

        return numberOfQIsQIsCombinations;
    }

    private static Map<String, List<Integer>> createRowPatternRowIDs(
            Map<Integer, String> dataset,
            String combinationToBeTested,
            Map<String, Integer> QIsToFieldIndex,
            String separator,
            Set<Integer> inheritedUnsafe
    ) {
        Map<String, List<Integer>> rowPatternRowIDs = new HashMap<>();

        List<Integer> fieldsToWrite = getFieldsToWrite(combinationToBeTested, QIsToFieldIndex, separator);

        Map<Integer, String> temp = new HashMap<>(dataset);
        temp.keySet().removeAll(inheritedUnsafe);

        for (Map.Entry<Integer, String> entry : temp.entrySet()) {
            int rowID = entry.getKey();
            String row = entry.getValue();

            String fields[] = row.split(separator);

            String rowToWrite = "";
            for (int i = 0; i < fields.length; i++) {
                if (fieldsToWrite.contains(i)) {
                    rowToWrite += fields[i] + separator;
                }
            }
            rowToWrite = rowToWrite.substring(0, rowToWrite.length() - 1);

            List<Integer> rowIDs;
            if (rowPatternRowIDs.containsKey(rowToWrite)) {
                rowIDs = rowPatternRowIDs.get(rowToWrite);
            } else {
                rowIDs = new ArrayList<>();
            }
            rowIDs.add(rowID);

            rowPatternRowIDs.put(rowToWrite, rowIDs);
        }

        return rowPatternRowIDs;
    }

    private static List<Integer> getFieldsToWrite(
            String combinationToBeTested,
            Map<String, Integer> QIsToFieldIndex,
            String separator
    ) {
        List<Integer> fieldsToWrite = new ArrayList<>();

        List<String> QIs = Arrays.asList(combinationToBeTested.split(separator));
        for (Map.Entry<String, Integer> entry : QIsToFieldIndex.entrySet()) {
            String QI = entry.getKey();

            if (QIs.contains(QI)) {
                fieldsToWrite.add(entry.getValue());
            }
        }

        return fieldsToWrite;
    }

    private static List<Integer> getUnsafeRows(
            Map<String, List<Integer>> rowPatternRowIDs,
            int k
    ) {
        List<Integer> unsafeRows = new ArrayList<>();

        for (Map.Entry<String, List<Integer>> entry : rowPatternRowIDs.entrySet()) {
            List<Integer> rowIDs = entry.getValue();

            if (rowIDs.size() < k) {
                for (int rowID : rowIDs) {
                    unsafeRows.add(rowID);
                }
            }
        }

        return unsafeRows;
    }

    private static boolean isSubsetOf(
            List<String> list1,
            List<String> list2
    ) {
        for (String element : list1) {
            if (!list2.contains(element)) {
                return false;
            }
        }

        return true;
    }

    private static Set<Integer> getInheritedUnsafe(
            String combinationToBeTested
    ) throws Exception {
        Set<Integer> unsafe = new HashSet<>();

        File file = new File("temp/" + combinationToBeTested);

        if (file.exists()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));

            String line;
            while ((line = br.readLine()) != null) {
                unsafe.add(Integer.parseInt(line));
            }

            br.close();
        }

        return unsafe;
    }

    private static void writeUnsafe(
            String combination,
            Set<Integer> unsafe
    ) throws Exception {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("temp/" + combination), StandardCharsets.UTF_8));

        for (int id : unsafe) {
            bw.write(id + "\n");
        }

        bw.close();
    }
}
