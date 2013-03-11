package ro.thehunters.digi.recipeManager.flags;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import ro.thehunters.digi.recipeManager.RecipeErrorReporter;
import ro.thehunters.digi.recipeManager.flags.FlagType.Bit;

public class Flags implements Cloneable
{
    private Map<FlagType, Flag> flags = new HashMap<FlagType, Flag>();
    protected Flaggable         flaggable;
    
    public Flags()
    {
    }
    
    public Flags(Flaggable flaggable)
    {
        this.flaggable = flaggable;
    }
    
    public boolean hasFlag(Class<? extends Flag> flagClass)
    {
        return flags.containsKey(flagClass);
    }
    
    /**
     * Gets a flag by its type.<br>
     * For automated casting you should use {@link #getFlag(Class)}
     * 
     * @param type
     * @return Flag object
     */
    public Flag getFlag(FlagType type)
    {
        return flags.get(type);
    }
    
    /**
     * Gets a flag by its class name.<br>
     * This is useful for easy auto-casting, example:<br>
     * <br>
     * <code>FlagCommands flag = flags.getFlag(FlagCommands.class);</code>
     * 
     * @param flagClass
     *            the class of the flag
     * @return Flag object
     */
    public <T extends Flag>T getFlag(Class<T> flagClass)
    {
        return flagClass.cast(flags.get(FlagType.getByClass(flagClass)));
    }
    
    /**
     * Checks if flag exists in this flag list.
     * 
     * @param type
     * @return
     */
    public boolean hasFlag(FlagType type)
    {
        return flags.containsKey(type);
    }
    
    /**
     * Checks if the flag can be added to this flag list.<br>
     * 
     * @param flag
     * @return false if flag can only be added on specific flaggables
     */
    public boolean canAdd(Flag flag)
    {
        return flag != null && flag.validate() && !flag.getType().hasBit(Bit.NO_STORE);
    }
    
    /**
     * Attempts to add a flag to this flag list.<br>
     * Throws an error in the console if flag is not compatible with recipe/result
     * 
     * @param flag
     */
    public void addFlag(Flag flag)
    {
        if(canAdd(flag))
        {
            flag.flagsContainer = this;
            flags.put(flag.getType(), flag);
        }
    }
    
    /**
     * Parses a string to create/get a flag and add to/update the list.<br>
     * This is used by RecipeManager's file processor.
     * 
     * @param string
     *            must not be null and should contain a flag expression like the ones in recipe files
     * @param recipeType
     *            can be null
     * @param item
     *            can be null
     */
    public void parseFlag(String string)
    {
        String[] split = string.split(" ", 2);
        String flagString = split[0].substring(1).trim().toLowerCase();
        
        if(flagString.isEmpty())
        {
            RecipeErrorReporter.warning("Flag name empty: " + string);
            return;
        }
        
        // check for : character at the end of string and remove it
        int len = flagString.length() - 1;
        
        if(flagString.charAt(len) == ':')
            flagString = flagString.substring(0, len);
        
        // Find the current flag
        FlagType type = FlagType.getByName(flagString);
        
        // If no valid flag was found
        if(type == null)
        {
            RecipeErrorReporter.warning("Unknown flag: @" + flagString);
            return;
        }
        
        Flag flag = flags.get(type); // get existing flag, if any
        
        // create a new instance of the flag does not exist
        if(flag == null)
            flag = type.createFlagClass();
        
        flag.flagsContainer = this; // set container before hand to allow checks
        
        String value = (split.length > 1 ? split[1].trim() : null);
        
        // make sure the flag can be added to this flag list
        if(!flag.validateParse(value))
            return;
        
        // check if parsed flag had valid values and needs to be added to flag list
        if(flag.onParse(value) && !flag.getType().hasBit(Bit.NO_STORE))
        {
            flags.put(flag.getType(), flag);
        }
    }
    
    /**
     * Removes the specified flag from this flag list.<br>
     * Alias for {@link #removeFlag(FlagType)}
     * 
     * @param flag
     */
    public void removeFlag(Flag flag)
    {
        if(flag == null)
            return;
        
        removeFlag(flag.getType());
    }
    
    /**
     * Removes the specified flag type from this flag list
     * 
     * @param type
     */
    public void removeFlag(FlagType type)
    {
        if(type == null)
            return;
        
        Flag flag = flags.remove(type);
        
        if(flag != null)
        {
            flag.onRemove();
            flag.flagsContainer = null;
        }
    }
    
    /**
     * Gets the Recipe or ItemResult that uses this flag list.<br>
     * You must check and cast accordingly.
     * 
     * @return Flaggable object or null if undefined
     */
    public Flaggable getFlaggable()
    {
        return flaggable;
    }
    
    /**
     * Checks all flags and compiles a list of failure reasons while returning if the list is empty (no errors).
     * Note: not all arguments are used, you may use null wherever you don't have anything to give.
     * 
     * @param a
     *            arguments class
     * @return true if recipe/result can be crafted by the arguments with the current flags
     */
    public boolean checkFlags(Arguments a)
    {
        Player p = a.player();
        
        if(p != null && p.hasPermission("recipemanager.noflag.*"))
        {
            return true;
        }
        
        FlagLoop: for(Flag flag : flags.values())
        {
            if(p != null)
            {
                for(String name : flag.getType().getNames())
                {
                    if(p.hasPermission("recipemanager.noflag." + name))
                    {
                        continue FlagLoop;
                    }
                }
            }
            
            flag.check(a);
        }
        
        return !a.hasReasons();
    }
    
    /**
     * Applies all flags to player/location/result and compiles a list of failure reasons while returning if the list is empty (no errors).
     * Note: not all arguments are used, you may use null wherever you don't have anything to give.
     * 
     * @param a
     *            arguments class
     * @return false if something was absolutely required and crafting should be cancelled
     */
    public boolean applyFlags(Arguments a)
    {
        for(Flag flag : flags.values())
        {
            flag.apply(a);
        }
        
        return !a.hasReasons();
    }
    
    /**
     * Sends failure notification to all flags
     * 
     * @param a
     *            arguments class
     */
    public void sendFailed(Arguments a)
    {
        for(Flag flag : flags.values())
        {
            flag.failed(a);
        }
    }
    
    /**
     * Copy this flag storage and give it a new container.<br>
     * 
     * @param newContainer
     * @return
     */
    public Flags clone(Flaggable newContainer)
    {
        Flags clone = clone();
        clone.flaggable = newContainer;
        
        return clone;
    }
    
    public Flags clone()
    {
        Flags clone = new Flags();
        
        for(Flag f : flags.values())
        {
            f = f.clone();
            f.flagsContainer = clone;
            clone.flags.put(f.getType(), f);
        }
        
        return clone;
    }
}