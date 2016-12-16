/**
 * It implements the dbscan for hypernetworks The algorithm is based on DBSCAN
 * and has been expanded to multiparite networks It's also based on DenGraph by
 * Falkowski et.al.
 * 
* @author Dimitrios Vogiatzis
 * @version 2.0
 * @since 2014-10-31
 */
package demokritos.iit.hyperCommunity;

import static demokritos.iit.hyperCommunity.CommunityUtils.find;
import static demokritos.iit.hyperCommunity.CommunityUtils.shuffleArray;
import static demokritos.iit.hyperCommunity.CommunityUtils.shuffleArrayV2;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

/**
 *
 * @author Dimitrios The algorithm is based on DBSCAN and has been expanded to
 * multiparite networks It's also based on DenGraph by Falkowski et.al.
 */
public class HGraphScan {
    //    static HashMap<Integer, ArrayList<Integer>> commMultiLabels;

    //  public static HashMap<Integer, ArrayList<Integer>> getCommMultiLabels () {
    //      return commMultiLabels;
    // }
    // hyperGraphScanV2 is not used
    // The difference between 'hyperGraphScanV2' and 'hyperGraphScan' is that the hypergraphScanV2' tries to use borde nodes
    // 'graph' is a multipartite graph, where each row denotes the nodes that belong to a hyper edge
    // 'eta' is a parameter that specifies the minimum number of neighbours to be present so that the current node
    // can be consider to belong to a community
    // 'epsilon' is not currently user
    // 'adjList' is the adjacency List matrix
    // note epsilon and eta have been reversed compare to previous code in matalb
    public static int[] hyperGraphScanV2(int[][] graph, int epsilon, int eta, int[][] adjList) {

        final int Noise = -1;  //Noise node have fewer than 'epsilon' neighbours
        final int NoLabel = 0;
        final int Border = -2;
        final int Core = -3;

        int n = adjList.length;         // number of nodes from all partites
        int[] randMap = new int[n];     //Random re-arrangmenet of nodes
        int[] commLabels = new int[n];
        int[] states = new int[n];
        int[] degrees = new int[n];
        int[] visited = new int[n];
        int[] state = new int[n];
        int[] adjNodes;
        int[] nonLabeledNodesIdx;
        int[] noiseNodesIdx;

        int commLabelIdx = 0;
        int rNodeIdx = 0;
        int commAssignments = 0;

        boolean existUnlabeledNodes = true;
        Queue<Integer> queue = new LinkedList<Integer>();

        for (int i = 0; i < n; i++) {
            commLabels[i] = 0;
            degrees[i] = adjList[i].length;
            randMap[i] = i;
            visited[i] = 0;
        }

        shuffleArray(randMap); // randMap is a set of random indexes
        System.out.println("Starting form node=" + randMap[rNodeIdx]);

        for (rNodeIdx = 0; rNodeIdx < n; rNodeIdx++) {
            commLabels[randMap[rNodeIdx]] = Noise;
            if (degrees[randMap[rNodeIdx]] >= eta) {
                ++commLabelIdx;
                commLabels[randMap[rNodeIdx]] = commLabelIdx;
                state[randMap[rNodeIdx]] = Core;
                adjNodes = new int[adjList[randMap[rNodeIdx]].length];
                System.arraycopy(adjList[randMap[rNodeIdx]], 0, adjNodes, 0, adjList[randMap[rNodeIdx]].length);
                for (int i = 0; i < adjNodes.length; i++) {
                    state[adjNodes[i]] = Border;
                    commLabels[adjNodes[i]] = commLabelIdx;
                    queue.add(adjNodes[i]);
                }
                while (!queue.isEmpty()) {
                    int top = queue.remove();
                    if (degrees[top] >= eta) {
                        state[top] = Core;
                        commLabels[top] = commLabelIdx;
                        int[] adjNodes2 = new int[adjList[top].length];
                        for (int j = 0; j < adjNodes2.length; j++) {
                            commLabels[adjNodes2[j]] = commLabelIdx;
                            state[adjNodes2[j]] = Border;
                            queue.add(adjNodes2[j]);
                        }
                    }
                }
            }
        }
        return commLabels;
    }

    /**
     * it performs the hyperCommunity community detection and discovers
     * Overlapping communities compare to the simple hyperGraphScan
     * <p>
     * This method discovers multipartite communities in a graph that is made of
     * hyperedges. The starting point of community detection is random
     *
     * @param epsilon not currently used
     * @param eta: a basic parameter denoting the least number of neighbours
     * @param adjList: is an adjacency list for every node
     * @return an integer array of the community labels, one per node
     */
    public static void hyperGraphScanOverlappingV2(int epsilon, int eta, int[][] adjList, HashMap<Integer, ArrayList<Integer>> commMultiLabels) {
        final int Noise = -1;  //Noise node have fewer than 'epsilon' neighbours
        final int Border = -2;  // This node can be part of more communities, no just one
        final int NoLabel = 0;
        int n = adjList.length;         // number of nodes from all partites
        int[] randMap = new int[n];     //Random re-arrangmenet of nodes
        int[] commLabels = new int[n];
        //    HashMap<Integer, ArrayList<Integer>> commMultiLabels = new HashMap<>();

        int[] degrees = new int[n];
        boolean[] visited = new boolean[n];
        int[] adjNodes;
        int[] nonLabeledNodesIdx;
        int[] noiseNodesIdx;
        int[] labeledNodesIdx;
        int commLabelIdx = 0;
        int rNodeIdx = 0;
        int commAssignments = 0;
        boolean existUnlabeledNodes = true;
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            commLabels[i] = NoLabel;
            degrees[i] = adjList[i].length;
            randMap[i] = i;
            visited[i] = false;
        }

        shuffleArrayV2(randMap); // randMap is a set of random indexes
        System.out.println("Starting from node=" + randMap[rNodeIdx]);

        while (existUnlabeledNodes) {
            adjNodes = new int[adjList[randMap[rNodeIdx]].length];
            if (degrees[randMap[rNodeIdx]] >= eta) {

                // can this be twice?
                //  if ((commLabels[randMap[rNodeIdx]] != NoLabel) && (commLabels[randMap[rNodeIdx]] == NoLabel)) {
                //      System.out.println("  rNodeIdx already there ");
                //   }
                commLabels[randMap[rNodeIdx]] = ++commLabelIdx;
                visited[randMap[rNodeIdx]] = true;
                if (commMultiLabels.get(commLabelIdx) == null) {
                    commMultiLabels.put(commLabelIdx, new ArrayList<>());
                }

                commMultiLabels.get(commLabelIdx).add(randMap[rNodeIdx]);

                commAssignments++;
                //  System.out.println("Comm Assign=" + commAssignments);
                // System.out.println("A-Node added=" + randMap[rNodeIdx] + "=" + "Comm=" + commLabelIdx);

                System.arraycopy(adjList[randMap[rNodeIdx]], 0, adjNodes, 0, adjList[randMap[rNodeIdx]].length);

                // Get All the neighbours of the currrent node
                // The neighbours are unlabeled, but they might have been marked as
                // noise nodes
                nonLabeledNodesIdx = find(commLabels, adjNodes, NoLabel);
                noiseNodesIdx = find(commLabels, adjNodes, Noise);

                // add 'non-labeled' neighbours to stack
                // for (int i = 0; i < noiseNodesIdx.length; i++) {
                //     stack.push(noiseNodesIdx[i]);
                // }
                // previously found 'noise nodes' neighbours to stack 
                // for (int i = 0; i < nonLabeledNodesIdx.length; i++) {
                //     stack.push(nonLabeledNodesIdx[i]);
                //}
                for (int i = 0; i < adjNodes.length; i++) {
                    stack.push(adjNodes[i]);
                }

                labeledNodesIdx = find(commLabels, adjNodes, commLabelIdx);

                if (nonLabeledNodesIdx.length + noiseNodesIdx.length != adjNodes.length) {
                    //    System.out.println(nonLabeledNodesIdx.length + noiseNodesIdx.length - adjNodes.length + " " + labeledNodesIdx.length + "A-  Error in logic of HyperScan ");
                }

                //Same community expands: No new label assignments
                while (!stack.isEmpty()) {
                    int top = stack.pop();
                    // if (commLabels[top] > 0) {
                    //     System.out.println("top is labeled alread=" + top + " " + commLabels[top] + " " + commLabelIdx);
                    // }

                    //If the node is 'dense'  
                    if ((degrees[top] >= eta) && (commLabels[top] == NoLabel)) {//&& (commLabels[top]!=NoLabel)) {   

                        if (commLabels[top] != NoLabel) {
                            //System.out.println("  top node already there ");
                        }

                        commLabels[top] = commLabelIdx; // The  children of the current node will belong to the same community
                        visited[top] = true;

                        if (commMultiLabels.get(commLabelIdx) == null) {
                            commMultiLabels.put(commLabelIdx, new ArrayList<>());
                        }

                        if (!commMultiLabels.get(commLabelIdx).contains(top)) {
                            commMultiLabels.get(commLabelIdx).add(top);
                        }

                        commAssignments++;
                        //      System.out.println("Comm Assign=" + commAssignments);
                        //    System.out.println("B-Node added=" + top + "=" + "Comm=" + commLabelIdx);

                        int[] adjNodes2 = new int[adjList[top].length];
                        System.arraycopy(adjList[top], 0, adjNodes2, 0, adjList[top].length);

                        // Neighbours of the 'top' node may have been labeled as noise
                        // or they might
                        nonLabeledNodesIdx = find(commLabels, adjNodes2, NoLabel);
                        noiseNodesIdx = find(commLabels, adjNodes2, Noise);
                        labeledNodesIdx = find(commLabels, adjNodes2, commLabelIdx);

                        if (nonLabeledNodesIdx.length + noiseNodesIdx.length != adjNodes2.length) {
                            //        System.out.println(nonLabeledNodesIdx.length + noiseNodesIdx.length - adjNodes2.length + " " + labeledNodesIdx.length + "B  Error in logic of HyperScan ");
                        }

                        for (int i = 0; i < noiseNodesIdx.length; i++) {
                            stack.add(noiseNodesIdx[i]);
                        }

                        for (int i = 0; i < nonLabeledNodesIdx.length; i++) {
                            stack.add(nonLabeledNodesIdx[i]);
                        }

                        //It's a border node for the current community
                        // it may have been named as noise nodes by a previous community
                    } // It can be revisited
                    else if (degrees[top] < eta) {
                        commLabels[top] = NoLabel;
                        visited[top] = true;
                        if (!commMultiLabels.get(commLabelIdx).contains(top)) {
                            commMultiLabels.get(commLabelIdx).add(top);
                        }
                    }
                }
            } // A noise node from the perspective of the current core node,
            //   It might be a border node for another community or a noise node for every community
            else if (degrees[randMap[rNodeIdx]] < eta) {
                commLabels[randMap[rNodeIdx]] = Noise;    //17-06-2016  change it back to Noise. It was made to produce crisp communities
            }

            // Find the first unlabelled node. Do not find a noise node
            // Noise nodes can be noise nodes for all communities, or it might
            // be a border node 
            do {
                rNodeIdx++;
            } while (((rNodeIdx < n) && (commLabels[randMap[rNodeIdx]] != NoLabel) && (commLabels[randMap[rNodeIdx]] != Noise)));

            if (rNodeIdx >= n) {
                existUnlabeledNodes = false;
            }
        }   // End of community labeling

        if (commMultiLabels.get(Noise) == null) {
            commMultiLabels.put(Noise, new ArrayList<>());
        }
        // only the unvisited nodes become noise nodes?
        for (int i = 0; i < commLabels.length; i++) {
            if (visited[i] == false) {
                commMultiLabels.get(Noise).add(i);
            }
        }

        for (HashMap.Entry<Integer, ArrayList<Integer>> entry : commMultiLabels.entrySet()) {
            entry.getKey();
            //System.out.print ("Node="+entry.getKey()+" size="+entry.getValue().size());
            double sum = 0;
            for (int i = 0; i < entry.getValue().size(); i++) {
                sum += entry.getValue().get(i);
            }
            //   System.out.println("Node= " + entry.getKey() + " size=" + entry.getValue().size() + " sum= " + sum + " avg= " + sum / entry.getValue().size());
        }

        // return commMultiLabels;
    }

    /**
     * it performs the hyperCommunity community detection
     * <p>
     * This method discovers multipartite communities in a graph that is made of
     * hyperedges. The starting point of community detection is random
     *
     * @param graph where each row denotes the nodes that belong to a hyper edge
     * @param epsilon not currently used
     * @param eta: a basic parameter denoting the least number of neighbours
     * @param adjList: is an adjacency list for every node
     * @return an integer array of the community labels, one per node
     */
    // A possible extension is to consider overlapping communities:
    // In particular the border nodes could be made to belong to
    // multipe edges
    public static int[] hyperGraphScan(int[][] graph, int epsilon, int eta, int[][] adjList) {
        final int Noise = -1;  //Noise node have fewer than 'epsilon' neighbours
        final int NoLabel = 0;
        int n = adjList.length;         // number of nodes from all partites
        int[] randMap = new int[n];     //Random re-arrangmenet of nodes
        int[] commLabels = new int[n];
        int[] degrees = new int[n];
        int[] visited = new int[n];
        int[] adjNodes;
        int[] nonLabeledNodesIdx;
        int[] noiseNodesIdx;
        int commLabelIdx = 0;
        int rNodeIdx = 0;
        int commAssignments = 0;
        boolean existUnlabeledNodes = true;
        //Queue<Integer> queue = new LinkedList<Integer>();
        Queue<Integer> queue = new LinkedList<Integer>();

        for (int i = 0; i < n; i++) {
            commLabels[i] = 0;
            degrees[i] = adjList[i].length;
            randMap[i] = i;
            visited[i] = 0;
        }

        shuffleArray(randMap); // randMap is a set of random indexes
        System.out.println("Starting form node=" + randMap[rNodeIdx]);

        while (existUnlabeledNodes) {
            // queue.removeAll(queue);
            adjNodes = new int[adjList[randMap[rNodeIdx]].length];
            if (degrees[randMap[rNodeIdx]] >= eta) {
                //System.out.println (" Degree="+adjList[randMap[rNodeIdx]].length);
                commLabels[randMap[rNodeIdx]] = ++commLabelIdx;
                commAssignments++;
                System.arraycopy(adjList[randMap[rNodeIdx]], 0, adjNodes, 0, adjList[randMap[rNodeIdx]].length);
                nonLabeledNodesIdx = find(commLabels, adjNodes, NoLabel);
                noiseNodesIdx = find(commLabels, adjNodes, Noise);

                // add 'non-labeled' and previously found 'noise nodes' to queue 
                for (int i = 0; i < nonLabeledNodesIdx.length; i++) {
                    queue.add(nonLabeledNodesIdx[i]);
                }

                //6-3-2015
                for (int i = 0; i < noiseNodesIdx.length; i++) {
                    queue.add(noiseNodesIdx[i]);
                }

                while (!queue.isEmpty()) {
                    int top = queue.remove();

                    if (degrees[top] >= eta) {   //If the node is 'dense'  //ADITION TO BE REMOVED!!!
                        commLabels[top] = commLabelIdx; // The  children of the current node will belong to the same community
                        commAssignments++;
                        // adjNodes = adjList[top];
                        int[] adjNodes2 = new int[adjList[top].length];
                        System.arraycopy(adjList[top], 0, adjNodes2, 0, adjList[top].length);
                        nonLabeledNodesIdx = find(commLabels, adjNodes2, NoLabel);
                        noiseNodesIdx = find(commLabels, adjNodes2, Noise);

                        for (int i = 0; i < nonLabeledNodesIdx.length; i++) {
                            if (visited[nonLabeledNodesIdx[i]] == 0) {    //2/3/15   to remove
                                queue.add(nonLabeledNodesIdx[i]);
                                visited[nonLabeledNodesIdx[i]] = 1;             //2/3/15   to remove
                            }
                        }

                        //      for (int i = 0; i < noiseNodesIdx.length; i++) {
                        //          if (visited[noiseNodesIdx[i]] == 0) {
                        //             queue.add(noiseNodesIdx[i]);
                        //           visited[noiseNodesIdx[i]] = 1;
                        //        }
                        //    }          
                    } else { //if the node is not 'dense', it's a border node
                        commLabels[top] = commLabelIdx;  // Noise;  CHANGE if bacj
                    }
                }   // queue is empty
            } else {
                commLabels[randMap[rNodeIdx]] = Noise;
            }

            do {  // Find the first unlabelled node
                rNodeIdx++;
            } while (((rNodeIdx < n) && (commLabels[randMap[rNodeIdx]] != NoLabel)));//&& (commLabels[randMap[rNodeIdx]] != Noise)));

            // the condition commAssignemtns==n is irrelevant. n is the number of all nodes
            // while commAssignments-> refers to the discovered 
            if ((rNodeIdx >= n) || (commAssignments == n)) {
                existUnlabeledNodes = false;
            }
        }

        //    System.out.println(" Community Labels=" + Arrays.toString(commLabels));
        return commLabels;
    }

}
