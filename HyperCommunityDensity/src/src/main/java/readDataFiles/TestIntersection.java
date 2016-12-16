/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package readDataFiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Dimitrios
 */
public class TestIntersection {

    static ArrayList<Integer> inComm;
    static ArrayList<Integer> outComm;

    /**
     * It discovers whether an the hyper-edge 'edge' is completely with a
     * community
     *
     * @param edge: is a hyperedge denoted as a list of notes
     * @param nodesCommunities: is a map from node to the communities in which
     * it belongs, this is the case for overlappng nodes
     * @returns: the communities in which each node of the hyperedge belongs to
     *
     */
    public static boolean edgeInCommunity(ArrayList<Integer> edge, HashMap<Integer, ArrayList<Integer>> nodesCommunities) {
        ArrayList<Integer> list1 = edge;
        List<Integer> intersect = null;
        for (int i = 0; i < nodesCommunities.size(); i++) {
            ArrayList<Integer> list2 = nodesCommunities.get(i);
            intersect = list1.stream().filter(list2::contains).collect(Collectors.toList());
            list1 = (ArrayList<Integer>) intersect;
        }

        return !intersect.isEmpty();
    }

    /**
     * EXPERIMENTAL It discovers the communities which the edge 'edge' crosses.
     *
     * @param edge: is a hyperedge denoted as a list of notes
     * @param nodesCommunities: is a map from 'node' to the 'communities' in
     * which it belongs, this is the case for overlappng nodes The community is
     * an integer starting from 1 and increasing there is a special community
     * that is labeled by -1 and is the noise community
     * 
     *
     */
    public static ArrayList<Integer> edgeInCommunities(ArrayList<Integer> edge, HashMap<Integer, ArrayList<Integer>> nodesCommunities) {

        //intersection: contains the community labels that host the whole of 'edge'
        ArrayList<Integer> intersection = null;

        ArrayList<Integer> list1 = nodesCommunities.get(edge.get(0));
        Set<Integer> commsWithCrossover = new HashSet();
        commsWithCrossover.addAll(list1);

        for (int i = 1; i < edge.size(); i++) {
            Integer node = edge.get(i);
            ArrayList<Integer> list2 = nodesCommunities.get(node);
            intersection = (ArrayList<Integer>) list1.stream().filter(list2::contains).collect(Collectors.toList());

            commsWithCrossover.addAll(list2);

            list1 = (ArrayList<Integer>) intersection;
        }
        commsWithCrossover.removeAll(intersection);
        inComm = intersection;
        outComm.addAll(commsWithCrossover);

        return intersection;
    }

    public static void main(String args[]) {
        // an 'edge' containing the following contes
        ArrayList<Integer> edge = new ArrayList<>(Arrays.asList(0, 1, 2));

        // The key is a 'node', and the value are the 'communities'
        HashMap<Integer, ArrayList<Integer>> nodesComm = new HashMap<>();
        nodesComm.put(0, new ArrayList<>(Arrays.asList(2, 3, 4, 5)));
        nodesComm.put(1, new ArrayList<>(Arrays.asList(4, 5, 55)));
        nodesComm.put(2, new ArrayList<>(Arrays.asList(3, 4, 8, 1)));
        //nodesComm.put(3, new ArrayList<>(Arrays.asList(22, 11, 51)));

        inComm = new ArrayList<>();
        outComm = new ArrayList<>();

        System.out.println(" Communities that completely contain the current edge=" + edgeInCommunities(edge, nodesComm));
        System.out.println(" Communities that completely contain the current edge=" + inComm);
        System.out.println(" Communities that  contain an edge that crosses other communities=" + outComm);
        System.out.println(" Edge completely within a community=" + edgeInCommunity(edge, nodesComm));

    }
}
