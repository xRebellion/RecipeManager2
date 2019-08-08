package haveric.recipeManager.flag.flags.any;

import haveric.recipeManager.flag.Flag;
import haveric.recipeManager.flag.FlagType;
import haveric.recipeManager.flag.args.Args;
import haveric.recipeManagerCommon.util.RMCUtil;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.List;

public class FlagMessage extends Flag {

    @Override
    public String getFlagType() {
        return FlagType.MESSAGE;
    }

    @Override
    protected String[] getArguments() {
        return new String[] {
            "{flag} <text>", };
    }

    @Override
    protected String[] getDescription() {
        return new String[] {
            "Prints a message when recipe or item is successfully crafted.",
            "This flag can be used more than once to add more messages.",
            "",
            "Colors are supported (<red>, &5, etc).",
            "You can also use these variables:",
            "  {player}         = crafter's name or '(nobody)' if not available",
            "  {playerdisplay}  = crafter's display name or '(nobody)' if not available",
            "  {result}         = the result item name or '(nothing)' if recipe failed.",
            "  {recipename}     = recipe's custom or autogenerated name or '(unknown)' if not available",
            "  {recipetype}     = recipe type or '(unknown)' if not available",
            "  {inventorytype}  = inventory type or '(unknown)' if not available",
            "  {world}          = world name of event location or '(unknown)' if not available",
            "  {x}              = event location's X coord or '(?)' if not available",
            "  {y}              = event location's Y coord or '(?)' if not available",
            "  {z}              = event location's Z coord or '(?)' if not available",
            "    Relative positions are supported: {x-1},{y+7},{z+12}",
            "  {rand #1-#2}     = output a random integer between #1 and #2. Example: {rand 5-10} will output an integer from 5-10",
            "  {rand #1-#2, #3} = output a random number between #1 and #2, with decimal places of #3. Example: {rand 1.5-2.5, 2} will output a number from 1.50 to 2.50",
            "  {rand n}         = reuse a random output, where n is the nth {rand} used excluding this format",
            "",
            "Allows quotes to prevent spaces being trimmed.", };
    }

    @Override
    protected String[] getExamples() {
        return new String[] {
            "{flag} <green>Good job!",
            "{flag} <gray>Now you can die&c happy<gray> that you crafted that.",
            "{flag} \"  Extra space  \" // Quotes at the beginning and end will be removed, but spaces will be kept.", };
    }


    private List<String> messages = new ArrayList<>();

    public FlagMessage() {
    }

    public FlagMessage(FlagMessage flag) {
        messages.addAll(flag.messages);
    }

    @Override
    public FlagMessage clone() {
        return new FlagMessage((FlagMessage) super.clone());
    }

    public List<String> getMessages() {
        return messages;
    }

    /**
     * Set the message list.
     *
     * @param newMessages
     */
    public void setMessages(List<String> newMessages) {
        if (newMessages == null) {
            remove();
        } else {
            messages = newMessages;
        }
    }

    /**
     * Set the message.<br>
     * Supports parsable color tags and codes.<br>
     * You can use null, "false" or "remove" to remove the entire flag.
     *
     * @param message
     */
    public void addMessage(String message) {
        if (message == null || message.equalsIgnoreCase("false") || message.equalsIgnoreCase("remove")) {
            remove();
        } else {
            if (messages == null) {
                messages = new ArrayList<>();
            }

            messages.add(message);
        }
    }

    @Override
    public boolean onParse(String value) {
        value = RMCUtil.trimExactQuotes(value);

        addMessage(value);
        return true;
    }

    @Override
    public void onCrafted(Args a) {
        Validate.notNull(messages);

        for (String s : messages) {
            a.addCustomEffect(a.parseVariables(s));
        }
    }
}
