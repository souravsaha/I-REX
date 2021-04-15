/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import static common.CommonMethods.analyzeText;
import java.io.StringReader;
import java.util.HashMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 *
 * @author dwaipayan
 */
public class TRECQuery {
    public String       qid;
    public String       qtitle;
    public String       qdesc;
    public String       qnarr;
    public Query        luceneQuery;
    public String       fieldToSearch;

    public HashMap<String, Integer> judgedRelDocs;
    public int          relDocsCount;

    @Override
    public String toString() {
        return qid + "\t" + qtitle;
    }

    public Query getLuceneQuery() { return luceneQuery; }

    /**
     * Returns analyzed queryFieldText from the query
     * @param analyzer
     * @param queryFieldText
     * @return (String) The content of the field
     * @throws Exception 
     */
    public String queryFieldAnalyze(Analyzer analyzer, String queryFieldText) throws Exception {
        StringBuffer localBuff = new StringBuffer(); 
//        queryFieldText = queryFieldText.replace(".", "");
        TokenStream stream = analyzer.tokenStream(fieldToSearch, new StringReader(queryFieldText));
        CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
        stream.reset();
        while (stream.incrementToken()) {
            String term = termAtt.toString();
            term = term.toLowerCase();
            localBuff.append(term).append(" ");
        }
        stream.end();
        stream.close();
        return localBuff.toString();
    }

    /**
     * Returns analyzed queryFieldText from the query
     * @param analyzer
     * @param query_str
     * @return (String) The content of the field
     * @throws Exception 
     */
    public String queryStrAnalyze(String query_str, Analyzer analyzer) throws Exception {

        TokenStream stream = analyzer.tokenStream(null, new StringReader(query_str));
        // public final TokenStream tokenStream(String fieldName, String text)
        //      Returns a TokenStream suitable for fieldName, tokenizing the contents of text.
        // NOTE: we are only analyzing the text, so fieldName is specified as null; anything else would also work 

        StringBuffer localBuff = analyzeText(analyzer, query_str, null);

        return localBuff.toString();
    }

    public Query getBOWQuery(Analyzer analyzer, TRECQuery query, String searchField) throws Exception {
        fieldToSearch = searchField;
        BooleanQuery.Builder q = new BooleanQuery.Builder();
        Term thisTerm;

        String[] terms = queryStrAnalyze(query.qtitle, analyzer).split("\\s+");
        for (String term : terms) {
            thisTerm = new Term(fieldToSearch, term);
            Query tq = new TermQuery(thisTerm);
            q.add(tq, BooleanClause.Occur.SHOULD);
        }
        BooleanQuery.setMaxClauseCount(8192);
        return q.build();
    }

}
