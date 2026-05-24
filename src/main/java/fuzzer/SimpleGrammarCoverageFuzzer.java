package fuzzer;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleGrammarCoverageFuzzer extends TrackingGrammarCoverageFuzzer {

    public SimpleGrammarCoverageFuzzer(JSONObject grammar, int max_count_of_expansions, int seed) {
        super(grammar, seed);
        this.maxNumOfExpansions = max_count_of_expansions;
    }

    @Override
    public int choose_node_expansion(DerivationTree node, List<List<DerivationTree>> children_alternatives) {
        String symbol = node.get_symbol_name();
        List<List<DerivationTree>> uncovered_children = children_alternatives.stream()
                .filter(c -> !covered_expansions.contains(expansion_key(symbol, c)))
                .collect(Collectors.toList());

        List<Integer> indexMap = new ArrayList<>();
        int i = 0;
        for (List<DerivationTree> c : uncovered_children) {
            if (uncovered_children.contains(c)) {
                indexMap.add(i);
            }
            i++;
        }

        if (uncovered_children.isEmpty()) {
            return super.choose_node_expansion(node, children_alternatives);
        }
        int index = super.choose_node_expansion(node, uncovered_children);
        return indexMap.get(index);
    }

}
