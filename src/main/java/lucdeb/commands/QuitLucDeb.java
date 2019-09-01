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
public class QuitLucDeb extends Commands {

    public QuitLucDeb(LucDebObjects lucivObjects) {
        super(lucivObjects, "quit");
    }

    @Override
    public String help() {
        return "Quit LucIV.";
    }

    @Override
    public String usage() {
        return "quit";
    }

    @Override
    public void execute(String[] args, PrintStream out) throws IOException {

        out.flush();
        lucdebObjects.closeAll();
        System.exit(0);
    }
    
}
