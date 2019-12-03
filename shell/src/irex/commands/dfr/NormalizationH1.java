package irex.commands.dfr;
import org.apache.lucene.search.similarities.BasicStats;

/**
 * Normalization model that assumes a uniform distribution of the term frequency.
 * <p>While this model is parameterless in the
 * <a href="http://citeseer.ist.psu.edu/viewdoc/summary?doi=10.1.1.101.742">
 * original article</a>, <a href="http://dl.acm.org/citation.cfm?id=1835490">
 * information-based models</a> (see {@link IBSimilarity}) introduced a
 * multiplying factor.
 * The default value for the {@code c} parameter is {@code 1}.</p>
 * @lucene.experimental
 */
public class NormalizationH1 extends Normalization {
  private final float c;
  
  /**
   * Creates NormalizationH1 with the supplied parameter <code>c</code>.
   * @param c hyper-parameter that controls the term frequency 
   * normalization with respect to the document length.
   */
  public NormalizationH1(float c) {
    this.c = c;
  }
  
  /**
   * Calls {@link #NormalizationH1(float) NormalizationH1(1)}
   */
  public NormalizationH1() {
    this(1);
  }
  
  @Override
  public final float tfn(BasicStats stats, float tf, float len) {
    return tf * stats.getAvgFieldLength() / len;
  }

  @Override
  public String toString() {
    return "1";
  }
  
  /**
   * Returns the <code>c</code> parameter.
   * @see #NormalizationH1(float)
   */
  public float getC() {
    return c;
  }
}
