package fuzzer;

import org.json.simple.JSONArray;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EfficientGrammarFuzzer extends BasicGrammarBasedFuzzer {

    private static final String nonTerminalsRegex = "(<[^<> ]*>)";
    private int expansionsCount;
    protected int maxNumOfExpansions;

    private Function<DerivationTree, DerivationTree> expansionStrategy;

    public EfficientGrammarFuzzer(FuzzerConfig config) {
        super(config);
        maxNumOfExpansions = config.getMaxExpansions();
    }

    @Override
    public String generate() {
        DerivationTree tree = initTree();
        expansionsCount = 0;
        tree = expandTree(tree);
        return tree.toString();
    }

    private DerivationTree initTree() {
        return new DerivationTree(INITIAL_SYMBOL, null);
    }

    private DerivationTree expandTree(DerivationTree tree) {
        expansionStrategy = this::expandNodeRandomly;
        while (anyPossibleExpansion(tree)) {
            if (expansionsCount > maxNumOfExpansions) {
                expansionStrategy = this::expandNodeMinCost;
            }
            tree = expandTreeOnce(tree);
        }
        assert possibleExpansions(tree) == 0;
        return tree;
    }

    public DerivationTree expandTreeOnce(DerivationTree tree) {
        List<DerivationTree> children = tree.getChildren();
        if (children == null) {
            return expandNode(tree);
        }
        List<DerivationTree> expandableChildren = children.stream()
                .filter(this::anyPossibleExpansion)
                .collect(Collectors.toList());
        List<Integer> indexMap = new ArrayList<>();
        int i = 0;
        for (DerivationTree c : children) {
            if (expandableChildren.contains(c)) {
                indexMap.add(i);
            }
            i++;
        }
        int childToExpand = chooseTreeExpansion(tree, expandableChildren);
        children.set(indexMap.get(childToExpand), expandTreeOnce(expandableChildren.get(childToExpand)));
        return tree;
    }

    public DerivationTree expandNode(DerivationTree node) {
        expansionsCount++;
        return expansionStrategy.apply(node);
    }

    public DerivationTree expandNodeMinCost(DerivationTree node) {
        return expandNodeByCost(node, BinaryOperator::minBy);
    }

    public DerivationTree expandNodeMaxCost(DerivationTree node) {
        return expandNodeByCost(node, BinaryOperator::maxBy);
    }

    private DerivationTree expandNodeRandomly(DerivationTree node) {
        String symbol = node.getSymbolName();
        List<DerivationTree> children = node.getChildren();
        assert children == null;

        JSONArray expansions = (JSONArray) grammar.get(symbol);
        List<List<DerivationTree>> childrenAlternatives = new ArrayList<>();
        for (Object expansion : expansions) {
            childrenAlternatives.add(expansionToChildren((String) expansion));
        }

        int index = chooseNodeExpansion(node, childrenAlternatives);
        List<DerivationTree> chosenChildren = childrenAlternatives.get(index);
        chosenChildren = processChosenChildren(chosenChildren, (String) expansions.get(index));

        return new DerivationTree(symbol, chosenChildren);
    }

    public DerivationTree expandNodeByCost(DerivationTree node, Function<Comparator<Double>, BinaryOperator<Double>> choose) {
        String symbol = node.getSymbolName();
        List<DerivationTree> children = node.getChildren();
        assert children == null;

        JSONArray expansions = (JSONArray) grammar.get(symbol);

        List<List<DerivationTree>> childrenAlternatives = new ArrayList<>();
        List<Double> costs = new ArrayList<>();
        for (Object e : expansions) {
            String expansion = (String) e;
            childrenAlternatives.add(expansionToChildren(expansion));
            costs.add(expansionCost(expansion, Collections.singleton(symbol)));
        }

        double chosenCost = costs.stream().reduce(choose.apply(Comparator.comparingDouble(Double::doubleValue))).get();

        List<List<DerivationTree>> childrenWithChosenCost = new ArrayList<>();
        List<String> expansionsWithChosenCost = new ArrayList<>();

        for (int i = 0; i < expansions.size(); i++) {
            if (chosenCost == costs.get(i)) {
                expansionsWithChosenCost.add((String) expansions.get(i));
                childrenWithChosenCost.add(childrenAlternatives.get(i));
            }
        }

        int index = chooseNodeExpansion(node, childrenWithChosenCost);

        List<DerivationTree> chosenChildren = childrenWithChosenCost.get(index);
        String chosenExpansion = expansionsWithChosenCost.get(index);
        chosenChildren = processChosenChildren(chosenChildren, chosenExpansion);

        return new DerivationTree(symbol, chosenChildren);
    }

    public int chooseNodeExpansion(DerivationTree node, List<List<DerivationTree>> childrenAlternatives) {
        return rand.nextInt(childrenAlternatives.size());
    }

    private int chooseTreeExpansion(DerivationTree tree, List<DerivationTree> children) {
        return rand.nextInt(children.size());
    }

    protected List<DerivationTree> processChosenChildren(List<DerivationTree> chosenChildren, String expansion) {
        // Do nothing, but can be overloaded by a subclass
        return chosenChildren;
    }

    protected List<DerivationTree> expansionToChildren(String expansion) {
        Pattern p = Pattern.compile(nonTerminalsRegex + "|([^<>]+)");
        Matcher m = p.matcher(expansion);
        List<String> strings = new ArrayList<>();
        while(m.find()) {
            String token = m.group(0);
            strings.add(token);
        }

        List<DerivationTree> children = new ArrayList<>();
        for (String s : strings) {
            if (isNonTerminal(s)) {
                children.add(new DerivationTree(s, null));
            } else {
                children.add(new DerivationTree(s, new LinkedList<>()));
            }
        }
        return children;
    }

    private boolean isNonTerminal(String s) {
        return s.matches(nonTerminalsRegex);
    }

    private int possibleExpansions(DerivationTree node) {
        List<DerivationTree> children = node.getChildren();
        if (children == null) {
            return 1;
        }
        return children.stream().map(this::possibleExpansions).mapToInt(i -> i).sum();
    }

    private boolean anyPossibleExpansion(DerivationTree node) {
        List<DerivationTree> children = node.getChildren();
        if (children == null) {
            return true;
        }
        return children.stream().anyMatch(this::anyPossibleExpansion);
    }

    private double symbolCost(String symbol, Set<String> seen) {
        JSONArray expansions = (JSONArray) grammar.get(symbol);
        Set<String> newSeen = new HashSet<>(seen);
        newSeen.add(symbol);
        double min = Double.MAX_VALUE;
        for (Object e : expansions) {
            String expansion = (String) e;
            min = Math.min(expansionCost(expansion, newSeen), min);
        }
        return min;
    }

    private double expansionCost(String expansion, Set<String> seen) {
        List<String> symbols = nonTerminals(expansion);
        if (symbols.isEmpty()) {
            return 1;
        }
        if (symbols.stream().anyMatch(seen::contains)) {
            return Double.MAX_VALUE;
        }

        return symbols.stream().map(s -> symbolCost(s, seen)).mapToDouble(d -> d).sum() + 1;
    }

}
