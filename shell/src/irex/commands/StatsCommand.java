/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irex.commands;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import irex.IRexObjects;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.Terms;

/**
 *
 * @author dwaipayan
 */
public class StatsCommand extends Commands {

    HashMap<String,Boolean> isSetStats;

    /**
     *
     * @param lucivObjects
     */

    public StatsCommand(IRexObjects lucivObjects) {
        super(lucivObjects, "stats");
        isSetStats = new HashMap<>();
    }

    /**
     *
     * @param args
     * @param out
     * @throws IOException
     */
    @Override
    public void execute(String[] args, PrintStream out) throws IOException{

        out.println("Number of docs: " + lucdebObjects.numDocs);
        out.println("Number of fields: " + lucdebObjects.numFields);

        int fcount = 0;
        for (Object[] finfo : lucdebObjects.fields.values()) {
            FieldInfo fieldInfo = (FieldInfo) finfo[0];
            String fieldName = fieldInfo.name;
            out.println("=== Field "+ ++fcount + ": "+fieldInfo.name+" ===");
            out.println("Field name:\t\t" + fieldInfo.name);
            out.println("Term vectors stored:\t" + fieldInfo.hasVectors());

            if(null == (isSetStats.get(fieldName))) // if not already computed
                setStats(finfo, out);
            //else
            //    System.out.println("already computed");

            List<Long> counts = lucdebObjects.getTermCounts(fieldName);
            out.println("Unique term count:\t" + counts.get(0));
            out.println("Total term count:\t" + counts.get(1) + "\n");
        }
    }

    /**
     *
     * @param info
     * @param out
     * @throws IOException
     */
    public void setStats(Object[] info, PrintStream out) throws IOException {

        FieldInfo fieldInfo = (FieldInfo) info[0];
        String fieldName = fieldInfo.name;

        List<Terms> termList = (List<Terms>) info[1];

        if (termList != null) {
            long numTerms = 0L;
            long sumTotalTermFreq = 0L;

            try{
                for (Terms t : termList) {
                    if (t != null) {
                        numTerms += t.size();
                        sumTotalTermFreq += t.getSumTotalTermFreq();
                    }
                }
            }
            catch(IOException ex) {
                System.err.println("IOException in Stats.prettyPrint()");
                System.err.println(ex);
            }

            if (numTerms < 0)
                numTerms = -1;
            if (sumTotalTermFreq < 0)
                sumTotalTermFreq = -1;

            // (fieldName, uniqTermCount, totalTermCount)
            lucdebObjects.setTermCounts(fieldName, numTerms, sumTotalTermFreq);
        }
        isSetStats.put(fieldName, true);
    }

    @Override
    public String help() {
        return "stats - show the basic stats of the index";
    }

    @Override
    public String usage() {
        return "stats";
    }

}
