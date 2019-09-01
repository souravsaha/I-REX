/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lucdeb.commands;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;
import lucdeb.LucDebObjects;

/**
 *
 * @author dwaipayan
 */
public class HelpCommand extends Commands{

    public HelpCommand(LucDebObjects lucivObjects) {
        super(lucivObjects, "help");
    }
    @Override
    public String help() {
        return "Prints all the commands with their utility";
    }

    @Override
    public void execute(String[] args, PrintStream out) throws IOException {

        Map<String, Commands> cmdMap = lucdebObjects.getCommandMap();
        Collection<Commands> allCommands = cmdMap.values();

        for (Commands cmd : allCommands) {
            out.println(cmd.getName() + " - " + cmd.help());
        }

    }

    @Override
    public String usage() {
        return "help";
    }
    
}
