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
import java.util.HashMap;
import java.lang.Float;
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

import common.PerTermStat;
import common.DocumentDivergence;
/**
 *
 * @author sourav
 */
public class DocSimilarCommand extends Commands {

	String fieldName;
	int luceneDocid1;
	int luceneDocid2;

	public DocSimilarCommand(IRexObjects lucivObjects) {
		super(lucivObjects, "docsimilar");
	}

	@Override
	public String help() {
		return usage();
	}

	@Override
	public void execute(String[] args, PrintStream out) throws IOException {

		Options options = new Options();
		Option modelOption = new Option("i1", "luceneDocId1", true, "Lucene Doc Id");
		modelOption.setRequired(false);
		options.addOption(modelOption);

		options.addOption("i2", "luceneDocId2", true, "Lucene Doc Id 2");

		options.addOption("n1", "docName1", true, "Document Name 1");

		options.addOption("n2", "docName2", true, "Document Name 2");

		options.addOption("r", "retrievalParams", true, "Retrieval Models with params");

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
		String luceneDocIdNum1 = cmd.getOptionValue("luceneDocId1");
		String luceneDocIdNum2 = cmd.getOptionValue("luceneDocId2");
		String docNameValue1 = cmd.getOptionValue("docName1");
		String docNameValue2 = cmd.getOptionValue("docName2");
		String retrievalParams = cmd.getOptionValue("retrievalParams");

		if (cmd.hasOption("i1")) {
			// Parsing the arguments
			try {
				luceneDocid1 = Integer.parseInt(luceneDocIdNum1);
			} catch (NumberFormatException ex) {
				out.println("error reading docid; expected integers.");
			}
		} else if (cmd.hasOption("n1")) {
			try {
				// out.println(docNameValue1);
				luceneDocid1 = irexObjects.getLuceneDocid(docNameValue1);
			} catch (Exception ex) {
				out.println("Error while getting luceneDocid");
			}
			if (luceneDocid1 < 0) {
				return;
			}
		} else
			return;

		if (cmd.hasOption("i2")) {
			// Parsing the arguments
			try {
				luceneDocid2 = Integer.parseInt(luceneDocIdNum2);
			} catch (NumberFormatException ex) {
				out.println("error reading docid; expected integers.");
			}
		} else if (cmd.hasOption("n2")) {
			try {
				// out.println(docNameValue2);
				luceneDocid2 = irexObjects.getLuceneDocid(docNameValue2);
			} catch (Exception ex) {
				out.println("Error while getting luceneDocid2");
			}
			if (luceneDocid2 < 0) {
				return;
			}
		} else
			return;

		if (luceneDocid1 < 0 || luceneDocid1 > irexObjects.getNumDocs()) {
			out.println(luceneDocid1 + ": not in the docid range (0 - " + irexObjects.getNumDocs() + ")");
			return;
		}

		if (luceneDocid2 < 0 || luceneDocid2 > irexObjects.getNumDocs()) {
			out.println(luceneDocid2 + ": not in the docid range (0 - " + irexObjects.getNumDocs() + ")");
			return;
		}

		if (cmd.hasOption("r")) {
			String param1 = "", param2 = "", param3 = "";
			String[] params = retrievalParams.split(" ");
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
			irexObjects.setRetreivalParameter(params[0], param1, param2, param3);
		} else
			irexObjects.getRetreivalParameter();

		HashMap<String, Float> docVector1 = new HashMap();
		HashMap<String, Float> docVector2 = new HashMap();

		IndexReader indexReader = irexObjects.getIndexReader();
		fieldName = irexObjects.getSearchField();
		// Term vector for this document and field, or null if term vectors were not
		// indexed
		Terms terms = indexReader.getTermVector(luceneDocid1, fieldName);
		if (null == terms) {
			out.println("Error: Term vectors not indexed: " + luceneDocid1);
			return;
		}
		// out.println("unique-term-count " + terms.size());
		TermsEnum iterator = terms.iterator();
		BytesRef byteRef = null;

		// * for each word in the document
		int docSize = 0;

		while ((byteRef = iterator.next()) != null) {
			String term = new String(byteRef.bytes, byteRef.offset, byteRef.length);
			float tf = iterator.totalTermFreq(); // tf of 't'
			// out.println(term + " " + tf);
			docVector1.put(term, tf);
			docSize += tf;
		}
		out.println("doc-size 1 : " + docSize);

		Terms terms2 = indexReader.getTermVector(luceneDocid2, fieldName);
		if (null == terms2) {
			out.println("Error: Term vectors not indexed: " + luceneDocid2);
			return;
		}
		// out.println("unique-term-count " + terms.size());
		TermsEnum iterator2 = terms2.iterator();

		// * for each word in the document
		int docSize2 = 0;

		while ((byteRef = iterator2.next()) != null) {
			String term = new String(byteRef.bytes, byteRef.offset, byteRef.length);
			float tf = iterator2.totalTermFreq(); // tf of 't'
			//float prob = tf/7525;
			// out.println(term + " " + tf);
			docVector2.put(term, tf);
			docSize2 += tf;
		}
		out.println("doc-size 2 : " + docSize2);
		// TODO Divide by docsize
		
		// test
		/*
		HashMap<String, Float> d1 = new HashMap();
		HashMap<String, Float> d2 = new HashMap();
		d1.put("honda", 0.1f);
		d1.put("city", 0.5f);
		d1.put("car", 0.4f);
		
		d2.put("honda", 0.1f);
		d2.put("city", 0.9f);
		//d2.put("car", 0.4f);
		*/
		
		for (Map.Entry<String, Float> entrySet : docVector1.entrySet()) {
			String w = entrySet.getKey();
			float prob = entrySet.getValue();
			
			docVector1.put(w, prob/docSize);
		}
		
		for (Map.Entry<String, Float> entrySet : docVector2.entrySet()) {
			String w = entrySet.getKey();
			float prob = entrySet.getValue();
			//System.out.println(w +" " + prob/docSize2);
			docVector2.put(w, prob/docSize2);
		}
		
		
		DocumentDivergence docDiv = new DocumentDivergence();
		out.println("JS divergence of 2 docs: " + docDiv.jsDiv(docVector1, docVector2));
	}

	@Override
	public String usage() {
		return "docsimilar <i1/n1> Doc1 <i2/n2> Doc2";
	}

}
