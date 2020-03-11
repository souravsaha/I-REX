/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irex.commands;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import irex.IRexObjects;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.lucene.search.IndexSearcher;

/**
 *
 * @author dwaipayan
 */
public class DumpCommand extends Commands {

    String queryTerms[];
    String fieldName;
    int luceneDocid;

    public DumpCommand(IRexObjects lucivObjects) {
        super(lucivObjects, "dump");
    }

    @Override
    public String help() {
        return usage();
    }

    @Override
    public void execute(String[] args, PrintStream out) throws IOException {

        LinkedList<String> fieldNames = new LinkedList<>();
        List queryList = null;

    	Options options = new Options();
        OptionGroup input = new OptionGroup();
        input.addOption(new Option("i", "luceneDocId", true, "Lucene Doc Id"));
        input.addOption(new Option("n", "docName", true, "Document Name"));
        input.setRequired(true);
        options.addOptionGroup(input);

    	options.addOption("f","fieldName", true, "Field name which will be dumped" );
    	options.addOption("q","query", true, "Query terms to highlight in the dump" );    	
    	
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
            luceneDocid = Integer.parseInt(luceneDocIdNum.trim());

        String docNameValue = cmd.getOptionValue("docName");

        if(cmd.hasOption("i")) {
            // Parsing the arguments
            try {
                luceneDocid = Integer.parseInt(luceneDocIdNum.trim());

            } catch(NumberFormatException ex) {
                out.println("error reading docid; expected integers.");
            }
        }
        else if(cmd.hasOption("n")) {
        	try {
        		//out.println(docNameValue);
                luceneDocid = irexObjects.getLuceneDocid(docNameValue.trim());
            } catch (Exception ex) {
                out.println("Error while getting luceneDocid");
            }
            if(luceneDocid < 0) {
                return;
            }
        }
        else {
            // execution should not reach here
            return;
        }

        if(luceneDocid < 0 || luceneDocid > irexObjects.getNumDocs()) {
            out.println(luceneDocid + ": not in the docid range (0 - " + irexObjects.getNumDocs() + ")");
            return;
        }
        
        String fieldNameValue = cmd.getOptionValue("fieldName");
        if(null != fieldNameValue)
            fieldNames.add(fieldNameValue);        
        else
        // add and search in all fields of the index
            fieldNames = (LinkedList<String>) irexObjects.getFieldNames();

        String query = cmd.getOptionValue("query");
        if(null != query) {
            queryTerms = query.split(" ");
            queryList = Arrays.asList(queryTerms);
        }

        IndexSearcher indexSearcher = irexObjects.getIndexSearcher();

        ArrayList<String> list;
        list = new ArrayList<>();
        int terminalWidth = (int)jline.TerminalFactory.get().getWidth();

        for (String field : fieldNames) {
            String content = indexSearcher.doc(luceneDocid).get(field);
            String tokens[] = content.split("\\W+");

            content = "";
            for(String token : tokens) {
                if(queryList != null && queryList.contains(token))
                    content = content + " \033[37;41;1m"+token+"\033[0;0;1m";
                else
                    content = content + " " + token;
            }
            String temp = field+"="+content;
            temp = temp.replaceAll("(.{"+(terminalWidth-5)+"})", "$1\n");
            tokens = temp.split("\n");
            for(String token:tokens)
                list.add(token);
        }
        irexObjects.printPagination(list);
    }

    @Override
    public String usage() {
        return "dv <lucene-docid> [ <field-name> ]";
    }
    
}
