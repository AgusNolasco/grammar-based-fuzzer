package fuzzer;

import org.json.simple.JSONObject;

import java.util.*;

public class GrammarCoverageFuzzer extends SimpleGrammarCoverageFuzzer {

    public GrammarCoverageFuzzer(JSONObject grammar, int max_count_of_expansions, int seed) {
        super(grammar, max_count_of_expansions, seed);
    }

    @Override
    public int choose_node_expansion(DerivationTree node, List<List<DerivationTree>> children_alternatives) {
        String symbol = node.get_symbol_name();

        List<Set<String>> new_coverages = new_coverages(node, children_alternatives);

        if (new_coverages == null) {
            return super.choose_node_expansion(node, children_alternatives);
        }

        int max_new_coverage = new_coverages.stream().map(Set::size).max(Comparator.comparingInt(i -> i)).get();

        List<List<DerivationTree>> children_with_max_new_coverage = new ArrayList<>();
        int idx = 0;
        for (List<DerivationTree> c : children_alternatives) {
            if (new_coverages.get(idx).size() == max_new_coverage) {
                children_with_max_new_coverage.add(c);
            }
            idx++;
        }

        List<Integer> indexMap = new ArrayList<>();
        int i = 0;
        for (List<DerivationTree> c : children_alternatives) {
            if (new_coverages.get(i).size() == max_new_coverage) {
                indexMap.add(i);
            }
            i++;
        }

        int new_children_index = super.choose_node_expansion(node, children_with_max_new_coverage);
        List<DerivationTree> new_children = children_with_max_new_coverage.get(new_children_index);
        String key = expansion_key(symbol, new_children);
        covered_expansions.add(key);
        return indexMap.get(new_children_index);
    }

    private List<Set<String>> new_coverages(DerivationTree node, List<List<DerivationTree>> children_alternatives) {
        String symbol = node.get_symbol_name();
        for (int max_depth = 0; max_depth < grammar.size(); max_depth++) {
            List<Set<String>> new_coverages = new ArrayList<>();
            for (List<DerivationTree> c : children_alternatives) {
                new_coverages.add(new_child_coverage(symbol, c, (double) max_depth));
            }
            int max_new_coverage = new_coverages.stream().map(Set::size).max(Comparator.comparingInt(i -> i)).get();
            if (max_new_coverage > 0) {
                return new_coverages;
            }
        }

        return null;
    }

    private Set<String> new_child_coverage(String symbol, List<DerivationTree> children, Double max_depth) {
        Set<String> new_cov = new_child_coverage0(children, max_depth);
        new_cov.add(expansion_key(symbol, children));
        new_cov.removeAll(expansion_coverage());
        return new_cov;
    }

    private Set<String> new_child_coverage0(List<DerivationTree> children, Double max_depth) {
        Set<String> new_cov = new HashSet<>();
        for (DerivationTree c : children) {
            String symbol = c.get_symbol_name();
            if (grammar.containsKey(symbol)) {
                new_cov.addAll(max_expansion_coverage(symbol, max_depth));
            }
        }
        return new_cov;
    }


}
