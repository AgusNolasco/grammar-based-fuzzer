package fuzzer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EfficientGrammarFuzzer extends BasicFuzzer {

    private static final String re_nonterminal = "(<[^<> ]*>)";
    private int expansions_count;
    protected int max_count_of_expansions = Integer.MAX_VALUE;

    private Function<DerivationTree, DerivationTree> expand_node_strategy;

    public EfficientGrammarFuzzer(JSONObject grammar, int seed) {
        super(grammar, seed);
    }

    @Override
    public String fuzz() {
        DerivationTree tree = init_tree();
        expansions_count = 0;
        tree = expand_tree(tree);
        return tree.toString();
    }

    private DerivationTree init_tree() {
        return new DerivationTree(INITIAL_SYMBOL, null);
    }

    private DerivationTree expand_tree(DerivationTree tree) {
        expand_node_strategy = this::expand_node_randomly;
        while (any_possible_expansion(tree)) {
            if (expansions_count > max_count_of_expansions) {
                expand_node_strategy = this::expand_node_min_cost;
            }
            tree = expand_tree_once(tree);
        }
        assert possible_expansions(tree) == 0;
        return tree;
    }

    public DerivationTree expand_tree_once(DerivationTree tree) {
        List<DerivationTree> children = tree.get_children();
        if (children == null) {
            return expand_node(tree);
        }
        List<DerivationTree> expandable_children = children.stream()
                .filter(this::any_possible_expansion)
                .collect(Collectors.toList());
        List<Integer> indexMap = new ArrayList<>();
        int i = 0;
        for (DerivationTree c : children) {
            if (expandable_children.contains(c)) {
                indexMap.add(i);
            }
            i++;
        }
        int child_to_expand = choose_tree_expansion(tree, expandable_children);
        children.set(indexMap.get(child_to_expand), expand_tree_once(expandable_children.get(child_to_expand)));
        return tree;
    }

    public DerivationTree expand_node(DerivationTree node) {
        expansions_count++;
        return expand_node_strategy.apply(node);
    }

    public DerivationTree expand_node_min_cost(DerivationTree node) {
        return expand_node_by_cost(node, BinaryOperator::minBy);
    }

    public DerivationTree expand_node_max_cost(DerivationTree node) {
        return expand_node_by_cost(node, BinaryOperator::maxBy);
    }

    private DerivationTree expand_node_randomly(DerivationTree node) {
        String symbol = node.get_symbol_name();
        List<DerivationTree> children = node.get_children();
        assert children == null;

        JSONArray expansions = (JSONArray) grammar.get(symbol);
        List<List<DerivationTree>> children_alternatives = new ArrayList<>();
        for (Object expansion : expansions) {
            children_alternatives.add(expansion_to_children((String) expansion));
        }

        int index = choose_node_expansion(node, children_alternatives);
        List<DerivationTree> chosen_children = children_alternatives.get(index);
        chosen_children = process_chosen_children(chosen_children, (String) expansions.get(index));

        return new DerivationTree(symbol, chosen_children);
    }

    public DerivationTree expand_node_by_cost(DerivationTree node, Function<Comparator<Double>, BinaryOperator<Double>> choose) {
        String symbol = node.get_symbol_name();
        List<DerivationTree> children = node.get_children();
        assert children == null;

        JSONArray expansions = (JSONArray) grammar.get(symbol);

        List<List<DerivationTree>> children_alternatives = new ArrayList<>();
        List<Double> costs = new ArrayList<>();
        for (Object e : expansions) {
            String expansion = (String) e;
            children_alternatives.add(expansion_to_children(expansion));
            costs.add(expansion_cost(expansion, Collections.singleton(symbol)));
        }

        double chosen_cost = costs.stream().reduce(choose.apply(Comparator.comparingDouble(Double::doubleValue))).get();

        List<List<DerivationTree>> children_with_chosen_cost = new ArrayList<>();
        List<String> expansions_with_chosen_cost = new ArrayList<>();

        for (int i = 0; i < expansions.size(); i++) {
            if (chosen_cost == costs.get(i)) {
                expansions_with_chosen_cost.add((String) expansions.get(i));
                children_with_chosen_cost.add(children_alternatives.get(i));
            }
        }

        int index = choose_node_expansion(node, children_with_chosen_cost);

        List<DerivationTree> chosen_children = children_with_chosen_cost.get(index);
        String chosen_expansion = expansions_with_chosen_cost.get(index);
        chosen_children = process_chosen_children(chosen_children, chosen_expansion);

        return new DerivationTree(symbol, chosen_children);
    }

    public int choose_node_expansion(DerivationTree node, List<List<DerivationTree>> children_alternatives) {
        return rand.nextInt(children_alternatives.size());
    }

    private int choose_tree_expansion(DerivationTree tree, List<DerivationTree> children) {
        return rand.nextInt(children.size());
    }

    protected List<DerivationTree> process_chosen_children(List<DerivationTree> chosen_children, String expansion) {
        // Do nothing, but can be overloaded by a subclass
        return chosen_children;
    }

    protected List<DerivationTree> expansion_to_children(String expansion) {
        Pattern p = Pattern.compile(re_nonterminal + "|([^<> ]+)"); // TODO: Improve this regex
        Matcher m = p.matcher(expansion);
        List<String> strings = new ArrayList<>();
        while(m.find()) {
            String token = m.group(0);
            strings.add(token);
        }

        List<DerivationTree> children = new ArrayList<>();
        for (String s : strings) {
            if (is_nonterminal(s)) {
                children.add(new DerivationTree(s, null));
            } else {
                children.add(new DerivationTree(s, new LinkedList<>()));
            }
        }
        return children;
    }

    private boolean is_nonterminal(String s) {
        return s.matches(re_nonterminal);
    }

    private int possible_expansions(DerivationTree node) {
        List<DerivationTree> children = node.get_children();
        if (children == null) {
            return 1;
        }
        return children.stream().map(this::possible_expansions).mapToInt(i -> i).sum();
    }

    private boolean any_possible_expansion(DerivationTree node) {
        List<DerivationTree> children = node.get_children();
        if (children == null) {
            return true;
        }
        return children.stream().anyMatch(this::any_possible_expansion);
    }

    private double symbol_cost(String symbol, Set<String> seen) {
        JSONArray expansions = (JSONArray) grammar.get(symbol);
        Set<String> new_seen = new HashSet<>(seen);
        new_seen.add(symbol);
        double min = Double.MAX_VALUE;
        for (Object e : expansions) {
            String expansion = (String) e;
            min = Math.min(expansion_cost(expansion, new_seen), min);
        }
        return min;
    }

    private double expansion_cost(String expansion, Set<String> seen) {
        List<String> symbols = nonterminals(expansion);
        if (symbols.size() == 0) {
            return 1;
        }
        if (symbols.stream().anyMatch(seen::contains)) {
            return Double.MAX_VALUE;
        }

        return symbols.stream().map(s -> symbol_cost(s, seen)).mapToDouble(d -> d).sum() + 1;
    }

}
