/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irex.commands;

import java.io.IOException;
import java.io.PrintStream;
import irex.IRexObjects;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author dwaipayan
 */
public class DocLengthNameCommand extends Commands {

    String fieldName;
    String docid;
    int luceneDocid;

    public DocLengthNameCommand(IRexObjects lucivObjects) {
        super(lucivObjects, "dln");
    }

    @Override
    public String help() {
        return usage();
    }

    @Override
    public void execute(String[] args, PrintStream out) throws IOException {

         if (args.length != 1 && args.length != 2) {
            out.println(help());
            return;
        }

        // Parsing the arguments
        docid = args[0];
        if (args.length == 2 )
            fieldName = args[1];
        else 
            fieldName = irexObjects.getSearchField();

        try {
            luceneDocid = irexObjects.getLuceneDocid(docid);
        } catch (Exception ex) {
            out.println("Error while getting luceneDocid");
        }
        if(luceneDocid < 0) {
            return;
        }

        IndexReader indexReader = irexObjects.getIndexReader();
        // Term vector for this document and field, or null if term vectors were not indexed
        Terms terms = indexReader.getTermVector(luceneDocid, fieldName);
        if(null == terms) {
            System.err.println("Error: Term vector null: "+luceneDocid);
            System.exit(1);
        }

        System.out.println("unique-term-count " + terms.size());
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
        System.out.println("doc-size: " + docSize);
    }

    @Override
    public String usage() {
        return "dln <docid> [<field-name>] ";
    }
    
}
