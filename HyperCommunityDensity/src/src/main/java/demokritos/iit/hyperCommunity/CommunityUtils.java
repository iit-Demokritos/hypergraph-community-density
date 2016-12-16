/**
 * Various utilities to handle arrays, find adjacecnt lists etc...
 * 
* @author Dimitrios Vogiatzis
 * @version 1.0
 * @since 2014-10-31
 */
package demokritos.iit.hyperCommunity;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 *
 * @author Dimitrios
 */
public class CommunityUtils {

    //Given a 'edge' which are the communities that completely contain the edge
    private ArrayList<Integer> edgeInCommunities;

    //Given a 'edge' which are the communities that partially contain the edge
    private ArrayList<Integer> edgeCrossoverCommunities;

    private Map<Integer, Set<Integer>> dictAdjList;

    public CommunityUtils() {
        edgeInCommunities = new ArrayList();
        edgeCrossoverCommunities = new ArrayList();

    }

    // Implementing Fisher–Yates shuffle
    static void shuffleArray(int[] ar) {
        Random rnd = new Random();
        // rnd.setSeed(101);
        for (int i = ar.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    // ================================================================
    // Shuffle int[]
    // ================================================================
    static void shuffleArrayV2(int[] arr) {
        Random rand = new Random();
        if (rand == null) {
            rand = new Random();
        }

        for (int i = arr.length - 1; i > 0; i--) {
            swap(arr, i, rand.nextInt(i + 1));
        }
    }

    public static void swap(int[] arr, int i, int j) {
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    // find minMax degree for each partite
    public void printMinMaxDegree(Map<Integer, Set<Integer>> adjList, int[] lowLimits, int[] highLimits) {
        int[] maxDegrees = {1, 1, 1};
        int[] minDegrees = {1, 1, 1};
        int i;
        for (i = 0; i < adjList.size(); i++) {
            if (i <= highLimits[0]) {
                if (adjList.get(i).size() > maxDegrees[0]) {
                    maxDegrees[0] = adjList.get(i).size();
                }
                //  if (adjList.get(i).size()>=100)
                //    System.out.println("partite 1" + " " + adjList.get(i).size());
            } else if (i >= lowLimits[1] && i <= highLimits[1]) {
                if (adjList.get(i).size() > maxDegrees[1]) {
                    maxDegrees[1] = adjList.get(i).size();
                }

            } else {
                if (adjList.get(i).size() > maxDegrees[2]) {
                    maxDegrees[2] = adjList.get(i).size();
                }
            }
        }

        System.out.println("Max Degrees=" + maxDegrees[0] + " " + maxDegrees[1] + " " + maxDegrees[2]);
    }

    /**
     * This method prints an array to a file, typically a community
     *
     */
    public void print2fileArray(int[] commLabels, String fileName) throws IOException {
        BufferedWriter outputWriter = null;
        outputWriter = new BufferedWriter(new FileWriter(fileName));
        outputWriter.write(Arrays.toString(commLabels));
        outputWriter.close();

    }

 

    /**
     * This method is used to to build adjacency lists, given a graph with
     * hyper-edges expressed as triplets. One triplet per row.
     *
     * @param graph. In the graph there is one triplet per row
     * @return a 'Map' structure. Where the key is the node id, and the elements
     * that correspond to the key are the adjacent nodes (1 hop away)
     */
    public Map<Integer, Set<Integer>> findAdjacencyList(int[][] graph) {
        int k, j;
        int nPartites = 3;
        int key;

        //A dictionary for an adjacency list
        dictAdjList = new HashMap<Integer, Set<Integer>>();

        for (int jCols = 0; jCols < nPartites; jCols++) {
            for (int iRows = 0; iRows < graph.length; iRows++) {

                key = graph[iRows][jCols];
                Set<Integer> nodesA = new HashSet<Integer>();
                k = jCols;
                for (j = 0; j < nPartites - 1; j++) {
                    k = (k + 1) % nPartites;
                    nodesA.add(graph[iRows][k]);
                }
                Set<Integer> nodesB = new HashSet<Integer>();
                if (dictAdjList.containsKey(graph[iRows][jCols])) {
                    nodesB = dictAdjList.get(graph[iRows][jCols]);
                    //System.out.println (nodesB);
                    Set<Integer> nodesC = new HashSet<Integer>();
                    nodesC.addAll(nodesA);
                    nodesC.addAll(nodesB);
                    dictAdjList.put(graph[iRows][jCols], (Set<Integer>) nodesC);
                } else {
                    dictAdjList.put(graph[iRows][jCols], nodesA);
                }
            }
        }

        System.out.println("AdjList dict size=" + dictAdjList.size());

        return dictAdjList;
    }

    //find the subset of if 'indexes' within 'array' that have 'value' or 'value2'
    static int[] findEnhanced(int[] array, int[] indexes, int value, int value2) {
        int j = 0;
        Vector<Integer> resIdx = new Vector();
      //System.out.println (resIdx.capacity());

        //   System.out.println("len="+indexes.length+" Value ="+value + " Indexes="+Arrays.toString(indexes));
        for (int i = 0; i < indexes.length; i++) {
            // System.out.println (array[indexes[i]]);
            if ((array[indexes[i]] == value) || (array[indexes[i]] == value2)) {
                resIdx.addElement(indexes[i]);
            }
        }

        int[] result = new int[resIdx.size()];
        for (int i = 0; i < resIdx.size(); i++) {
            result[i] = resIdx.get(i);
        }

        //System.out.println (result.length);
        return result;

    }

    //find the subset of if 'indexes' within 'array' that have 'value'
    static int[] find(int[] array, int[] indexes, int value) {
        int j = 0;
        Vector<Integer> resIdx = new Vector();
      //System.out.println (resIdx.capacity());

        //   System.out.println("len="+indexes.length+" Value ="+value + " Indexes="+Arrays.toString(indexes));
        for (int i = 0; i < indexes.length; i++) {
            // System.out.println (array[indexes[i]]);
            if (array[indexes[i]] == value) {
                resIdx.addElement(indexes[i]);
            }
        }

        int[] result = new int[resIdx.size()];
        for (int i = 0; i < resIdx.size(); i++) {
            result[i] = resIdx.get(i);
        }

        //System.out.println (result.length);
        return result;

    }

    /**
     * It discover the edges that are contained completely in a community as
     * well as edges that are partially within a community
     *
     * @param edge: is a hyperedge denoted as a list of notes
     * @param nodesCommunities: is a map from 'node' to the 'communities' in
     * which it belongs, this is the case for overlapping nodes The community is
     * an integer starting from 1 and increasing there is a special community
     * that is labeled by -1 and is the noise community 'edgesInCommunities',
     * 'edgesCrossoverCommunities'
     *
     */
    public void setEdgesCommunities(ArrayList<Integer> edge, HashMap<Integer, ArrayList<Integer>> nodesCommunities) {
        //intersection: contains the community labels that host the whole of 'edge'
        ArrayList<Integer> intersection = null;

        //collect the 'communities' of the first node of the edge
        ArrayList<Integer> communities = nodesCommunities.get(edge.get(0));
        Set<Integer> commsWithCrossover = new HashSet();
        commsWithCrossover.addAll(communities);

        for (int i = 1; i < edge.size(); i++) {
            Integer node = edge.get(i);
            ArrayList<Integer> list2 = nodesCommunities.get(node);
            intersection = (ArrayList<Integer>) communities.stream().filter(list2::contains).collect(Collectors.toList());

            commsWithCrossover.addAll(list2);

            communities = (ArrayList<Integer>) intersection;
        }
        commsWithCrossover.removeAll(intersection);
        edgeInCommunities = intersection;
        edgeCrossoverCommunities.addAll(commsWithCrossover);
        //  System.out.println ("Edge in communities="+edgeInCommunities);
    }

    public ArrayList<Integer> getEdgeInCommunities() {
        return edgeInCommunities;
    }

    public ArrayList<Integer> getEdgeCrossOverCommunities() {
        return edgeCrossoverCommunities;
    }

    /**
     * Inverse the source hashMap and produce the destination 'inverted'
     *
     * @param source
     */
    public static void inverseHashMap(HashMap<Integer, ArrayList<Integer>> source, HashMap<Integer, ArrayList<Integer>> inversed) {

        for (Map.Entry<Integer, ArrayList<Integer>> entry : source.entrySet()) {
            List<Integer> list = entry.getValue();
            for (Integer obj : list) {
                if (inversed.containsKey(obj)) {
                    inversed.get(obj).add(entry.getKey());
                } else {
                    inversed.put(obj, new ArrayList<Integer>(Arrays.asList(new Integer[]{entry.getKey()})));
                }
            }
        }
/*
        source.entrySet().stream().forEach((entry) -> {
            for (Integer node : entry.getValue()) {
                if (!inversed.containsKey(node)) {
                    ArrayList<Integer> invVals = new ArrayList();
                    inversed.put(node, invVals);
                }
                inversed.get(node).add(entry.getKey());

            }
        });
    }*/

}
    
}
