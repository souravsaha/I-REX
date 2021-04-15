/**
 * RM3: Complete;
 * Relevance Based Language Model with query mix.
 * References:
 *      1. Relevance Based Language Model - Victor Lavrenko - SIGIR-2001
 *      2. UMass at TREC 2004: Novelty and HARD - Nasreen Abdul-Jaleel - TREC-2004
 */
package irex.commands.qe;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import common.TRECQueryParser;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import irex.IRexObjects;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.similarities.AfterEffectB;
import org.apache.lucene.search.similarities.BasicModelIF;
import org.apache.lucene.search.similarities.DFRSimilarity;
import org.apache.lucene.search.similarities.NormalizationH2;

/**
 *
 * @author dwaipayan
 */

public class RelevanceBasedLanguageModel {

    String          indexPath;
    String          queryPath;      // path of the query file
    File            queryFile;      // the query file
    String          stopFilePath;
    IndexReader     indexReader;
    IndexSearcher   indexSearcher;
    int             numHits;      // number of document to retrieveWithExpansionTermsFromFile
    String          queryStr;
    Query           luceneQuery;

    File            indexFile;          // place where the index is stored
//    Analyzer        analyzer;           // the analyzer
    boolean         boolIndexExists;    // boolean flag to indicate whether the index exists or not
    String          fieldToSearch;      // the field in the index to be searched
    String          fieldForFeedback;   // field, to be used for feedback
    TRECQueryParser trecQueryparser;
    int             simFuncChoice;
    float           param1, param2;
    long            vocSize;            // vocabulary size
    RLM             rlm;
    Boolean         feedbackFromFile;

    HashMap<String, TopDocs> allTopDocsFromFileHashMap;     // For feedback from file, to contain all topdocs from file

    // +++ TRF
    String qrelPath;
    boolean trf;    // true or false depending on whether True Relevance Feedback is choosen
    HashMap<String, TopDocs> allRelDocsFromQrelHashMap;     // For TRF, to contain all true rel. docs.
    // --- TRF

    int             numFeedbackTerms;// number of feedback terms
    int             numFeedbackDocs; // number of feedback documents
    float           QMIX;
    PrintStream     out;

    public RelevanceBasedLanguageModel(IRexObjects lucivObjects, String queryStr, int numExpTerms, PrintStream out) throws IOException, QueryNodeException { 
        
        // +++++ setting the analyzer with English Analyzer with Smart stopword list
//        stopFilePath = lucivObjects.stopwordPath;
//        EnglishAnalyzerWithSmartStopword engAnalyzer = new EnglishAnalyzerWithSmartStopword(stopFilePath);
//        analyzer = engAnalyzer.setAndGetEnglishAnalyzerWithSmartStopword();
        // ----- analyzer set: analyzer

        fieldToSearch = lucivObjects.getSearchField();
        fieldForFeedback = lucivObjects.getSearchField();
        /* index path set */

        /* setting indexReader and indexSearcher */
        indexReader = lucivObjects.getIndexReader();
        indexSearcher = lucivObjects.getIndexSearcher();
        /* indexReader and searher set */

        this.queryStr = queryStr;
        luceneQuery = lucivObjects.getAnalyzedQuery(queryStr);

        
        // numFeedbackTerms = number of top terms to select
        numFeedbackTerms = numExpTerms;
        // numFeedbackDocs = number of top documents to select
        numFeedbackDocs = 20;
        numHits = numFeedbackDocs;

        rlm = new RLM(this);

        this.out = out;
    }

    /**
     * Sets indexSearcher.setSimilarity() with parameter(s)
     * @param choice similarity function selection flag
     * @param param1 similarity function parameter 1
     * @param param2 similarity function parameter 2
     */
    private void setSimilarityFunction(int choice, float param1, float param2) {

        switch(choice) {
            case 0:
                indexSearcher.setSimilarity(new BM25Similarity());
                System.out.println("Similarity function set to DefaultSimilarity");
                break;
            case 1:
                indexSearcher.setSimilarity(new BM25Similarity(param1, param2));
                System.out.println("Similarity function set to BM25Similarity"
                    + " with parameters: " + param1 + " " + param2);
                break;
            case 2:
                indexSearcher.setSimilarity(new LMJelinekMercerSimilarity(param1));
                System.out.println("Similarity function set to LMJelinekMercerSimilarity"
                    + " with parameter: " + param1);
                break;
            case 3:
                indexSearcher.setSimilarity(new LMDirichletSimilarity(param1));
                System.out.println("Similarity function set to LMDirichletSimilarity"
                    + " with parameter: " + param1);
                break;
            case 4:
                indexSearcher.setSimilarity(new DFRSimilarity(new BasicModelIF(), new AfterEffectB(), new NormalizationH2()));
                System.out.println("Similarity function set to DFRSimilarity with default parameters");
                break;
        }
    } // ends setSimilarityFunction()

    public void retrieveAll() throws Exception {

        ScoreDoc[] hits;
        TopDocs topDocs;
        TopScoreDocCollector collector;

        HashMap<String, String> queryTerms = new HashMap<>();
        
        collector = TopScoreDocCollector.create(numHits, numHits);
        out.println("Initial query: " + luceneQuery.toString(fieldToSearch));

        for(String queryTerm : luceneQuery.toString(fieldToSearch).split(" "))
            queryTerms.put(queryTerm, queryTerm);
            
        // performing the search
        indexSearcher.search(luceneQuery, collector);
        topDocs = collector.topDocs();

        rlm.setFeedbackStats(topDocs, luceneQuery.toString(fieldToSearch).split(" "), this);
        /**
         * HashMap of P(w|R) for 'numFeedbackTerms' terms with top P(w|R) among each w in R,
         * keyed by the term with P(w|R) as the value.
         */
        HashMap<String, WordProbability> hashmap_PwGivenR;
        hashmap_PwGivenR = rlm.RM1(topDocs);
        //hashmap_PwGivenR = rlm.RM3(query, topDocs);

        Set<String> keySet = hashmap_PwGivenR.keySet();
        List<String> keyList = new ArrayList<>(keySet);
        int listSize = keyList.size();
        int qterms = 0;
        for (int i = 0; i < Math.min(listSize, numFeedbackTerms)+qterms; i++) {
            String expansionTerm = keyList.get(i);
            if(queryTerms.containsKey(expansionTerm))
                qterms ++;
            else
                out.println(keyList.get(i));
        }

    } // ends retrieveAll


}
