import fuzzer.FuzzerConfig;
import fuzzer.FuzzerFactory;
import fuzzer.FuzzerType;
import fuzzer.GrammarBasedFuzzer;
import picocli.CommandLine;
import picocli.CommandLine.Option;

@CommandLine.Command(name = "fuzzer")
public class Fuzzer implements Runnable {

    @Option(names = {"--grammar", "-g"}, required = true)
    private String grammarPath;

    @Option(names = {"--fuzzer-type", "-ft"})
    private FuzzerType fuzzerType = FuzzerType.BASIC;

    @Option(names = "-n")
    private int count = 1;

    @Option(names = "--max-num-of-expansions")
    private int maxNumOfExpansions = Integer.MAX_VALUE;

    @Option(names = "--seed")
    private int seed = 0;

    @Override
    public void run() {
        FuzzerConfig config = new FuzzerConfig(grammarPath, count, maxNumOfExpansions, seed);
        GrammarBasedFuzzer fuzzer = FuzzerFactory.create(fuzzerType, config);
        for (String s : fuzzer.generate(count)) {
            System.out.println(s);
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Fuzzer()).execute(args);
        System.exit(exitCode);
    }
}