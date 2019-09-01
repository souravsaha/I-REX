package org.sourav.lucDeb.resources;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.JSONP;

import lucdeb.LucDeb;

@Path("/cf")
public class CollectionFrequencyResource {
	
	@POST
	@Produces(MediaType.TEXT_PLAIN)
	public Response getCollectionFrequency(@FormParam("term") String term, @FormParam("fieldName") String fieldName)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		PrintStream old = System.out;
		System.setOut(ps);
		LucDeb lucdeb;
		try {
			lucdeb = new LucDeb("/user1/faculty/cvpr/irlab/collections/indexed/trec678");
			String args = "-t "+ term + (fieldName.length()> 0 ? "-f "+ fieldName : "");
			lucdeb.executeCommand("cf", args.split(" "), System.out);
		} catch (Exception e) {
			//e.printStackTrace();
			System.out.println("File not found!!");
		}
		System.out.flush();
		System.setOut(old);
		String cf = baos.toString();
		
		return Response.ok(cf)
			      .header("Access-Control-Allow-Origin", "*")
			      .header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, OPTIONS")
			      .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With").build();
	}
}
