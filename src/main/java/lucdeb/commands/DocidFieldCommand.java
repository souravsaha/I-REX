/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lucdeb.commands;

import java.io.IOException;
import java.io.PrintStream;
import lucdeb.LucDebObjects;

/**
 *
 * @author dwaipayan
 */
public class DocidFieldCommand extends Commands {

    public DocidFieldCommand(LucDebObjects lucivObjects) {
        super(lucivObjects, "docid");
    }

    @Override
    public String help() {
        return "Set unique docid of the index";
    }

    @Override
    public String usage() {
        return "docid <field-name>";
    }

    @Override
    public void execute(String[] args, PrintStream out) throws IOException {

        if(args.length != 1) {
            out.println(usage());
            return;
        }

        String field = args[0];

        if(lucdebObjects.fields.get(field)==null)
            out.println("Error setting docid field." + field + " not present.");

        else {
            lucdebObjects.setDocidField(field);
            out.println("unique docid field set to " + field);
        }
    }
    
}
