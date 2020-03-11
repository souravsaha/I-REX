// TODO: change 'content' to something more generic
package irex;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import irex.commands.Commands;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author dwaipayan
 */
public class IRex {

    String          indexPath;
    File            indexFile;
    Boolean         boolIndexExists;
    IndexReader     indexReader;
    IRexObjects     irexObjects;

    public IRex(String indexPath) throws IOException, Exception {
        irexObjects = new IRexObjects(indexPath);
    }

    public void openIndex(String indexPath) throws IOException {

        // +++ index path setting 
        this.indexPath = indexPath;
        indexFile = new File(indexPath);
        Directory indexDir = FSDirectory.open(indexFile.toPath());

        if (!DirectoryReader.indexExists(indexDir)) {
            System.err.println("Index doesn't exists in "+indexPath);
            boolIndexExists = false;
            System.exit(1);
        }
        // --- index path set

        /* setting IndexReader */
        indexReader = DirectoryReader.open(FSDirectory.open(indexFile.toPath()));

        System.out.println("Index opened successfully.");
    }

    public static void usage() {
        String usage = "java LucIV <index-path>";
        System.out.println(usage);
    }

    public void runLucIV() throws IOException {

        while(true) {
            String command = irexObjects.readCommand();
            if (command == null || command.isEmpty()) 
                continue;
            command = command.trim();
            // + puts the command line argument from IRex command line to appropriate arrays
            List<String> tokens = new ArrayList<>();
            StringBuilder sb = new StringBuilder();

            boolean insideQuote = false;

            for (char c : command.toCharArray()) {

                if (c == '"')
                    insideQuote = !insideQuote;

                if (c == ' ' && !insideQuote) {//when space is not inside quote split..
                    tokens.add(sb.toString().replace("\"", "")); //token is ready, lets add it to list
                    sb.delete(0, sb.length()); //and reset StringBuilder`s content
                } else 
                    sb.append(c);//else add character to token
            }
            //lets not forget about last token that doesn't have space after it
            tokens.add(sb.toString().replace("\"", ""));

            String[] parts=tokens.toArray(new String[0]);
            // - puts the command line argument from IRex command line to appropriate arrays

            if (parts.length > 0){
                String cmd = parts[0];
                String[] cmdArgs = new String[parts.length - 1];
                System.arraycopy(parts, 1, cmdArgs, 0, cmdArgs.length);
                executeCommand(cmd, cmdArgs, System.out);
            }

        }
    }

    public void executeCommand(String cmdName, String[] args, PrintStream out) throws IOException {
        Commands cmd = irexObjects.getCommand(cmdName);

        if(null == cmd) {
            out.println(cmdName + ": Command not found");
            return;
        }

        cmd.execute(args, out);
    }

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException, Exception {

        IRex irex;

        if(args.length != 1) {
            usage();

            // for test running
            args = new String[1];
            args[0] = "/store/trec123";
//            args[0] = "/store/collections/indexed/trec678";
            irex = new IRex(args[0]);
            irex.executeCommand("tf", "--luceneDocId 1 -q europ".split(" "), System.out);
//            irex.executeCommand("dump", "--luceneDocId 1".split(" "), System.out);
            irex.executeCommand("dump", "--docName WSJ880829-0084".split(" "), System.out);
//            irex.executeCommand("explain2", "xyzzzz young group company FT922-7489".split(" "), System.out);
//            irex.executeCommand("explain", "\"xyzzzz young group company\" \"FT922-7489\"".split(" "), System.out);
//            irex.executeCommand("stats", "".split(" "), System.out);
//            irex.executeCommand("stats", "".split(" "), System.out);
//            irex.executeCommand("vocab", "".split(" "), System.out);
            irex.executeCommand("rank", "-i 211077 -q \"  -r lmdir:500".split(" "), System.out);
//            irex.executeCommand("dv", "-i 1 -d 5 -r \"lmdir 1000\"".split(" "), System.out);
//            irex.executeCommand("rank", "What is a Bengals cat\tWTX095-B05-124 WTX095-B05-119\tlmjm 0.4\tbm25 0.2 0.75".split("\t"), System.out);
            System.exit(0);
        }

        irex = new IRex(args[0]);

        irex.runLucIV();

//        irex.executeCommand("stats", "".split(" "), System.out);

//        irex.executeCommand("man", "docid".split(" "), System.out);
//        irex.executeCommand("help", "".split(" "), System.out);
//        irex.executeCommand("setfield", "content".split(" "), System.out);
//        irex.executeCommand("explain", "xyzzzz young group company FT922-7489".split(" "), System.out);
//        irex.executeCommand("man", "stats".split(" "), System.out);
//        irex.executeCommand("dv", "content 216038".split(" "), System.out);
//        irex.executeCommand("dvn", "content WTX001-B06-78".split(" "), System.out);
//        irex.executeCommand("dvn", "content FT922-7489".split(" "), System.out);
// explain "international organized crime" "FBIS4-46734 FBIS3-24145" lmdir 1000
// docterm "FBIS4-46734 FBIS3-24145" "lmdir 1000"
// rank "What is a Bengals cat" "WTX095-B05-124 WTX095-B05-119" "lmjm 0.4" "bm25 0.2 0.75"
    }
    
}
