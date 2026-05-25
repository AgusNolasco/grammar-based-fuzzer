package fuzzer;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FuzzerConfig {

    private final JSONObject grammar;
    private final int count;
    private final int maxExpansions;
    private final int seed;

    public FuzzerConfig(
            String grammarPath,
            int count,
            int maxExpansions,
            int seed) {
        try {
            this.grammar = read_grammar(grammarPath);
        } catch (Exception e) {
            throw new IllegalArgumentException("The given grammar path is not valid");
        }
        this.count = count;
        this.maxExpansions = maxExpansions;
        this.seed = seed;
    }

    private JSONObject read_grammar(String grammar_file_name) throws IOException, ParseException {
        BufferedReader reader = new BufferedReader(new FileReader(grammar_file_name));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        reader.close();

        String content = stringBuilder.toString();
        JSONParser parser = new JSONParser();
        return (JSONObject) parser.parse(content);
    }

    public JSONObject getGrammar() {
        return grammar;
    }

    public int getCount() {
        return count;
    }

    public Integer getMaxExpansions() {
        return maxExpansions;
    }

    public int getSeed() {
        return seed;
    }
}