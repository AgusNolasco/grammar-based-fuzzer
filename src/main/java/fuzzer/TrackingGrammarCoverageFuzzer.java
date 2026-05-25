package fuzzer;

import org.json.simple.JSONArray;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class TrackingGrammarCoverageFuzzer extends EfficientGrammarFuzzer {

    protected Set<String> coveredExpansions;
    private Set<String> symbolsSeen;

    public TrackingGrammarCoverageFuzzer(FuzzerConfig config) {
        super(config);
    }

    @Override
    public Set<String> generate(int n) {
        reset_coverage();
        Set<String> fuzzed_terms = super.generate(n);

        System.out.println("Covered expansions: " + expansion_coverage().size());
        System.out.println("Uncovered expansions: " + missing_expansion_coverage().size());

        return fuzzed_terms;
    }

    public void reset_coverage() {
        coveredExpansions = new HashSet<>();
    }

    public Set<String> max_expansion_coverage(String symbol, Double max_depth) {
        symbolsSeen = new HashSet<>();
        Set<String> cov = max_expansion_coverage0(symbol, max_depth);
        if (Objects.equals(symbol, INITIAL_SYMBOL)) {
            assert symbolsSeen.size() == grammar.size();
        }
        return cov;
    }

    private Set<String> max_expansion_coverage0(String symbol, Double max_depth) {
        if (max_depth <= 0) {
            return new HashSet<>();
        }

        symbolsSeen.add(symbol);

        Set<String> expansions = new HashSet<>();
        for (Object e : (JSONArray) grammar.get(symbol)) {
            String expansion = (String) e;
            expansions.add(expansion_key(symbol, expansion));
            for (String non_terminal : nonTerminals(expansion)) {
                if (!symbolsSeen.contains(non_terminal)) {
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
    public int chooseNodeExpansion(DerivationTree node, List<List<DerivationTree>> childrenAlternatives) {
        int index = super.chooseNodeExpansion(node, childrenAlternatives);
        add_coverage(node.getSymbolName(), childrenAlternatives.get(index));
        return index;
    }

    public void add_coverage(String symbol, List<DerivationTree> new_child) {
        coveredExpansions.add(expansion_key(symbol, new_child));
    }

    public Set<String> expansion_coverage() {
        return coveredExpansions;
    }

    public Set<String> missing_expansion_coverage() {
        Set<String> missing_expansion_coverage = max_expansion_coverage(INITIAL_SYMBOL, Double.MAX_VALUE);
        missing_expansion_coverage.removeAll(expansion_coverage());
        return missing_expansion_coverage;
    }

}
