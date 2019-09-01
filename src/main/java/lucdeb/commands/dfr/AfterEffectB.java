package lucdeb.commands.dfr;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.Explanation;

/**
 * Model of the information gain based on the ratio of two Bernoulli processes.
 * @lucene.experimental
 */
public class AfterEffectB extends AfterEffect {

  /** Sole constructor: parameter-free */
  public AfterEffectB() {}

  @Override
  public final float score(BasicStats stats, float tfn) {
    long F = stats.getTotalTermFreq()+1;
    long n = stats.getDocFreq()+1;
    return (F + 1) / (n * (tfn + 1));
  }
  
  @Override
  public final Explanation explain(BasicStats stats, float tfn) {
    return Explanation.match(
        score(stats, tfn),
        getClass().getSimpleName() + ", computed from: ",
        Explanation.match(tfn, "tfn"),
        Explanation.match(stats.getTotalTermFreq(), "totalTermFreq"),
        Explanation.match(stats.getDocFreq(), "docFreq"));
  }

  @Override
  public String toString() {
    return "B";
  }
}
