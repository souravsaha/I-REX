/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lucdeb.commands;

import common.DocumentVector;
import java.util.List;
import java.util.Locale;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.LMSimilarity;

/**
 *
 * @author dwaipayan
 */
public class CustomLMDirSim extends LMSimilarity {
    
    /** The &mu; parameter. */
    private final float mu;
    DocumentVector dv;
  
  /** Instantiates with the specified collectionModel and &mu; parameter. */
  public CustomLMDirSim(CollectionModel collectionModel, float mu) {
    super(collectionModel);
    this.mu = mu;
  }

  /** Instantiates with the specified &mu; parameter. */
  public CustomLMDirSim(float mu) {
    this.mu = mu;
  }

  public void setDocVector(DocumentVector dv) {
      this.dv = dv;
  }

  @Override
  protected float score(BasicStats stats, float freq, float docLen) {
      float score =
          stats.getTotalBoost() *
        (float)(Math.log(1 + freq /(mu * ((LMStats)stats).getCollectionProbability())) + Math.log(mu / (docLen + mu)));
    return score > 0.0f ? score : 0.0f;
  }
  
//  protected float score() {
//      float scr = 0;
//      scr = 
//        (float)Math.log(1 + 
//            ((1-mu) * dv.getTf(term, dv) / dv.getDocSize()) / 
//                (mu * dv.getCollectionProbability(term, reader, fieldName))
//                ;
//      return scr;
//  }

  /** Returns the &mu; parameter. */
  public float getMu() {
    return mu;
  }

  @Override
  public String getName() {
    return String.format(Locale.ROOT, "Dir(%f)", getMu());
  }

  /** Implemented as <code>log(1 + (numDocs - docFreq + 0.5)/(docFreq + 0.5))</code>. */
  protected float idf(long docFreq, long numDocs) {
    return (float) Math.log(1 + (numDocs - docFreq + 0.5D)/(docFreq + 0.5D));
  }

    /**
     *
     * @param subs
     * @param stats
     * @param doc
     * @param freq
     * @param docLen
     */
    @Override
    protected void explain(List<Explanation> subs, BasicStats stats, int doc,
        float freq, float docLen) {
        //System.out.println("\nHere in CustomeLMJM.Explain()");
        //System.out.println("getAvgFieldLength: "+stats.getAvgFieldLength());
        //System.out.println("getDocFreq: "+stats.getDocFreq());
        //System.out.println("getNumberOfDocuments: "+stats.getNumberOfDocuments());
        //System.out.println("getNumberOfFieldTokens: "+stats.getNumberOfFieldTokens());
        //System.out.println("getTotalBoost: "+stats.getTotalBoost());
        //System.out.println("getTotalTermFreq: "+ stats.getTotalTermFreq());
        //System.out.println("docLen: " + docLen);
        //System.out.println("getValueForNormalization" + stats.getValueForNormalization());

        // following two are used for CollectionProba. computation.
//        System.out.println("getTotalTermFreq: "+ stats.getTotalTermFreq());
//        System.out.println("getNumberOfFieldTokens: "+stats.getNumberOfFieldTokens());
        float collectionProbability = (float)stats.getTotalTermFreq() / (float)stats.getNumberOfFieldTokens();
        float score = 
            stats.getTotalBoost() *
            (float)(Math.log(1 + freq /(mu * ((LMStats)stats).getCollectionProbability())) + Math.log(mu / (docLen + mu)));

//        System.out.println("CollectionProbability: " + collectionProbability);

//        if (stats.getTotalBoost() != 1.0f) {
//            subs.add(Explanation.match(stats.getTotalBoost(), "boost"));
//        }
//        subs.add(Explanation.match(mu, "mu"));
//        super.explain(subs, stats, doc, freq, docLen);
//        System.out.println("freq\tdocLen\tcoll-Proba\tscore");
        System.out.println("("+freq + "\t" + docLen + "\t" + collectionProbability + "\t" + score + ")");
//        System.out.println("Freq: " + freq);
//        System.out.println("DocLen: " + docLen);

        System.out.println("2. cf: "+ stats.getTotalTermFreq());
        System.out.println("3. df: "+stats.getDocFreq());
        System.out.println("4. idf: "+idf(stats.getDocFreq(), stats.getNumberOfDocuments()));
        System.out.println("5. tf: " + freq);
        System.out.println("6. |d|: " + docLen);
        System.out.println("7. avgdl: "+stats.getAvgFieldLength());
        System.out.println("8. coll-proba.: "+collectionProbability);
        System.out.println("9. score: "+score);
        System.out.println("num-docs: "+stats.getNumberOfDocuments());
        System.out.println("coll-size: "+stats.getNumberOfFieldTokens());
    }
}
