/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lucdeb.commands;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lucdeb.LucDebObjects;
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

    public Postings(LucDebObjects lucivObjects) {
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
        if (args.length != 1 && args.length != 2) {
            out.println(help());
            return;
        }

        // Parsing the arguments
        term = args[0];
        if (args.length == 2 )
            fieldName = args[1];
        else 
            fieldName = lucdebObjects.getSearchField();

        // +
        List<PostingValues> postingsLists = new ArrayList<>();

        IndexReader indexReader = lucdebObjects.getIndexReader();
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
            list.add(s);
            /*
            if(s.equals("315394 1"))
                list.add("\033[37;41;1m"+s+"\033[0;0;1m");
            else
                list.add("\033[0;0;1m"+s);
        	*/
        }
        out.println(String.join("\n", list));
        //lucdebObjects.printPagination(list);
        // -
    }

}
