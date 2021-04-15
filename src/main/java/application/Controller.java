package application;

import algorithms.TabularAlgorithm;
import datatypes.Aggregated;
import datatypes.Invoices;
import datatypes.Tabular;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class Controller {

    private String inputFile;
    private String separator;
    public static String header;

    @PostMapping("/upload")
    public String fileUpload(
            @RequestParam("inputfile") MultipartFile file,
            @RequestParam("type") String type,
            @RequestParam("separator") String separator
    ) throws IOException {
        inputFile = file.getOriginalFilename();
        this.separator = separator;

        byte[] bytes = file.getBytes();
        Path path = Paths.get(file.getOriginalFilename());
        Files.write(path, bytes);

        return "<html>"
                + "<head>"
                + "<meta http-equiv=\"refresh\" content=\"0; URL='/" + type + ".html'\" />"
                + "</head>"
                + "</html>";
    }

    @PostMapping("/getattributes")
    public List<String> getAttributes() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));

        List<String> attributes = new ArrayList<>(Arrays.asList(br.readLine().split(separator)));

        br.close();

        return attributes;
    }

    @PostMapping("/analysetabular")
    public Tabular[] analyseTabular(
            @RequestParam("qis") String QIs
    ) throws Exception {
        List<String> listQIs = Arrays.asList(QIs.split(";"));

        return TabularAlgorithm.analyse(inputFile, separator, listQIs);
    }

    @PostMapping("/anonymise")
    public String anonymise(
            @RequestParam("qis") String QIs,
            @RequestParam("k") String k
    ) throws Exception {
        List<String> listQIs = Arrays.asList(QIs.split(separator));

        Collections.shuffle(listQIs);

        Map<Integer, String> anonymisedDataset = algorithms.Anonymise.anonymise(inputFile, separator, listQIs, Integer.parseInt(k));

        String response = inputFile.substring(0, inputFile.indexOf(".")) + "_k_" + k + ".csv";

        String outputPath = "public/" + response;

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath), StandardCharsets.UTF_8));

        bw.write(header + "\n");

        for (String row : anonymisedDataset.values()) {
            bw.write(row + "\n");
        }

        bw.close();

        return response;
    }

    @PostMapping("/analyseaggregated")
    public Map<String, List<Aggregated>> analyseAggregated(
            @RequestParam("qis") String QIs
    ) throws Exception {
        return algorithms.AggregatedAlgorithm.analyse(QIs, separator, inputFile);
    }

    @PostMapping("/analyseinvoices")
    public Invoices[] analyseInvoices(
            @RequestParam("idcolumn") String idColumn,
            @RequestParam("datecolumn") String dateColumn,
            @RequestParam("dateformat") String dateFormat,
            @RequestParam("amountcolumn") String amountColumn,
            @RequestParam("users") String users,
            @RequestParam("amount") String amount,
            @RequestParam("time") String time,
            @RequestParam("timeframe") String timeframe
    ) throws Exception {
        return algorithms.InvoicesAlgorithm.analyse(inputFile, separator, idColumn, dateColumn, dateFormat, amountColumn, users, amount, time, timeframe);
    }

}
