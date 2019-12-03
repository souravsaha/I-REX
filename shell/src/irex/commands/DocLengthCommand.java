/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irex.commands;

import java.io.IOException;
import java.io.PrintStream;
import irex.IRexObjects;
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
public class DocLengthCommand extends Commands {

    String fieldName;
    String docid;
    int luceneDocid;

    public DocLengthCommand(IRexObjects lucivObjects) {
        super(lucivObjects, "dl");
    }

    @Override
    public String help() {
        return usage();
    }

    @Override
    public void execute(String[] args, PrintStream out) throws IOException {

    	Options options = new Options();
    	Option modelOption = new Option("i", "luceneDocId", true, "Lucene Doc Id");
//        modelOption = Option.builder("i").longOpt("luceneDocId").desc("Lucene Doc Id").hasArg(true).build();
    	modelOption.setRequired(true);
    	options.addOption(modelOption);
    	
    	Option paramOption = new Option("n", "docName", true, "Document Name");
    	paramOption.setRequired(false);
    	options.addOption(paramOption);

    	options.addOption("f", "fieldName", true, "Field name in which collection frequency will be computed" );

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
        if(null != luceneDocIdNum)
            luceneDocid = Integer.parseInt(luceneDocIdNum);
        if(cmd.hasOption("luceneDocId"))
            System.out.println(cmd.getOptionValue("luceneDocId"));
        // TODO: to include this option
        String docNameValue = cmd.getOptionValue("docName");
        String fieldNameValue = cmd.getOptionValue("fieldName");
        if(null != fieldNameValue)
            fieldName = fieldNameValue;
        else {
            fieldName = lucdebObjects.getSearchField();
        }

        IndexReader indexReader = lucdebObjects.getIndexReader();

        // Term vector for this document and field, or null if term vectors were not indexed
        Terms terms = indexReader.getTermVector(luceneDocid, fieldName);
        if(null == terms) {
            out.println("Term vector null: ("+luceneDocid + ":"+ fieldName+")");
            return;
        }

        out.println("unique-term-count " + terms.size());
        TermsEnum iterator = terms.iterator();
        BytesRef byteRef = null;

        //* for each word in the document
        int docSize = 0;
        while((byteRef = iterator.next()) != null) {
            String term = new String(byteRef.bytes, byteRef.offset, byteRef.length);
            long tf = iterator.totalTermFreq();    // tf of 't'
//            System.out.println(term+" "+tf);
//            int docFreq = indexReader.docFreq(new Term(fieldName, term));      // df of 't'
//            long cf = indexReader.totalTermFreq(new Term(fieldName, term));    // tf of 't'
//            System.out.println(term+": cf: "+cf + " : df: " + docFreq);
            docSize += tf;
        }
        out.println("doc-size: " + docSize);
    }

    @Override
    public String usage() {
        return "dl <lucene-docid> [<field-name>]";
    }
    
}
