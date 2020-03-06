package net.gazeplay;

import com.amazonaws.services.polly.model.Voice;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.apache.commons.lang3.tuple.Pair;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class SynthesisTask {

    public static void main(String[] args) {
        SynthesisTask task = new SynthesisTask();
        CSVReader reader = task.readInputCSV();
        String[] languages = new String[]{"cmn-CN", "en-US", "fr-FR"};

        // Language Code -> (GazePlay Language Code, (Male Voice ID, Female Voice ID))
        Map<String, Pair<String, Pair<Integer, Integer>>> voices = Map.of(
                "cmn-CN", Pair.of("chn", Pair.of(9, 9)), // Only one chinese voice for now.
                "en-US", Pair.of("eng", Pair.of(26, 12)),
                "fr-FR", Pair.of("fra", Pair.of(43, 39))
        );

        Polly polly = new Polly(args[0]);

        // This lists all the available voices in case you want to change them.
        int i = 0;
        for (Voice voice : polly.listVoices()) {
            System.out.println(
                    String.format("%d\t%12s\t%8s\t%10s", i++, voice.getName(), voice.getLanguageCode(), voice.getGender()));
        }

        // Looping through the CSV file.
        // We traverse each row, and get the language we are on. We then look up in the voices map the GazePlay
        // language format (eng, fra, etc...) and create the file key for both male and female. Finally, we
        // tell Polly to synthesize the speech using the hardcoded voice ids.
        try {
            List<String[]> allData = reader.readAll();
            for (String[] row : allData) {
                String name = row[0].toLowerCase();
                for (i = 1; i < row.length; i++) {
                    String language = languages[i - 1];
                    String message = row[i];

                    String fileKeyF = String.format("%s.f.%s", name, voices.get(language).getLeft());
                    String fileKeyM = String.format("%s.m.%s", name, voices.get(language).getLeft());

                    polly.synthesizeSpeech(message, fileKeyF, voices.get(language).getRight().getRight());
                    polly.synthesizeSpeech(message, fileKeyM, voices.get(language).getRight().getLeft());

                    System.out.println(String.format("Submitted voices for %s and %s", fileKeyF, fileKeyM));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CSVReader readInputCSV() {
        InputStream inputStream = this.getClass().getResourceAsStream("/inputs.csv");
        return new CSVReaderBuilder(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .withSkipLines(1)
                .build();
    }
}
