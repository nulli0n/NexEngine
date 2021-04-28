package su.nexmedia.engine.config.api;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.utils.Reflex;
import su.nexmedia.engine.utils.StringUT;

public abstract class ILangTemplate {

    protected NexPlugin<?> plugin;
    protected JYML config;
    protected ILangTemplate parent;
    protected Map<String, String> customPlaceholders;

    public ILangTemplate(@NotNull NexPlugin<?> plugin, @NotNull JYML config) {
        this(plugin, config, null);
    }

    public ILangTemplate(@NotNull NexPlugin<?> plugin, @NotNull JYML config, @Nullable ILangTemplate parent) {
        this.plugin = plugin;
        this.config = config;
        this.parent = parent;
        this.customPlaceholders = new HashMap<>();
    }

    public void setup() {
        this.load();
        this.setupEnums();
        this.config.saveChanges();

        for (String place : this.config.getSection("custom-placeholders")) {
            this.customPlaceholders.put("%" + place + "%", this.config.getString("custom-placeholders." + place));
        }
    }

    protected void setupEnums() {

    }

    protected void setupEnum(@NotNull Class<? extends Enum<?>> clazz) {
        if (!clazz.isEnum())
            return;
        for (Object o : clazz.getEnumConstants()) {
            if (o == null)
                continue;

            String name = o.toString();
            String path = clazz.getSimpleName() + "." + name;
            String val = StringUT.capitalizeFully(name.replace("_", " "));
            this.config.addMissing(path, val);
        }
    }

    @NotNull
    public String getEnum(@NotNull Enum<?> e) {
        String path = e.getClass().getSimpleName() + "." + e.name();
        String locEnum = this.getCustom(path);
        if (locEnum == null && !this.plugin.isEngine()) {
            return NexPlugin.getEngine().lang().getEnum(e);
        }
        return locEnum == null ? "null" : locEnum;
    }

    @Nullable
    public String getCustom(@NotNull String path) {
        String str = this.config.getString(path);
        return str == null ? str : StringUT.color(str);
    }

    @NotNull
    public Map<String, String> getCustomPlaceholders() {
        return this.customPlaceholders;
    }

    private void load() {
        for (Field field : Reflex.getFields(this.getClass())) {
            if (!ILangMsg.class.isAssignableFrom(field.getType())) {
                continue;
            }

            ILangMsg jmsg;
            try {
                jmsg = (ILangMsg) field.get(this);
            }
            catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
                continue;
            }

            jmsg.setPath(field.getName()); // Set the path to String in config

            // Fill message fields from extended class with parent message field values.
            if (!field.getDeclaringClass().equals(this.getClass())) {
                ILangMsg superField = (ILangMsg) Reflex.getFieldValue(this.parent, field.getName());
                if (superField != null) {
                    jmsg.setMsg(superField.getMsg());
                    continue;
                }
            }

            String path = jmsg.getPath();
            JYML cfg = this.config;

            // Add missing lang node in config.
            if (!cfg.contains(path)) {
                String msg = jmsg.getDefaultMsg();
                String[] split = msg.split("\n");
                cfg.set(path, split.length > 1 ? Arrays.asList(split) : msg);
            }

            // Load message text from lang config
            String msgLoad = null;
            List<String> cList = cfg.getStringList(path);
            if (!cList.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                cList.forEach(line -> {
                    if (builder.length() > 0)
                        builder.append("\\n");
                    builder.append(line);
                });
                msgLoad = builder.toString();
            }
            else {
                msgLoad = cfg.getString(path, "");
            }
            jmsg.setMsg(msgLoad);
        }
        this.config.saveChanges();
    }
}
