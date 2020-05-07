package haveric.recipeManager.recipes.compost;

import haveric.recipeManager.ErrorReporter;
import haveric.recipeManager.flag.FlagType;
import haveric.recipeManager.flag.Flags;
import haveric.recipeManager.flag.args.ArgBuilder;
import haveric.recipeManager.flag.args.Args;
import haveric.recipeManager.recipes.BaseRecipeParser;
import haveric.recipeManager.recipes.ItemResult;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CompostRecipeParser extends BaseRecipeParser {
    public CompostRecipeParser() {
        super();
    }

    @Override
    public boolean parseRecipe(int directiveLine) {
        CompostRecipe recipe = new CompostRecipe(fileFlags);

        reader.parseFlags(recipe.getFlags()); // parse recipe's flags

        while (!reader.lineIsResult()) {
            // get the ingredient
            String[] splitIngredient = reader.getLine().split("%");

            List<Material> choices = parseIngredient(splitIngredient, recipe.getType());
            if (choices == null || choices.isEmpty()) {
                return false;
            }

            Flags ingredientFlags = new Flags();
            reader.parseFlags(ingredientFlags);

            if (ingredientFlags.hasFlags()) {
                List<ItemStack> items = new ArrayList<>();
                for (Material choice : choices) {
                    Args a = ArgBuilder.create().result(new ItemStack(choice)).build();
                    ingredientFlags.sendCrafted(a, true);

                    items.add(a.result());
                }
                recipe.addIngredientChoiceItems(items);
            } else {
                recipe.addIngredientChoice(choices);
            }

            if (splitIngredient.length > 1) {
                try {
                    double chance = Double.parseDouble(splitIngredient[1].trim());

                    if (chance > 0 && chance <= 100) {
                        recipe.setLevelSuccessChance(chance);
                    } else {
                        ErrorReporter.getInstance().warning("Invalid level success chance: " + splitIngredient[1] + ". Defaulting to 100.", "Allowed values > 0, <= 100 (Decimal values allowed).");
                    }

                } catch (NumberFormatException e) {
                    ErrorReporter.getInstance().warning("Invalid level success chance: " + splitIngredient[1] + ". Defaulting to 100.", "Allowed values > 0, <= 100 (Decimal values allowed).");
                }
            }

            if (splitIngredient.length > 2) {
                try {
                    double levels = Double.parseDouble(splitIngredient[2].trim());

                    if (levels > 0 && levels <= 7) {
                        recipe.setLevels(levels);
                    } else {
                        ErrorReporter.getInstance().warning("Invalid levels: " + splitIngredient[1] + ". Defaulting to 1.", "Allowed values > 0, <= 7 (Decimal values allowed).");
                    }
                } catch (NumberFormatException e) {
                    ErrorReporter.getInstance().warning("Invalid levels: " + splitIngredient[1] + ". Defaulting to 1.", "Allowed values > 0, <= 7 (Decimal values allowed).");
                }

            }
        }

        boolean isRemove = recipe.hasFlag(FlagType.REMOVE);

        // get result or move current line after them if we got @remove and results
        List<ItemResult> results = new ArrayList<>();

        if (isRemove) { // ignore result errors if we have @remove
            ErrorReporter.getInstance().setIgnoreErrors(true);
        }

        boolean hasResults = parseResults(recipe, results);

        if (!hasResults) {
            return false;
        }

        ItemResult result = results.get(0);

        recipe.setResult(result);

        if (isRemove) { // un-ignore result errors
            ErrorReporter.getInstance().setIgnoreErrors(false);
        }

        // check if the recipe already exists
        if (!conditionEvaluator.recipeExists(recipe, directiveLine, reader.getFileName())) {
            return recipe.hasFlag(FlagType.REMOVE);
        }

        if (recipeName != null && !recipeName.isEmpty()) {
            recipe.setName(recipeName); // set recipe's name if defined
        }

        // add the recipe to the Recipes class and to the list for later adding to the server
        recipeRegistrator.queueRecipe(recipe, reader.getFileName());

        return true;
    }
}
