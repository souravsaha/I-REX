/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irex.commands;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Map;

import irex.IRexObjects;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.BytesRef;

import common.DocumentVector;
import common.PerTermStat;

/**
 *
 * @author dwaipayan
 */
public class DocVectorCommand extends Commands {

    String fieldName;
    int luceneDocid;

    public DocVectorCommand(IRexObjects lucivObjects) {
        super(lucivObjects, "dv");
    }

    @Override
    public String help() {
        return usage();
    }

    @Override
    public void execute(String[] args, PrintStream out) throws IOException {

    	Options options = new Options();
    	Option modelOption = new Option("i", "luceneDocId", true, "Lucene Doc Id");
    	modelOption.setRequired(false);
    	options.addOption(modelOption);
    	
    	Option paramOption = new Option("n", "docName", true, "Document Name");
    	paramOption.setRequired(false);
    	options.addOption(paramOption);
    	options.addOption("d","discTerms", true, "Discriminative Terms" );
    	options.addOption("f","fieldName", true, "Field Length in which Document length will be computed" );
    	options.addOption("r","retrievalParams", true, "Retrieval Models with params" );
    	    	
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
        String luceneDocIdNum = cmd.getOptionValue("luceneDocId");
        String docNameValue = cmd.getOptionValue("docName");
        String discTerms = cmd.getOptionValue("discTerms");
        String fieldNameValue = cmd.getOptionValue("fieldName");
        String retrievalParams = cmd.getOptionValue("retrievalParams");

        //out.println(discTerms);
        
    	/* will revisit later
        if (args.length != 1 && args.length != 2) {
            out.println(help());
            return;
        }
        */
        
        if(cmd.hasOption("i"))
        {
	        // Parsing the arguments
	        try {
	            luceneDocid = Integer.parseInt(luceneDocIdNum);
	        } catch(NumberFormatException ex) {
	            out.println("error reading docid; expected integers.");
	        }
        }
        else if(cmd.hasOption("n"))
        {
        	try {
        		out.println(docNameValue);
                luceneDocid = lucdebObjects.getLuceneDocid(docNameValue);
            } catch (Exception ex) {
                out.println("Error while getting luceneDocid");
            }
            if(luceneDocid < 0) {
                return;
            }
        }
        else 
        	return;
        
        if (cmd.hasOption("f"))
            fieldName = fieldNameValue;
        else 
            fieldName = lucdebObjects.getSearchField();

        if(luceneDocid < 0 || luceneDocid > lucdebObjects.getNumDocs()) {
            out.println(luceneDocid + ": not in the docid range (0 - " + lucdebObjects.getNumDocs() + ")");
            return;
        }
        
        if(cmd.hasOption("r"))
        {
        	String param1="", param2= "", param3 = "";
            String[] params = retrievalParams.split(" ");
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
            lucdebObjects.setRetreivalParameter(params[0],param1, param2, param3);
        }
        else
        	lucdebObjects.getRetreivalParameter();
        
        IndexReader indexReader = lucdebObjects.getIndexReader();
        // Term vector for this document and field, or null if term vectors were not indexed
        Terms terms = indexReader.getTermVector(luceneDocid, fieldName);
        if(null == terms) {
            out.println("Error: Term vectors not indexed: "+luceneDocid);
            return;
        }

        //out.println("unique-term-count " + terms.size());
        TermsEnum iterator = terms.iterator();
        BytesRef byteRef = null;

        //* for each word in the document
        int docSize = 0;
        if(!cmd.hasOption("d")) {
	        while((byteRef = iterator.next()) != null) {
	            String term = new String(byteRef.bytes, byteRef.offset, byteRef.length);
	            long tf = iterator.totalTermFreq();    // tf of 't'
	            out.println(term+" "+tf);
	//            int docFreq = indexReader.docFreq(new Term(fieldName, term));      // df of 't'
	//            long cf = indexReader.totalTermFreq(new Term(fieldName, term));    // tf of 't'
	//            System.out.println(term+": cf: "+cf + " : df: " + docFreq);
	            docSize += tf;
	        }
	        out.println("doc-size: " + docSize);
        }
        else
        {
        	int discValue = 0;
        	if(discTerms!= null)
        		discValue = Integer.parseInt(discTerms);
        	//System.out.println(discValue);
        	//System.out.println(lucdebObjects.retModelName);
        	//System.out.println(lucdebObjects.retModelParam1);
        	//System.out.println(lucdebObjects.retModelParam2);
        	//System.out.println(lucdebObjects.retModelParam3);
        	
        	IndexSearcher indexSearcher = lucdebObjects.getIndexSearcher();
            SimilarityFunctions simFunc = new SimilarityFunctions(lucdebObjects.retModelName, lucdebObjects.retModelParam1,lucdebObjects.retModelParam2, lucdebObjects.retModelParam3);
            indexSearcher.setSimilarity(simFunc);
            Query luceneQuery;
            try {
                //IndexReader indexReader = lucdebObjects.getIndexReader();

                // for each of the documents
                    DocumentVector dv = new DocumentVector();

                    //luceneDocid = lucdebObjects.getLuceneDocid(docName);
                    //out.println(/*luceneDocid + " " + */docName);

                    if(luceneDocid == -1) {
                        out.println("not found");
                        return;
                    }
                    dv = dv.getDocumentVector(luceneDocid, indexReader);
                    simFunc.setDocVector(dv);
                    DocTermStat dts = new DocTermStat(lucdebObjects.getIndexSearcher().doc(luceneDocid).get(lucdebObjects.idField), luceneDocid);
                    simFunc.setDocTermStat(dts);

                    // for each of the query terms q:
                    //  1. tf(q,d),
                    //  2. ntf(q,d),
                    //  3. df(q), idf(q),
                    //  4. retrieval-score(q, d)
                    String searchField = lucdebObjects.getSearchField();
                    
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
                        //System.out.println(expln.getValue());
                    }
                    System.out.println(dts.doclen + "\t" + dts.avgdl);
               
                    Collections.sort(dts.terms, new sortByTermScore());

                    System.out.println("term: (cf\tidf\ttf\tcol-proba\tscore)");

                    for (int i = 0; i < Math.min(discValue, dts.terms.size()); i++) {
                        TermStats ts = dts.terms.get(i);
                        System.out.printf("%s: (%d\t%.4f\t%.0f\t%f\t%.4f)\n", 
                            ts.term, ts.cf, ts.idf, ts.tf, ts.collectionProbability, ts.score);
                    }
            } 
            catch (Exception ex) {

            }

        }
        
    }

    @Override
    public String usage() {
        return "dv <lucene-docid> [<field-name>]";
    }
    
}
