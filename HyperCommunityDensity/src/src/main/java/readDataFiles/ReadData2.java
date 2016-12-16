/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package readDataFiles;

/**
 * READS a file with json objects:
 *
 * @author Dimitrios read a json file
 */
import static com.sun.corba.se.impl.logging.OMGSystemException.get;
import demokritos.iit.hyperCommunity.Pair;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ReadData2 {

    public static void main(String[] args) throws IOException {
        //String filename ="D:/Source/dataSets/data_Twitter_Crimea/tweets.json.1";
        try {
            String fileName = "D:/Source/dataSets/data_Twitter_Crimea/tweets.json.1";;
            ArrayList<JSONObject> jsons = ReadJSON(new File(fileName), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void write2JsonCommV2(HashMap<Integer, ArrayList<Integer>> nodesCommLabels,
            Map<Integer, Integer> nodeMap,
            int[] maxValues,
            String FileNameCommResults) throws IOException {

        JSONObject obj4 = new JSONObject();
        JSONObject obj5 = new JSONObject();

        for (int i = 1; i < maxValues.length; i++) {
            maxValues[i] += maxValues[i - 1];
        }

        int prev = 0;
        int i = 0;
        int j = 0;
        for (HashMap.Entry<Integer, ArrayList<Integer>> entry : nodesCommLabels.entrySet()) {
            //  System.out.println("entry=" + entry);
            prev = i;
            i = 0;
            while (!(entry.getKey() < maxValues[i])) {
                i++;
            }

            if (!(prev == i)) {
               // System.out.println("obj-4=" + obj4.toJSONString());
                JSONObject clone = new JSONObject(obj4);
                obj5.put(prev + 1, clone);
                obj4.clear();
                j++;
                //      System.out.println("Obj5=" + obj5.toJSONString());

            }

            obj4.put(nodeMap.get(entry.getKey()), entry.getValue());

            //   System.out.println(i+" obj3=comms="+obj3);
            //System.out.println(i+" "+obj3.toJSONString());
        }
        obj5.put(prev + 1, obj4);
        //  System.out.println("obj4=" + obj4.toJSONString());
        System.out.println("obj5=" + obj5.toJSONString());

        try (FileWriter file = new FileWriter(FileNameCommResults)) {
            file.write(obj5.toJSONString());
            System.out.println("Successfully Copied Communities Object to File...");
            //      System.out.println("\nJSON Object: " + obj);
            file.close();
        }
    }

    /**
     * @param nodes2Comm: the key is a node, and the values are the communities
     * in which it belongs
     * @param commsFile is a json file in which the results will be written.
     * {"comm": [[5], [14, 4], [2]} -> node 1 is in comm 5, node 2 in community
     * 14 and 4 "nodes": [1,2,3]}
     * @param Nodes is a json file that includes the nodes {"nodes":[1, 2, 3]}
     */
    public static void write2JsonComm(HashMap<Integer, ArrayList<Integer>> nodes2Comm, String commsFile) throws IOException {
        JSONObject obj = new JSONObject();
        JSONObject obj2 = new JSONObject();
        JSONArray allNodes2comms = new JSONArray();
        JSONArray singleNode2comms = new JSONArray();

        JSONObject rep3 = new JSONObject();
        JSONArray singleNode = new JSONArray();

        for (HashMap.Entry<Integer, ArrayList<Integer>> entry : nodes2Comm.entrySet()) {
            //  entry.getKey();
            singleNode2comms.add(entry.getValue());
            singleNode.add(entry.getKey());

            for (int i = 0; i < entry.getValue().size(); i++) {
                rep3.put(entry.getKey(), entry.getValue());
                //    System.out.println(entry.getValue().size()); 
            }
        }

        obj.put("comm", singleNode2comms);
        obj.put("Node", singleNode);
        //System.out.println(obj);

        // try-with-resources statement based on post comment below :)
        try (FileWriter file = new FileWriter(commsFile)) {
            file.write(rep3.toJSONString());
            System.out.println("Successfully Copied Communities Object to File...");
            //      System.out.println("\nJSON Object: " + obj);
            file.close();
        }

        // try-with-resources statement based on post comment below :)
        try (FileWriter file = new FileWriter(commsFile + "_c")) {
            file.write(obj.toJSONString());
            System.out.println("Successfully Copied Communities Object to File...");
            //      System.out.println("\nJSON Object: " + obj);
            file.close();
        }

        /*
         try (FileWriter file = new FileWriter(commsFile)) {
         file.write(nodeObj.toJSONString());
         System.out.println("Successfully Copied Nodes Object to File...");
         //      System.out.println("\nJSON Object: " + obj);
         file.close();
         }
         */
    }

    public static synchronized ArrayList<JSONObject> ReadJSON(File MyFile, String Encoding) throws FileNotFoundException, ParseException {
        Scanner scn = new Scanner(MyFile, Encoding);
        ArrayList<JSONObject> json = new ArrayList<JSONObject>();

//Reading and Parsing Strings to Json
        while (scn.hasNext()) {
            JSONObject obj = (JSONObject) new JSONParser().parse(scn.nextLine());
            json.add(obj);
        }

//Here Printing Json Objects
        int cnt = 1, urlCnt = 1;

        for (JSONObject obj : json) {
            //  System.out.println((String)obj.get("user")+" : ");
            Object userId = ((JSONObject) obj.get("user")).get("id");
            Object urls = ((JSONObject) obj.get("entities")).get("urls");
            JSONArray urlsArray = (JSONArray) urls;

            if ((urls.toString().length()) >= 3) {
                JSONObject url = (JSONObject) urlsArray.get(0);

                Object urlObj = url.get("expanded_url");
                Object tweet = obj.get("text");
                System.out.println(userId + "::" + urlObj + "::" + tweet);
                urlCnt++;
                // System.out.println(userId+" : "+tweet);
            }
            cnt++;
        }
        System.out.println("\nNumber of lines-tweets read / URLs " + cnt + " " + urlCnt);
        return json;
    }

}
