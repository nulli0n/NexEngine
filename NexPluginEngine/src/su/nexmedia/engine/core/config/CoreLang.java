package su.nexmedia.engine.core.config;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.config.api.ILangMsg;
import su.nexmedia.engine.config.api.ILangTemplate;
import su.nexmedia.engine.utils.StringUT;

public class CoreLang extends ILangTemplate {

    public CoreLang(@NotNull NexPlugin<?> plugin) {
        super(plugin, plugin.getConfigManager().configLang, plugin.isEngine() ? null : NexEngine.get().lang());
    }

    public CoreLang(@NotNull NexPlugin<?> plugin, @Nullable ILangTemplate parent) {
        super(plugin, plugin.getConfigManager().configLang, parent);
    }

    @Override
    protected void setupEnums() {
        this.setupEnum(EntityType.class);
        this.setupEnum(Material.class);

        for (PotionEffectType type : PotionEffectType.values()) {
            this.config.addMissing("PotionEffectType." + type.getName(),
                    StringUT.capitalizeFully(type.getName().replace("_", " ")));
        }

        for (Enchantment e : Enchantment.values()) {
            this.config.addMissing("Enchantment." + e.getKey().getKey(),
                    StringUT.capitalizeFully(e.getKey().getKey().replace("_", " ")));
        }
    }

    @NotNull
    public String getPotionType(@NotNull PotionEffectType type) {
        if (!this.plugin.isEngine())
            return NexEngine.get().lang().getPotionType(type);
        return this.config.getString("PotionEffectType." + type.getName(), type.getName());
    }

    @NotNull
    public String getEnchantment(@NotNull Enchantment e) {
        if (!this.plugin.isEngine())
            return NexEngine.get().lang().getEnchantment(e);
        this.config.addMissing("Enchantment." + e.getKey().getKey(), StringUT.capitalizeFully(e.getKey().getKey()));
        this.config.saveChanges();

        return this.config.getString("Enchantment." + e.getKey().getKey(), e.getKey().getKey().replace("_", " "));
    }

    public ILangMsg Prefix = new ILangMsg(this, "&e%plugin% &8» &7");

    public ILangMsg Core_Command_Usage = new ILangMsg(this, "&c* Usage: &f/%label% %cmd% %usage%");
    public ILangMsg Core_Command_Help_Format = new ILangMsg(this, "&6» &e/%label% %cmd% %usage% &7- %description%");
    public ILangMsg Core_Command_Help_List = new ILangMsg(this,
            "&8&m━━━━━━━━━━━━&8&l[ &e&l%plugin% &7- &6&lHelp &8&l]&8&m━━━━━━━━━━━━" + "\n" + "%cmds%");
    public ILangMsg Core_Command_Help_Desc = new ILangMsg(this, "Show help page.");
    public ILangMsg Core_Command_Editor_Desc = new ILangMsg(this, "Opens GUI Editor.");
    public ILangMsg Core_Command_About_Desc = new ILangMsg(this, "Some info about the plugin.");
    public ILangMsg Core_Command_Reload_Desc = new ILangMsg(this, "Reload the plugin.");
    public ILangMsg Core_Command_Reload_Done = new ILangMsg(this, "Reloaded!");

    public ILangMsg Core_Editor_Tips_Commands = new ILangMsg(this,
            "{message: ~prefix: false;}&7" + "\n" + "&b&lCommand Tips:" + "\n" + "&7" + "\n"
                    + "&2• &a[CONSOLE] <command> &2- Execute from Console." + "\n"
                    + "&2• &a[OP] <command> &2- Execute as an Operator." + "\n"
                    + "&2• (no prefix) &a<command> &2- Execute from a Player." + "\n"
                    + "&2• &a%player% &2- Player name placeholder." + "\n" + "&7");

    public ILangMsg Core_Editor_Tips_Header = new ILangMsg(this,
            "{message: ~prefix: false;}&7" + "\n" + "&e&lSUGGESTED (ALLOWED) VALUES:" + "\n");
    public ILangMsg Core_Editor_Tips_Hint = new ILangMsg(this, "&b&nClick to select!");

    public ILangMsg Core_Editor_Tips_Exit_Name = new ILangMsg(this,
            "&b<Click this message to &dExit &bthe &dEdit Mode&b>");
    public ILangMsg Core_Editor_Tips_Exit_Hint = new ILangMsg(this, "&7Click to exit edit mode.");
    public ILangMsg Core_Editor_Display_Edit_Format = new ILangMsg(this,
            "{message: ~type: TITLES; ~fadeIn: 10; ~stay: -1; ~fadeOut: 10;}" + "%title%" + "\n" + "&7%message%");
    public ILangMsg Core_Editor_Display_Done_Title = new ILangMsg(this, "&a&lDone!");
    public ILangMsg Core_Editor_Display_Edit_Title = new ILangMsg(this, "&a&lEditing...");
    public ILangMsg Core_Editor_Display_Error_Title = new ILangMsg(this, "&c&lError!");
    public ILangMsg Core_Editor_Display_Error_Number_Invalid = new ILangMsg(this, "&c&lInvalid number!");
    public ILangMsg Core_Editor_Display_Error_Number_MustDecimal = new ILangMsg(this,
            "&7Must be &cInteger &7or &cDecimal");
    public ILangMsg Core_Editor_Display_Error_Number_MustInteger = new ILangMsg(this, "&7Must be &cInteger");
    public ILangMsg Core_Editor_Display_Error_Type_Title = new ILangMsg(this, "&c&lInvalid Type!");
    public ILangMsg Core_Editor_Display_Error_Type_Values = new ILangMsg(this, "&7See allowed values in chat.");

    public ILangMsg Core_Editor_Actions_Section_Add = new ILangMsg(this, "&7Enter unique section id...");
    public ILangMsg Core_Editor_Actions_Subject_Add = new ILangMsg(this, "&7Select a subject...");
    public ILangMsg Core_Editor_Actions_Subject_Invalid = new ILangMsg(this, "&cInvalid provided!");
    public ILangMsg Core_Editor_Actions_Subject_Hint = new ILangMsg(this,
            "%description%" + "\n" + "&7" + "\n" + "&d&nClick to select!");
    public ILangMsg Core_Editor_Actions_Subject_NoParams = new ILangMsg(this, "&c* No Params Available *");

    public ILangMsg Core_Editor_Actions_Param_Add = new ILangMsg(this, "&7Select a param and type it's value...");
    public ILangMsg Core_Editor_Actions_Param_Edit = new ILangMsg(this, "&7Enter a new value...");
    public ILangMsg Core_Editor_Actions_Param_Hint = new ILangMsg(this, "&d&nClick to select!");
    public ILangMsg Core_Editor_Actions_Param_Invalid = new ILangMsg(this, "&cNo such param!");

    public ILangMsg Core_Editor_Actions_Action_ActionBar_Desc = new ILangMsg(this, "&7Sends a message to action bar.");
    public ILangMsg Core_Editor_Actions_Action_Broadcast_Desc = new ILangMsg(this,
            "&7Broadcasts a message to whole server.");
    public ILangMsg Core_Editor_Actions_Action_Burn_Desc = new ILangMsg(this, "&7Burns an entity.");
    public ILangMsg Core_Editor_Actions_Action_CommandConsole_Desc = new ILangMsg(this,
            "&7Executes a command from console.");
    public ILangMsg Core_Editor_Actions_Action_CommandOp_Desc = new ILangMsg(this,
            "&7Executes a command with OP permissions.");
    public ILangMsg Core_Editor_Actions_Action_CommandPlayer_Desc = new ILangMsg(this,
            "&7Executes a command by a player.");
    public ILangMsg Core_Editor_Actions_Action_Hunger_Desc = new ILangMsg(this, "&7Changes player's hunger level.");
    public ILangMsg Core_Editor_Actions_Action_Saturation_Desc = new ILangMsg(this,
            "&7Changes player's saturation level.");
    public ILangMsg Core_Editor_Actions_Action_Damage_Desc = new ILangMsg(this, "&7Damages an entity.");
    public ILangMsg Core_Editor_Actions_Action_Firework_Desc = new ILangMsg(this, "&7Launches a random firework.");
    public ILangMsg Core_Editor_Actions_Action_Goto_Desc = new ILangMsg(this,
            "&7Executes actions of certain actions section.");
    public ILangMsg Core_Editor_Actions_Action_Health_Desc = new ILangMsg(this, "&7Changes entity's health level.");
    public ILangMsg Core_Editor_Actions_Action_Hook_Desc = new ILangMsg(this, "&7Pulls towards the target.");
    public ILangMsg Core_Editor_Actions_Action_Lightning_Desc = new ILangMsg(this, "&7Summons a lightning strike.");
    public ILangMsg Core_Editor_Actions_Action_Message_Desc = new ILangMsg(this, "&7Send a message to a target.");
    public ILangMsg Core_Editor_Actions_Action_ParticleSimple_Desc = new ILangMsg(this, "&7Plays certain particle.");
    public ILangMsg Core_Editor_Actions_Action_Potion_Desc = new ILangMsg(this, "&7Adds certain potion effect.");
    public ILangMsg Core_Editor_Actions_Action_ProgressBar_Desc = new ILangMsg(this, "&7Displays progress bar.");
    public ILangMsg Core_Editor_Actions_Action_Projectile_Desc = new ILangMsg(this, "&7Launches certain projectile.");
    public ILangMsg Core_Editor_Actions_Action_Sound_Desc = new ILangMsg(this, "&7Plays certain sound.");
    public ILangMsg Core_Editor_Actions_Action_Teleport_Desc = new ILangMsg(this, "&7Teleport to the target.");
    public ILangMsg Core_Editor_Actions_Action_Throw_Desc = new ILangMsg(this, "&7Pulls away targets.");
    public ILangMsg Core_Editor_Actions_Action_Titles_Desc = new ILangMsg(this, "&7Shows custom titles.");

    public ILangMsg Core_Editor_Actions_Condition_EntityHealth_Desc = new ILangMsg(this,
            "&7Checks the target's health.");
    public ILangMsg Core_Editor_Actions_Condition_EntityType_Desc = new ILangMsg(this, "&7Checks the target's type.");
    public ILangMsg Core_Editor_Actions_Condition_Permission_Desc = new ILangMsg(this,
            "&7Checks the target's permission.");
    public ILangMsg Core_Editor_Actions_Condition_VaultBalance_Desc = new ILangMsg(this,
            "&7Checks the player's balance.");
    public ILangMsg Core_Editor_Actions_Condition_WorldTime_Desc = new ILangMsg(this, "&7Checks the world's time.");

    public ILangMsg Core_Editor_Actions_TargetSelector_FromSight_Desc = new ILangMsg(this,
            "&7Selects a target from executor's sight.");
    public ILangMsg Core_Editor_Actions_TargetSelector_Radius_Desc = new ILangMsg(this,
            "&7Selects targets in a radius.");
    public ILangMsg Core_Editor_Actions_TargetSelector_Self_Desc = new ILangMsg(this,
            "&7Selects executor as a target.");

    public ILangMsg Time_Day = new ILangMsg(this, "%s%d.");
    public ILangMsg Time_Hour = new ILangMsg(this, "%s%h.");
    public ILangMsg Time_Min = new ILangMsg(this, "%s%min.");
    public ILangMsg Time_Sec = new ILangMsg(this, "%s%sec.");

    public ILangMsg Other_Yes = new ILangMsg(this, "&aYes");
    public ILangMsg Other_No = new ILangMsg(this, "&cNo");
    public ILangMsg Other_Any = new ILangMsg(this, "Any");

    public ILangMsg Error_NoPlayer = new ILangMsg(this, "&cPlayer not found.");
    public ILangMsg Error_NoWorld = new ILangMsg(this, "&cWorld not found.");
    public ILangMsg Error_Number = new ILangMsg(this, "&7%num% &cis not a valid number.");
    public ILangMsg Error_NoPerm = new ILangMsg(this, "&cYou don't have permissions to do that!");
    public ILangMsg Error_NoData = new ILangMsg(this, "&4Error while get data for &c%player%&4.");
    public ILangMsg Error_NoItem = new ILangMsg(this, "&cYou must hold an item!");
    public ILangMsg Error_Type = new ILangMsg(this, "Invalid type. Available: %types%");
    public ILangMsg Error_Self = new ILangMsg(this, "Can not be used on yourself.");
    public ILangMsg Error_Sender = new ILangMsg(this, "This command is for players only.");
    public ILangMsg Error_Internal = new ILangMsg(this, "&cInternal error!");

    @NotNull
    public String getBool(boolean b) {
        return (b ? this.Other_Yes : this.Other_No).getMsg();
    }
}
