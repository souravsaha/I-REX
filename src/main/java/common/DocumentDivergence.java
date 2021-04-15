package common;
import java.util.HashMap;
import java.util.Map;
/**
 *
 * @author sourav
 */

public class DocumentDivergence {

	public DocumentDivergence() {

	}

	/**
	 * Merges two relevance models that are stored in two hashmaps.
	 * 
	 * @param hashmap_TruePwGivenR
	 * @param hashmap_PwGivenR
	 * @return
	 */
	public HashMap<String, Float> mergeModels(HashMap<String, Float> hashmap_TruePwGivenR, 
        HashMap<String, Float> hashmap_PwGivenR) {

        HashMap<String, Float> mergedAvgModel = new HashMap<>();

        for (Map.Entry<String, Float> entrySet : hashmap_TruePwGivenR.entrySet()) {
            String key = entrySet.getKey();
            float value = entrySet.getValue();
            mergedAvgModel.put(key, value/2);
        }

        for (Map.Entry<String, Float> entrySet : hashmap_PwGivenR.entrySet()) {
            String key = entrySet.getKey();
            float value = entrySet.getValue();
            float existingValue = 0;
            
            float newWeight = value/ 2;

            if(mergedAvgModel.containsKey(key)) {
            	existingValue = mergedAvgModel.get(key); 
            	newWeight += existingValue;
            }
            mergedAvgModel.put(key, newWeight);
        }

        return mergedAvgModel;
    }

	/**
	 * KL Divergence
	 * 
	 * @param dist1
	 * @param dist2
	 * @return
	 */
	public float klDiv(HashMap<String, Float> dist1, HashMap<String, Float> dist2) {

		float score = 0f;
		float proba1, proba2;
		float wp2;
		String w;

		for (Map.Entry<String, Float> entrySet : dist1.entrySet()) {
			// for each of the words of True Relevant Documents:
			w = entrySet.getKey();
			proba1 = entrySet.getValue();
			wp2 = 0;
			if (dist2.containsKey(w)) {
				wp2 = dist2.get(w);
				proba2 = wp2;
				score += (proba1 * (double) Math.log(proba1 / proba2));
                //System.out.println(proba1 + " " + proba2);
			} else {
				System.err.println("The Kullback–Leibler divergence is defined only if Q(i)=0 implies P(i)=0");
			}
		} // ends for each t of True Relevant Documents

		return score;
	}

	/**
	 * JS Divergence
	 * 
	 * @param docvector1
	 * @param docvector2
	 * @return
	 */
	public float jsDiv(HashMap<String, Float> dist1, HashMap<String, Float> dist2) {

		HashMap<String, Float> avgModel = mergeModels(dist1, dist2);
		
		float score1 = klDiv(dist1, avgModel);
		float score2 = klDiv(dist2, avgModel);
		return (score1 + score2) / 2;
	}
}