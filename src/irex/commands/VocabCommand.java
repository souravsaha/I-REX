/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irex.commands;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import irex.IRexObjects;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author dwaipayan
 */
public class VocabCommand extends Commands {

    static String cmdName = "vocab";

    String fieldName;

    public VocabCommand(IRexObjects lucivObjects) {
        super(lucivObjects, cmdName);
    }

    @Override
    public String help() {
        return cmdName + " - print the vocabulary and df of each term";
    }

    @Override
    public String usage() {
        return cmdName + "";
    }

    @Override
    public void execute(String[] args, PrintStream out) throws IOException {

        IndexReader indexReader = lucdebObjects.getIndexReader();
        fieldName = lucdebObjects.getSearchField();
        Fields fields = MultiFields.getFields(lucdebObjects.getIndexReader());

        Terms terms = fields.terms(fieldName);

        out.println("Number of tokens: " + lucdebObjects.getTermCounts(fieldName).get(0));

//        out.println("Press anykey to start printing the tokens. (press n to stop)");
//        char ch = (char) System.in.read();

//        if(ch!='n'||ch!='N') 
        {

            ArrayList<String> list = new ArrayList<>();
/*
            TermsEnum termsEnum = terms.iterator();
            while (termsEnum.next() != null) {
                String term = termsEnum.term().utf8ToString();
                Term termInstance = new Term(fieldName, term);

//                list.add(termsEnum.term().utf8ToString() + "\t" + ( indexReader.docFreq(termInstance)) + "\t" + (indexReader.totalTermFreq(termInstance)));
                out.println(termsEnum.term().utf8ToString() + "\t" 
                        + ( indexReader.docFreq(termInstance)) + "\t" + (indexReader.totalTermFreq(termInstance)));
            }
*/
            TermsEnum iterator = terms.iterator();
            BytesRef byteRef = null;
            while((byteRef = iterator.next()) != null) {
                String term = new String(byteRef.bytes, byteRef.offset, byteRef.length);
                out.println(term);
            }
        }
    }

}
