/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irex.commands;

import common.DocumentVector;
import java.util.List;
import java.util.Locale;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.LMSimilarity;

/**
 *
 * @author dwaipayan
 */
public class CustomLMJMSim extends LMSimilarity {
    
  /** The &lambda; parameter. */
  private final double lambda;
    DocumentVector dv;
  
  /** Instantiates with the specified collectionModel and &lambda; parameter. */
  public CustomLMJMSim(CollectionModel collectionModel, float lambda) {
    super(collectionModel);
    this.lambda = lambda;
  }

  /** Instantiates with the specified &lambda; parameter. */
  public CustomLMJMSim(float lambda) {
    this.lambda = lambda;
  }

  public void setDocVector(DocumentVector dv) {
      this.dv = dv;
  }

  @Override
  protected double score(BasicStats stats, double freq, double docLen) {
    return stats.getBoost() *
        (double)Math.log(1 +
            ((1 - lambda) * freq / docLen) /
            (lambda * ((LMStats)stats).getCollectionProbability()));
  }
  
//  protected float score() {
//      float scr = 0;
//      scr = 
//        (float)Math.log(1 + 
//            ((1-lambda) * dv.getTf(term, dv) / dv.getDocSize()) / 
//                (lambda * dv.getCollectionProbability(term, reader, fieldName))
//                ;
//      return scr;
//  }

  /** Returns the &lambda; parameter. */
  public double getLambda() {
    return lambda;
  }

  @Override
  public String getName() {
    return String.format(Locale.ROOT, "Jelinek-Mercer(%f)", getLambda());
  }

    /**
     *
     * @param subs
     * @param stats
     * @param freq
     * @param docLen
     */
    @Override
    protected void explain(List<Explanation> subs, BasicStats stats,
        double freq, double docLen) {
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
        float score = (float) Math.log(1 + ((1-lambda)*freq) / (lambda * docLen * collectionProbability));
//        System.out.println("CollectionProbability: " + collectionProbability);

//        if (stats.getTotalBoost() != 1.0f) {
//            subs.add(Explanation.match(stats.getTotalBoost(), "boost"));
//        }
//        subs.add(Explanation.match(lambda, "lambda"));
//        super.explain(subs, stats, doc, freq, docLen);
//        System.out.println("freq\tdocLen\tcoll-Proba\tscore");
        System.out.println("("+freq + "\t" + docLen + "\t" + collectionProbability + "\t" + score + ")");
//        System.out.println("Freq: " + freq);
//        System.out.println("DocLen: " + docLen);
    }
}
