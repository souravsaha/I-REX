/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lucdeb.commands;

import java.io.IOException;
import java.io.PrintStream;
import lucdeb.LucDebObjects;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author dwaipayan
 */
public class TermFrequencyNameCommand extends Commands {

    String fieldName;
    int luceneDocid;
    String docid;
    String term;

    public TermFrequencyNameCommand(LucDebObjects lucivObjects) {
        super(lucivObjects, "tfn");
    }

    @Override
    public String help() {
        return "tfn - Term Frequency of a term in document with docid\n" + usage();
    }

    @Override
    public void execute(String[] args, PrintStream out) throws IOException {

        if (args.length != 2 && args.length != 3) {
            out.println(help());
            return;
        }

        // Parsing the arguments
        term = args[0];
        docid = args[1];
        if (args.length == 3 )
            fieldName = args[2];
        else 
            fieldName = lucdebObjects.getSearchField();

        try {
            luceneDocid = lucdebObjects.getLuceneDocid(docid);
            if(luceneDocid < 0)
                throw (new IllegalArgumentException(""));

            IndexReader indexReader = lucdebObjects.getIndexReader();
            // t vector for this document and field, or null if t vectors were not indexed
            Terms terms = indexReader.getTermVector(luceneDocid, fieldName);
            if(null == terms) {
                out.println(0);
            }

            TermsEnum iterator = terms.iterator();
            BytesRef byteRef = null;

            //* for each word in the document
            long termFreq = -1;
            while((byteRef = iterator.next()) != null) {
                String token = new String(byteRef.bytes, byteRef.offset, byteRef.length);
                if(token.equalsIgnoreCase(this.term)) {
                    termFreq = iterator.totalTermFreq();    // tf of 't'
                    break;
                }
            }
            out.println(termFreq);
        } catch (Exception ex) {
        }
    }

    @Override
    public String usage() {
        return "tfn <term> <docid> [<field-name>]";
    }
    
}
