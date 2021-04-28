package su.nexmedia.engine.config.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.MsgUT;
import su.nexmedia.engine.utils.StringUT;

public class ILangMsg {

    private static final Pattern PATTERN_MESSAGE = Pattern.compile("(\\{message:)+(.)+?(\\})+(.*?)(\\})?");
    private static final String[] MESSAGE_ARGUMENTS = new String[] { "type", "prefix", "fadeIn", "stay", "fadeOut" };
    private static final Pattern[] PATTERN_ARGUMENTS = new Pattern[MESSAGE_ARGUMENTS.length];

    static {
        for (int i = 0; i < MESSAGE_ARGUMENTS.length; i++) {
            PATTERN_ARGUMENTS[i] = Pattern.compile("(~)+(" + MESSAGE_ARGUMENTS[i] + ")+?(:)+(.*?)(;)");
        }
    }

    private ILangTemplate template;
    private String msgDefault;
    private String msgColor;
    private String path;

    private OutputType out = OutputType.CHAT;
    private boolean isPrefix = true;
    private int[] titlesTimes = new int[3];

    public ILangMsg(@NotNull ILangTemplate template, @NotNull String msg) {
        this.template = template;
        this.msgDefault = msg;
        this.setMsg(msg);
    }

    ILangMsg(@NotNull ILangMsg from) {
        this.template = from.template;
        this.msgDefault = from.getDefaultMsg();
        this.msgColor = from.getMsg();
        this.path = from.getPath();
        this.out = from.out;
        this.isPrefix = from.isPrefix;
        this.titlesTimes = from.titlesTimes;
    }

    void setPath(@NotNull String path) {
        this.path = path.replace("_", ".");
    }

    @NotNull
    public String getPath() {
        return this.path;
    }

    public void setMsg(@NotNull String msg) {
        // When TRUE, then 'msgColor' is already set by this 'msg' value.
        if (!this.setArguments(msg)) {
            this.msgColor = msg;
        }

        // Do not replace colors for JSON message, otherwise it will be broken.
        if (!MsgUT.isJSON(msg)) {
            this.msgColor = StringUT.color(this.msgColor);
        }
    }

    boolean setArguments(@NotNull String msg) {
        Matcher mArgs = PATTERN_MESSAGE.matcher(msg);
        if (!mArgs.find())
            return false;

        // String with only args
        String extract = mArgs.group(0);
        String arguments = extract.replace("{message:", "").replace("}", "").trim();
        this.msgColor = msg.replace(extract, "");

        for (int i = 0; i < MESSAGE_ARGUMENTS.length; i++) {
            // Search for flag of this parameter
            String argType = MESSAGE_ARGUMENTS[i];
            Pattern pArgVal = PATTERN_ARGUMENTS[i];
            Matcher mArgVal = pArgVal.matcher(arguments); // TODO 200ms

            // Get the flag value
            if (mArgVal.find()) {
                // Extract only value from all flag string
                String argValue = mArgVal.group(4).trim();
                switch (argType) {
                    case "type": {
                        this.out = CollectionsUT.getEnum(argValue, ILangMsg.OutputType.class);
                        break;
                    }
                    case "prefix": {
                        this.isPrefix = Boolean.parseBoolean(argValue);
                        break;
                    }
                    case "fadeIn": {
                        this.titlesTimes[0] = StringUT.getInteger(argValue, -1);
                        break;
                    }
                    case "stay": {
                        this.titlesTimes[1] = StringUT.getInteger(argValue, -1);
                        if (this.titlesTimes[1] < 0)
                            this.titlesTimes[1] = 10000;
                        break;
                    }
                    case "fadeOut": {
                        this.titlesTimes[2] = StringUT.getInteger(argValue, -1);
                        break;
                    }
                }
            }
        }
        return true;
    }

    @NotNull
    public String getDefaultMsg() {
        return this.msgDefault;
    }

    @NotNull
    public String getMsgReady() {
        return this.replaceDefaults().apply(this.msgColor);
    }

    @NotNull
    public String getMsg() {
        return this.msgColor;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public ILangMsg replace(@NotNull String var, @NotNull Object replacer) {
        if (this.isEmpty())
            return this;
        if (replacer instanceof List)
            return this.replace(var, (List<Object>) replacer);

        return this.replace(str -> str.replace(var, String.valueOf(replacer)));
    }

    @NotNull
    public ILangMsg replace(@NotNull String var, @NotNull List<Object> replacer) {
        if (this.isEmpty())
            return this;

        StringBuilder builder = new StringBuilder();
        replacer.forEach(rep -> {
            if (builder.length() > 0)
                builder.append("\\n");
            builder.append(rep.toString());
        });

        return this.replace(str -> str.replace(var, builder.toString()));
    }

    @NotNull
    public ILangMsg replace(@NotNull UnaryOperator<String> replacer) {
        if (this.isEmpty())
            return this;

        ILangMsg msgCopy = new ILangMsg(this);
        msgCopy.msgColor = StringUT.color(replacer.apply(msgCopy.getMsg()));
        return msgCopy;
    }

    public boolean isEmpty() {
        return (this.out == ILangMsg.OutputType.NONE || this.getMsg().isEmpty());
    }

    public void broadcast() {
        if (this.isEmpty())
            return;

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (player != null)
                this.send(player);
        }
        this.send(Bukkit.getServer().getConsoleSender());
    }

    public void send(@NotNull CommandSender sender) {
        if (this.isEmpty())
            return;

        if (this.out == ILangMsg.OutputType.CHAT) {
            String prefix = isPrefix ? template.plugin.lang().Prefix.getMsgReady() : "";

            this.asList().forEach(line -> {
                MsgUT.sendWithJSON(sender, prefix + line);
            });
        }
        else if (sender instanceof Player) {
            Player player = (Player) sender;
            if (this.out == ILangMsg.OutputType.ACTION_BAR) {
                MsgUT.sendActionBar(player, this.getMsgReady());
            }
            else if (this.out == ILangMsg.OutputType.TITLES) {
                List<String> list = this.asList();
                if (list.isEmpty())
                    return;

                String title = list.get(0);
                String subtitle = list.size() > 1 ? list.get(1) : "";
                player.sendTitle(title, subtitle, this.titlesTimes[0], this.titlesTimes[1], this.titlesTimes[2]);
            }
        }
    }

    @NotNull
    public List<String> asList() {
        String msg = this.getMsgReady();
        if (msg.isEmpty())
            return Collections.emptyList();

        List<String> list = new ArrayList<>();
        for (String line : msg.split("\\\\n")) {
            list.add(line.trim());
        }
        return list;
    }

    /**
     * Replaces a raw '\n' new line splitter with a system one.
     * 
     * @return A string with a system new line splitters.
     */
    @NotNull
    public String normalizeLines() {
        StringBuilder text = new StringBuilder("");
        for (String line : this.asList()) {
            if (text.length() > 0)
                text.append("\n");
            text.append(line);
        }
        return text.toString();
    }

    @NotNull
    private UnaryOperator<String> replaceDefaults() {
        return str -> {
            for (Map.Entry<String, String> entry : this.template.getCustomPlaceholders().entrySet()) {
                str = str.replace(entry.getKey(), entry.getValue());
            }
            return str.replace("%plugin%", template.plugin.cfg().pluginName);
        };
    }

    public static enum OutputType {
        CHAT, ACTION_BAR, TITLES, NONE,;
    }
}