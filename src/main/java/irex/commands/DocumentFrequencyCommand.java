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
public class DocumentFrequencyCommand extends Commands {

    static String cmdName = "df";

    String fieldName;
    String term;

    public DocumentFrequencyCommand(IRexObjects lucivObjects) {
        super(lucivObjects, cmdName);
    }

    @Override
    public String help() {
        return cmdName + " - Document Frequency\n" + usage();
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
        long docCount = indexReader.docFreq(termInstance);       // DF: Returns the number of documents containing the term

        out.println(docCount);
    }

    /*
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
            fieldName = irexObjects.getSearchField();

        IndexReader indexReader = irexObjects.getIndexReader();
        List<LeafReaderContext> leaves = indexReader.leaves();
        TreeMap<BytesRef,TermsEnum> termMap = null;
        HashMap<BytesRef,AtomicInteger> termCountMap = new HashMap<>();

        int numCount = 0;
        int numPerPage = 20;

        for (LeafReaderContext leaf : leaves){
          LeafReader atomicReader = leaf.reader();

          Terms terms = atomicReader.fields().terms(fieldName);

          if (terms == null) {
            continue;
          }

          if (termMap == null){
            termMap = new TreeMap<>();
          }


          TermsEnum te = terms.iterator();
          BytesRef termBytes;
          if (term != null){
              if (!te.seekExact(new BytesRef(term))){
                continue;
            }
            termBytes = te.term();
          }
          else{
            termBytes = te.next();
          }

          while(true){
            if (termBytes == null) break;        
            AtomicInteger count = termCountMap.get(termBytes);
            if (count == null){
              termCountMap.put(termBytes, new AtomicInteger(te.docFreq()));
              termMap.put(termBytes, te);
              break;
            }
            count.getAndAdd(te.docFreq());
            termBytes = null; 
          }
        }

        while(termMap != null && !termMap.isEmpty()){
          numCount++;
          Map.Entry<BytesRef,TermsEnum> entry = termMap.pollFirstEntry();
          if (entry == null) break;
          BytesRef key = entry.getKey();
          AtomicInteger count = termCountMap.remove(key);
//          out.println(bytesRefPrinter.print(key)+" ("+count+") ");
          out.println(key+" ("+count+") " + term);

          TermsEnum te = entry.getValue();
          BytesRef nextKey = null;

          while(true){
            if (nextKey == null) break;
            count = termCountMap.get(nextKey);
            if (count == null){
              termCountMap.put(nextKey, new AtomicInteger(te.docFreq()));
              termMap.put(nextKey, te);
              break;
            }
            count.getAndAdd(te.docFreq());
            nextKey = te.next();
          }
        }
        out.flush();

    }
    */

    public void allDF() throws IOException {

        IndexReader indexReader = irexObjects.getIndexReader();
        fieldName = irexObjects.getSearchField();

        int leavesCount = indexReader.leaves().size();

        BytesRef byteRef = null;

        for(int l = 0; l < leavesCount; l++) {
          System.out.println("l: " + l);
          TermsEnum iterator = indexReader.leaves().get(l).reader().terms(fieldName).iterator();
          while((byteRef = iterator.next()) != null) {
            String t = new String(byteRef.bytes, byteRef.offset, byteRef.length);
            int df = iterator.docFreq();           // df of 't'
            Term termInstance = new Term(fieldName, term);
            System.out.println(t + " " + ( indexReader.docFreq(termInstance)));
          }
        }
    }

    @Override
    public String usage() {
        return cmdName + " <term> [<field-name>]";
    }
    
}
