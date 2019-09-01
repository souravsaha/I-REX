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
 * Manual of the commands defined.
 * @author dwaipayan
 */
public class Man extends Commands {

    public Man(LucDebObjects lucivObjects) {
        super(lucivObjects, "man");
    }

    @Override
    public String help() {
        return "Prints all the commands with their utility";
    }

    @Override
    public void execute(String[] args, PrintStream out) throws IOException {

        String cmdName = args[0];   // consider the first argument as the command to find the manual of.

        Commands cmd = lucdebObjects.getCommand(cmdName);

        if(null == cmd) {
            out.println(cmdName + ": Command not found");
            return;
        }
        out.println(cmd.help());
        out.println(cmd.usage());
    }

    @Override
    public String usage() {
        return "man <command-name>";
    }
    
}
