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
import org.apache.lucene.index.Term;

/**
 *
 * @author dwaipayan
 */
public class CollectionFrequencyCommand extends Commands {

    static String cmdName = "cf";

    String fieldName;
    String term;

    public CollectionFrequencyCommand(IRexObjects lucivObjects) {
        super(lucivObjects, cmdName);
    }

    @Override
    public String help() {
        return cmdName + " - Collection Frequency\n" + usage();
    }

    @Override
    public void execute(String[] args, PrintStream out) throws IOException {

    	Options options = new Options();
    	Option modelOption = new Option("t", "term", true, "Term to get the collection frequency");
    	modelOption.setRequired(true);
    	options.addOption(modelOption);

    	options.addOption("f","fieldName", true, "Field name in which collection frequency will be computed" );

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
        term = cmd.getOptionValue("term");
        String fieldNameValue = cmd.getOptionValue("fieldName");
        if(null != fieldNameValue)
            fieldName = fieldNameValue;
        else {
            fieldName = irexObjects.getSearchField();
        }

        IndexReader indexReader = irexObjects.getIndexReader();
        Term termInstance = new Term(fieldName, term);
        long termFreq = indexReader.totalTermFreq(termInstance); // CF: Returns the total number of occurrences of term across all documents (the sum of the freq() for each doc that has this term).

        out.println(termFreq);
    }

    @Override
    public String usage() {
        return cmdName + " <term> [ <field-name> ]";
    }
    
}
