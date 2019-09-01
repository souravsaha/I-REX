package lucdeb.commands.dfr;
import org.apache.lucene.search.similarities.BasicStats;
/**
 * Pareto-Zipf Normalization
 * @lucene.experimental
 */
public class NormalizationZ extends Normalization {
  final float z;

  /**
   * Calls {@link #NormalizationZ(float) NormalizationZ(0.3)}
   */
  public NormalizationZ() {
    this(0.30F);
  }

  /**
   * Creates NormalizationZ with the supplied parameter <code>z</code>.
   * @param z represents <code>A/(A+1)</code> where <code>A</code> 
   *          measures the specificity of the language.
   */
  public NormalizationZ(float z) {
    this.z = z;
  }
  
  @Override
  public float tfn(BasicStats stats, float tf, float len) {
    return (float)(tf * Math.pow(stats.getAvgFieldLength() / len, z));
  }

  @Override
  public String toString() {
    return "Z(" + z + ")";
  }
  
  /**
   * Returns the parameter <code>z</code>
   * @see #NormalizationZ(float)
   */
  public float getZ() {
    return z;
  }
}
