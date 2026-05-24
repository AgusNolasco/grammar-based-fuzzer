package fuzzer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class TrackingGrammarCoverageFuzzer extends EfficientGrammarFuzzer {

    protected Set<String> covered_expansions;
    private Set<String> symbols_seen;

    public TrackingGrammarCoverageFuzzer(JSONObject grammar, int seed) {
        super(grammar, seed);
    }

    @Override
    public Set<String> fuzz(int n) {
        reset_coverage();
        Set<String> fuzzed_terms = super.fuzz(n);

        System.out.println("Covered expansions: " + expansion_coverage().size());
        System.out.println("Uncovered expansions: " + missing_expansion_coverage().size());

        return fuzzed_terms;
    }

    public void reset_coverage() {
        covered_expansions = new HashSet<>();
    }

    public Set<String> max_expansion_coverage(String symbol, Double max_depth) {
        symbols_seen = new HashSet<>();
        Set<String> cov = max_expansion_coverage0(symbol, max_depth);
        if (Objects.equals(symbol, INITIAL_SYMBOL)) {
            assert symbols_seen.size() == grammar.size();
        }
        return cov;
    }

    private Set<String> max_expansion_coverage0(String symbol, Double max_depth) {
        if (max_depth <= 0) {
            return new HashSet<>();
        }

        symbols_seen.add(symbol);

        Set<String> expansions = new HashSet<>();
        for (Object e : (JSONArray) grammar.get(symbol)) {
            String expansion = (String) e;
            expansions.add(expansion_key(symbol, expansion));
            for (String non_terminal : nonterminals(expansion)) {
                if (!symbols_seen.contains(non_terminal)) {
                    expansions.addAll(max_expansion_coverage0(non_terminal, max_depth-1));
                }
            }
        }

        return expansions;
    }

    public String expansion_key(String symbol, String expansion) {
        return symbol + " -> " + expansion;
    }

    public String expansion_key(String symbol, List<DerivationTree> node) {
        return symbol + " -> " + new DerivationTree(null, node);
    }

    @Override
    public int choose_node_expansion(DerivationTree node, List<List<DerivationTree>> children_alternatives) {
        int index = super.choose_node_expansion(node, children_alternatives);
        add_coverage(node.get_symbol_name(), children_alternatives.get(index));
        return index;
    }

    public void add_coverage(String symbol, List<DerivationTree> new_child) {
        covered_expansions.add(expansion_key(symbol, new_child));
    }

    public Set<String> expansion_coverage() {
        return covered_expansions;
    }

    public Set<String> missing_expansion_coverage() {
        Set<String> missing_expansion_coverage = max_expansion_coverage(INITIAL_SYMBOL, Double.MAX_VALUE);
        missing_expansion_coverage.removeAll(expansion_coverage());
        return missing_expansion_coverage;
    }

}
