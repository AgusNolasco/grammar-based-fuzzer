import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Utils {

    public static JSONObject readGrammar(String grammarFileName) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(grammarFileName));
            String content = new String(bytes, StandardCharsets.UTF_8);

            return (JSONObject) new JSONParser().parse(content);

        } catch (IOException | ParseException e) {
            throw new IllegalArgumentException(
                    "Failed to load grammar from '" + grammarFileName + "'", e);
        }
    }

}
