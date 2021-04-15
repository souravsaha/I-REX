/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irex.commands;

import java.io.IOException;
import java.io.PrintStream;
import irex.IRexObjects;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

/**
 *
 * @author dwaipayan
 */
public class ExplainDefaultCommand extends Commands {

    String docid;
    String query;
    int luceneDocid;

    public ExplainDefaultCommand(IRexObjects lucivObjects) {
        super(lucivObjects, "explain2");
    }

    @Override
    public String help() {
        return "Returns an Explanation that describes how doc scored against query.";
    }

    @Override
    public void execute(String[] args, PrintStream out) throws IOException {

        if (args.length < 2) {
            out.println("Usage: " + usage());
            return;
        }

        StringBuilder buf = new StringBuilder();
        for(int i=0; i<args.length-1; i++)
            buf.append(args[i]).append(" ");
        docid = args[args.length-1];
        query = buf.toString();
        out.println(query + " : " + docid);

        try {
            luceneDocid = irexObjects.getLuceneDocid(docid);
            if(luceneDocid == -1) {
                return;
            }
        } 
        catch (Exception ex) {
            
        }

        Query luceneQuery;

        try {
            luceneQuery = irexObjects.getAnalyzedQuery(query, "content");
            IndexSearcher indexSearcher = irexObjects.getIndexSearcher();
            System.out.println(luceneQuery.toString());
            Explanation expln = indexSearcher.explain(luceneQuery, luceneDocid);
            out.println(expln.toString());
//            out.println(Arrays.toString(expln.getDetails()));
        } catch (QueryNodeException ex) {
            out.println("QueryNodeException in ExplainCommand.execute();\n"
                + ex);
        }

    }

    @Override
    public String usage() {
        return "explain2 <query-terms> <doc>";
    }
}
