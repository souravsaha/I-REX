package lucdeb.commands.dfr;

import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.similarities.BasicStats;


/**
 * Model of the information gain based on Laplace's law of succession.
 * @lucene.experimental
 */
public class AfterEffectL extends AfterEffect {
  
  /** Sole constructor: parameter-free */
  public AfterEffectL() {}

  @Override
  public final float score(BasicStats stats, float tfn) {
    return 1 / (tfn + 1);
  }
  
  @Override
  public final Explanation explain(BasicStats stats, float tfn) {
    return Explanation.match(
        score(stats, tfn),
        getClass().getSimpleName() + ", computed from: ",
        Explanation.match(tfn, "tfn"));
  }
  
  @Override
  public String toString() {
    return "L";
  }
}
