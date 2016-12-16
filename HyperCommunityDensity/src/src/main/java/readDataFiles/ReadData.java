/**
 *read a json file from disk
 * 
 * @author Dimitrios 
 */
package readDataFiles;


import demokritos.iit.hyperCommunity.ReadNetwork.*;

//import org.codehaus.jackson.map.ObjectMapper;
//import org.codehaus.jackson.type.TypeReference;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ReadData {

    public static void main(String[] args) throws IOException {
        //String filename = "D:/Source/dataSets/data_Twitter_GameOfThrones_dataset/tweets.json.3";
        String filename;
        Boolean desktop = true;

        if (desktop == true) {
            filename = "D:/Source/dataSets/data_Twitter_Crimea/tweets.json.1";
            filename = "D:/Source/dataSets/data_Twitter_Crimea/test.json";
        } else {
            filename = "C:\\Users\\Dimitrios\\Source\\dataSets\\Crimea_dataset\\test.json";
        }

        JSONParser parser = new JSONParser();
        JSONParser parser2 = new JSONParser();
        try {

            Object obj = parser.parse(new FileReader(filename));

              // while () {
            JSONObject jsonObject = (JSONObject) obj;
            Map<String, String> all = new HashMap<String, String>();

            String lang = (String) jsonObject.get("text");
            Object lang2 = jsonObject.get("user");
            String lang3 = lang2.toString();

            System.out.println("Text: " + lang);
            System.out.println("User: " + lang2);
            System.out.println(lang2.toString());
            System.out.println("User: " + lang2);

            Object obj2 = parser2.parse((String) lang3);

            JSONObject jsonObject2 = (JSONObject) obj2;
            Object id = jsonObject2.get("id");
            System.out.println("id:" + id);

            Object lang5 = ((JSONObject) jsonObject.get("user")).get("id");
            System.out.println("id5:" + id);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
