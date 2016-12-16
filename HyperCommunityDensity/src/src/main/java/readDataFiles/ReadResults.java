/**
 *
 * @author Dimitrios This is meant to read community results that we produced by
 * an outside programme (stored in a Json file) Later it feed to results to
 * HyperCommunityVx.java class for further processing * Dimitrios The
 * communities should be in integer array. The index of the array denotes the
 * node, and the value the community label. Community labels start froem
 */
package readDataFiles;

//import demokritos.iit.hyperCommunity.*;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 */
public class ReadResults {

    enum RESULTS_COMM_ALGORITHM {

        CLIQUES3_MOVIELENS,
        CLIQUES3_DELICIOUS,
        CLIQUES3_LASTFM,
        CLIQUES3_SNOW,
        CLIQUES3_CRIMEA,
        CLIQUES3_WORLDCUP,
        SPECTRAL_CUSTOM_10_MOVIELENS,
        SPECTRAL_CUSTOM_50_MOVIELENS,
        SPECTRAL_CUSTOM_100_MOVIELENS,
        SPECTRAL_CUSTOM_LASTFM,
        SPECTRAL_CUSTOM_CUSTOM_DELICIOUS,
        SYNTHETIC_MURATA_p001,
        SYNTHETIC_MURATA_p01,
        SYNTHETIC_MURATA_p02

    };

    public static int[] readResults() {
        int commLabels[] = null;
        String path = "D:\\Dropbox\\syntheticData\\results_synthetic_murata\\results\\";
        String file = "";
        RESULTS_COMM_ALGORITHM resultsIndex = RESULTS_COMM_ALGORITHM.SYNTHETIC_MURATA_p001;

        try {
            JSONParser parser = new JSONParser();
            //  String s;
            Object obj;

            switch (resultsIndex) {
                case CLIQUES3_MOVIELENS:
                    file = "\\MovieLens\\communities_gt.json";
                    break;
                case SPECTRAL_CUSTOM_10_MOVIELENS:
                    file = "\\MovieLens\\communities_hg_k-10_w_custom.json";
                    break;
                case SPECTRAL_CUSTOM_50_MOVIELENS:
                    file = "\\MovieLens\\communities_hg_k-50_w_custom.json";
                    break;
                case SPECTRAL_CUSTOM_100_MOVIELENS:
                    file = "\\MovieLens\\communities_hg_k-100_w_custom.json";
                    break;
                case CLIQUES3_SNOW:
                    file = "\\Snow\\3cliqueResults\\communities_gt.json";
                case SYNTHETIC_MURATA_p001:
                    file = "\\D:\\Dropbox\\SyntheticData\\results_synthetic_murata\\results\\murata_N5000_R3_L5_p0.01\\";
                case SYNTHETIC_MURATA_p01:
                    
                case SYNTHETIC_MURATA_p02:
            }

            String completeFile = path + file;
            obj = parser.parse(new FileReader(path + file));

            JSONObject jsonObject = (JSONObject) obj;

            JSONArray commList = (JSONArray) jsonObject.get("comm");
            Iterator<Long> iter = commList.iterator();
            //System.out.println(jsonObject.get("comm"));
            int nNodes = commList.size();
            commLabels = new int[nNodes];
            int i = 0;
            while (iter.hasNext()) {
                long label = iter.next();
                if (label >= 0) {
                    label++;
                } else {
                    label = -1;
                }
                commLabels[i] = (int) label;
                i++;
            }

        } catch (IOException ex) {
            Logger.getLogger(ReadResults.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(ReadResults.class.getName()).log(Level.SEVERE, null, ex);
        }
        return commLabels;
    }

    public static void main(String args[]) {
        readResults();
    }

}
