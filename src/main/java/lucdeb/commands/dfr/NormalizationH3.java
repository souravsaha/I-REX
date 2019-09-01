package lucdeb.commands.dfr;
import org.apache.lucene.search.similarities.BasicStats;

/**
 * Dirichlet Priors normalization
 * @lucene.experimental
 */
public class NormalizationH3 extends Normalization {
  private final float mu;
  
  /**
   * Calls {@link #NormalizationH3(float) NormalizationH3(800)}
   */
  public NormalizationH3() {
    this(800F);
  }
  
  /**
   * Creates NormalizationH3 with the supplied parameter <code>&mu;</code>.
   * @param mu smoothing parameter <code>&mu;</code>
   */
  public NormalizationH3(float mu) {
    this.mu = mu;
  }

  @Override
  public float tfn(BasicStats stats, float tf, float len) {
    return (tf + mu * ((stats.getTotalTermFreq()+1F) / (stats.getNumberOfFieldTokens()+1F))) / (len + mu) * mu;
  }

  @Override
  public String toString() {
    return "3(" + mu + ")";
  }
  
  /**
   * Returns the parameter <code>&mu;</code>
   * @see #NormalizationH3(float)
   */
  public float getMu() {
    return mu;
  }
}
