/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irex.commands;

import java.io.IOException;
import java.io.PrintStream;
import irex.IRexObjects;

/**
 *
 * @author dwaipayan
 */
public class DocidFieldCommand extends Commands {

    public DocidFieldCommand(IRexObjects lucivObjects) {
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

        if(irexObjects.fields.get(field)==null)
            out.println("Error setting docid field." + field + " not present.");

        else {
            irexObjects.setDocidField(field);
            out.println("unique docid field set to " + field);
        }
    }
    
}
