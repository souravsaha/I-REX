/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irex.commands;

import static common.DocField.FIELD_ID;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import irex.IRexObjects;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author dwaipayan
 */
public class SearchCommand extends Commands {

    public SearchCommand(IRexObjects lucivObjects) {
        super(lucivObjects, "search");
    }

    @Override
    public String help() {
        return "search - search a collection with a query and retrieval model\n" + usage();
    }

    @Override
    public String usage() {
        return "search - <query-terms> [<number of expansion terms> (defaulut 10)]";
    }

    @Override
    public void execute(String[] args, PrintStream out) throws IOException {

        // +
	Options options = new Options();
    	Option queryOption = new Option("q", "queryTerms", true, "Query Terms");
    	queryOption.setRequired(true);
    	options.addOption(queryOption);
    	
    	Option retModelOption = new Option("r", "retrievalParams", true, "Retrieval Models with params");
    	retModelOption.setRequired(false);
    	options.addOption(retModelOption);
    	
    	Option rankOption = new Option("k", "rank", false, "Rank of the retrieved documents");
    	rankOption.setRequired(false);
    	options.addOption(rankOption);

    	Option scoreOption = new Option("s", "score", false, "Score of the retrieved documents");
    	scoreOption.setRequired(false);
    	options.addOption(scoreOption);
    	
    	Option doclenOption = new Option("dl", "doclen", false, "Length of the retrieved documents");
    	doclenOption.setRequired(false);
    	options.addOption(doclenOption);
    	
    	CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);
            return;
        }
        
        String queryTerms = cmd.getOptionValue("queryTerms");
        String retrievalParams = cmd.getOptionValue("retrievalParams");        // -
        boolean rankFlag  = cmd.hasOption("rank");
        boolean scoreFlag = cmd.hasOption("score");
        boolean doclenFlag = cmd.hasOption("doclen");

        String searchField;
        ScoreDoc[] hits;
        TopDocs topDocs;
        int numDocs = 10;

        String param1="", param2= "", param3 = "";
        searchField = irexObjects.getSearchField();
        IndexSearcher indexSearcher = irexObjects.getIndexSearcher();
        if(null != retrievalParams) {
            String retModel = cmd.getOptionValue("retrievalParams");

            System.out.println(retModel);
            String[] params = retModel.split("\\s+");
            if(!irexObjects.isKnownRetFunc(params[0])) {
                out.println("Unknown retrieval model: " + retModel +"\n"
                        + "Available retrieval models: " + irexObjects.retFuncMap.keySet());
                return;
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
            SimilarityFunctions simFunc = new SimilarityFunctions(params[0], param1, param2, param3);
            indexSearcher.setSimilarity(simFunc);
        }
        indexSearcher.setSimilarity(new BM25Similarity());
        TopScoreDocCollector collector = TopScoreDocCollector.create(numDocs, numDocs);
        Query luceneQuery;

        try {
            luceneQuery = irexObjects.getAnalyzedQuery(queryTerms, searchField);
        } catch (QueryNodeException ex) {
            out.println("Error analysing the query. Returning...");
            return;
        }

        indexSearcher.search(luceneQuery, collector);
        topDocs = collector.topDocs();
        hits = topDocs.scoreDocs;
        int hits_length = hits.length;
        if(hits == null||hits_length==0) {
            System.out.println("Nothing found");
            return;
        }

        ArrayList<String> searchOutput = new ArrayList<>();
        for (int i = 0; i < hits_length; ++i) {
            int luceneDocId = hits[i].doc;
            Document d = indexSearcher.doc(luceneDocId);
            String singleDocInfo = d.get(FIELD_ID) + "\t" + i;
            searchOutput.add(singleDocInfo);
        }

        if(scoreFlag) {
            for (int i = 0; i < hits_length; ++i) {
                searchOutput.set(i, searchOutput.get(i)+"\t"+hits[i].score);
            }
        }
        if(doclenFlag) {
            for (int i = 0; i < hits_length; ++i) {
                searchOutput.set(i, searchOutput.get(i)+"\t"+getDocLength(hits[i].doc));
            }
        }
        for (String temp : searchOutput) {
	    System.out.println(temp);
        }

    }
    // REPEATED function: already implemented elsewhere
    public int getDocLength(int luceneDocid) throws IOException
    {
        IndexReader indexReader = irexObjects.getIndexReader();
        String fieldName = irexObjects.getSearchField();
        
        // Term vector for this document and field, or null if term vectors were not indexed
        Terms terms = indexReader.getTermVector(luceneDocid, fieldName);
        if(null == terms) {
            System.out.println("Term vector null: ("+luceneDocid + ":"+ fieldName+")");
            return -1;
        }
        TermsEnum iterator = terms.iterator();
        BytesRef byteRef = null;

        //* for each word in the document
        int docSize = 0;
        while((byteRef = iterator.next()) != null) {
            String term = new String(byteRef.bytes, byteRef.offset, byteRef.length);
            long tf = iterator.totalTermFreq();    // tf of 't'
            docSize += tf;
        }
        return docSize;

    }

    
}
