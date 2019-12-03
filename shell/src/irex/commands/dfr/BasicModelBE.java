package irex.commands.dfr;

import org.apache.lucene.search.similarities.BasicStats;
import static org.apache.lucene.search.similarities.SimilarityBase.log2;
/**
 * Limiting form of the Bose-Einstein model. The formula used in Lucene differs
 * slightly from the one in the original paper: {@code F} is increased by {@code tfn+1}
 * and {@code N} is increased by {@code F} 
 * @lucene.experimental
 * NOTE: in some corner cases this model may give poor performance with Normalizations that
 * return large values for {@code tfn} such as NormalizationH3. Consider using the 
 * geometric approximation ({@link BasicModelG}) instead, which provides the same relevance
 * but with less practical problems. 
 */
public class BasicModelBE extends BasicModel {
  
  /** Sole constructor: parameter-free */
  public BasicModelBE() {}

  @Override
  public final float score(BasicStats stats, float tfn) {
    double F = stats.getTotalTermFreq() + 1 + tfn;
    // approximation only holds true when F << N, so we use N += F
    double N = F + stats.getNumberOfDocuments();
    return (float)(-log2((N - 1) * Math.E)
        + f(N + F - 1, N + F - tfn - 2) - f(F, F - tfn));
  }
  
  /** The <em>f</em> helper function defined for <em>B<sub>E</sub></em>. */
  private final double f(double n, double m) {
    return (m + 0.5) * log2(n / m) + (n - m) * log2(n);
  }
  
  @Override
  public String toString() {
    return "Be";
  }
}
