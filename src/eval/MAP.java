/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.benchmark.quality.*;//Judge;
import org.apache.lucene.benchmark.quality.QualityQuery;
import org.apache.lucene.benchmark.quality.QualityQueryParser;
import org.apache.lucene.benchmark.quality.trec.TrecJudge;
import org.apache.lucene.benchmark.quality.trec.TrecTopicsReader;
import org.apache.lucene.benchmark.quality.utils.SimpleQQParser;
import org.apache.lucene.benchmark.quality.utils.SubmissionReport;
import org.apache.lucene.store.Directory;

/**
 *
 * @author Dwaipayan <dwaipayan.roy@gmail.com>
 */
public class MAP {

    /*
    public static void main(String[] args) throws IOException, Exception {
        File topicsFile = new File("aos/lucene/benchmark/topics.txt");
        File qrelsFile = new File("aos/lucene/benchmark/qrels.txt");
        Directory dir = FSDirectory.open(new File("indexes/MeetLucene").toPath());
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);

        String docNameField = "filename";

        PrintWriter LOGGER = new PrintWriter(System.out, true);

        TrecTopicsReader qReader = new TrecTopicsReader();
        QualityQuery qqs[] = qReader.readQueries(new BufferedReader(new FileReader(topicsFile)));

        Judge judge = new TrecJudge(new BufferedReader(new FileReader(qrelsFile)));

        judge.validateData(qqs, LOGGER);

        QualityQueryParser qqParser = new SimpleQQParser("title", "contents");

        QualityBenchmark qrun = new QualityBenchmark(qqs, qqParser, searcher, docNameField);
        SubmissionReport submitLog = null;
        QualityStats stats[] = qrun.execute(judge, submitLog, LOGGER);

        QualityStats avg = QualityStats.average(stats);
        avg.log("SUMMARY", 2, LOGGER, "  ");

        dir.close();
    }
*/
    public static void main(String[] args) throws FileNotFoundException, IOException, Exception {
        File topicsFile = new File("/home/dwaipayan/Dropbox/ir/corpora-stats/topics_xml/301.xml");
        File qrelsFile = new File("/home/dwaipayan/Dropbox/ir/corpora-stats/qrels/301.qrel");
        File resFile = new File("/home/dwaipayan/301-dfr.res");
        BufferedReader br = new BufferedReader(new FileReader(resFile));
        IndexReader indexReader = DirectoryReader.open(FSDirectory.open(new File("/store/collections/indexed/trec678/").toPath()));

        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        int maxResults = 1000;
        String docNameField = "docid"; 

        PrintWriter logger = new PrintWriter(System.out,true); 

        // use trec utilities to read trec topics into quality queries
        TrecTopicsReader qReader = new TrecTopicsReader();
        QualityQuery qqs[] = qReader.readQueries(new BufferedReader(new FileReader(topicsFile)));

        // prepare judge, with trec utilities that read from a QRels file
        Judge judge = new TrecJudge(new BufferedReader(new FileReader(qrelsFile)));

//        // validate topics & judgments match each other
        judge.validateData(qqs, logger);

        // set the parsing of quality queries into Lucene queries.
//        QualityQueryParser qqParser = new SimpleQQParser("title", "content");

        // run the benchmark
//        QualityBenchmark qrun = new QualityBenchmark(qqs, qqParser, indexSearcher, docNameField);
//        SubmissionReport submitLog = null;
//        QualityStats stats[] = qrun.execute(judge, submitLog, logger);

        String line;
        QualityStats qs = new QualityStats(1000, (long) 0.0);
        int i = 0;
        while((line=br.readLine())!=null) {
            boolean isRel;
//            System.out.println(line.split("\t")[2]);
            isRel = judge.isRelevant(line.split("\t")[2].trim(), qqs[0]);
            if(isRel)
                System.out.println(line.split("\t")[2]);            
            qs.addResult(++i, isRel, (long) 1.0);
//            System.out.println(qs.getAvp());
        }
//        for (QualityStats qs : stats) {
//            qs.addResult(maxResults, true, maxResults);
//            System.out.println(qs.getAvp());
//        }
        // print an average sum of the results
//        QualityStats avg = QualityStats.average(stats);
//        avg.log("SUMMARY",2,logger, "  ");
    }
}
