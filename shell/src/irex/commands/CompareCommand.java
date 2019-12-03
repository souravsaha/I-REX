/**
 * 
 */
package irex.commands;

import static common.CommonVariables.FIELD_ID;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.util.BytesRef;

import common.DocumentVector;
import irex.IRexObjects;

/**
 * @author souravsaha
 *
 */
public class CompareCommand extends Commands{

	/**
	 * 
	 */
	int luceneDocid1;
	int luceneDocid2;

	public CompareCommand(IRexObjects lucivObjects) {
        super(lucivObjects, "compare");
    }
	
	@Override
    public String help() {
        return usage();
    }
	
	@Override
    public String usage() {
        return "compare <-i1/-n1 > <-i2/-n2 > <-q query terms>";
    }
	
	public int findRank(int luceneDocid, String queryTerms[]) throws IOException
	{
		TopScoreDocCollector collector;
		ScoreDoc[] hits = null;
        TopDocs topDocs = null;
        String query;
        
		IndexSearcher indexSearcher = lucdebObjects.getIndexSearcher();
		
		//System.out.println(lucdebObjects.retModelName+" " +lucdebObjects.retModelParam1+" " +lucdebObjects.retModelParam2+" " +lucdebObjects.retModelParam3);
		
		
		String searchField = lucdebObjects.getSearchField();
        SimilarityFunctions simFunc = new SimilarityFunctions(lucdebObjects.retModelName, lucdebObjects.retModelParam1,lucdebObjects.retModelParam2, lucdebObjects.retModelParam3);
        indexSearcher.setSimilarity(simFunc);

        StringBuilder buf = new StringBuilder();
        for (String queryTerm : queryTerms) {
            buf.append(queryTerm).append(" ");
        }
        query = buf.toString();

        Query luceneQuery;
        try {
            luceneQuery = lucdebObjects.getAnalyzedQuery(query, searchField);
            collector = TopScoreDocCollector.create(1000);
            //System.out.println(luceneQuery.toString(searchField));
            indexSearcher.search(luceneQuery, collector);
            topDocs = collector.topDocs();
            hits = topDocs.scoreDocs;
            int hits_length = hits.length;
            if(hits == null||hits_length==0)
                System.out.println("Nothing found");

            HashMap<String, Integer> rankedList = new HashMap<>();
            for (int i = 0; i < hits_length; ++i) {
                int luceneDocId = hits[i].doc;
                Document d = indexSearcher.doc(luceneDocId);
                rankedList.put(d.get(FIELD_ID), i);
            }

            //for(String docName : docNames) {
            return rankedList.get(lucdebObjects.getIndexSearcher().doc(luceneDocid).get(lucdebObjects.idField));
            //}
        } catch (QueryNodeException ex) {
            Logger.getLogger(RankCommand.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
	}
	
	public int getDocLength(int luceneDocid) throws IOException
	{
        IndexReader indexReader = lucdebObjects.getIndexReader();
        String fieldName = lucdebObjects.getSearchField();
        
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
	
	public long getTermFrequency(int luceneDocid, String term) throws IOException
	{
        String fieldName = lucdebObjects.getSearchField();
        IndexReader indexReader = lucdebObjects.getIndexReader();
        Query luceneQuery = null;
		try {
			luceneQuery = lucdebObjects.getAnalyzedQuery(term, fieldName);
		} catch (QueryNodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        term = luceneQuery.toString(fieldName);
        // t vector for this document and field, or null if t vectors were not indexed
        Terms terms = indexReader.getTermVector(luceneDocid, fieldName);
        if(null == terms) {
        }
        TermsEnum iterator = terms.iterator();
        BytesRef byteRef = null;

        //* for each word in the document
        long termFreq = -1;
        while((byteRef = iterator.next()) != null) {
            String token = new String(byteRef.bytes, byteRef.offset, byteRef.length);
            if(token.equalsIgnoreCase(term)) {
                termFreq = iterator.totalTermFreq();    // tf of 't'
                break;
            }
        }
        return termFreq;
	}
	
	public float score(int luceneDocid, String queryTermsValue)
	{
		String queryTerms[];
	    float totalScore=0.0f;
		queryTerms = queryTermsValue.split(" ");
		String searchField = lucdebObjects.getSearchField();
        IndexSearcher indexSearcher = lucdebObjects.getIndexSearcher();
        //System.out.println("Retrieval model set to " + lucdebObjects.retModelName);

        SimilarityFunctions simFunc = new SimilarityFunctions(lucdebObjects.retModelName, lucdebObjects.retModelParam1,lucdebObjects.retModelParam2, lucdebObjects.retModelParam3);
        indexSearcher.setSimilarity(simFunc);

        StringBuilder buf = new StringBuilder();
        for (String queryTerm : queryTerms) {
            buf.append(queryTerm).append(" ");
        }
        String query = buf.toString();

        Query luceneQuery;
        try {
            IndexReader indexReader = lucdebObjects.getIndexReader();

            
                DocumentVector dv = new DocumentVector();

                //luceneDocid = lucdebObjects.getLuceneDocid(docid);
                //System.out.println(/*luceneDocid + " " + */luceneDocid);
                dv = dv.getDocumentVector(luceneDocid, indexReader);
                simFunc.setDocVector(dv);
                DocTermStat dts = new DocTermStat(lucdebObjects.getIndexSearcher().doc(luceneDocid).get(lucdebObjects.idField), luceneDocid);
                simFunc.setDocTermStat(dts);

                // reset totalScore variable to Zero
     
                for(String queryTerm : queryTerms) {
                    TermStats ts = new TermStats();
                    simFunc.setTermStats(ts);
                    luceneQuery = lucdebObjects.getAnalyzedQuery(queryTerm, searchField);
                    ts.term = luceneQuery.toString(searchField);
                    //System.out.printPagination(queryTerm + "\t");

                    Explanation expln = indexSearcher.explain(luceneQuery, luceneDocid);
                    //out.println(expln.toString());
                    if(!expln.isMatch());
                        //System.out.println("TODO: (0\t0\t0\t0)");
                    //System.out.println(expln.getValue());
                    totalScore += expln.getValue();
                    dts.docScore += expln.getValue();
                }
                //System.out.println(dts.doclen + "\t" + dts.avgdl);

                //System.out.println("term: (cf\tidf\ttf\tcol-proba\tscore)");
                for(TermStats ts : dts.terms) {
                    //System.out.printf("%s: (%d\t%.4f\t%.0f\t%.4f\t%.4f)\n", 
                    //    ts.term, ts.cf, ts.idf, ts.tf, ts.collectionProbability, ts.score);
                }

                //System.out.println("Total-score("+luceneDocid + "): " + totalScore);
        } 
        catch (Exception ex) {

        }
        return totalScore;
	}
	
	@Override
    public void execute(String[] args, PrintStream out) throws IOException {
		
		Options options = new Options();
    	Option queryOption = new Option("q", "queryTerms", true, "Query Terms");
    	queryOption.setRequired(true);
    	options.addOption(queryOption);

    	Option luceneDocIDOption1 = new Option("i1", "luceneDocId1", true, "Lucene Doc Id 1");
    	luceneDocIDOption1.setRequired(false);
    	options.addOption(luceneDocIDOption1);
    	
    	Option luceneDocIDOption2 = new Option("i2", "luceneDocId2", true, "Lucene Doc Id 2");
    	luceneDocIDOption2.setRequired(false);
    	options.addOption(luceneDocIDOption2);
    	
    	Option luceneDocNameOption1 = new Option("n1", "docName1", true, "Document Name 1");
    	luceneDocNameOption1.setRequired(false);
    	options.addOption(luceneDocNameOption1);
    	
    	Option luceneDocNameOption2 = new Option("n2", "docName2", true, "Document Name 2");
    	luceneDocNameOption2.setRequired(false);
    	options.addOption(luceneDocNameOption2);
    	
    	Option retModelOption = new Option("r", "retrievalParams", true, "Retrieval Models with params");
    	retModelOption.setRequired(false);
    	options.addOption(retModelOption);
    	
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
        
        String queryTermsValue = cmd.getOptionValue("queryTerms");
        String luceneDocIdNum1 = cmd.getOptionValue("luceneDocId1");
        String luceneDocIdNum2 = cmd.getOptionValue("luceneDocId2");
        String docNameValue1 = cmd.getOptionValue("docName1");
        String docNameValue2 = cmd.getOptionValue("docName2");
        String retrievalParams = cmd.getOptionValue("retrievalParams");

        if(cmd.hasOption("i1"))
        {
	        // Parsing the arguments
	        try {
	            luceneDocid1 = Integer.parseInt(luceneDocIdNum1);
	        } catch(NumberFormatException ex) {
	            out.println("error reading docid; expected integers.");
	        }
        }
        else if(cmd.hasOption("n1"))
        {
        	try {
        		//out.println(docNameValue1);
                luceneDocid1 = lucdebObjects.getLuceneDocid(docNameValue1);
            } catch (Exception ex) {
                out.println("Error while getting luceneDocid");
            }
            if(luceneDocid1 < 0) {
                return;
            }
        }
        else 
        	return;

        
        if(cmd.hasOption("i2"))
        {
	        // Parsing the arguments
	        try {
	            luceneDocid2 = Integer.parseInt(luceneDocIdNum2);
	        } catch(NumberFormatException ex) {
	            out.println("error reading docid; expected integers.");
	        }
        }
        else if(cmd.hasOption("n2"))
        {
        	try {
        		//out.println(docNameValue2);
                luceneDocid2 = lucdebObjects.getLuceneDocid(docNameValue2);
            } catch (Exception ex) {
                out.println("Error while getting luceneDocid2");
            }
            if(luceneDocid2 < 0) {
                return;
            }
        }
        else 
        	return;
        
        if(luceneDocid1 < 0 || luceneDocid1 > lucdebObjects.getNumDocs()) {
            out.println(luceneDocid1 + ": not in the docid range (0 - " + lucdebObjects.getNumDocs() + ")");
            return;
        }
        
        if(luceneDocid2 < 0 || luceneDocid2 > lucdebObjects.getNumDocs()) {
            out.println(luceneDocid2 + ": not in the docid range (0 - " + lucdebObjects.getNumDocs() + ")");
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
            //System.out.println(params[0]+" "+param1+ " "+ param2+ " " +param3);
            lucdebObjects.setRetreivalParameter(params[0],param1, param2, param3);
        }
        else
        	lucdebObjects.getRetreivalParameter();
    	
        String queryTerms[];
        queryTerms = queryTermsValue.split(" ");
        
        System.out.println("\t\t"+lucdebObjects.getIndexSearcher().doc(luceneDocid1).get(lucdebObjects.idField)+"\t"+lucdebObjects.getIndexSearcher().doc(luceneDocid2).get(lucdebObjects.idField));
        int rank1 = findRank(luceneDocid1, queryTerms);
        int rank2 = findRank(luceneDocid2, queryTerms);
        
        System.out.println("rank : \t\t"+ rank1 + "\t\t" + rank2);
        
        float score1 = score(luceneDocid1, queryTermsValue);
        float score2 = score(luceneDocid2, queryTermsValue);
        
        System.out.println("score : \t"+ score1 + "\t\t" + score2);
        
        int docLen1 = getDocLength(luceneDocid1);
        int docLen2 = getDocLength(luceneDocid2);
        
        System.out.println("docLen : \t"+ docLen1 + "\t\t" + docLen2);
        
        for(String queryTerm : queryTerms) {
        	System.out.println("tf("+queryTerm+")" + "\t\t"+getTermFrequency(luceneDocid1, queryTerm) +"\t" + getTermFrequency(luceneDocid2,queryTerm)) ;
        }
        
        for(String queryTerm : queryTerms) {
        	System.out.println("score("+queryTerm+")" +"\t\t"+ score(luceneDocid1, queryTerm) +"\t" + score(luceneDocid2,queryTerm)) ;
        }  
	}
}
