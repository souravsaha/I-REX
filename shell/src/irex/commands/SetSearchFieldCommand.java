/*
 * Incomplete.
 */
package irex.commands;

import java.io.IOException;
import java.io.PrintStream;
import irex.IRexObjects;

/**
 *  Set Search Field.
 * @author dwaipayan
 */
public class SetSearchFieldCommand extends Commands {

    public SetSearchFieldCommand(IRexObjects lucivObjects) {
        super(lucivObjects, "searchfield");
    }

    @Override
    public String help() {
        return "searchfield - Sets the field <field-name> to search";
    }

    @Override
    public String usage() {
        return "searchfield <field-name>";
    }

    @Override
    public void execute(String[] args, PrintStream out) throws IOException {

        if(args.length != 1) {
            out.println(usage());
            out.println("Search field set to: " + lucdebObjects.getSearchField());
            return;
        }

        String field = args[0];

        if(lucdebObjects.fields.get(field)==null)
            out.println(field + " not present.");

        else {
            lucdebObjects.setSearchField(field);
            out.println("defaultField for searching set to " + field);
        }
    }

}
