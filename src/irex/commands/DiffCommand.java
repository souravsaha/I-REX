package irex.commands;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import irex.IRexObjects;
import common.treceval.Qrel;

/**
 * @author souravsaha
 *
 */
public class DiffCommand extends Commands{

	public DiffCommand(IRexObjects lucivObjects) {
        super(lucivObjects, "diff");
    }
	
	@Override
    public String help() {
        return usage();
    }
	
	@Override
    public String usage() {
        return "diff <-f1> <-1st res file> <-f2 > <-2nd res file>";
    }
	@Override
    public void execute(String[] args, PrintStream out) throws IOException {
		Options options = new Options();
    	Option resFileOption1 = new Option("f1", "resfile1", true, "Name of the 1st res file");
    	resFileOption1.setRequired(true);
    	options.addOption(resFileOption1);

    	Option resFileOption2 = new Option("f2", "resFile2", true, "Name of the 2nd res file");
    	resFileOption2.setRequired(false);
    	options.addOption(resFileOption2);
    	
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
        
        String resFileName1 = cmd.getOptionValue("resFile1");
        String resFileName2 = cmd.getOptionValue("resFile2");
        
        if(cmd.hasOption("f1") && cmd.hasOption("f2"))
        {
        	// throw if s1 and s2 not found
    		Scanner s1 = new Scanner(new File("/Users/souravsaha/Downloads/"+resFileName1));
    		Scanner s2 = new Scanner(new File("/Users/souravsaha/Downloads/"+resFileName2));
        
    		
    		LinkedHashMap<LinkedHashMap<String, String>, String[]> qidDocScoreMap1 = new LinkedHashMap<LinkedHashMap<String, String>, String[]>();
    		LinkedHashMap<LinkedHashMap<String, String>, String[]> qidDocScoreMap2 = new LinkedHashMap<LinkedHashMap<String, String>, String[]>();
    		
    		
    		
    		while (s1.hasNext())
    		{
    			String qID = s1.next();
    			s1.next();
    			String docID = s1.next();
    			String rank = s1.next();
    			String score = s1.next();
    			s1.next();
        	    LinkedHashMap<String,String> queryDocMap1 = new LinkedHashMap<String,String>();
    		    queryDocMap1.put(qID, docID);
    		    qidDocScoreMap1.put(queryDocMap1, new String[] {rank, score});
    		}
    		
    		while (s2.hasNext())
    		{
    			String qID = s2.next();
    			s2.next();
    			String docID = s2.next();
    			String rank = s2.next();
    			String score = s2.next();
    			s2.next();
        	    LinkedHashMap<String,String> queryDocMap2 = new LinkedHashMap<String,String>();
    		    queryDocMap2.put(qID, docID);
    		    qidDocScoreMap2.put(queryDocMap2, new String[] {rank, score});
    		}
    		
    		out.println("QID" + "\t" + "DOCID" + "R1" + "\t" + "SIM1" + "\t" + "R2" + "\t" + "SIM2" + "\t" + "RANK-DIFF" + "\t" + "REL");
    		for (Map.Entry<LinkedHashMap<String, String>, String[]> entry : qidDocScoreMap1.entrySet()) {
    		    LinkedHashMap<String, String> qIDdocIDMap = entry.getKey();
    		    String[] rankScore = entry.getValue();
    		    
    		    out.print(qIDdocIDMap.entrySet().iterator().next().getKey());       // qid
    		    out.print(qIDdocIDMap.entrySet().iterator().next().getValue());     // docid
    		    out.print(rankScore[0] + "\t" + rankScore[1]);                      // rank1, score1
    		    String[] rankScore2 =null;
    		    if(qidDocScoreMap2.get(qIDdocIDMap)!=null)
    		    {
    		    	rankScore2 = qidDocScoreMap2.get(qIDdocIDMap);
    		    	out.print(rankScore2[0] + "\t" + rankScore2[1]);                    // rank2, score2
    		    }
    		    else
    		    	out.print("NOT FOUND" + "\t"+ "NOT FOUND");
    		    int rankDiff;
    		    Integer rank1 = Integer.valueOf(rankScore[0]);
    		    Integer rank2 = Integer.valueOf(rankScore2[0]);
    		    rankDiff = (rank1 > rank2) ? (rank1 - rank2) : (rank1 - rank2);
    		    out.print(String.valueOf(rankDiff));
    		    out.print(Qrel.getQrel().getQueryRelevance().get(qIDdocIDMap));         // relevant or not
    		}
        
        }
        else
        {
        	out.println("less than 2 res files found.");
        	return;
        }
        
        
        
    }
}
