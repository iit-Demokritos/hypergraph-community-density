/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package readDataFiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import static readDataFiles.TestIntersection.edgeInCommunities;
import static readDataFiles.TestIntersection.edgeInCommunity;

/**
 *
 * @author Dimitrios
 */
public class TestInverseHashMap {

    // Inverse the source hashMap and produce the desitnation
    /**
     *
     * @param source
     */
    public static void inverseHashMap(HashMap<Integer, ArrayList<Integer>> source, HashMap<Integer, ArrayList<Integer>> inversed) {

        for (HashMap.Entry<Integer, ArrayList<Integer>> entry : source.entrySet()) {
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());

            for (Integer node : entry.getValue()) {
                if (!inversed.containsKey(node)) {
                    ArrayList<Integer> invVals = new ArrayList();
                    inversed.put(node, invVals);
                }

                inversed.get(node).add(entry.getKey());

            }
        }
    }

    

    public static void main(String args[]) {
        // an 'edge' containing the following contes
        ArrayList<Integer> edge = new ArrayList<>(Arrays.asList(0, 1, 2));

        // The key is a 'node', and the value are the 'communities'
        HashMap<Integer, ArrayList<Integer>> nodesComm = new HashMap<>();
        HashMap<Integer, ArrayList<Integer>> invNodesComm = new HashMap<>();
        nodesComm.put(0, new ArrayList<>(Arrays.asList(2, 3, 4, 5)));
        nodesComm.put(1, new ArrayList<>(Arrays.asList(4, 5, 55)));
        nodesComm.put(2, new ArrayList<>(Arrays.asList(3, 4, 8, 1)));
        //nodesComm.put(3, new ArrayList<>(Arrays.asList(22, 11, 51)));

        inverseHashMap(nodesComm, invNodesComm);

    }

}
