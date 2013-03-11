package ro.thehunters.digi.recipeManager.recipes;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import ro.thehunters.digi.recipeManager.flags.Arguments;
import ro.thehunters.digi.recipeManager.flags.Flag;
import ro.thehunters.digi.recipeManager.flags.FlagType;
import ro.thehunters.digi.recipeManager.flags.Flaggable;
import ro.thehunters.digi.recipeManager.flags.Flags;

public class ItemResult extends ItemStack implements Flaggable
{
    private Flags      flags;
    private float      chance = 100;
    private BaseRecipe recipe;
    
    public ItemResult()
    {
    }
    
    public ItemResult(ItemStack item)
    {
        super(item);
    }
    
    public ItemResult(ItemResult result)
    {
        super((ItemStack)result);
        
        flags = result.hasFlags() ? result.getFlags().clone(this) : null;
        chance = result.chance;
        recipe = result.recipe; // don't clone, just a pointer
    }
    
    public ItemResult(ItemStack item, float chance)
    {
        super(item);
        
        setChance(chance);
    }
    
    public ItemResult(Material type, int amount, int data, float chance)
    {
        super(type, amount, (short)data);
        
        setChance(chance);
    }
    
    public ItemResult(ItemStack item, Flags flags)
    {
        super(item);
        
        this.flags = flags.clone(this);
    }
    
    public void setItemStack(ItemStack item)
    {
        setTypeId(item.getTypeId());
        setDurability(item.getDurability());
        setAmount(item.getAmount());
        setItemMeta(item.getItemMeta());
    }
    
    public boolean checkFlags(Arguments a)
    {
        return (flags == null ? true : flags.checkFlags(a));
    }
    
    public boolean applyFlags(Arguments a)
    {
        return (flags == null ? true : flags.applyFlags(a));
    }
    
    public void setChance(float chance)
    {
        this.chance = chance;
    }
    
    public float getChance()
    {
        return chance;
    }
    
    public BaseRecipe getRecipe()
    {
        return recipe;
    }
    
    public ItemResult setRecipe(BaseRecipe recipe)
    {
        this.recipe = recipe;
        return this;
    }
    
    // From Flaggable interface
    
    @Override
    public boolean hasFlag(FlagType type)
    {
        return (flags == null ? false : flags.hasFlag(type));
    }
    
    @Override
    public boolean hasFlags()
    {
        return (flags != null);
    }
    
    @Override
    public Flag getFlag(FlagType type)
    {
        return flags.getFlag(type);
    }
    
    @Override
    public <T extends Flag>T getFlag(Class<T> flagClass)
    {
        return flags.getFlag(flagClass);
    }
    
    @Override
    public Flags getFlags()
    {
        if(flags == null)
            flags = new Flags(this);
        
        return flags;
    }
    
    @Override
    public void addFlag(Flag flag)
    {
        flags.addFlag(flag);
    }
}