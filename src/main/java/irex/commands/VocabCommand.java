/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irex.commands;

import common.PerTermStat;
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

        IndexReader indexReader = irexObjects.getIndexReader();
        fieldName = irexObjects.getSearchField();

        BytesRef byteRef = null;

        int leavesCount = indexReader.leaves().size();

        for(int l = 0; l < leavesCount; l++) {
          TermsEnum iterator = indexReader.leaves().get(l).reader().terms(fieldName).iterator();
          while((byteRef = iterator.next()) != null) {
            //* for each word in the collection
            String term = new String(byteRef.bytes, byteRef.offset, byteRef.length);
            out.println(term);
          }
        }

    }

}
