package prioprivacy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrioPrivacy {

    public static int unsafe = -1;

    public static Map<Integer, String> Algorithm(
            Map<Integer, String> dataset,
            int k,
            Map<Integer, List<String>> QIsDomainsMap,
            List<String> QIsList,
            Map<String, Integer> QIsToFieldIndex,
            String separator
    ) throws Exception {
        Map<Integer, String> anonymisedDataset = new HashMap<Integer, String>(dataset);

        Map<Integer, String> unsafeDataset = AlgorithmOtherFunctions.createUnsafeDataset(anonymisedDataset, k);
        unsafe = unsafeDataset.size();

        List<Integer> QIs = new ArrayList<Integer>();

        List<String> suppRulesToExclude = new ArrayList<String>();

        for (String QIString : QIsList) {
            if (unsafeDataset.isEmpty()) {
                break;
            }

            QIs.add(QIsToFieldIndex.get(QIString));

            Suppression.suppression(QIs, dataset, anonymisedDataset, unsafeDataset, k, suppRulesToExclude,
                    QIsDomainsMap);
            unsafe = unsafeDataset.size();
        }

        for (int id : unsafeDataset.keySet()) {
            String row = "";
            for (int QI : QIs) {
                row += "*" + separator;
            }
            row = row.substring(0, row.length() - 1);

            anonymisedDataset.put(id, row);
        }

        unsafeDataset.clear();
        unsafe = 0;

        return anonymisedDataset;
    }
}
