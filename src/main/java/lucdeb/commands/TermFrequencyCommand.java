/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lucdeb.commands;

import java.io.IOException;
import java.io.PrintStream;
import lucdeb.LucDebObjects;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author dwaipayan
 */
public class TermFrequencyCommand extends Commands {

    String fieldName;
    int luceneDocid;
    String term;

    public TermFrequencyCommand(LucDebObjects lucivObjects) {
        super(lucivObjects, "tf");
    }

    @Override
    public String help() {
        return "tf - Term Frequency\n" + usage();
    }

    @Override
    public void execute(String[] args, PrintStream out) throws IOException {

    	Options options = new Options();
    	Option modelOption = new Option("i", "luceneDocId", true, "Lucene Doc Id");
//        modelOption = Option.builder("i").longOpt("luceneDocId").desc("Lucene Doc Id").hasArg(true).build();
    	modelOption.setRequired(false);
    	options.addOption(modelOption);
    	
    	Option paramOption = new Option("n", "docName", true, "Document Name");
    	paramOption.setRequired(false);
    	options.addOption(paramOption);

    	options.addOption("f", "fieldName", true, "Field name in which collection frequency will be computed" );

    	Option queryOption = new Option("q", "queryTerms", true, "Query Terms");
    	queryOption.setRequired(true);
    	options.addOption(queryOption);
    	
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
        String luceneDocIdNum = cmd.getOptionValue("luceneDocId");
        String docNameValue = cmd.getOptionValue("docName");
        
        if(cmd.hasOption("i"))
        {
	        // Parsing the arguments
	        try {
	            luceneDocid = Integer.parseInt(luceneDocIdNum.trim());
	            
	        } catch(NumberFormatException ex) {
	            out.println("error reading docid; expected integers.");
	        }
        }
        else if(cmd.hasOption("n"))
        {
        	try {
        		//out.println(docNameValue);
                luceneDocid = lucdebObjects.getLuceneDocid(docNameValue.trim());
            } catch (Exception ex) {
                out.println("Error while getting luceneDocid");
            }
            if(luceneDocid < 0) {
                return;
            }
        }
        else 
        	return;

        if(luceneDocid < 0 || luceneDocid > lucdebObjects.getNumDocs()) {
            out.println(luceneDocid + ": not in the docid range (0 - " + lucdebObjects.getNumDocs() + ")");
            return;
        }
        
        String fieldNameValue = cmd.getOptionValue("fieldName");
        if(null != fieldNameValue)
            fieldName = fieldNameValue;
        else {
            fieldName = lucdebObjects.getSearchField();
        }

        IndexReader indexReader = lucdebObjects.getIndexReader();
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
            if(token.equalsIgnoreCase(queryTermsValue)) {
                termFreq = iterator.totalTermFreq();    // tf of 't'
                break;
            }
        }
        out.println("Term Frequency of " + queryTermsValue +  " = " + termFreq);
        //System.out.println("DocSize: "+docSize);
    }

    @Override
    public String usage() {
        return "tf <term> <lucene-docid> [<field-name>]";
    }
    
}
