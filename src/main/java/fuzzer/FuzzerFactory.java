package fuzzer;

public final class FuzzerFactory {

    private FuzzerFactory() {
        // Utility class
    }

    public static GrammarBasedFuzzer create(
            FuzzerType type,
            FuzzerConfig config) {

        switch (type) {

            case BASIC:
                return new BasicGrammarBasedFuzzer(config);

            case EFFICIENT:
                return new EfficientGrammarFuzzer(config);

            case TRACKING:
                return new TrackingGrammarCoverageFuzzer(config);

            case SIMPLE_COVERAGE:
                return new SimpleGrammarCoverageFuzzer(config);

            case COVERAGE:
                return new GrammarCoverageFuzzer(config);

            default:
                throw new IllegalArgumentException(
                        "Unsupported fuzzer type: " + type);
        }
    }

}