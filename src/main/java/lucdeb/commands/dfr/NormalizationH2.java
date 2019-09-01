package lucdeb.commands.dfr;

import static org.apache.lucene.search.similarities.SimilarityBase.log2;
import org.apache.lucene.search.similarities.BasicStats;
/**
 * Normalization model in which the term frequency is inversely related to the
 * length.
 * <p>While this model is parameterless in the
 * <a href="http://citeseer.ist.psu.edu/viewdoc/summary?doi=10.1.1.101.742">
 * original article</a>, the <a href="http://theses.gla.ac.uk/1570/">thesis</a>
 * introduces the parameterized variant.
 * The default value for the {@code c} parameter is {@code 1}.</p>
 * @lucene.experimental
 */
public class NormalizationH2 extends Normalization {
  private final float c;
  
  /**
   * Creates NormalizationH2 with the supplied parameter <code>c</code>.
   * @param c hyper-parameter that controls the term frequency 
   * normalization with respect to the document length.
   */
  public NormalizationH2(float c) {
    this.c = c;
  }

  /**
   * Calls {@link #NormalizationH2(float) NormalizationH2(1)}
   */
  public NormalizationH2() {
    this(1);
  }
  
  @Override
  public final float tfn(BasicStats stats, float tf, float len) {
    return (float)(tf * log2(1 + c * stats.getAvgFieldLength() / len));
  }

  @Override
  public String toString() {
    return "2";
  }
  
  /**
   * Returns the <code>c</code> parameter.
   * @see #NormalizationH2(float)
   */
  public float getC() {
    return c;
  }
}
