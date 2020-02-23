package haveric.recipeManager.flag.flags.result;

import haveric.recipeManager.flag.Flag;
import haveric.recipeManager.flag.FlagType;
import haveric.recipeManager.flag.args.Args;
import haveric.recipeManager.common.util.RMCUtil;
import org.bukkit.inventory.meta.ItemMeta;

public class FlagLocalizedName extends Flag {

    @Override
    public String getFlagType() {
        return FlagType.LOCALIZED_NAME;
    }

    @Override
    protected String[] getArguments() {
        return new String[] {
            "{flag} <text or false>", };
    }

    @Override
    protected String[] getDescription() {
        return new String[] {
            "Changes result's localized name.",
            "",
            "Supports colors (e.g. <red>, <blue>, &4, &F, etc).",
            "",
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
            "",
            "Allows quotes to prevent spaces being trimmed.", };
    }

    @Override
    protected String[] getExamples() {
        return new String[] {
            "{flag} <light_purple>Weird Item",
            "{flag} <yellow>{player}'s Sword",
            "{flag} \"  Extra space  \" // Quotes at the beginning and end will be removed, but spaces will be kept.", };
    }


    private String name;

    public FlagLocalizedName() {
    }

    public FlagLocalizedName(FlagLocalizedName flag) {
        name = flag.name;
    }

    @Override
    public FlagLocalizedName clone() {
        return new FlagLocalizedName((FlagLocalizedName) super.clone());
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        name = newName;
    }

    @Override
    public boolean onParse(String value, String fileName, int lineNum) {
        super.onParse(value, fileName, lineNum);
        setName(RMCUtil.trimExactQuotes(value));
        return true;
    }

    @Override
    public void onPrepare(Args a) {
        onCrafted(a);
    }

    @Override
    public void onCrafted(Args a) {
        if (canAddMeta(a)) {
            ItemMeta meta = a.result().getItemMeta();
            if (meta != null && getName() != null) {
                String localizedName = RMCUtil.parseColors(a.parseVariables(getName()), false);
                meta.setLocalizedName(localizedName);

                a.result().setItemMeta(meta);
            }
        }
    }

    @Override
    public int hashCode() {
        String toHash = "" + super.hashCode();

        toHash += "name: " + name;

        return toHash.hashCode();
    }
}
