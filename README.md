# Grammar-based Fuzzer

## Build

```shell
./gradlew shadowJar
```

## How to use

```shell
java -jar build/libs/grammar-based-fuzzer-all.jar 
```

### Options:

* `--grammar=<path to grammar>`
* `-g=<path to grammar>`
* `-ft=<[BASIC, EFFICIENT, TRACKING, SIMPLE_COVERAGE, COVERAGE]>` 
* `-n=<count of outputs>`
* `--max-num-of-expansions=<maximum number of expansions>`
* `--seed=<seed>`

### Example

```shell
java -jar build/libs/grammar-based-fuzzer-all.jar -ft=COVERAGE -g=example/math_expr_grammar.json --max-num-of-expansions=100 -n=1000
```
