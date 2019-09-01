package org.sourav.lucDeb.resources;

import java.io.PrintStream;
import java.nio.file.Paths;
import java.io.ByteArrayOutputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import lucdeb.LucDeb;

@Path("/resource")
public class Resources {
	static int count = 0;
	static LucDeb lucdeb;

	@Path("/index")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response initializeIndex(@QueryParam("index") String index) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		PrintStream old = System.out;
		System.setOut(ps);
		try {
			// as of now index is hardcoded. Later change it TODO
			if ("trec678".equals(index))
				lucdeb = new LucDeb("/user1/faculty/cvpr/irlab/collections/indexed/trec678");
			
			else if("trec123".equals(index))
				lucdeb = new LucDeb("/user1/faculty/cvpr/irlab/collections/indexed/trec123");
			else if("wt10g".equals(index))
				lucdeb = new LucDeb("/user1/faculty/cvpr/irlab/collections/indexed/wt10g-jsoup");
			else if("gov2".equals(index))
				lucdeb = new LucDeb("/user1/faculty/cvpr/irlab/collections/indexed/gov2-jsoup");
			else if("clueweb".equals(index))
				lucdeb = new LucDeb("/user1/faculty/cvpr/irlab/collections/indexed/clueweb09b-spam70");
			else
				System.out.println("Invalid index name!");
			
		} catch (Exception e) {
			
			System.out.println("File not found!!");
		}
		System.out.flush();
		System.setOut(old);
		String indexResp = baos.toString();

		// String indexResp = "in the index";
		// System.out.println(input);
		return Response.ok(indexResp).header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, OPTIONS")
				.header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With").build();
	}
	public boolean checkEmpty(String args)
	{
		return (args.length()==0);
	}
	
	public String getErrorMsg(String type)
	{
		return "unable to execute "+type+" command" ;
	}
	@Path("/comm")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response executeCommand(@Context UriInfo info) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		PrintStream old = System.out;
		System.setOut(ps);

		MultivaluedMap<String, String> queryParameter = info.getQueryParameters();
		String command = queryParameter.getFirst("command");
		String args = "";
		switch (command) {
		case "stats":
			try 
			{
				lucdeb.executeCommand("stats", "".split(" "), System.out);
			} 
			catch (Exception e) 
			{
				System.out.println(getErrorMsg("stat"));
			}
			break;
			
		case "cf":
			try 
			{
				String field = queryParameter.getFirst("field");
				args = "-t "+ queryParameter.getFirst("term") + (!checkEmpty(field) ? " -f "+ field : "");
				//System.out.println(args);
				lucdeb.executeCommand("cf", args.split(" "), System.out);
			}
			catch (Exception e) 
			{
				System.out.println(getErrorMsg("cf"));
			}
			break;

		case "df":
			try 
			{
				String field = queryParameter.getFirst("field");
				
				args = " -t "+ queryParameter.getFirst("term") + (!checkEmpty(field) ? " -f "+ field : "");
				lucdeb.executeCommand("df", args.split(" "), System.out);
			}
			catch (Exception e) 
			{
				System.out.println(getErrorMsg("df"));
			}
			break;

		case "dl":
			try 
			{
				String docid = queryParameter.getFirst("docid");
				String docname = queryParameter.getFirst("docname");
				String field = queryParameter.getFirst("field");
				
				args = "";
				args += checkEmpty(docid) ? " -n "+ docname : " -i "+ docid;
				args += !checkEmpty(field) ? " -f "+ field : "";
				lucdeb.executeCommand("dl", args.split(" "), System.out);
			}
			catch (Exception e) 
			{
				System.out.println(getErrorMsg("dl"));
			}
			break;
			
		case "tf":
			try 
			{
				String docid = queryParameter.getFirst("docid");
				String docname = queryParameter.getFirst("docname");
				String field = queryParameter.getFirst("field");
				String term = queryParameter.getFirst("term");
				
				args = "";
				args += checkEmpty(docid) ? "<-n<"+ docname : "<-i<"+ docid;
				args += !checkEmpty(field) ? "<-f<"+ field : "";
				args += !checkEmpty(term) ? "<-q<"+ term : "";
				lucdeb.executeCommand("tf", args.split("<"), System.out);

				
			}
			catch(Exception e)
			{
				e.printStackTrace(System.out);
			}
			break;
			
		case "pl":
			try
			{
				String field = queryParameter.getFirst("field");
				args = queryParameter.getFirst("term") + (!checkEmpty(field) ? " "+ field : "");
				lucdeb.executeCommand("pl", args.split(" "), System.out);
			}
			catch (Exception e) 
			{
				System.out.println(getErrorMsg("pl"));
			}
			break;
			
		case "dv" :
			try
			{
				String docid = queryParameter.getFirst("docid");
				String docname = queryParameter.getFirst("docname");
				String field = queryParameter.getFirst("field");
				String discterm = queryParameter.getFirst("discterm");
				String retmodel = queryParameter.getFirst("retmodel");
				//String retparam = queryParameter.getFirst("retparam");
				String retparam = String.join(" ", queryParameter.get("retparam"));

				args = "";
				args += checkEmpty(docid) ? "<-n<"+ docname : "<-i<"+ docid;
				args += !checkEmpty(field) ? "<-f<"+ field : "";
				args += !checkEmpty(discterm) ? "<-d<"+ discterm : "";
				args += !checkEmpty(retmodel) ? "<-r<"+ retmodel + " " + retparam : "";
				
				lucdeb.executeCommand("dv", args.split("<"), System.out);
			}
			catch (Exception e) 
			{
				System.out.println(getErrorMsg("dv")+ e.getMessage());
			}
			break;
		case "dump" :
			try
			{
				String docid = queryParameter.getFirst("docid");
				String docname = queryParameter.getFirst("docname");
				String field = queryParameter.getFirst("field");
				String term = queryParameter.getFirst("term");
				
				args = "";
				args += checkEmpty(docid) ? " -n "+ docname : " -i "+ docid;
				args += !checkEmpty(field) ? " -f "+ field : "";
				args += !checkEmpty(term) ? " -q "+ term : "";
				//System.out.println("args"+ args);
				
				lucdeb.executeCommand("dump", args.split(" "), System.out);
			}
			catch (Exception e) 
			{
				e.printStackTrace(System.out);
			}
			break;
			
		case "search" :
			try
			{
				String term = queryParameter.getFirst("term");
				String retmodel = queryParameter.getFirst("retmodel");
				String retparam = String.join(" ", queryParameter.get("retparam"));
				String score = queryParameter.containsKey("score") ? queryParameter.getFirst("score") : "";
				String rank = queryParameter.containsKey("rank") ? queryParameter.getFirst("rank") : "";
				String doclen = queryParameter.containsKey("doclen") ? queryParameter.getFirst("doclen") : "";
				
				args = "";
				args += !checkEmpty(term) ? "<-q<"+ term : "";
				args += !checkEmpty(retmodel) ? "<-r<"+ retmodel + " " + retparam : "";
				args += !checkEmpty(score) ? "<-s" : "";
				args += !checkEmpty(rank) ? "<-k" : "";
				args += !checkEmpty(doclen) ? "<-dl" : "";
				//System.out.println(args);
				lucdeb.executeCommand("search", args.split("<"), System.out);
				
						
			}
			catch (Exception e) 
			{	
				e.printStackTrace(System.out);
			}
			break;
			
		case "explain" :
			try
			{ 
				
				String docid = queryParameter.getFirst("docid");
				String docname = queryParameter.getFirst("docname");
				String term = String.join(" ",queryParameter.get("term"));
				String retmodel = queryParameter.getFirst("retmodel");
				String retparam = String.join(" ", queryParameter.get("retparam"));

				//String retparam = queryParameter.getFirst("retparam");
				
				args = "";
				args += checkEmpty(docid) ? "-n "+ docname : "-i "+ docid;
				args += !checkEmpty(term) ? "<-q "+ term : "";
				args += !checkEmpty(retmodel) ? "<-r "+ retmodel + " " + retparam : "";
				//System.out.println(args);
				lucdeb.executeCommand("explain", args.split("<"), System.out);
				
			}
			catch (Exception e) 
			{
				System.out.println(getErrorMsg("explain"));
				//e.printStackTrace(System.out);
			}
			break;
			
		case "rank" :
			try
			{
				String docid = queryParameter.getFirst("docid");
				String docname = queryParameter.getFirst("docname");
				String term = String.join(" ",queryParameter.get("term"));
				String retmodel = queryParameter.getFirst("retmodel");
				String retparam = String.join(" ", queryParameter.get("retparam"));
				//String retparam = queryParameter.getFirst("retparam");
				
				args = "";
				args += checkEmpty(docid) ? "<-n<"+ docname : "<-i<"+ docid;
				args += !checkEmpty(term) ? "<-q<"+ term : "";
				
				args += !checkEmpty(retmodel) ? "<-r<"+ retmodel + " " + retparam : "";
				//System.out.println(args);
				lucdeb.executeCommand("rank", args.split("<"), System.out);

			}
			catch (Exception e) 
			{
				e.printStackTrace(System.out);
			}
			break;
			
			
		case "compare" :
			try
			{
				String docid = queryParameter.getFirst("docid");
				String docname = queryParameter.getFirst("docname");
				
				String docid2 = queryParameter.getFirst("docid2");
				String docname2 = queryParameter.getFirst("docname2");
				String term = String.join(" ", queryParameter.get("term"));
				String retmodel = queryParameter.getFirst("retmodel");
				//String retparam = queryParameter.getFirst("retparam");
				String retparam = String.join(" ", queryParameter.get("retparam"));

				args = "";
				args += checkEmpty(docid) ? "<-n1<"+ docname : "<-i1<"+ docid;
				
				args += checkEmpty(docid2) ? "<-n2<"+ docname2 : "<-i2<"+ docid2;
				
				args += !checkEmpty(term) ? "<-q<"+ term : "";
				args += !checkEmpty(retmodel) ? "<-r<"+ retmodel + " " + retparam : "";
				lucdeb.executeCommand("compare", args.split("<"), System.out);

			}
			catch (Exception e) 
			{
				//System.out.println(getErrorMsg("compare"));
				e.printStackTrace(System.out);
			}
			break;
						
		case "expansion" :
			try
			{
				String term = String.join(" ", queryParameter.get("term"));
				String expterm = queryParameter.getFirst("expterm");
				
				args = "";
				args +=  term ;
				args += !checkEmpty(expterm) ? "<"+ expterm : "";
				lucdeb.executeCommand("expansion", args.split("<"), System.out);

			}
			catch (Exception e) 
			{
				System.out.println(getErrorMsg("expansion"));
			}
			break;
			
		case "diff" : 
			try
			{
				
			}
			catch (Exception e) 
			{
				System.out.println(getErrorMsg("diff"));
			}
			break;
			
		case "sigtest" : 
			try
			{
				
			}
			catch (Exception e) 
			{
				System.out.println(getErrorMsg("sigtest"));
			}
			break;
			
		case "setRetrieval" :
			try
			{
				
				String retmodel = queryParameter.getFirst("retmodel");
				String retparam = String.join(" ", queryParameter.get("retparam"));				
				args = "";
				args += !checkEmpty(retmodel) ? "<-m<"+ retmodel + "<-p<" + retparam : "";
				lucdeb.executeCommand("setRetModel", args.split("<"), System.out);
				
			}
			catch (Exception e) 
			{
				System.out.println(getErrorMsg("setRetrieval"));
			}
			break;
			
		case "setSearchField" : 
			try
			{
			
				String field = queryParameter.getFirst("field");
				args = field;
				lucdeb.executeCommand("searchfield", args.split(" "), System.out);
				
			}
			catch (Exception e) 
			{
				System.out.println(getErrorMsg("setSearchField"));
			}
			break;
						
			
		default:
			System.out.println("Not such service found");
			break;
		}	

		System.out.flush();
		System.setOut(old);
		String response = baos.toString();
		String result = response;
		/**
		 *  TODO
		 */
		if("dv".equals(command))
		{
			if(!args.contains("<-d<")) 
			{		
				result = "<table class='table'>";
				result += "<thead class = 'thead-dark'> " + 
						"    <tr>" + 
						"      <th scope=\"col\">Term</th>" + 
						"      <th scope=\"col\">Count</th>" +
						"      <th scope=\"col\">Term</th>" + 
						"      <th scope=\"col\">Count</th>" +
						"      <th scope=\"col\">Term</th>" + 
						"      <th scope=\"col\">Count</th>" +
						"      <th scope=\"col\">Term</th>" + 
						"      <th scope=\"col\">Count</th>" +
						"    </tr>" + 
						"  </thead> <tbody>" ;
				String responseList[] = response.split("\n");
				
				for (int i = 0; i < responseList.length; i++) {
					if(i%4 ==0)
					{
						if(i==0)
							result += "<tr><td>"+responseList[i].split(" ")[0] + "</td>" + "<td>" + responseList[i].split(" ")[1] + "</td>";
						else
							result += "</tr><tr> <td>"+responseList[i].split(" ")[0] + "</td>" + "<td>" + responseList[i].split(" ")[1] + "</td>";
					}
					else
						result += "<td>" + responseList[i].split(" ")[0] + "</td>" + "<td>" + responseList[i].split(" ")[1] + "</td>";
				}
				result += "</tr></tbody></table>";
			}
			else {
				String lines[] = response.split("\n");
				String colNames[] = lines[0].split("\t");
				
				result = "<table class='table'>";
				result += "<thead class = 'thead-dark'> " + 
						"    <tr>" + 
						"      <th scope=\"col\">"+colNames[0]+"</th>" + 
						"      <th scope=\"col\">"+colNames[1]+"</th>" +
						"      <th scope=\"col\">"+colNames[2]+"</th>" + 
						"      <th scope=\"col\">"+colNames[3]+"</th>" +
						"      <th scope=\"col\">"+colNames[4]+"</th>" +
						"      <th scope=\"col\">"+colNames[5]+"</th>" +
						"    </tr>" + 
						"  </thead> <tbody>" ;
				
				for(int i = 1 ; i < lines.length ; i++)
				{
					String values[] = lines[i].split("\t");
					result += "<tr>";
					for(int j = 0; j < values.length;j++)
						result += "<td>" + values[j] + "</td>";
					result += "</tr>";
				}
				result += "</tbody></table>";
			}
		}
		
		/**
		 * TODO 
		*/		
		else if("pl".equals(command))
		{
			result = "<table class='table'>";
			result += "<thead class = 'thead-dark'> " + 
					"    <tr>" + 
					"      <th scope=\"col\">DocId</th>" + 
					"      <th scope=\"col\">Tf</th>" +
					"      <th scope=\"col\">DocId</th>" + 
					"      <th scope=\"col\">Tf</th>" +
					"      <th scope=\"col\">DocId</th>" + 
					"      <th scope=\"col\">Tf</th>" +
					"      <th scope=\"col\">DocId</th>" + 
					"      <th scope=\"col\">Tf</th>" +
					"    </tr>" + 
					"  </thead> <tbody>" ;
			String responseList[] = response.split("\n");
			
			for (int i = 0; i < responseList.length; i++) {
				if(i%4 ==0)
				{
					if(i==0)
						result += "<tr><td>"+responseList[i].split(" ")[0] + "</td>" + "<td>" + responseList[i].split(" ")[1] + "</td>";
					else
						result += "</tr><tr> <td>"+responseList[i].split(" ")[0] + "</td>" + "<td>" + responseList[i].split(" ")[1] + "</td>";
				}
				else
					result += "<td>" + responseList[i].split(" ")[0] + "</td>" + "<td>" + responseList[i].split(" ")[1] + "</td>";
			}
			result += "</tr></tbody></table>";
			
		}
		else if("compare".equals(command))
		{
			String responseList[] = response.split("\t");
			//System.out.println(responseList.length);
			if(responseList.length > 2)
			{
				result = "<table class='table'>";
				result += "<thead class = 'thead-dark'> " + 
						"    <tr>" + 
						"      <th scope=\"col\">Attributes</th>" + 
						"      <th scope=\"col\">"+responseList[0] +"</th>" +
						"      <th scope=\"col\">"+responseList[1] +"</th>" + 
						"    </tr>" + 
						"  </thead> <tbody>" ;
				for(int i = 2; i < responseList.length ; i++)
				{
					if(i%3 == 2)
						result += "<tr>";
					result += "<td>" + responseList[i] + "</td>";
					if(i%3 == 1)
					result += "</tr>";
				}
				if(responseList.length % 3 != 2)
					result += "</tr>";
				result += "</tbody></table>";
			}
		}
		
		else if ("search".equals(command))
		{
			result = "<table class='table'>";
			result += "<thead class = 'thead-dark'> <tr> ";
			result += "<th scope='col'> Document Name </th>";
			result += args.contains("<-k") ? "<th scope='col'> Rank </th>" : ""; 
			result += args.contains("<-s") ? "<th scope='col'> Score </th>" : ""; 
			result += args.contains("<-dl") ? "<th scope='col'> DocLen </th>" : ""; 
			result += "</tr></thead> <tbody>" ;
			
			String responseList[] = response.split("\n");
			for(int i = 0 ; i < responseList.length ; i++)
			{
				String values[] = responseList[i].split("\t");
				result += "<tr>";
				for(int j = 0; j < values.length;j++)
					result += "<td>" + values[j] + "</td>";
				result += "</tr>";
			}
			result += "</tbody></table>";
		}
		
		
		return Response.ok(result).header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, OPTIONS")
				.header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With").build();	
	}
}
