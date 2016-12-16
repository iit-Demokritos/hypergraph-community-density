/*
 Dec 2014, Dimitrios Vogiatzis (C)
 Read CVS files
 Create tri-partite network
 Result is sotre in dictionary for <key, value>, and also in Map for hyperedges
 */
package demokritos.iit.hyperCommunity;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Read a tri-partite network from text files
 *
 * @author Dimitrios
 */
public class ReadNetwork {

    private List<List<Integer>> incidenceMat;
    private Map<Integer, String> dictionary;

    //String[] labelsIds;
    public ReadNetwork(String fileName, String type) {
        if (type.equals("dict")) {
            dictionary = new HashMap<Integer, String>();
            readLabelCodeV2(fileName, dictionary);
        } else {   // read tri-partite F
            readTriPartite(fileName);
        }
    }

    public Map<Integer, String> getDictionary() {
        return dictionary;
    }

    public List<List<Integer>> getPartiteNet() {
        return incidenceMat;
    }

    // each partite is denoted by a csv file with three number in each line
    private void readTriPartite(String csvFile) {
        BufferedReader br = null;
        String line = "";
        incidenceMat = new ArrayList<List<Integer>>();
        String cvsSplitBy = " ";

        try {
            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] labelsIds = line.split(cvsSplitBy);

                //	System.out.println(labelsIds[0] + " " + labelsIds[1] + " " + labelsIds[2]);               //         incidenceMat[i] = new int[3];
                ArrayList edge = new ArrayList<Integer>();
                edge.add(Integer.parseInt(labelsIds[0]));
                edge.add(Integer.parseInt(labelsIds[1]));
                edge.add(Integer.parseInt(labelsIds[2]));
                incidenceMat.add(edge);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("File not found");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO Error");
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
       //  System.out.println (incidenceMat.size());
        //System.out.println("Done");       
    }

    //reads a csv file of labels and corresponing ids
    private void readLabelCode(String csvFile, Map<String, Integer> dictionary) {
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        String[] labelsIds;

        // int j=0;
        try {
            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
                // use comma as separator
                labelsIds = line.split(cvsSplitBy);

                //System.out.println(labelsIds[0] + " " + labelsIds[1]);
                dictionary.put(labelsIds[0], Integer.parseInt(labelsIds[1]));
                //   System.out.println (j++);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("File not found");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO Error");
        } finally {

            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    // System.out.println(labelsIds[0] + " " + labelsIds[1]);
                }
            }
        }
        //System.out.println("Done");
    }

    //reads a csv file of labels and corresponing ids
    private void readLabelCodeV2(String csvFile, Map<Integer, String> dictionary) {
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        String[] labelsIds;

        // int j=0;
        try {
            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
                // use comma as separator
                labelsIds = line.split(cvsSplitBy);

                //System.out.println(labelsIds[0] + " " + labelsIds[1]);
                dictionary.put(Integer.parseInt(labelsIds[1]), labelsIds[0]);
                //   System.out.println (j++);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("File not found");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO Error");
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    // System.out.println(labelsIds[0] + " " + labelsIds[1]);
                }
            }
        }
        //System.out.println("Done");
    }

}
