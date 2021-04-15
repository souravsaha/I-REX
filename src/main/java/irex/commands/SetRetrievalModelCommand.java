package irex.commands;

import java.io.IOException;
import java.io.PrintStream;

//import org.apache.lucene.search.TopScoreDocCollector;

import irex.IRexObjects;
import org.apache.commons.cli.*;

/**
 * 
 * @author souravsaha
 *
 */
public class SetRetrievalModelCommand extends Commands{

	public SetRetrievalModelCommand(IRexObjects irexObject) {
		super(irexObject, "setRetModel");
	}
    @Override
    public String help() {
        return "Set the retrieval model online";
    }
    @Override
    public String usage() {
        return "setRetrieval <-m Models> <-p Params...> ";
    }
    @Override
    public void execute(String[] args, PrintStream out) throws IOException {

    	// tokenize 
    	// call to my setRetrieval method
    	
    	Options options = new Options();
    	Option modelOption = new Option("m", "model", true, "Model name");
    	modelOption.setRequired(true);
    	options.addOption(modelOption);
    	
    	Option paramOption = new Option("p", "parameter", true, "List of parameters");
    	paramOption.setRequired(true);
    	options.addOption(paramOption);
    	
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
        String modelName = cmd.getOptionValue("model");
        String allParams = cmd.getOptionValue("parameter");

        out.println(modelName);
        out.println(allParams);
        
        String param1="", param2= "", param3 = "";
        String[] params = allParams.split(" ");
        switch(modelName) {
	        case "lmjm":
	            param1 = params[0];
	            break;
	        case "lmdir":
	            param1 = params[0];
	            break;
	        case "bm25":
	            param1 = params[0];
	            param2 = params[1];
	            break;
	        case "dfr":
	            param1 = params[0];
	            param2 = params[1];
	            param3 = params[2];
	            break;
	        default:
	            // TODO
	            break;
        }
        irexObjects.setRetreivalParameter(modelName,param1, param2, param3);
    }
}
