package haveric.recipeManager.flag.flags.any;

import haveric.recipeManager.flag.Flag;
import haveric.recipeManager.flag.FlagType;
import haveric.recipeManager.flag.args.Args;
import haveric.recipeManagerCommon.util.RMCUtil;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;

public class FlagBroadcast extends Flag {

    @Override
    public String getFlagType() {
        return FlagType.BROADCAST;
    }

    @Override
    protected String[] getArguments() {
        return new String[] {
            "{flag} <text> | [permission]", };
    }

    @Override
    protected String[] getDescription() {
        return new String[] {
            "Prints a chat message for all online players.",
            "Using this flag more than once will overwrite the previous message.",
            "",
            "Optionally you can set a permission node that will define who sees the message.",
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

    protected String[] getExamples() {
        return new String[] {
            "{flag} {playerdisplay} <green>crafted something!",
            "{flag} '{player}' crafted '{recipename}' at {world}: {x}, {y}, {z} | ranks.admins",
            "{flag} \"  Extra space  \" // Quotes at the beginning and end will be removed, but spaces will be kept.", };
    }


    private String message;
    private String permission;

    public FlagBroadcast() {
    }

    public FlagBroadcast(FlagBroadcast flag) {
        message = flag.message;
        permission = flag.permission;
    }

    @Override
    public FlagBroadcast clone() {
        return new FlagBroadcast((FlagBroadcast) super.clone());
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String newMessage) {
        message = newMessage;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String newPermission) {
        permission = newPermission;
    }

    @Override
    public boolean onParse(String value) {
        String[] split = value.split("\\|", 2);

        String message = RMCUtil.trimExactQuotes(split[0]);
        setMessage(message);
        setPermission(null);

        if (split.length > 1) {
            setPermission(split[1].trim().toLowerCase());
        }

        return true;
    }

    @Override
    public void onCrafted(Args a) {
        Validate.notNull(message);

        String parsedMessage = RMCUtil.parseColors(a.parseVariables(message), false);

        if (permission == null) {
            Bukkit.broadcastMessage(parsedMessage);
        } else {
            Bukkit.broadcast(parsedMessage, permission);
        }
    }
}
