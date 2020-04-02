/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irex.commands;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import irex.IRexObjects;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author Dwaipayan 
 */
public class Postings extends Commands{

    static String cmdName = "pl";

    String fieldName;
    String term;

    public Postings(IRexObjects lucivObjects) {
        super(lucivObjects, cmdName);
    }

    @Override
    public String help() {
        return cmdName + " - prints the posting list of a given term\n" + usage();
    }

    @Override
    public String usage() {
        return cmdName + " <term> [ <field-name> ]";
    }

    @Override
    public void execute(String[] args, PrintStream out) throws IOException {

    	Options options = new Options();
    	Option modelOption = new Option("t", "term", true, "Term to get the posting list");
    	modelOption.setRequired(true);
    	options.addOption(modelOption);

    	options.addOption("f","fieldName", true, "Field name for which posting list will be retrieved" );

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

        // +
        List<PostingValues> postingsLists = new ArrayList<>();

        IndexReader indexReader = irexObjects.getIndexReader();
        List<LeafReaderContext> leaves = indexReader.leaves();
        int docBase = 0;
        PostingsEnum postings = null;

        // for each occurrences
        for (LeafReaderContext leaf : leaves) {
            LeafReader atomicReader = leaf.reader();
            Terms terms = atomicReader.terms(fieldName);
            if (terms == null){
                continue;
            }
            if (terms != null && term != null) {
                TermsEnum termsEnum = terms.iterator();

                if (termsEnum.seekExact(new BytesRef(term))) {
                    postings = termsEnum.postings(postings, PostingsEnum.FREQS);

                    int docid;
                    while((docid = postings.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS){
                        int tf = postings.freq();
//                        out.println("docid: "+(docid+docBase)+", tf: "+tf);
                        postingsLists.add(new PostingValues(docid+docBase, tf));
                    }
                }
            }
            docBase += atomicReader.maxDoc();
        }

        Collections.sort(postingsLists, new sortByTermTF());
//        for (PostingValues pv : postingsLists) {
//            System.out.println(pv.luceneDocid + " " + pv.tf);
//        }

        ArrayList<String> list;
        list = new ArrayList<>();
        for(PostingValues pv : postingsLists) {
            String s = pv.toString();
//            System.out.println(s);
            if(s.equals("315394 1"))
                list.add("\033[37;41;1m"+s+"\033[0;0;1m");
            else
                list.add("\033[0;0;1m"+s);
        }

        irexObjects.printPagination(list);
        // -
    }

}
