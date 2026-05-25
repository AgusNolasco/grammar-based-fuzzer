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
            JSONObject grammar,
            int count,
            int maxExpansions,
            int seed) {
        this.grammar = grammar;
        this.count = count;
        this.maxExpansions = maxExpansions;
        this.seed = seed;
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