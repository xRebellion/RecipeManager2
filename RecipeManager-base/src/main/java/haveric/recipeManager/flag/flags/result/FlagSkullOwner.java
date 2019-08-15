package haveric.recipeManager.flag.flags.result;

import haveric.recipeManager.ErrorReporter;
import haveric.recipeManager.flag.Flag;
import haveric.recipeManager.flag.FlagType;
import haveric.recipeManager.flag.args.Args;
import haveric.recipeManager.recipes.ItemResult;
import haveric.recipeManager.tools.Version;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class FlagSkullOwner extends Flag {

    @Override
    public String getFlagType() {
        return FlagType.SKULL_OWNER;
    }

    @Override
    protected String[] getArguments() {
        return new String[] {
            "{flag} <name>",
            "{flag} <uuid>",
            "{flag} texture <base64>",
            "{flag} <name> | texture <base64>",
            "{flag} <uuid> | texture <base64>"};
    }

    @Override
    protected String[] getDescription() {
        return new String[] {
            "Sets the human skull's owner to apply the skin.",
            "If you set it to '{player}' then it will use crafter's name.",
            "",
            "For base64 textures, you can reference https://freshcoal.com/, https://minecraft-heads.com/, https://mineskin.org/ or any other Minecraft head repository",
            "  You can only use the base64 encoded string of a valid mojang texture. Each of the above sites should be able to provide those", };
    }

    @Override
    protected String[] getExamples() {
        return new String[] {
            "{flag} Notch",
            "{flag} {player}",
            "{flag} texture eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzBiOGI1ODg5ZWUxYzYzODhkYzZjMmM1ZGJkNzBiNjk4NGFlZmU1NDMxOWEwOTVlNjRkYjc2MzgwOTdiODIxIn19fQ== // Jam texture", };
    }


    private String owner;
    private UUID ownerUUID;
    private String textureBase64;

    public FlagSkullOwner() {
    }

    public FlagSkullOwner(FlagSkullOwner flag) {
        owner = flag.owner;
        ownerUUID = flag.ownerUUID;
        textureBase64 = flag.textureBase64;
    }

    @Override
    public FlagSkullOwner clone() {
        return new FlagSkullOwner((FlagSkullOwner) super.clone());
    }

    public boolean hasOwner() {
        return owner != null;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String newOwner) {
        owner = newOwner;
    }

    public boolean hasOwnerUUID() {
        return ownerUUID != null;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(UUID newOwnerUUID) {
        ownerUUID = newOwnerUUID;
    }

    public String getTextureBase64() {
        return textureBase64;
    }

    public void setTextureBase64(String base64) {
        textureBase64 = base64;
    }

    public boolean hasTextureBase64() {
        return textureBase64 != null;
    }

    @Override
    public boolean onValidate() {
        ItemResult result = getResult();

        if (Version.has1_13Support()) {
            if (result == null || !(result.getItemMeta() instanceof SkullMeta)) {
                return ErrorReporter.getInstance().error("Flag " + getFlagType() + " needs a PLAYER_HEAD");
            }

        } else {
            if (result == null || !(result.getItemMeta() instanceof SkullMeta) || result.getDurability() != 3) {
                return ErrorReporter.getInstance().error("Flag " + getFlagType() + " needs a SKULL_ITEM with data value 3 to work!");
            }
        }

        return true;
    }

    @Override
    public boolean onParse(String value) {
        String[] args = value.split("\\|");

        for (String arg : args) {
            arg = arg.trim();

            if (arg.toLowerCase().startsWith("texture")) {
                String texture = arg.substring("texture".length()).trim();
                setTextureBase64(texture);
            } else {
                String[] components = arg.split("-");
                if (components.length == 5) {
                    setOwnerUUID(UUID.fromString(arg));
                } else {
                    setOwner(arg);
                }
            }
        }

        return true;
    }

    @Override
    public void onPrepare(Args a) {
        onCrafted(a);
    }

    @Override
    public void onCrafted(Args a) {
        if (!a.hasResult()) {
            a.addCustomReason("Needs result!");
            return;
        }

        String owner = null;
        OfflinePlayer offlinePlayer = null;
        if (hasOwner()) {
            if (getOwner().equalsIgnoreCase("{player}")) {
                if (!a.hasPlayerUUID()) {
                    a.addCustomReason("Needs player UUID!");
                    return;
                }

                offlinePlayer = Bukkit.getOfflinePlayer(a.playerUUID());
            } else {
                owner = getOwner();
            }
        } else if (hasOwnerUUID()) {
            offlinePlayer = Bukkit.getOfflinePlayer(getOwnerUUID());
        }

        String name = "";
        UUID uuid = null;
        String id = "";
        String texture = "";

        if (hasTextureBase64()) {
            texture = "Properties:{textures:[{Value:\"" + textureBase64 + "\"}]}";
            uuid = new UUID(textureBase64.hashCode(), textureBase64.hashCode());
        }

        if (offlinePlayer != null) {
            uuid = offlinePlayer.getUniqueId();
        }

        if (uuid != null) {
            id = "Id:\"" + uuid + "\",";
        }

        if (owner != null) {
            name = "Name:\"" + owner + "\",";
        }

        addNBTRaw(a, "{SkullOwner:{" + id + name + texture + "}}");
    }
}
