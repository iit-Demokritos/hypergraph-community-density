/**
 *
 * Aims to discover communities in tri-partie networks A Tripartite network
 * comprises: users, named entities and URLs Data are comming from MongoDB
 *
 * @author Dimitrios Vogiatzis
 * @version 1.0
 * @since 2014-Dec modified July 2015
 *
 * This aims to read data (i.e. an edge list as text)
 *
 * The difference with respect to V3 of the same class is the handling of the
 * input arguments: It should take: 0 args -> meant to be run from within
 * Netbeans e.g. java HyperCommunityV4 1 argument -> configuration file only
 * e.g. java HyoerCommunityV5 config.file 2 arguments -> 'r' followed by
 * configuration file name: all info is contained in the configuration file e.g.
 * java HyperCommunityV5 -r config.file 5 arguments --> 'r' e.g. java
 * HyperCommunityV5 'r' assessmentID startTweet endTweet config.file
 *
 * The difference from HyperCommunityV4.java is that it works with a starting
 * and an ending time point. It assumes that data have been annotated with a
 * time field
 */
package demokritos.iit.hyperCommunity;

//import temp.Pair2;
import static demokritos.iit.hyperCommunity.HGraphScan.*;

import static readDataFiles.ReadData2.write2JsonComm;
import static readDataFiles.ReadData2.write2JsonCommV2;

//import edu.stanford.nlp.util.ArrayUtils;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import readDataFiles.ReadResults;

//import javafx.util.Pair;
/**
 *
 * @author Dimitrios
 */
public class HyperCommunityV8 {

    private static String FileNameCommResults = "";

    private static enum COMM_ALGORITHM {

        READ_RESULTS,
        HYPER_DEN,
        HYPER_DEN_OVERLAPPING
    };

    final static int NOISE = -1; //Noise nodes
    static int nPartites = 3;

    static String triPartiteFile;

    // <Communitity ID, partiteID>  <members>
    static Map<Pair<Integer, Integer>, List<Integer>> allCommunities;

    //Edges per community: e.g. com1: [[1 3 4], [4 5 6]]
    static Map<Integer, ArrayList<ArrayList<Integer>>> edgesCommunity;

    static List<List<Integer>> triPartite;

    // static ReadConfigurationFile config;
    static int eta = 1;   //It's a parameter for the hyperGraphScan: try 800 for snow
    static int epsilon = 2; //Currently irrelevants
    //static int minEta = eta, maxEta = eta, step = 500, numRuns = 10;
    static COMM_ALGORITHM commResultsProvenance = COMM_ALGORITHM.HYPER_DEN_OVERLAPPING;
    private static boolean adaptiveEta = false;

    /**
     *
     * @author Dimitrios Vogiatzis
     * @param args filename
     * @throws IOException if there is a problem
     * @throws FileNotFoundException if there is a problem
     * @throws ClassNotFoundException if there is a problem
     * @since Nov 2014
     */
    public static void main(String[] args) throws IOException, FileNotFoundException, ClassNotFoundException {
        System.out.println("Community discovery Initiated ");

        long tStartNetworkCreation = System.currentTimeMillis();
        int[] partiteIdx = new int[nPartites];

        //node map contains: newNodeId --> oldNodeId
        Map<Integer, Integer> nodeMap = new HashMap<>();

        // Three dictionaries: users, keywords and urls
        // <newUserId, originalUserID> ...  <newKeywordId, originalKeyword> ... <newURLid, originalURL>
        // the Ids for users, keywords and URLs start from '0'
        triPartite = new ArrayList<>();

        // Adjacency List of nodes for each node from every partite
        Map<Integer, Set<Integer>> dictAdjList;

        // Run it from Netbeans, or from keyboead
        handleInputArguments(args);

        // Read either from a Mongo or from a disk file (serialised java objects)
        try {
            read_data(triPartiteFile);
        } catch (InterruptedException ex) {
            Logger.getLogger(HyperCommunityV8.class.getName()).log(Level.SEVERE, null, ex);
        }
        long tEndNetCreation = System.currentTimeMillis();
        long tStart = System.currentTimeMillis();

        // transform List of Lists triPartite into a two dimensional matrix: graph
        int[][] graph = new int[triPartite.size()][];
        int[][] newGraph = new int[triPartite.size()][];
        int[] maxElementIdx = {0, 0, 0};
        int[] maxValues = new int[nPartites];

        // and make graph out of the triPartite structure
        copyArray(graph, newGraph, triPartite, maxElementIdx, maxValues);

        Boolean existDuplicates = hasDuplicatesInRows(graph);
        System.out.println("Duplicate Rows=" + existDuplicates);

        //newGraph will have consecutive numbers for all tri-partite nodes,
        makeConsecutiveNumbering(graph, newGraph, maxElementIdx, nodeMap);

        //min-max elements of each column of the graph, or of each partite
        // partiteIdx: indexes of partites
        int[] lowLimits = new int[3];
        int[] highLimits = new int[3];
        minMax(newGraph, partiteIdx, lowLimits, highLimits);

        //CommunityUtils.findAdjacencyList (newGraph);
        CommunityUtils d = new CommunityUtils();
        dictAdjList = d.findAdjacencyList(newGraph);
        int[][] adjList = new int[dictAdjList.size()][];

        //copy dictionary dictAdjList to a 2D matrix called adjList      
        for (int i = 0; i < dictAdjList.size(); i++) {
            adjList[i] = new int[dictAdjList.get(i).size()];
            Integer[] array = dictAdjList.get(i).toArray(new Integer[0]);
            int j1 = 0;
            for (int value : array) {
                adjList[i][j1++] = value;
            }
        }

        double elapsedSecondsNetCreation = (tEndNetCreation - tStartNetworkCreation) / 1000.0;
        System.out.println("Time elapsed for network creation=" + elapsedSecondsNetCreation + " s");

        d.printMinMaxDegree(dictAdjList, lowLimits, highLimits);
        long tEnd = System.currentTimeMillis();
        long tDelta = tEnd - tStart;
        double elapsedSeconds = tDelta / 1000.0;
        System.out.println("Time elapsed for preprocessing=" + elapsedSeconds + " s");

        //   int commLabels[];  //one position for each node, it contains the node labels, -1 denotes a noise node
        // epsilon, and eta are related to community discovery
        int minEta = eta;
        int maxEta = eta;
        int step = 1;
        int numRuns = 1;
      //  if (adaptiveEta) {
        //        minEta = 1000;
//            List b = Arrays.asList(ArrayUtils.toDouble(highLimits));
        //          maxEta = Arrays.stream(highLimits).max().getAsInt() / 17;
        //}

        // The actual community discovery  
        // Explores from a minEta to a maxEta following an arbitrary rule
        multiRuns(minEta, maxEta, step, epsilon, numRuns, nodeMap, newGraph,
                adjList, dictAdjList, partiteIdx, maxValues);
    }

    /**
     * Uses the input arguments
     *
     * @param args
     * @since 2015 Dimitrios Vogiatzis
     */
    private static void handleInputArguments(String[] args) {
        System.out.println("**** Number of args read=" + args.length);
        switch (args.length) {
            case 3: // Read: assessment Id, start Tweet, end Tweet, config file
                eta = Integer.parseInt(args[0]);
                triPartiteFile = args[1];
                FileNameCommResults = args[2];
                break;
            default:
                System.out.println("You need \n arg:1 for the value of eta,\n arg:2 for the file with the edge list\n arg:3 for the resulting comms ");
                // System.exit(0);
                eta = 6;
                triPartiteFile = "net5.data";
                FileNameCommResults = "resultsNet4.json";
        }
    }

    /**
     * Furnishes the community results, either by computing them or by reading
     * them from a file
     * <p>
     * @param commLabels comms labels that nodes belong to it. -1 denotes a
     * noise node
     */
    private static void furnishCommResults(int eta, int[] commLabels, int[][] newGraph,
            int[][] adjList, Integer nNoiseNodes, Integer nCommunities,
            HashMap<Integer, ArrayList<Integer>> nodesCommLabels2,
            HashMap<Integer, ArrayList<Integer>> commContentsList2) {

        switch (commResultsProvenance) {
            case HYPER_DEN:
                commLabels = hyperGraphScan(newGraph, epsilon, eta, adjList);
                break;
            // case READ_RESULTS:
            //     ReadResults results = new ReadResults();
            //    commLabels = ReadResults.readResults();
            //     break;
            case HYPER_DEN_OVERLAPPING:
                hyperGraphScanOverlappingV2(epsilon, eta, adjList, commContentsList2);

                // copy commContentsList to nodesCommLabelsList
                CommunityUtils.inverseHashMap(commContentsList2, nodesCommLabels2);

                nCommunities = commContentsList2.size();
                if (commContentsList2.get(-1) != null) {
                    nNoiseNodes = commContentsList2.get(-1).size();
                } else {
                    nNoiseNodes = 0;
                }
                break;
        }
    }

    // Run community detection multiple times with different  to get statistics about
    // number of communities, modularity, average community density, percentage of noise noes
    private static void multiRuns(int minEta, int maxEta, int step, int epsilon, int numRuns,
            Map<Integer, Integer> nodeMap, int[][] newGraph, int[][] adjList,
            Map<Integer, Set<Integer>> dictAdjList, int[] partiteIdx, int maxValues[])
            throws FileNotFoundException, IOException {

        PrintWriter writer = null, writerDistr = null;
        try {
            writer = new PrintWriter("multipartiteStats.data", "UTF-8");
            writerDistr = new PrintWriter("communitySizeDistribution.data", "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(HyperCommunityV8.class.getName()).log(Level.SEVERE, null, ex);
        }

        // community label per node
        int commLabels[] = null;

        // edges per community (completely within a community)
        edgesCommunity = new HashMap<>();

        double[] numCommunities = new double[numRuns];
        Integer nCommunities = 0;
        Integer nNoiseNodes = 0;
        double[] percentageNoise = new double[numRuns];
        int bestEta;

        bestEta = minEta;

        writer.println("Eta,  Avg/Std: Number of Communities, Avg/Std: Modularity, Avg/Std: Average Density, Avg/Std: Percentage of Noise, ");
        HashMap<Integer, ArrayList<Integer>> nodesCommLabels;// = new HashMap<>();
        HashMap<Integer, ArrayList<Integer>> nodesCommLabelsOriginalEncoding; //
        HashMap<Integer, ArrayList<Integer>> commContentsList;
        long tStart = System.currentTimeMillis();
        HashMap<Integer, Integer> a = new HashMap<>();
        for (eta = minEta; eta <= maxEta; eta += step) {
            for (int i = 0; i < numRuns; i++) {
                commLabels = null;
                nodesCommLabels = new HashMap<>();
                nodesCommLabelsOriginalEncoding = new HashMap<>();
                commContentsList = new HashMap<>();
                System.out.println("Eta=" + eta);
                furnishCommResults(eta, commLabels, newGraph, adjList, nNoiseNodes, nCommunities, nodesCommLabels, commContentsList);
                //  System.out.println("nodesCommLabels=" + nodesCommLabels.size() + " Commlabels=" + commContentsList.size());
                nCommunities = commContentsList.size();
                if (commContentsList.get(-1) != null) {
                    nNoiseNodes = commContentsList.get(-1).size();
                } else {
                    nNoiseNodes = 0;
                }

                //<commId, partiteId>:  <NodeId1, NodeId2, ....>
                allCommunities = new HashMap<>();

                storeCommResultsV2(commContentsList, nodeMap, partiteIdx, maxValues);
                convert2OrginalNodeEnc(nodesCommLabels, nodesCommLabelsOriginalEncoding, nodeMap);
                // write2JsonComm(nodesCommLabelsOriginalEncoding, FileNameCommResults);

                int elements[] = new int[newGraph.length];
                int nElementsPerPartite[] = new int[3];

                int jj, ii;
                for (jj = 0; jj < newGraph[0].length; jj++) {
                    for (ii = 0; ii < newGraph.length; ii++) {
                        elements[ii] = newGraph[ii][jj];
                    }
                    nElementsPerPartite[jj] = numUniqueElements(elements);
                }

                write2JsonCommV2(nodesCommLabels, nodeMap, nElementsPerPartite, FileNameCommResults);

                nCommunities = commContentsList.size();
                if (commContentsList.get(-1) != null) {
                    nNoiseNodes = commContentsList.get(-1).size();
                } else {
                    nNoiseNodes = 0;
                }
            }

        }

        // CommunityStats stats = new CommunityStats();
        //   stats = modularityEnhanced(newGraph, nodesCommLabels, dictAdjList, nCommunities, nNoiseNodes);
        //   stats.setEta(eta);
        long tEnd = System.currentTimeMillis();
        long tDelta = tEnd - tStart;
        double elapsedSeconds = tDelta / 1000.0;
        System.out.println("Time elapsed for community discovery=" + elapsedSeconds + "s");
        if (writer == null) {
        } else {
            writer.close();
        }

    }

    private static void convert2OrginalNodeEnc(HashMap<Integer, ArrayList<Integer>> nodesCommLabels,
            HashMap<Integer, ArrayList<Integer>> nodesCommLabelsOriginalEncoding,
            Map<Integer, Integer> nodeMap) {

        for (HashMap.Entry<Integer, ArrayList<Integer>> entry : nodesCommLabels.entrySet()) {

            nodesCommLabelsOriginalEncoding.put(nodeMap.get(entry.getKey()), entry.getValue());

        }
    }

    /**
     * Store the results that are in 'commLabels' array to a data structure that
     * is named 'allCommunities' which is a hash map with
     * <commId, PartiteID> as key, and list of nodes as value
     * <p>
     * @param commContentsList comms-->nodes that belong to it. -1 denotes a
     * noise node
     * @param partSize[] is the size of each partite in number of nodes
     * @param nodeMap[] is a hashMap from the 'newNodeId' to the 'oldNodeId' the
     * newNodeId requires all nodes to be consecutive starting from '1'.
     * @param maxValues[] are the maxima used in the encodings of the partites
     * {@value} allCommunities is a hashMap that contains the results
     */
    public static void storeCommResultsV2(HashMap<Integer, ArrayList<Integer>> commContentsList,
            Map<Integer, Integer> nodeMap,
            int[] partSize,
            int[] maxValues) {
        int partNum, sub, originalNode, i, code;
        boolean verbose = true;
        Map<Integer, List<Integer>> communities = new HashMap<>();

        System.out.println("Number of communities (included noise)"
                + commContentsList.size());

        // Build the 'all' that contains <communityId, PartiteID>: Nodes Ids
        for (HashMap.Entry<Integer, ArrayList<Integer>> entry : commContentsList.entrySet()) {
            if (entry.getKey() != NOISE) {
                for (Integer node : entry.getValue()) {
                    if (node < partSize[1]) {
                        partNum = 1;
                        sub = 0;
                    } else if (node < partSize[2]) {
                        partNum = 2;
                        sub = maxValues[0];
                    } else {
                        partNum = 3;
                        sub = maxValues[0] + maxValues[1];
                    }

                    originalNode = nodeMap.get(node) - sub;
                    if (partNum == 3) { //26/1/2014          
                        originalNode = node - partSize[2];
                    } else if (partNum == 2) {
                        originalNode = node - partSize[1];
                    } else if (partNum == 1) {
                        originalNode = node - partSize[0];
                    }

                    ArrayList<Integer> list;
                    Pair vec = new Pair(entry.getKey(), partNum);
                    if (allCommunities.get(vec) == null) {
                        list = new ArrayList<>();
                        list.add(nodeMap.get(node));
                        allCommunities.put(vec, list);
                    } else {
                        allCommunities.get(vec).add(nodeMap.get(node));
                    }
                }
            }
        }
    }

    /**
     * Read data either from mongoDB or read graph from a file In the first case
     * it will create a graph and will store it in a disk file In the second
     * case it will read a graph file from a disk
     *
     * @author Dimitrios
     */
    static void read_data(String filename) throws FileNotFoundException, IOException, InterruptedException {
        System.out.println("Starting processing:");
        //  SerialStuctures netData = new SerialStuctures();
        ReadNetwork net = new ReadNetwork(filename, "");
        triPartite = net.getPartiteNet();

    }

    /**
     * compute statistical properties of the discovered communities (1)
     * Tri-partite modularity and density
     * <p>
     * For tri-partite modularity Q = \sum_{i=1}_{i=C} (e_{i,i} - a^2_i) C is
     * the number of communities e_{ii} is the fraction of edges that are
     * entirely in the community a^2 is the fraction of edges that have a least
     * a node in the community, It also sets 'edgesCommunity' which contains the
     * edges per community
     * <p>
     * @param newGraph[] A graph, where each row is a hyper edge. And with
     * consecutive numbering
     * @param commLabels[] Community labels for each node
     * @param dictAdjList A dictionary of adjacency lists for each node
     * {@value edgesCommunity} contains triplets of encoded nodes (edges) per
     * community. it contains edges that are fully within a community it is a
     * dictionary, with the community id as key and as values an array list of
     * array lists (triplets) of edges
     * @return: Statistics related to modularity and other measures
     */
    public static CommunityStats modularity(int[][] newGraph, int[] commLabels,
            Map<Integer, Set<Integer>> dictAdjList) {

        double result = 0.0;
        double resultV2 = 0.0;

        //= allCommunities.size()/nPartites-1;
        int commPresence[] = new int[nPartites];

        Set<Integer> setUniqueCommLabels = new LinkedHashSet<Integer>();
        for (int x : commLabels) {
            if (x != NOISE) {
                setUniqueCommLabels.add(x);
            }
        }

        int nCommunities = setUniqueCommLabels.size();
        int commIn[] = new int[nCommunities];  // number of edges that are completely within a community
        int commOut[] = new int[nCommunities]; // number of edges that connect two communities
        double modularities[] = new double[nCommunities];
        double modularitiesV2[] = new double[nCommunities];
        double[] maxEdgesPerCommunity = new double[nCommunities];

        for (int i = 0; i < nCommunities; i++) {
            commIn[i] = 0;
            commOut[i] = 0;
            modularities[i] = 0.0;
            modularitiesV2[i] = 0.0;
        }

        // For all hyperEdges
        for (int i = 0; i < newGraph.length; i++) {
            //Community Labels at which hyperEdge: newGraph[i][1:end] resides
            //each row is a hyperdedge, each column is a partite
            for (int j = 0; j < newGraph[i].length; j++) {
                commPresence[j] = commLabels[newGraph[i][j]];
            }

            //Number of unique community labels for a hyper edge
            Set<Integer> setUniqueNumbers = new LinkedHashSet<Integer>();
            for (int x : commPresence) {
                setUniqueNumbers.add(x);
            }

            // Edges that point to noise nodes are not counted
            if (!setUniqueNumbers.contains(NOISE)) {

                // It's an edge that traverses at least two communities
                if (setUniqueNumbers.size() > 1) {
                    for (Integer x : setUniqueNumbers) {
                        commOut[x - 1]++;
                    }
                } else { // It's an edge that it is completely within a single community
                    commIn[commLabels[newGraph[i][0]] - 1]++;
                    int edge[] = new int[3];
                    int commLabel = commLabels[newGraph[i][0]];
                    edge = newGraph[i];
                    if (edgesCommunity.containsKey(commLabel)) {
                        ArrayList<ArrayList<Integer>> edges = new ArrayList<ArrayList<Integer>>();
                        edges = edgesCommunity.get(commLabel);
                        ArrayList<Integer> triplet = new ArrayList<Integer>();
                        triplet.add(edge[0]);
                        triplet.add(edge[1]);
                        triplet.add(edge[2]);
                        edges.add(triplet);
                        edgesCommunity.put(commLabel, edges);
                    } else {
                        ArrayList<Integer> triplet = new ArrayList<Integer>();
                        ArrayList<ArrayList<Integer>> edges = new ArrayList<ArrayList<Integer>>();
                        triplet.add(edge[0]);
                        triplet.add(edge[1]);
                        triplet.add(edge[2]);
                        edges.add(triplet);
                        edgesCommunity.put(commLabel, edges);
                    }
                }
            }
        }

        int nEntries = nCommunities;
        for (int ctr = 1; ctr < nEntries; ctr++) {
            if (edgesCommunity.get(new Integer(ctr)) != null) {
                System.out.println("Community ID=" + ctr + " " + "Edges per comm=" + edgesCommunity.get(new Integer(ctr)).size());
            }
        }

        result = 0.0;
        int allEdges = newGraph.length;
        for (int i = 0; i < commIn.length; i++) {
            //  if (commOut[i] + commIn[i] != 0) {
            //    modularities[i] = (double) commIn[i]/allEdges -  Math.pow((double) commOut[i]/allEdges,3);            //Math.pow(commOut[i], 2) / (commOut[i] + commIn[i]);
            // modularities[i] =  ((double)commIn[i]/allEdges - (double) commOut[i]/allEdges);
            modularities[i] = (double) commIn[i] / allEdges - (double) Math.pow((commIn[i] + commOut[i]) / allEdges, 3);
            modularitiesV2[i] = commIn[i] - commOut[i];
            double prod = 1;
            for (int j = 1; j <= nPartites; j++) {
                Pair vecKey = new Pair(i + 1, j);   //The vecKey contains: commId, partiteID
                if (!(allCommunities.get(vecKey) == null)) {
                    prod *= allCommunities.get(vecKey).size();
                }
            }
            maxEdgesPerCommunity[i] = prod;

            result += modularities[i];
        }

        //  result /= commIn.length;
        System.out.printf("Modularity=" + Arrays.toString(modularities));
        System.out.println("\nOveral modularity=" + result / nCommunities);
        System.out.println();
        System.out.println("nEdges within comm=" + Arrays.toString(commIn));
        System.out.println("maxEdges per comm=" + Arrays.toString(maxEdgesPerCommunity));

        double averageDensity = 0.0;
        double[] commDensity = new double[nCommunities];
        for (int i = 0; i < nCommunities; i++) {
            commDensity[i] = (double) commIn[i] / maxEdgesPerCommunity[i];
            averageDensity += commDensity[i];
        }
        averageDensity /= nCommunities;

        System.out.println("Community Density=" + Arrays.toString(commDensity));
        System.out.println("Average Density=" + averageDensity);
        System.out.println("nEdges that cross comm=" + Arrays.toString(commOut));

        CommunityStats stats = new CommunityStats();

        stats.setDensity(averageDensity);
        stats.setNumCommunities(nCommunities);
        stats.setModularity(result);

        int numNoise = 0;
        for (int i = 0; i < commLabels.length; i++) {
            if (commLabels[i] == NOISE) {
                ++numNoise;
            }
        }

        stats.setPercentageNoise((double) numNoise / commLabels.length);

        return stats;
    }

    /**
     * Checks whether a hyperedge is completely within a community
     *
     * @returns whether a hyper-Edge is completely with in a community
     */
    /**
     * compute statistical properties of the discovered communities (1)
     * Tri-partite modularity and density: It works on
     * <p>
     * For tri-partite modularity Q = \sum_{i=1}_{i=C} (e_{i,i} - a^2_i) C is
     * the number of communities e_{ii} is the fraction of edges that are
     * entirely in the community a^2 is the fraction of edges that have a least
     * a node in the community, It also sets 'edgesCommunity' which contains the
     * edges per community
     * <p>
     * @param newGraph[] A graph, where each row is a hyper edge. And with
     * consecutive numbering
     * @param nodesCommLabels[] Community labels for each node. There might be
     * more than one label per node
     * @param dictAdjList A dictionary of adjacency lists for each node
     * @param nCommunities is the number of communities {@value edgesCommunity}
     * @param nNoiseNodes is the number of noise nodes contains triplets of
     * encoded nodes (edges) per community. it contains edges that are fully
     * within a community it is a dictionary, with the community id as key and
     * as values an array list of array lists (triplets) of edges
     * @return: Statistics related to modularity and other measures
     */
    public static CommunityStats modularityEnhanced(int[][] newGraph,
            HashMap<Integer, ArrayList<Integer>> nodesCommLabels,
            Map<Integer, Set<Integer>> dictAdjList, int nCommunities, int nNoiseNodes) {

        double result = 0.0;
        double resultV2 = 0.0;

        int commIn[] = new int[nCommunities];  // number of edges that are completely within a community
        int commOut[] = new int[nCommunities]; // number of edges that connect two communities
        double modularities[] = new double[nCommunities];
        double modularitiesV2[] = new double[nCommunities];
        double[] maxEdgesPerCommunity = new double[nCommunities];

        for (int i = 0; i < nCommunities; i++) {
            commIn[i] = 0;
            commOut[i] = 0;
            modularities[i] = 0.0;
            modularitiesV2[i] = 0.0;
        }

        // For all hyperEdges
        int commLabel = 0;

        for (int i = 0; i < newGraph.length; i++) {
            ArrayList<Integer> edge = new ArrayList<>();

            for (int j = 0; j < newGraph[i].length; j++) {
                edge.add(newGraph[i][j]);
            }
            CommunityUtils commOb = new CommunityUtils();
            commOb.setEdgesCommunities(edge, nodesCommLabels);

            ArrayList<Integer> commInEdges = new ArrayList<>();
            ArrayList<Integer> commCrossEdges = new ArrayList<>();
            commInEdges = commOb.getEdgeInCommunities();
            commCrossEdges = commOb.getEdgeCrossOverCommunities();

            //Find edges that are completely in a community
            //Find edges that cross communities
            if (!commCrossEdges.contains(NOISE) && !commInEdges.contains(NOISE)) {
                if (!commCrossEdges.isEmpty()) {
                    for (int commId : commCrossEdges) {
                        commOut[commId - 1]++;
                    }
                }

                //commInEdges = completely in a community
                if (!commInEdges.isEmpty()) {
                    for (int commId : commInEdges) {
                        commIn[commId - 1]++;
                        if (edgesCommunity.containsKey(commId)) {
                            edgesCommunity.get(commId).add(edge);
                        } else {
                            ArrayList<ArrayList<Integer>> edges = new ArrayList<>();
                            edges.add(edge);
                            edgesCommunity.put(commId, edges);
                        }
                    }
                }
            }
        }

        int nEntries = nCommunities;
        for (int ctr = 1; ctr < nEntries; ctr++) {
            if (edgesCommunity.get(new Integer(ctr)) != null) {
                System.out.println("Community ID=" + ctr + " " + "Edges per comm=" + edgesCommunity.get(new Integer(ctr)).size());
            }
        }

        result = 0.0;
        int allEdges = newGraph.length;
        for (int i = 0; i < commIn.length; i++) {
            //  if (commOut[i] + commIn[i] != 0) {
            //    modularities[i] = (double) commIn[i]/allEdges -  Math.pow((double) commOut[i]/allEdges,3);            //Math.pow(commOut[i], 2) / (commOut[i] + commIn[i]);
            // modularities[i] =  ((double)commIn[i]/allEdges - (double) commOut[i]/allEdges);
            modularities[i] = (double) commIn[i] / allEdges - (double) Math.pow((commIn[i] + commOut[i]) / allEdges, 3);
            modularitiesV2[i] = commIn[i] - commOut[i];
            double prod = 1;
            for (int j = 1; j <= nPartites; j++) {
                Pair vecKey = new Pair(i + 1, j);   //The vecKey contains: commId, partiteID
                if (!(allCommunities.get(vecKey) == null)) {
                    prod *= allCommunities.get(vecKey).size();
                }
            }
            maxEdgesPerCommunity[i] = prod;

            result += modularities[i];
        }

        //  result /= commIn.length;
        System.out.printf("Modularity=" + Arrays.toString(modularities));
        System.out.println("\nOveral modularity=" + result / nCommunities);
        System.out.println();
        System.out.println("nEdges within comm=" + Arrays.toString(commIn));
        System.out.println("maxEdges per comm=" + Arrays.toString(maxEdgesPerCommunity));

        double averageDensity = 0.0;
        double[] commDensity = new double[nCommunities];
        for (int i = 0; i < nCommunities; i++) {
            commDensity[i] = (double) commIn[i] / maxEdgesPerCommunity[i];
            averageDensity += commDensity[i];
        }
        averageDensity /= nCommunities;

        System.out.println("Community Density=" + Arrays.toString(commDensity));
        System.out.println("Average Density=" + averageDensity);
        System.out.println("nEdges that cross comm=" + Arrays.toString(commOut));

        CommunityStats stats = new CommunityStats();

        stats.setDensity(averageDensity);
        stats.setNumCommunities(nCommunities);
        stats.setModularity(result);

        stats.setPercentageNoise((double) nNoiseNodes / nodesCommLabels.size());

        return stats;
    }

    static int numUniqueElements(int numbers[]) {

        Set<Integer> setUniqueNumbers = new LinkedHashSet<Integer>();
        for (int x : numbers) {
            setUniqueNumbers.add(x);
        }
       // for (Integer x : setUniqueNumbers) {
        //     System.out.println(x);
        // }
        return setUniqueNumbers.size();
    }

    // Prints the unique elements of an integer 'array', plus the number of occurances of each element
    static void printUniqueInteger(int array[]) {
        HashMap<Integer, Integer> map = new HashMap();
        for (Integer string : array) {
            if (!map.containsKey(string)) {
                map.put(string, 1);
            } else {
                Integer count = map.get(string);
                count = count + 1;
                map.put(string, count);
            }
        }

        System.out.println("Community ID    #Members");
        for (Integer key : map.keySet()) {
            System.out.println(key + " " + map.get(key));
        }
    }

    // for an integer 'array' it returns true or false depending on existence
    // of duplicate rows
    static boolean hasDuplicatesInRows(int[][] array) {
        for (int i = 0; i < array.length; i++) {
            HashSet<Integer> set = new HashSet<Integer>();
            for (int j = 0; j < array.length; j++) {
                if (set.contains(array[j][i])) {
                    return false;
                }
                set.add(array[j][i]);
            }
        }
        return true;
    }

    // The number of all nodes in 'graph' will be made consecutive starting from 0 till the end
    // 'graph' is the original hyper-graph represented as an incidence matrix
    // 'newGraph' is the re-encoded graph
    // 'nPartites': number of partites
    // 'maxElementsIdx': are the indices of the maximum elements for each partite
    // 'resultMap' is a hashmap containing key: newNodeCode,  value: oldCode
    static void makeConsecutiveNumbering(int graph[][], int newGraph[][], int maxElementIdx[], Map<Integer, Integer> resultMap) {
        //map will contain a mapping between the old encoding of nodes and the new one
        //which is consecutive
        //works  for 3 -parties only
        Map<Integer, Integer> map = new HashMap();

        for (int j = 1; j < nPartites; j++) {
            for (int i = 0; i < graph.length; i++) {
                if (j == 1) {
                    graph[i][j] += graph[maxElementIdx[j - 1]][j - 1] + 1;
                } else if (j == 2) {
                    graph[i][j] += graph[maxElementIdx[j - 1]][j - 1] + graph[maxElementIdx[j - 2]][j - 2] + 2;
                }
            }
        }

        // make newGraph to have consecutive numbering
        int index = 0;
        int nNodesPart = 0;
        for (int j = 0; j < nPartites; j++) {
            nNodesPart += index;

            for (int i = 0; i < graph.length; i++) {
                if (!map.containsKey(graph[i][j])) {
                    map.put(graph[i][j], index);
                    int sub = 0;
                    if (j == 1) {
                        sub = graph[maxElementIdx[j - 1]][j - 1] + 1;
                    } else if (j == 2) {
                        sub = graph[maxElementIdx[j - 1]][j - 1] + graph[maxElementIdx[j - 2]][j - 2] + 2;
                    }
                    resultMap.put(index, graph[i][j] - sub);
                    newGraph[i][j] = index;
                    index++;
                } else {
                    int val = (Integer) map.get(graph[i][j]);
                    newGraph[i][j] = val;
                }
            }
        }
    }

    //* finds min-max in an array.
    //assumes the values start from 0 and go upwards
    // input: nums   : 2D array
    //ouput: indexes
    static public void minMax(int nums[][], int[] indexes, int lowLimits[], int highLimits[]) {
        for (int j = 0; j < nums[0].length; j++) {
            int min = nums[0][j];
            int max = nums[0][j];

            for (int i = 0; i < nums.length; i++) {
                if (nums[i][j] < min) {
                    min = nums[i][j];
                } else if (nums[i][j] > max) {
                    max = nums[i][j];
                }
            }
            System.out.println("Partite-" + j + "  Min/Max " + min + " / " + max);
            lowLimits[j] = min;
            highLimits[j] = max;
            indexes[j] = min;
        }
    }

    // Copy 'graph' to 'newGraph' and mind the maximum values in each columnn 
    // (maxElementIdx are the indexes, and maxValues are the max values
    static public void copyArray(int[][] graph, int[][] newGraph, List<List<Integer>> triPartite, int[] maxElementIdx, int[] maxValues) {
        for (int i = 0; i < triPartite.size(); i++) {
            graph[i] = new int[triPartite.get(i).size()];
            newGraph[i] = new int[triPartite.get(i).size()];
            for (int j = 0; j < triPartite.get(i).size(); j++) {
                graph[i][j] = (triPartite.get(i).get(j));
                newGraph[i][j] = graph[i][j];
                if (graph[i][j] > graph[maxElementIdx[j]][j]) {
                    maxElementIdx[j] = i;
                    maxValues[j] = graph[i][j];
                }
            }
        }
    }

}
