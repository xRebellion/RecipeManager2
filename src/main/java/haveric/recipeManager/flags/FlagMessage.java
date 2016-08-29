package haveric.recipeManager.flags;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

public class FlagMessage extends Flag {

    @Override
    protected String getFlagType() {
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
            "The message can also contain variables:",
            "  {player}         = crafter's name or '(nobody)' if not available",
            "  {playerdisplay}  = crafter's display name or '(nobody)' if not available",
            "  {result}         = the result item name or '(nothing)' if recipe failed.",
            "  {recipename}     = recipe's custom or autogenerated name or '(unknown)' if not available",
            "  {recipetype}     = recipe type or '(unknown)' if not available",
            "  {inventorytype}  = inventory type or '(unknown)' if not available",
            "  {world}          = world name of event location or '(unknown)' if not available",
            "  {x}              = event location's X coord or '(?)' if not available",
            "  {y}              = event location's Y coord or '(?)' if not available",
            "  {z}              = event location's Z coord or '(?)' if not available", };
    }

    @Override
    protected String[] getExamples() {
        return new String[] {
            "{flag} <green>Good job!",
            "{flag} <gray>Now you can die&c happy<gray> that you crafted that.", };
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
     * @param messages
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
    protected boolean onParse(String value) {
        addMessage(value);
        return true;
    }

    @Override
    protected void onCrafted(Args a) {
        Validate.notNull(messages);

        for (String s : messages) {
            a.addCustomEffect(a.parseVariables(s));
        }
    }
}
