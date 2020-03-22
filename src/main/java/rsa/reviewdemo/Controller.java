package rsa.reviewdemo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class Controller {

    private String inputFile;

    private String thousandSeparator = ".";

    private NumberFormat formatter = new DecimalFormat("#0.00");

    @PostMapping("/upload")
    public String fileUpload(@RequestParam("inputfile") MultipartFile file) throws IOException {
        inputFile = file.getOriginalFilename();

        byte[] bytes = file.getBytes();
        Path path = Paths.get(file.getOriginalFilename());
        Files.write(path, bytes);

        return "<html>"
                + "<head>"
                + "<meta http-equiv=\"refresh\" content=\"0; URL='/analysis.html'\" />"
                + "</head>"
                + "</html>";
    }

    @PostMapping("/getattributes")
    public List<String> getAttributes(@RequestParam("separator") String separator) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));

        List<String> attributes = new ArrayList<>(Arrays.asList(br.readLine().split(separator)));

        br.close();

        return attributes;
    }

    @PostMapping("/analysenonagg")
    public NonAggregated[] analyseNonAggregated(@RequestParam("separator") String separator,
            @RequestParam("qis") String QIs,
            @RequestParam("k") String k) throws Exception {
        Map<Integer, String> dataset = new HashMap<Integer, String>();
        Map<String, Integer> QIsIndexMap = new HashMap<String, Integer>();

        List<String> QIsList = Arrays.asList(QIs.split(separator));

        createDataset(inputFile, separator, QIsList, dataset, QIsIndexMap);

        List<String> QIsCombinations = createQIsCombinations(QIsList, QIsList.size());

        NonAggregated response[] = new NonAggregated[QIsCombinations.size()];

        Map<String, Double> combinationsUnsafePercentageMap = new HashMap<String, Double>();

        IntStream.range(0, QIsCombinations.size()).parallel().forEach(combinationsIndex -> {
            try {
                getPercentageOfUnsafeRecords(combinationsIndex, QIsCombinations, dataset, QIsIndexMap, separator,
                        combinationsUnsafePercentageMap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        int index = 0;
        for (Map.Entry<String, Double> entry : combinationsUnsafePercentageMap.entrySet()) {
            String value = formatter.format(entry.getValue() * 100).replaceAll(",", ".");
            double doubleValue = Double.parseDouble(value);

            response[index++] = new NonAggregated(entry.getKey(), entry.getKey().split(" ").length, doubleValue);
        }

        return response;
    }

    @PostMapping("/fnet")
    public NonAggregated[] FNET() throws Exception {
        NonAggregated response[] = new NonAggregated[2047];

        int index = 0;

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("fnet.csv"), "UTF8"));

        String line = br.readLine();
        while ((line = br.readLine()) != null) {
            String fields[] = line.split(",");

            response[index++] = new NonAggregated(fields[1], Integer.parseInt(fields[2]), Double.parseDouble(fields[0]));
        }

        br.close();

        return response;
    }

    @PostMapping("/analyseagg")
    public Map<String, List<Aggregated>> analyseAggregated(@RequestParam("separator") String separator,
            @RequestParam("qis") String QIs) throws Exception {
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

    private static List<String> createQIsCombinations(List<String> QIs, int N) {
        String sequence[] = new String[QIs.size()];
        for (int i = 0; i < QIs.size(); i++) {
            sequence[i] = QIs.get(i);
        }

        List<String> combinations = new ArrayList<String>();

        String[] data = new String[N];

        for (int r = 0; r < sequence.length; r++) {
            combinations(sequence, data, 0, N - 1, 0, r, combinations);
        }

        String all = "";
        for (String QI : QIs) {
            all += QI + " ";
        }
        all = all.substring(0, all.length() - 1);

        combinations.add(all);

        return combinations;
    }

    private static void combinations(String[] sequence, String[] data, int start, int end, int index, int r,
            List<String> combinations) {

        if (index == r) {
            String combination = "";

            for (int j = 0; j < r; j++) {
                combination += data[j] + " ";
            }

            if (!combination.equals("")) {
                combination = combination.substring(0, combination.length() - 1);

                combinations.add(combination);
            }
        }

        for (int i = start; i <= end && ((end - i + 1) >= (r - index)); i++) {
            data[index] = sequence[i];

            combinations(sequence, data, i + 1, end, index + 1, r, combinations);
        }
    }

    private static void getPercentageOfUnsafeRecords(int combinationIndex, List<String> combinations,
            Map<Integer, String> dataset, Map<String, Integer> QIsIndexMap, String separator,
            Map<String, Double> combinationsUnsafePercentageMap)
            throws Exception {
//        System.out.println(combinations.get(combinationIndex));
        Map<String, Integer> rowKAnonymityMap = new HashMap<String, Integer>();

        List<String> QIs = Arrays.asList(combinations.get(combinationIndex).split(" "));
//		for (String QI : QIs) {
//			System.out.println("\t" + QI);
//		}

        for (Map.Entry<Integer, String> entry : dataset.entrySet()) {
            String fields[] = entry.getValue().split(separator);

//            if (entry.getValue().split(separator).length < 16) {
//                System.out.println(entry.getValue() + "\t" + entry.getValue().split(separator).length);
//            }
            String rowQIs = "";
            for (String QI : QIs) {
                if (fields[QIsIndexMap.get(QI)].equals("")) {
                    rowQIs += "NULL,";
                } else {
                    rowQIs += fields[QIsIndexMap.get(QI)] + ",";
                }
            }
            rowQIs = rowQIs.substring(0, rowQIs.length() - 1);

            if (rowKAnonymityMap.containsKey(rowQIs)) {
                rowKAnonymityMap.put(rowQIs, rowKAnonymityMap.get(rowQIs) + 1);
            } else {
                rowKAnonymityMap.put(rowQIs, 1);
            }
        }

        int totalUnsafe = 0;

        for (Map.Entry<String, Integer> entry : rowKAnonymityMap.entrySet()) {
            int rowK = entry.getValue();

            if (rowK < 2) {
                totalUnsafe += rowK;
            }
        }

        combinationsUnsafePercentageMap.put(combinations.get(combinationIndex),
                (totalUnsafe / (double) dataset.size()));

        Map<String, Map<String, Integer>> QIsValuesFreqMap = new HashMap<String, Map<String, Integer>>();
        for (int i = 0; i < QIs.size(); i++) {
            Map<String, Integer> valuesFreqMap = new HashMap<String, Integer>();

            for (Map.Entry<String, Integer> entry : rowKAnonymityMap.entrySet()) {
                int rowK = entry.getValue();

                if (rowK < 2) {
//					System.out.println(entry.getKey());
                    String value = entry.getKey().split(",")[i];

                    if (valuesFreqMap.containsKey(value)) {
                        valuesFreqMap.put(value, valuesFreqMap.get(value) + rowK);
                    } else {
                        valuesFreqMap.put(value, rowK);
                    }
                }
            }

            QIsValuesFreqMap.put(QIs.get(i), valuesFreqMap);
        }
    }

}
