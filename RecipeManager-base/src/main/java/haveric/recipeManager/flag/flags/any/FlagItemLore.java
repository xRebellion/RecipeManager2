package haveric.recipeManager.flag.flags.any;

import haveric.recipeManager.ErrorReporter;
import haveric.recipeManager.flag.Flag;
import haveric.recipeManager.flag.FlagType;
import haveric.recipeManager.flag.args.Args;
import haveric.recipeManagerCommon.util.RMCUtil;
import org.apache.commons.lang.Validate;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class FlagItemLore extends Flag {

    @Override
    public String getFlagType() {
        return FlagType.ITEM_LORE;
    }

    @Override
    protected String[] getArguments() {
        return new String[] {
            "{flag} <text>",
            "{flag} <text> | display",
            "{flag} <text> | result", };
    }

    @Override
    protected String[] getDescription() {
        return new String[] {
            "Adds a line to result's lore (description)",
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
            "  {rand n}         = reuse a random output, where n is the nth {rand} used excluding this format",
            "",
            "Allows quotes to prevent spaces being trimmed.",
            "",
            "Optional Arguments:",
            "  display          = only show on the displayed item when preparing to craft (only relevant to craft/combine recipes)",
            "  result           = only show on the result, but hide from the prepared result",
            "    Default behavior with neither of these arguments is to display in both locations", };
    }

    @Override
    protected String[] getExamples() {
        return new String[] {
            "{flag} <red>Awesome item",
            "{flag} <magic>some scrambled text on line 2",
            "{flag} <gray>Crafted at {world}:{x},{y},{z}",
            "{flag} \"  Extra space  \" // Quotes at the beginning and end will be removed, but spaces will be kept.", };
    }


    private List<String> displayLores = new ArrayList<>();
    private List<String> resultLores = new ArrayList<>();

    public FlagItemLore() {
    }

    public FlagItemLore(FlagItemLore flag) {
        displayLores.addAll(flag.displayLores);
        resultLores.addAll(flag.resultLores);
    }

    @Override
    public FlagItemLore clone() {
        return new FlagItemLore((FlagItemLore) super.clone());
    }

    public List<String> getDisplayLores() {
        return displayLores;
    }

    public void setDisplayLores(List<String> newLores) {
        Validate.notNull(newLores, "The 'lore' argument must not be null!");

        displayLores.clear();

        for (String value : newLores) {
            addDisplayLore(value);
        }
    }

    public void addDisplayLore(String value) {
        displayLores.add(RMCUtil.parseColors(value, false));
    }

    public List<String> getResultLores() {
        return resultLores;
    }

    public void setResultLores(List<String> newLores) {
        Validate.notNull(newLores, "The 'lore' argument must not be null!");

        resultLores.clear();

        for (String value : newLores) {
            addResultLore(value);
        }
    }

    public void addResultLore(String value) {
        resultLores.add(RMCUtil.parseColors(value, false));
    }

    public void addBothLore(String value) {
        String parsed = RMCUtil.parseColors(value, false);
        displayLores.add(parsed);
        resultLores.add(parsed);
    }

    @Override
    public boolean onParse(String value) {
        if (value == null) {
            addBothLore(""); // convert empty flag to blank line
        } else {
            // Match on single pipes '|', but not double '||'
            String[] args = value.split("(?<!\\|)\\|(?!\\|)");
            String lore = args[0];

            // Replace double pipes with single pipe: || -> |
            lore = lore.replaceAll("\\|\\|", "|");
            lore = RMCUtil.trimExactQuotes(lore);

            if (args.length > 1) {
                String display = args[1].trim().toLowerCase();
                if (display.equals("display")) {
                    addDisplayLore(lore);
                } else if (display.equals("result")) {
                    addResultLore(lore);
                } else {
                    ErrorReporter.getInstance().warning("Flag " + getFlagType() + " has invalid argument: " + args[1] + ". Defaulting to set lore in both locations.");
                    addBothLore(lore);
                }
            } else {
                addBothLore(lore);
            }
        }

        return true;
    }

    @Override
    public void onPrepare(Args a) {
        if (!a.hasResult()) {
            a.addCustomReason("Need result!");
            return;
        }

        ItemMeta meta = a.result().getItemMeta();
        if (meta != null) {
            List<String> newLore = meta.getLore();

            if (newLore == null) {
                newLore = new ArrayList<>();
            }

            for (String line : displayLores) {
                newLore.add(a.parseVariables(line, true));
            }

            meta.setLore(newLore);

            a.result().setItemMeta(meta);
        }
    }

    @Override
    public void onCrafted(Args a) {
        if (!a.hasResult()) {
            a.addCustomReason("Need result!");
            return;
        }

        ItemMeta meta = a.result().getItemMeta();
        if (meta != null) {
            List<String> newLore = meta.getLore();

            if (newLore == null) {
                newLore = new ArrayList<>();
            }

            for (String line : resultLores) {
                newLore.add(a.parseVariables(line));
            }

            meta.setLore(newLore);

            a.result().setItemMeta(meta);
        }
    }
}
