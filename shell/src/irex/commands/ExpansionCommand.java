/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irex.commands;

import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import irex.IRexObjects;
import irex.commands.qe.RelevanceBasedLanguageModel;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.BM25Similarity;

/**
 *
 * @author dwaipayan
 */
public class ExpansionCommand extends Commands {

    static int M = 20;      // number of pseudo-relevant documents to use for relevance feedback

    public ExpansionCommand(IRexObjects lucivObjects) {
        super(lucivObjects, "expansion");
    }

    @Override
    public String help() {
        return "expansion - Show some potential expansion terms\n" + usage();
    }

    @Override
    public String usage() {
        return "expansion - <query-terms> [<number of expansion terms> (defaulut 10)]";
    }

    @Override
    public void execute(String[] args, PrintStream out) throws IOException {

        String queryStr;
        String searchField;
        ScoreDoc[] hits;
        TopDocs topDocs;
        int numExpTerms;
        RelevanceBasedLanguageModel rblm;

        if (args.length != 1 && args.length != 2) {
            out.println(help());
            return;
        }

        // query terms are in quotes
        queryStr = args[0]; //.split(" ");
        if(args.length == 2)
            numExpTerms = Integer.parseInt(args[1]);
        else
            numExpTerms = 10;

        searchField = lucdebObjects.getSearchField();
        IndexSearcher indexSearcher = lucdebObjects.getIndexSearcher();
        indexSearcher.setSimilarity(new BM25Similarity());
        TopScoreDocCollector collector = TopScoreDocCollector.create(M);
        Query luceneQuery;

        try {
            luceneQuery = lucdebObjects.getAnalyzedQuery(queryStr, searchField);
        } catch (QueryNodeException ex) {
            out.println("Error analysing the query. Returning...");
            return;
        }

        indexSearcher.search(luceneQuery, collector);
        topDocs = collector.topDocs();
        hits = topDocs.scoreDocs;
        if(hits == null) {
            out.println("Failed finding pseudo-relevant document.");
            return;
        }

        {
            try {
                rblm = new RelevanceBasedLanguageModel(lucdebObjects, queryStr, numExpTerms, out);
                rblm.retrieveAll();
            } catch (QueryNodeException ex) {
                Logger.getLogger(ExpansionCommand.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(ExpansionCommand.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
