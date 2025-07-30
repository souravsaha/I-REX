/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irex.commands;

import common.DocumentVector;
import common.PerTermStat;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import irex.IRexObjects;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopScoreDocCollector;

/**
 *
 * @author dwaipayan
 */
public class DocTermsCommand extends Commands {

    int luceneDocid;

    public DocTermsCommand(IRexObjects irexObjects) {
        super(irexObjects, "docterm");
    }

    @Override
    public String help() {
        return "Returns a list of terms with highest retrieval score in the documents provided.";
    }

    @Override
    public String usage() {
        return CMD_NAME + " \n" +
              "1- <\"list of docid\" (in quotes)> \n"
            + "2- <retrieval-function "+irexObjects.retFuncMap.toString()+"> \n"
            + "3- ...<parameters>";
    }

    @Override
    public void execute(String[] args, PrintStream out) throws IOException {

        String docNames[];
        //float param1, param2 = (float) 0.0;
        String param1="", param2= "", param3 = "";

        String[] retrievalModelsWithParam;
        TopScoreDocCollector collector = TopScoreDocCollector.create(10, 10);
        String searchField = irexObjects.getSearchField();

        // usage: >4 arguments:
        //  1. set of documents (in quotes)
        //  2. retrieval models with parameters: lmjm/lmdir etc.
        if (args.length < 2) {
            out.println("Usage: " + usage());
            return;
        }

        // document-names are in quotes
        docNames = args[0].split(" ");
        retrievalModelsWithParam = Arrays.copyOfRange(args, 1, args.length);

        for(String retrievalModelWithParam : retrievalModelsWithParam) {
            String[] params = retrievalModelWithParam.split(" ");
            if(!irexObjects.isKnownRetFunc(params[0])) {
                out.println("Unknown retrieval model: " + retrievalModelWithParam +"\n"
                        + "Available retrieval models: " + irexObjects.retFuncMap.keySet());
                continue;
            }
            switch(params[0]) {
                case "lmjm":
                    param1 = params[1];
                    break;
                case "lmdir":
                    param1 = params[1];
                    break;
                case "bm25":
                    param1 = params[1];
                    param2 = params[2];
                    break;
                case "dfr":
                    param1 = params[1];
                    param2 = params[2];
                    param3 = params[3];
                    break;
                default:
                    // TODO
                    break;
            }
            IndexSearcher indexSearcher = irexObjects.getIndexSearcher();
            System.out.println("Retrieval model set to " + retrievalModelWithParam);

            SimilarityFunctions simFunc = new SimilarityFunctions(params[0], param1, param2, param3);
            indexSearcher.setSimilarity(simFunc);
            Query luceneQuery;
            try {
                IndexReader indexReader = irexObjects.getIndexReader();

                // for each of the documents
                for(String docName: docNames) {
                    DocumentVector dv = new DocumentVector(searchField);

                    luceneDocid = irexObjects.getLuceneDocid(docName);
                    out.println(/*luceneDocid + " " + */docName);

                    if(luceneDocid == -1) {
                        out.println("not found");
                        return;
                    }
                    dv = dv.getDocumentVector(luceneDocid, indexReader);
                    simFunc.setDocVector(dv);
                    DocTermStat dts = new DocTermStat(docName, luceneDocid);
                    simFunc.setDocTermStat(dts);

                    // for each of the query terms q:
                    //  1. tf(q,d),
                    //  2. ntf(q,d),
                    //  3. df(q), idf(q),
                    //  4. retrieval-score(q, d)


                    for (Map.Entry<String, PerTermStat> entrySet : dv.docPerTermStat.entrySet()) {
                        String key = entrySet.getKey();
                        TermStats ts = new TermStats();
                        simFunc.setTermStats(ts);
                        luceneQuery = new QueryParser(searchField, new WhitespaceAnalyzer()).parse(key);
                        ts.term = luceneQuery.toString(searchField);
                        //System.out.printPagination(queryTerm + "\t");

                        //System.out.printPagination(key + "\t");
                        Explanation expln = indexSearcher.explain(luceneQuery, luceneDocid);
                        //out.println(expln.toString());
                        if(!expln.isMatch())
                            System.out.println("(0\t0\t0\t0)");
                        else {
                            ts.setScore(expln.getValue().doubleValue());
                            ts.setTF(entrySet.getValue().getCF());
                            ts.setDF(entrySet.getValue().getDF());
//                            System.out.println(expln.toString());
                        }
                        dts.terms.add(ts);
                        dts.doclen += entrySet.getValue().getCF();
                    }
                    System.out.println(dts.doclen + "\t" + dts.avgdl);
                    /*
                    for(TermStats ts : dts.terms) {
                        System.out.printf("%s: (%d\t%.4f\t%.0f\t%.4f\t%.4f)\n", 
                            ts.term, ts.cf, ts.idf, ts.tf, ts.collectionProbability, ts.score);
                    }
                    */
                    Collections.sort(dts.terms, new sortByTermScore());

                    System.out.println("term: (cf\tidf\ttf\tcol-proba\tscore)");

                    for (int i = 0; i < Math.min(10, dts.terms.size()); i++) {
                        TermStats ts = dts.terms.get(i);
                        System.out.printf("%s: (%d\t%.4f\t%.0f\t%f\t%.4f)\n", 
                            ts.term, ts.cf, ts.idf, ts.tf, ts.collectionProbability, ts.score);
                    }

                    /*
                    luceneQuery = irexObjects.getAnalyzedQuery(query, searchField);
                    System.out.println("Query: " + luceneQuery.toString());
                    Explanation expln = indexSearcher.explain(luceneQuery, luceneDocid);
                    out.println(expln.toString());
                    //*/
                }
            } 
            catch (Exception ex) {
                System.out.println("Exception in DocTermsCommand\n" + ex);
            }
        }
    }
    
}
