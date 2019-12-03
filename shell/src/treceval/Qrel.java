package treceval;

import java.util.*;
import java.io.*;

public class Qrel {
	private static Qrel qRel=null;
	private static HashMap<HashMap<String, String>, Integer> qRelMap = null;
	private Qrel()
	{
	}
	public static Qrel getQrel()
	{
		if(qRel ==null)
			qRel = new Qrel();
		return qRel;
	}
	public static HashMap<HashMap<String, String>, Integer> getQueryRelevance() throws IOException
	{
		/* TODO: change later */
		Scanner s = new Scanner(new File("/Users/souravsaha/Downloads/qrels.robust2004.txt"));
		//List<String> qID = new ArrayList<String>();
		//List<String> docID = new ArrayList<String>();
		//List<Integer> relevanceNumber = new ArrayList<Integer>();
		qRelMap = new HashMap<HashMap<String, String>, Integer>();
		// Read each line, ensuring correct format.
		while (s.hasNext())
		{
			String queryID = s.next();
		    //qID.add(queryID);
		    s.nextInt();
		    String documentID = s.next();
		    //docID.add(documentID);
		    Integer relevanceNum = s.nextInt();
		    //relevanceNumber.add(relevanceNum);
		    
		    HashMap<String,String> queryDocMap = new HashMap<String,String>();
		    queryDocMap.put(queryID, documentID);
		    qRelMap.put(queryDocMap, relevanceNum);
		}
		
		
		return qRelMap;
	}
	public static void main(String[] args) throws IOException {
		HashMap<HashMap<String, String>, Integer> map= Qrel.getQrel().getQueryRelevance();
		HashMap<String,String> qMap = new HashMap<String,String>();
		qMap.put("301","FBIS3-11210");
		System.out.println(map.get(qMap));
		
	}
}
