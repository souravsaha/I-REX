/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irex;

import common.TRECQuery;
import common.TRECQueryParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import static java.lang.System.out;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;

import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.completer.FileNameCompleter;
import jline.console.completer.StringsCompleter;

import irex.commands.CollectionFrequencyCommand;
import irex.commands.Commands;
import irex.commands.CompareCommand;
import irex.commands.CustomLMJMSim;
import irex.commands.DocLengthCommand;
import irex.commands.DocTermsCommand;
import irex.commands.DocVectorCommand;
import irex.commands.DocumentFrequencyCommand;
import irex.commands.DumpCommand;
import irex.commands.ExpansionCommand;
import irex.commands.ExplainCommand;
import irex.commands.HelpCommand;
import irex.commands.Postings;
import irex.commands.QuitIRex;
import irex.commands.RankCommand;
import irex.commands.SearchCommand;
import irex.commands.SetRetrievalModelCommand;
import irex.commands.SetSearchFieldCommand;
import irex.commands.StatsCommand;
import irex.commands.TermFrequencyCommand;
import irex.commands.DocSimilarCommand;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.FieldInfos;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.SegmentCommitInfo;
import org.apache.lucene.index.SegmentInfos;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.xml.sax.SAXException;

/**
 *
 * @author dwaipayan
 */
public final class IRexObjects {

    private String      initFilePath;
    private final String      indexPath;
    private final File        indexFile;
    private final IndexReader indexReader;

    private final IndexSearcher indexSearcher;

    private Analyzer queryAnalyzer;
    private StandardQueryParser queryParser;

    private final IndexSearcher docidSearcher;

    private final TreeMap<String, Commands> cmdMap;

    /**
     * Map of all the retrieval models.
     */
    public HashMap<String, String> retFuncMap;
    public Properties prop;

    public String              idField;
    private String              searchField;

    public boolean              toStopRemove;
    public String               stopwordPath;
    public String               analyzerName;

    public String               queryPath;
    TRECQueryParser             trecQueryParser;
    HashMap<Integer,TRECQuery>  queries;
    public String               qrelPath;
    boolean                     isQuerySet;
    boolean                     isQrelSet;

    public SortedMap<String, Object[]> fields;  // to contain all name of the fields
    public long                 numDocs;        // number of documents in the index
    public int                  numFields;      // number of fields in the index
    public String 				retModelName;   // name of the retrieval model
    public String 				retModelParam1;   // name of the retrieval model parameter 1
    public String 				retModelParam2;   // name of the retrieval model parameter 2
    public String 				retModelParam3;   // name of the retrieval model parameter 3
    
    /**
     * Key: fieldName
     * Value: ArrayList (uniqNumTerms, totalNumTerms)
     */
    private HashMap<String, List<Long>>                termCounts;   //uniqNumTerms;

    private final ConsoleReader consoleReader;

    public void init() {

        this.initFilePath = "/resources/init.properties";
        prop = new Properties();
        try {
            prop.load(getClass().getResourceAsStream(this.initFilePath));
        } catch (IOException ex) {
            System.err.println("Error: init file missing from the root directory");
            System.exit(1);
        }

    } // ends init()

    public void setQueryAnalyzer() {

        toStopRemove = Boolean.parseBoolean(prop.getProperty("toStopRemove", "false"));
        System.out.println("toStopRemove : "+ toStopRemove);

        if(toStopRemove) {
            if(prop.containsValue("stopwordPath")) {
                if(prop.getProperty("default").equals("default"))
                    stopwordPath = "/resources/smart-stopwords";
                else
                    stopwordPath = prop.getProperty("stopwordPath");
            }
            else
                stopwordPath = "/resources/smart-stopwords";
        }
        else    // if(NOT toStopRemove)
            stopwordPath = "";

        List<String> stopwords = new ArrayList<>();

        String line;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(stopwordPath)));
            while ( (line = br.readLine()) != null ) stopwords.add(line.trim());

            br.close();
        } catch (FileNotFoundException ex) {
            System.err.println("Error: Stopword file not found in the jar");
            System.exit(1);
        } catch (IOException ex) {
            System.err.println("Error: IOException occurs while reading stopword file");
            System.exit(1);
        }

        // TODO: add other analyzers here and in the init.prop file
        analyzerName = prop.getProperty("analyzer");
        switch(analyzerName) {
            case "simple":
                System.out.println("QueryAnalyzer: SimpleAnalyzer()");
                queryAnalyzer = new SimpleAnalyzer();
                break;
            case "stop":
                System.out.println("QueryAnalyzer: StopAnalyzer()");
                queryAnalyzer = new StopAnalyzer(StopFilter.makeStopSet(stopwords));
                break;
            case "white":
                System.out.println("QueryAnalyzer: WhitespaceAnalyzer()");
                queryAnalyzer = new WhitespaceAnalyzer();
                break;
            case "eng":
                System.out.println("QueryAnalyzer: EnglishAnalyzer()");
                queryAnalyzer = new EnglishAnalyzer(StopFilter.makeStopSet(stopwords));
                break;
        }

        queryParser = new StandardQueryParser(queryAnalyzer);

    }

    public IRexObjects(String indexPath) throws IOException, Exception {

        init();

        // +++ index opening
        System.out.println("Opening index from: " + indexPath);
        this.indexPath = indexPath;
        indexFile = new File(this.indexPath);
        Directory indexDir = FSDirectory.open(indexFile.toPath());

        if (!DirectoryReader.indexExists(indexDir)) {
            System.err.println("Index doesn't exists in "+indexPath);
            System.exit(1);
        }

        /* setting IndexReader */
        indexReader = DirectoryReader.open(FSDirectory.open(indexFile.toPath()));

        indexSearcher = new IndexSearcher(indexReader);

        indexSearcher.setSimilarity(new CustomLMJMSim(0.4f));

        System.out.println("Index opened successfully.");
        // --- index opened


        if (indexReader instanceof DirectoryReader) {
            DirectoryReader dirReader = (DirectoryReader) indexReader;
            SegmentInfos segInfo = SegmentInfos.readLatestCommit(dirReader.directory());
            for (SegmentCommitInfo commitInfo : segInfo) {
                if (commitInfo != null) {
                    System.out.println("Lucene codec used: " + commitInfo.info.getCodec().getName());
                    break;
                }
            }
        }

        List<LeafReaderContext> leaves = indexReader.leaves();

        numDocs = indexReader.numDocs();
        fields = new TreeMap<>();

        try {
            for (LeafReaderContext leaf : leaves) {
                LeafReader leafReader = leaf.reader();
                FieldInfos fieldInfos = leafReader.getFieldInfos();
                Iterator<FieldInfo> fieldInfoIter = fieldInfos.iterator();
                Fields flds = leafReader.fields();

                while (fieldInfoIter.hasNext()) {
                    FieldInfo finfo = fieldInfoIter.next();
                    Terms t = flds.terms(finfo.name);

                    Object[] data = fields.get(finfo.name);
                    if (data == null) {
                        data = new Object[2];
                        LinkedList<Terms> termsList = new LinkedList<>();
                        termsList.add(t);
                        data[0] = finfo;
                        data[1] = termsList;
                        fields.put(finfo.name, data);
                    } 
                    else {
                        List<Terms> termsList = (List<Terms>) data[1];
                        termsList.add(t);
                    }
                }
            }
            numFields = fields.size();
        }
        catch(IOException ex) {
            System.err.println("IOException in Stats.execute()");
            System.err.println(ex);
        }
        // --- basic stats set
        termCounts = new HashMap<>();

        System.out.println("Number of docs: " + numDocs);
        System.out.println("Number of fields: " + numFields);
        int fcount = 0;
        for (Object[] finfo : fields.values()) {
            FieldInfo f = (FieldInfo) finfo[0];
            System.out.println("=== Field "+ ++fcount + ": "+f.name+" ===");
            System.out.println("Term vectors stored:\t" + f.hasVectors());
            //setSearchField(f.name);
            setStats(finfo, out);
        }
        
        setSearchField(prop.getProperty("searchField", "content"));

        setDocidField(prop.getProperty("docidField", "docid"));

        setRetrievalModelNames();

        setQueryAnalyzer();

        setQueryFile();
        if(queries != null)
            setJudgedDocs();

        cmdMap = new TreeMap<>();

        /* setting docidSearcher */
        docidSearcher = new IndexSearcher(getIndexReader());
        docidSearcher.setSimilarity(new DefaultSimilarity());

        // Registering commands
        // To add new commands in I-Rex, add the constractor here; 
        // Please keep the lexicographical order for better understatnding.

        new CollectionFrequencyCommand(this);
        new DocumentFrequencyCommand(this);
        new DocLengthCommand(this);
//        new DocLengthNameCommand(this);
        new DocTermsCommand(this);
        new DocVectorCommand(this);
        new DumpCommand(this);
//        new DocidFieldCommand(this);
        new ExpansionCommand(this);
        new ExplainCommand(this);
        //new ExplainDefaultCommand(this);
        new HelpCommand(this);
//        new Man(this);
        new Postings(this);
        new QuitIRex(this);
        new RankCommand(this);
        new SearchCommand(this);
        new SetSearchFieldCommand(this);
        new StatsCommand(this);
        new TermFrequencyCommand(this);
//        new VocabCommand(this);
        new SetRetrievalModelCommand(this);
        new CompareCommand(this);
        new DocSimilarCommand(this);

        this.consoleReader = new ConsoleReader();
//        String[] list = {"211077","39", "307362", "21"};
//        this.consoleReader = new ConsoleReader(list);
//        this.consoleReader.clearScreen();
        this.consoleReader.setPaginationEnabled(true);
        this.consoleReader.setBellEnabled(false);
        initAutoCompletion();

    }

    void initAutoCompletion() {
        LinkedList<Completer> completors = new LinkedList<>();

        completors.add(new StringsCompleter(cmdMap.keySet()));      // The Commands: first argument
        completors.add(new StringsCompleter(getAllFieldName()));    // The field names: second argument
        completors.add(new FileNameCompleter());                    // Any path:  third argument

        consoleReader.addCompleter(new ArgumentCompleter(completors));
    }

    Collection<String> getAllFieldName() {
        LinkedList<String> fieldNames = new LinkedList<>();
        for (LeafReaderContext leaf : getIndexReader().leaves()) {
            LeafReader leafReader = leaf.reader();
            for(FieldInfo info : leafReader.getFieldInfos())
                fieldNames.add(info.name);
        }
        return fieldNames;
    }

    public String readCommand() {
        try {
            return consoleReader.readLine("> ");
        } catch (IOException e) {
            System.err.println("Unable to read from command line. " + e.getMessage());
            throw new IllegalStateException("Unable to read from command line.", e);
        }
    }

    public StandardQueryParser getQueryParser() {

        return queryParser;
    }

    /**
     * Returns the lucene docid of the document
     * @param docid
     * @return
     * @throws Exception 
     */
    public int getLuceneDocid(String docid) throws Exception {

        ScoreDoc[] hits;
        TopDocs topDocs;

        TopScoreDocCollector collector = TopScoreDocCollector.create(1);
        Query luceneDocidQuery = new TermQuery(new Term(idField, docid));

        docidSearcher.search(luceneDocidQuery, collector);
        topDocs = collector.topDocs();
        hits = topDocs.scoreDocs;
        if(hits.length <= 0) {
            System.out.println(docid+": document not found");
            return -1;
        }
        else {
//            System.out.println(docid + " : " + hits[0].doc);
            return hits[0].doc;
        }
    }

    /**
     * Returns the IndexReader initialized for the given Lucene index.
     * @return IndexReader
     */
    public IndexReader getIndexReader() {
        return indexReader;
    }

    public IndexSearcher getIndexSearcher() {
        return indexSearcher;
    }

    public void setSearchField(String field) {
        if(isValidFieldName(field)) {
            searchField = field;
            System.out.println("Field to search: "+searchField);
        }
        else {
            System.out.println("Warning: field to search set to invalid value.");
            System.out.println("Field names: " + fields.keySet().toString());
            System.out.print("Set field to search now: ");
            Scanner scr = new Scanner(System.in);
            field = scr.next();
            setSearchField(field);
        }
        //System.out.println("Search field set to: " + searchField);
    }

    public String getSearchField() {
        return searchField;
    }

    public void setDocidField(String field) {
        if(isValidFieldName(field)) {
            idField = field;
            System.out.println("Docid field: "+idField);
        }
        else {
            System.out.println("Warning: docid field set to invalid value.");
            System.out.println("Field names: " + fields.keySet().toString());
            System.out.print("Set docid field now: ");
            Scanner scr = new Scanner(System.in);
            field = scr.next();
            setDocidField(field);
        }
    }

    public long getNumDocs() {
        return numDocs;
    }

    public void setTermCounts(String fieldName, long uniqNumTerms, long totalNumTerms) {
        ArrayList<Long> counts = new ArrayList<>();
        counts.add(uniqNumTerms);
        counts.add(totalNumTerms);
        termCounts.put(fieldName, counts);
    }

    public List<Long> getTermCounts(String fieldName) {
        return termCounts.get(fieldName);
    }
    
    public long getTotalNumTerms(String fieldName) {
//        termCounts.get(fieldName);
        return termCounts.get(fieldName).get(1);
    }

    public long getUniqNumTerms(String fieldName) {return termCounts.get(fieldName).get(1);}

    public boolean isValidFieldName(String fieldName) {return fields.keySet().contains(fieldName);}

    /**
     * Check whether the luceneDocid has a corresponding document in the index.
     * @param luceneDocid
     * @return 
     */
    public boolean isValidLucenDocid(int luceneDocid) throws IOException {
        try {
            Document doc = docidSearcher.doc(luceneDocid);
            return doc != null;
        }
        catch(IllegalArgumentException ex) {
            System.out.println("Error: docid must be >= 0 and < " + (getNumDocs()-1));
            return false;
        }
    }

    /**
     * Opens the Lucene index for subsequent operations.
     * @param indexPath Path of the Lucene index.
     * @throws IOException In case of any exception during opening of the index.
     */
    public void openIndex(String indexPath) throws IOException {

    }

    /**
     * Register the command in I-Rex.
     * @param cmd 
     */
    public void registerCommand(Commands cmd) {

        String cmdName = cmd.getName();
        if(cmdMap.containsKey(cmdName))
            throw new IllegalArgumentException(cmdName + " already defined.");

        cmdMap.put(cmdName, cmd);
    }

    /**
     * Get the corresponding the command to execute.
     * @param cmdName
     * @return 
     */
    public Commands getCommand(String cmdName) {
        return cmdMap.get(cmdName);
    }

    /**
     * Get all the commands.
     * @return 
     */
    public Map<String, Commands> getCommandMap() {
        return cmdMap;
    }

    public Query getAnalyzedQuery(String query) throws QueryNodeException {
        return queryParser.parse(query, this.searchField);
    }

    public Query getAnalyzedQuery(String query, String searchField) throws QueryNodeException {
        //System.out.println(queryParser.parse(query, searchField));
        return queryParser.parse(query, searchField);
    }

    void initTabCompletion() {

        LinkedList<Completer> completors = new LinkedList<>();
        completors.add(new StringsCompleter(cmdMap.keySet()));
//        completors.add(new StringsCompleter(getFieldNames()));
//        completors.add(new FileNameCompleter());

        consoleReader.addCompleter(new ArgumentCompleter(completors));
    }

    public void printPagination(ArrayList<String> list) throws IOException {
        consoleReader.printColumns(list);
    }

    public Collection<String> getFieldNames() {
        LinkedList<String> fieldNames = new LinkedList<>();
        
        for (Map.Entry<String, Object[]> entrySet : fields.entrySet()) {
            String fieldName = entrySet.getKey();
            fieldNames.add(fieldName);
        }

        return fieldNames;
    }

    /**
     * Setting name of all the retrieval functions.
     * To add any new retrieval functions here.
     */
    public void setRetrievalModelNames() {
        retFuncMap = new HashMap<>();

        retFuncMap.put("lmjm", "lmjm");
        retFuncMap.put("lmdir", "lmdir");
        retFuncMap.put("bm25", "bm25");
        retFuncMap.put("dfr", "dfr");
    }

    public boolean isKnownRetFunc(String retFunc) {
        return (null != retFuncMap.get(retFunc));
    }
    /**
     * Getting the default retrieval functions.
     */
    public void getRetreivalParameter() {
    
    	retModelName = prop.getProperty("retModel");
    	
    	switch(retModelName) 
    	{
        	case "lmjm":
        		retModelParam1 = prop.getProperty("param1");
        		break;
        	case "lmdir":
        		retModelParam1 = prop.getProperty("param1");
        		break;
        	case "bm25":
        		retModelParam1 = prop.getProperty("param1");
        		retModelParam2 = prop.getProperty("param2");
        		break;
        	case "dfr":            
        		retModelParam1 = prop.getProperty("param1");
        		retModelParam2 = prop.getProperty("param2");
        		retModelParam3 = prop.getProperty("param3");
        		break;
        	default :
        		System.err.println("Sorry! No such retrieval models found.");
        		return;
    	}
    }
    /**
     * Setting the retrieval model and parameter
     * on the fly.
     */
    public void setRetreivalParameter(String retModel, String param1,String param2, String param3) {
    	retModelName = retModel;
    	switch(retModel) 
    	{
        	case "lmjm":
        		retModelParam1 = param1;
        		break;
        	case "lmdir":
        		retModelParam1 = param1;
        		break;
        	case "bm25":
        		retModelParam1 = param1;
        		retModelParam2 = param2;
        		break;
        	case "dfr":            
        		retModelParam1 = param1;
        		retModelParam2 = param2;
        		retModelParam3 = param3;
        		//System.out.println(retModelParam1 + retModelParam2 + retModelParam3);
        		break;
        	default :
        		System.err.println("Sorry! No such retrieval models found.");
        		return;
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
            setTermCounts(fieldName, numTerms, sumTotalTermFreq);
        }
    }

    public void setQueryFile() throws SAXException, Exception {

        if(prop.containsKey("queryPath")) {
            queryPath = prop.getProperty("queryPath");
            if(new File(queryPath).exists()) {
                trecQueryParser = new TRECQueryParser(queryPath, getQueryAnalyzer(), getSearchField());
                queries = constructQueries();
                isQuerySet = true;
            }
            else {
                System.err.println("Error: query file not exists in " + queryPath);
                isQuerySet = false;
            }
        }
        else {
            System.out.println("Query Path not set.");
            isQuerySet = false;
            queryPath = "";
        }
    }

    public Analyzer getQueryAnalyzer() {return queryAnalyzer;}

    private HashMap<Integer,TRECQuery> constructQueries() throws Exception {

        trecQueryParser.queryFileParse();
        return trecQueryParser.queries;
    }

    public void setJudgedDocs() {

        if(prop.containsKey("qrelPath")) 
            qrelPath = prop.getProperty("qrelPath");
        else {
            System.out.println("Query Path not set.");
            qrelPath = "";
            isQrelSet = false;
        }

        int lastReadQid = -1;

        String line;
        BufferedReader br = null;
        int relDocsCount = 0;
        HashMap<String, Integer> judgedRelDocs = new HashMap<>();
        try {
            br = new BufferedReader(new FileReader(qrelPath));
            line = br.readLine();
            String tokens[] = line.split("\\s+");
            if(line == null) {
                System.err.println("Error: qrel file empty.");
                return;
            }
            else {
                if(Integer.parseInt(tokens[3])>0) {
                    judgedRelDocs.put(tokens[2], Integer.parseInt(tokens[3]));
                    relDocsCount ++;
                }
                lastReadQid = Integer.parseInt(tokens[0]);
            }

            TRECQuery tq;
            while((line = br.readLine()) != null) {
                tokens = line.split("\\s+");
                if(lastReadQid!=Integer.parseInt(tokens[0])) {
                    tq = queries.get(lastReadQid);
                    if(tq != null) {
                        tq.judgedRelDocs = judgedRelDocs;
                        tq.relDocsCount = relDocsCount;
                    }
                    judgedRelDocs = new HashMap<>();
                    relDocsCount = 0;

                    if(Integer.parseInt(tokens[3])>0) {
                        relDocsCount ++;
                        judgedRelDocs.put(tokens[2], Integer.parseInt(tokens[3]));
                    }
                    lastReadQid = Integer.parseInt(tokens[0]);
                }
                else {
                    if(Integer.parseInt(tokens[3])>0) {
                        relDocsCount ++;
                        judgedRelDocs.put(tokens[2], Integer.parseInt(tokens[3]));
                    }
                }
            }
            tq = queries.get(lastReadQid);
            if(tq != null) {
                tq.judgedRelDocs = judgedRelDocs;
                tq.relDocsCount = relDocsCount;
            }
            isQrelSet = true;
        } catch (FileNotFoundException ex) {
            System.err.println("Error reading qrel file. File not found at: " + qrelPath);
            isQrelSet = false;
        } catch (IOException ex) {
            System.err.println("Error reading qrel file. IOException received.");
            isQrelSet = false;
        }
    }

    public void closeAll() throws IOException {
        indexReader.close();
    }
}
