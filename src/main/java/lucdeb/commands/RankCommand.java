/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lucdeb.commands;

import static common.CommonVariables.FIELD_ID;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import lucdeb.LucDebObjects;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;

/**
 *
 * @author Dwaipayan
 */
public class RankCommand extends Commands {

	static String cmdName = "rank";

	String fieldName;
	String queryTerms[];
	String docNames[];
	String[] retrievalModelsWithParam;
	String docid;
	int luceneDocid;

	public RankCommand(LucDebObjects lucivObjects) {
		super(lucivObjects, cmdName);
	}

	@Override
	public String help() {
		return usage();
	}

	@Override
	public String usage() {
		return cmdName + " <query> <set-of-docNames (in quotes)> <set-of-retrieval-models-with-parameters>\n"
				+ "retrieval models: " + lucdebObjects.retFuncMap.toString() + "\n"
				+ "the parameters of each retrieval models in quote separately";
		// rank "What is a Bengals cat" "WTX095-B05-124 WTX095-B05-119" "lmjm 0.2" "bm25
		// 0.2 0.75"

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

		modelOption = new Option("q", "queryTerms", true, "Query Terms");
		modelOption.setRequired(true);
		options.addOption(modelOption);
		options.addOption("r", "retrievalParams", true, "Retrieval Models with params");
		options.addOption("f", "fieldName", true, "Field name in which collection frequency will be computed");

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;

		TopScoreDocCollector collector;
		String searchField = lucdebObjects.getSearchField();

		ScoreDoc[] hits = null;
		TopDocs topDocs = null;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("utility-name", options);
			return;
		}
		String luceneDocIdNum = cmd.getOptionValue("luceneDocId");
		if (null != luceneDocIdNum)
			luceneDocid = Integer.parseInt(luceneDocIdNum.trim());
		//if (cmd.hasOption("luceneDocId"))
		//	System.out.println(cmd.getOptionValue("luceneDocId"));
		// TODO: to include this option
		String docNameValue = cmd.getOptionValue("docName");
		if (null != docNameValue)
			try {
				luceneDocid = lucdebObjects.getLuceneDocid(docNameValue.trim());
			} catch (Exception ex) {
				Logger.getLogger(RankCommand.class.getName()).log(Level.SEVERE, null, ex);
			}
		String fieldNameValue = cmd.getOptionValue("fieldName");
		if (null != fieldNameValue)
			fieldName = fieldNameValue.trim();
		else {
			fieldName = lucdebObjects.getSearchField();
		}
		String query = cmd.getOptionValue("queryTerms").trim();
		IndexSearcher indexSearcher = lucdebObjects.getIndexSearcher();
		if (cmd.hasOption("r")) {
			String param1 = "", param2 = "", param3 = "";
		
			String retModel = cmd.getOptionValue("retrievalParams");

			//System.out.println(retModel);
			String[] params = retModel.split("\\s+");
			if (!lucdebObjects.isKnownRetFunc(params[0])) {
				out.println("Unknown retrieval model: " + retModel + "\n" + "Available retrieval models: "
						+ lucdebObjects.retFuncMap.keySet());
				return;
			}
			switch (params[0]) {
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

			System.out.println("Retrieval model set to " + retModel);
			SimilarityFunctions simFunc = new SimilarityFunctions(params[0], param1, param2, param3);
			indexSearcher.setSimilarity(simFunc);
		}
		else 
			lucdebObjects.getRetreivalParameter();
		
		StringBuilder buf = new StringBuilder();
		for (String queryTerm : query.split("\\s+")) {
			buf.append(queryTerm).append(" ");
		}
		query = buf.toString();

		Query luceneQuery;
		try {
			luceneQuery = lucdebObjects.getAnalyzedQuery(query, searchField);
			collector = TopScoreDocCollector.create(1000);
			System.out.println("Query Term(s) : " + luceneQuery.toString(searchField));
			indexSearcher.search(luceneQuery, collector);
			topDocs = collector.topDocs();
			hits = topDocs.scoreDocs;
			int hits_length = hits.length;
			if (hits == null || hits_length == 0)
				System.out.println("Nothing found");
			//System.out.println(hits_length);
			HashMap<Integer, Integer> rankedList = new HashMap<>();
			for (int i = 0; i < hits_length; ++i) {
				int luceneDocId = hits[i].doc;
				//Document d = indexSearcher.doc(luceneDocId);
				rankedList.put(luceneDocId, i);
			}
			String finalRank = "Not Found";
			if(rankedList.containsKey(luceneDocid))
				finalRank = ">1000";
			else 
				finalRank = String.valueOf(rankedList.get(luceneDocid));
			
			System.out.println("Doc ID : " + luceneDocid + "\t Rank : " + finalRank);

		} catch (QueryNodeException ex) {
			Logger.getLogger(RankCommand.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
