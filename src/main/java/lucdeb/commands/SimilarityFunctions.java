// TODO: actual doclen or the normalized doclen?
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lucdeb.commands;

import common.DocumentVector;
import lucdeb.commands.dfr.*;

import java.util.List;
import java.util.Locale;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.LMSimilarity;

/**
 *
 * @author dwaipayan
 */
public class SimilarityFunctions extends LMSimilarity {
    
    /** The parameters. */
    /*
	private final float param1;
    private final float param2;
    */
    private final String param1;
    private final String param2;
    private final String param3;
    
    String simFunc;
    DocumentVector dv;

    DocTermStat dts;
    TermStats ts;

    /** Instantiates with the specified collectionModel and parameters. */
    public SimilarityFunctions(CollectionModel collectionModel, String simFunc, String param1, String param2, String param3) {
        super(collectionModel);
        this.simFunc = simFunc;
        this.param1 = param1;
        this.param2 = param2;
        this.param3 = param3;
    }

  /** Instantiates with the specified parameters. */
    public SimilarityFunctions(String simFunc, String param1, String param2, String param3) {
        this.simFunc = simFunc;
        this.param1 = param1;
        this.param2 = param2;
        this.param3 = param3;
    }

    public void setDocVector(DocumentVector dv) {
        this.dv = dv;
    }

    public void setDocTermStat(DocTermStat dts) {
        this.dts = dts;
    }

    public void setTermStats(TermStats ts) {
        this.ts = ts;
    }

    @Override
    protected float score(BasicStats stats, float freq, float docLen) {
        // TODO: actual doclen or the normalized doclen?
        float doclen = docLen /*dv.getDocSize()*/;
        float score = 0;

        switch(simFunc) {
            case "lmjm":
                score = score_LMJM(stats, freq, doclen, Float.valueOf(param1));
                break;
            case "lmdir":
                score = score_LMDir(stats, freq, doclen, Float.valueOf(param1));
                break;
            case "bm25":
                score = score_BM25(stats, freq, doclen, Float.valueOf(param1), Float.valueOf(param2));
                break;
            case "dfr":
                score = score_DFR(stats, freq, doclen, param1, param2, param3); 
                break;
        }

//        catch(UnknownFunctionException ex) {
//            System.err.println("No retrieval function: " + ex);
//        }
        return score > 0.0f ? score : 0.0f;
    }

  /** Returns the &mu; parameter. */
  public float getMu() {
    return Float.valueOf(param1);
  }

  @Override
  public String getName() {
    return String.format(Locale.ROOT, "Dir(%f)", getMu());
  }

    /** Implemented as <code>log(1 + (numDocs - docFreq + 0.5)/(docFreq + 0.5))</code>. */
    protected float idf(long docFreq, long numDocs) {
        return (float) Math.log(1 + (numDocs - docFreq + 0.5D)/(docFreq + 0.5D));
    }

    public float score_LMJM(BasicStats stats, 
        float freq, float docLen, float lambda) {

        float collectionProbability = (float)stats.getTotalTermFreq() / (float)stats.getNumberOfFieldTokens();
        float score = 
            stats.getTotalBoost() *
            (float) Math.log(1 + ((1-lambda)*freq) / (lambda * docLen * collectionProbability));

        return score;
    }

    public float score_LMDir(BasicStats stats, 
        float freq, float docLen, float mu) {

        float collectionProbability = (float)stats.getTotalTermFreq() / (float)stats.getNumberOfFieldTokens();
        float score = 
            stats.getTotalBoost() *
            (float)(Math.log(1 + freq /(mu * ((LMStats)stats).getCollectionProbability())) + Math.log(mu / (docLen + mu)));

        return score > 0.0f ? score : 0.0f;
    }

    public float score_BM25(BasicStats stats,
        float freq, float docLen, float k1, float b) {

        float weightValue = idf(stats.getDocFreq(), stats.getNumberOfDocuments()) * (k1 + 1);
        float norm = k1 * ( 1 - b + b * (docLen/stats.getAvgFieldLength()));
        float score = weightValue * freq / (freq + norm);

        return score;
    }
    public float score_DFR(BasicStats stats, 
    		float freq, float docLen, String basicModelParam, String afterEffectParam,
    		String normalizationParam)
    {
    	/*
    	try
    	{
	    	Class c = Class.forName("Normalization"+normalizationParam.toUpperCase());
	    	Normalization normalization = c.newInstance();
    	} catch(ClassNotFoundException ex)
    	{
    		System.out.println(ex.toString());
    	}*/
    	Normalization normalization = null;
    	/* very bad :( no other solutions except if, else */
    	if(normalizationParam.equals("h1"))
    	    normalization = new NormalizationH1();
    	else if(normalizationParam.equals("h2"))
    		normalization = new NormalizationH2();
    	else if(normalizationParam.equals("h3"))
    		normalization = new NormalizationH3();
    	else if(normalizationParam.equals("z"))
    		normalization = new NormalizationZ();
    	else if(normalizationParam.equals("n"))
    		normalization = new Normalization.NoNormalization();
    	else
    		System.err.println("Normalization parameter not found");
    	float tfn = normalization.tfn(stats, freq, docLen);
    	
    	/* very bad :( no other solutions except if, else */
    	AfterEffect afterEffect = null;
    	if(afterEffectParam.equals("l"))
    		afterEffect = new AfterEffectL();
    	else if(afterEffectParam.equals("b"))
    		afterEffect = new AfterEffectB();
    	else if(afterEffectParam.equals("n"))
    		afterEffect = new AfterEffect.NoAfterEffect();
    	else
    		System.err.println("AfterEffect parameter not found");
    	
    	/* very bad :( no other solutions except if, else */
    	BasicModel basicModel = null;
    	if(basicModelParam.equals("be"))
    		basicModel = new BasicModelBE();
    	else if(basicModelParam.equals("g"))
    		basicModel = new BasicModelG();
    	else if(basicModelParam.equals("p"))
    		basicModel = new BasicModelP();
    	else if(basicModelParam.equals("d"))
    		basicModel = new BasicModelD();
    	else if(basicModelParam.equals("in"))
    		basicModel = new BasicModelIn();
    	else if(basicModelParam.equals("ine"))
    		basicModel = new BasicModelIne();
    	else if(basicModelParam.equals("if"))
    		basicModel = new BasicModelIF();
    	else
    		System.err.println("BasicModel parameter not found");
    	
    	return stats.getTotalBoost() *
            basicModel.score(stats, tfn) * afterEffect.score(stats, tfn);
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

        long cf = stats.getTotalTermFreq();
        long df = stats.getDocFreq();
        long numDocs = stats.getNumberOfDocuments();
        long collSize = stats.getNumberOfFieldTokens();
        float idf = idf(df, numDocs);
        float tf = freq;
        // TODO: actual doclen or the normalized doclen?
        float doclen = docLen /*dv.getDocSize()*/;
        float avgdl = stats.getAvgFieldLength();
        float collectionProbability = cf / (float)collSize;

        float score = 0.f; 
//            stats.getTotalBoost() *
//            (float)(Math.log(1 + freq /(param1 * ((LMStats)stats).getCollectionProbability())) + Math.log(param1 / (doclen + param1)));
            //stats.getTotalBoost() *
            //(float)(Math.log(1 + freq /(Float.valueOf(param1) * ((LMStats)stats).getCollectionProbability())) + Math.log(Float.valueOf(param1) / (doclen + Float.valueOf(param1))));

        switch(simFunc) {
            case "lmjm":
                score = score_LMJM(stats, freq, doclen, Float.valueOf(param1));
                break;
            case "lmdir":
                score = score_LMDir(stats, freq, doclen, Float.valueOf(param1));
                break;
            case "bm25":
                score = score_BM25(stats, freq, doclen, Float.valueOf(param1), Float.valueOf(param2));
                break;
            case "dfr":
            	score = score_DFR(stats, freq, doclen, param1, param2, param3); 
            	break;
        }

//        System.out.println("("+freq + "\t" + docLen + "\t" + collectionProbability + "\t" + score + ")");

        
        dts.setDocLen(doclen);
        dts.setAvgDocLen(avgdl);

        ts.setCF(cf);
        ts.setDF(df);
        ts.setIDF(idf);
        ts.setCollProba(collectionProbability);
        ts.setTF(tf);
        ts.setScore(score);

        dts.terms.add(ts);

//        System.out.printf("(%d\t%d\t%.4f\t%.0f\t%.4f\t%.4f\t%.4f\t%.4f)\n", 
//                cf, df, idf, tf, doclen, avgdl, collectionProbability, score);

//        System.out.println("(" + cf + "\t" + 
//                df + "\t" +
//                idf + "\t" + 
//                tf + "\t" + 
//                doclen + /* ":" + dv.getDocSize() + */
//                "\t" +
//                avgdl + "\t" + 
//                collectionProbability + "\t" +
//                score);

//        System.out.println("2. cf: "+ stats.getTotalTermFreq());
//        System.out.println("3. df: "+stats.getDocFreq());
//        System.out.println("4. idf: "+idf(stats.getDocFreq(), stats.getNumberOfDocuments()));
//        System.out.println("5. tf: " + freq);
//        System.out.println("6. |d|: " + docLen);
//        System.out.println("7. avgdl: "+stats.getAvgFieldLength());
//        System.out.println("8. coll-proba.: "+collectionProbability);
//        System.out.println("9. score: "+score);
//        System.out.println("num-docs: "+stats.getNumberOfDocuments());
//        System.out.println("coll-size: "+stats.getNumberOfFieldTokens());
    }
}

class UnknownFunctionException extends Exception {

    String exp;

    public UnknownFunctionException(String exp) {
        this.exp = exp;
    }

    @Override
    public String toString() {return "UnknownFunctionException occurs: " + exp;}
}