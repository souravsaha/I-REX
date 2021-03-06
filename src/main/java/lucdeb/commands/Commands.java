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
public abstract class Commands {

    String CMD_NAME;
    protected LucDebObjects lucdebObjects;

    public Commands (LucDebObjects lucivObjects, String cmdName) {
        CMD_NAME = cmdName;
        this.lucdebObjects = lucivObjects;
        this.lucdebObjects.registerCommand(this);
    }

    /**
     * Returns the name of the command.
     * @return Command name.
     */
    public String getName() {
        return CMD_NAME;
    }
    public abstract String help();
    public abstract String usage();

    /**
     * The execution of the actual command.
     * @param args
     * @param out
     * @throws IOException
     */
    public abstract void execute(String[] args, PrintStream out) throws IOException ;


}
