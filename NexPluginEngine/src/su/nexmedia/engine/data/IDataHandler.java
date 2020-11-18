package su.nexmedia.engine.data;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sqlite.JDBC;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.data.serial.ItemStackSerializer;
import su.nexmedia.engine.data.users.IAbstractUser;

public abstract class IDataHandler<P extends NexPlugin<P>, U extends IAbstractUser<P>> {

	@NotNull protected final P plugin;
    protected final String TABLE_USERS;
    
    private final String url;
    private String user;
    private String password;
    
    protected StorageType dataType;
    protected Connection con;
    protected long lastLive;
    
    protected Gson gson;
    
    protected static final String COL_USER_UUID = "uuid";
    protected static final String COL_USER_NAME = "name";
    protected static final String COL_USER_LAST_ONLINE = "last_online";
    
    private static final ItemStackSerializer ITEM_SERIALIZER = new ItemStackSerializer();
    
	protected IDataHandler(@NotNull P plugin) throws SQLException {
		this.plugin = plugin;
		this.lastLive = System.currentTimeMillis();
		this.dataType = plugin.cfg().dataStorage;
		this.TABLE_USERS = plugin.getNameRaw() + "_users";
		if (this.dataType == StorageType.MYSQL) {
			this.url = "jdbc:mysql://" + plugin.cfg().mysqlHost + "/" + plugin.cfg().mysqlBase + "?useSSL=false";
	    	this.user = plugin.cfg().mysqlLogin;
	    	this.password = plugin.cfg().mysqlPassword;
		}
		else {
			this.url = "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/data.db";
			DriverManager.registerDriver(new JDBC());
		}
	}
	
	public final void setup() {
		this.gson = this.registerAdapters(new GsonBuilder().setPrettyPrinting()).create();
		
		this.open();
		this.create();
		this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
			this.purge();
		});
	}
	
	public final void shutdown() {
		this.close();
	}
	
	private final void open() {
		try {
			if (this.dataType == StorageType.MYSQL) {
				this.con = DriverManager.getConnection(this.url, this.user, this.password);
			}
			else {
				this.con = DriverManager.getConnection(this.url);
			}
		}
		catch (SQLException e) {
			plugin.error("Could not open SQL connection!");
			e.printStackTrace();
		}
	}
	
	private final void create() {
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		map.put(COL_USER_UUID, DataTypes.CHAR.build(this.dataType, 36));
		map.put(COL_USER_NAME, DataTypes.STRING.build(this.dataType, 24));
		map.put(COL_USER_LAST_ONLINE, DataTypes.LONG.build(this.dataType, 64));
		this.getColumnsToCreate().forEach((col, type) -> {
			map.merge(col, type, (oldV, newV) -> newV);
		});
		this.createTable(this.TABLE_USERS, map);
		this.addColumn(TABLE_USERS, COL_USER_LAST_ONLINE, DataTypes.LONG.build(this.dataType), String.valueOf(System.currentTimeMillis()));
		
		this.onTableCreate();
	}

	private final void close() {
		try {
	        if (con != null) con.close(); 
		}
		catch (SQLException se) {}
	    finally {
	        try { if (con != null) con.close(); } 
	        catch (SQLException se) {}
	    }
	}

	@NotNull
	protected GsonBuilder registerAdapters(@NotNull GsonBuilder builder) {
		// TODO Register for  location?
		return builder
				.registerTypeAdapter(ItemStack.class, ITEM_SERIALIZER);
	}

	@NotNull
	protected final Connection getConnection() {
	    try {
	        if (this.con == null || this.con.isClosed()) {
	            this.open();
	        }
	        if (System.currentTimeMillis() - this.lastLive >= 10000L) {
	            this.con.prepareStatement("SELECT 1").executeQuery();
	            this.lastLive = System.currentTimeMillis();
	        }
	    }
	    catch (SQLException ex) {
	        this.open();
	    }
	    return this.con;
	}

	protected void createTable(@NotNull String table, @NotNull LinkedHashMap<String, String> valMap) {
		StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + table + "(");
		
		StringBuilder columns = new StringBuilder();
		
		// Adding primary id-key column.
		if (this.dataType == StorageType.MYSQL) {
			columns.append("`id` int(11) NOT NULL AUTO_INCREMENT");
		}
		else if (this.dataType == StorageType.SQLITE) {
			columns.append("`id` INTEGER PRIMARY KEY AUTOINCREMENT");
		}
		
		// Adding all other columns with their types.
		valMap.forEach((col, type) -> {
			if (columns.length() > 0) {
				columns.append(", ");
			}
			columns.append("`" + col + "` " + type);
		});
		
		// For MySQL define 'id' column as primary key.
		if (this.dataType == StorageType.MYSQL) {
			columns.append(", PRIMARY KEY (`id`)");
		}
		// Add columns to main sql builder and close the statement.
		sql.append(columns.toString());
		sql.append(");");
		
		this.execute(sql.toString());
	}
	
	protected void renameTable(@NotNull String from, @NotNull String to) {
		if (!this.hasTable(from)) return;
		
		StringBuilder sql = new StringBuilder();
		if (this.dataType == StorageType.MYSQL) {
			sql.append("RENAME TABLE ").append(from).append(" TO ").append(to).append(";");
		}
		else {
			sql.append("ALTER TABLE ").append(from).append(" RENAME TO ").append(to);
		}
		this.execute(sql.toString());
	}
	
	protected boolean hasTable(@NotNull String table) {
		boolean b = false;
		try {
			DatabaseMetaData dbm = this.getConnection().getMetaData();
			ResultSet tables = dbm.getTables(null, null, table, null);
			b = tables.next();
			tables.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return b;
	}
	
	protected void addColumn(
			@NotNull String table, 
			@NotNull String column, 
			@NotNull String type,
			@NotNull String def) {
	    if (this.hasColumn(table, column)) return;
	    
	    StringBuilder sql = new StringBuilder();
	    sql.append("ALTER TABLE ").append(table).append(" ");
	    sql.append("ADD ").append(column).append(" ").append(type).append(" ");
	    sql.append("DEFAULT '").append(def).append("'");
	    
	    this.execute(sql.toString());
	}
	
	public final boolean hasColumn(@NotNull String table, @NotNull String columnName) {
		this.con = this.getConnection();
		String sql = "SELECT * FROM " + table;
		try (Statement ps = this.con.createStatement()) {
	        ResultSet rs = ps.executeQuery(sql);
		    ResultSetMetaData rsmd = rs.getMetaData();
		    int columns = rsmd.getColumnCount();
		    for (int x = 1; x <= columns; x++) {
		        if (columnName.equals(rsmd.getColumnName(x))) {
		            return true;
		        }
		    }
		    return false;
		}
		catch (SQLException e) {
			plugin.error("Could not check SQL column: '" + columnName + "' for '" + table + "'");
			e.printStackTrace();
	        return false;
	    }
	}

	protected void addData(@NotNull String table, @NotNull LinkedHashMap<String, String> keys) {
		StringBuilder sql = new StringBuilder("INSERT INTO " + table + "(");
		
		
		StringBuilder columns = new StringBuilder();
		keys.keySet().forEach((key) -> {
			if (columns.length() > 0) {
				columns.append(", ");
			}
			columns.append("`" + key + "`");
		});
		sql.append(columns.toString());
		sql.append(") VALUES(");
		
		
		StringBuilder values = new StringBuilder();
		keys.values().forEach((value) -> {
			if (values.length() > 0) {
				values.append(", ");
			}
			values.append("'" + value + "'");
		});
		sql.append(values.toString());
		sql.append(")");
		
		this.execute(sql.toString());
	}

	protected void saveData(
			@NotNull String table, 
			@NotNull LinkedHashMap<String, String> valMap, 
			@NotNull Map<String, String> whereMap) {
		
		StringBuilder sql = new StringBuilder("UPDATE " + table + " SET ");
		
		StringBuilder values = new StringBuilder();
		valMap.forEach((key, value) -> {
			if (values.length() > 0) {
				values.append(", ");
			}
			values.append("`" + key + "` = '" + value + "'");
		});
		sql.append(values.toString());
		sql.append(" WHERE ");
		
		StringBuilder wheres = new StringBuilder();
		whereMap.forEach((key, value) -> {
			if (wheres.length() > 0) {
				wheres.append(" AND ");
			}
			wheres.append("`" + key + "` = '" + value + "'");
		});
		sql.append(wheres.toString());
		
		this.execute(sql.toString());
	}
	
	protected void deleteData(
			@NotNull String table,  
			@NotNull Map<String, String> whereMap) {
		
		StringBuilder sql = new StringBuilder("DELETE FROM " + table + " WHERE ");

		StringBuilder wheres = new StringBuilder();
		whereMap.forEach((key, value) -> {
			if (wheres.length() > 0) {
				wheres.append(" AND ");
			}
			wheres.append("`" + key + "` = '" + value + "'");
		});
		sql.append(wheres.toString());
		
		this.execute(sql.toString());
	}

	@Nullable
	protected <T> T getData(@NotNull String table, @NotNull Map<String, String> whereMap, @NotNull Function<ResultSet, T> fn) {
		List<T> data = this.getDatas(table, whereMap, fn, 1);
		return data.isEmpty() ? null : data.get(0);
	}

	@NotNull
	protected <T> List<@NotNull T> getDatas(
			@NotNull String table, 
			@NotNull Map<String, String> whereMap, 
			@NotNull Function<ResultSet, T> dataFunction,
			int amount) {
		
		StringBuilder sql = new StringBuilder("SELECT * FROM " + table);
		List<T> list = new ArrayList<>();
		this.con = this.getConnection();
		
		if (!whereMap.isEmpty()) {
		    StringBuilder wheres = new StringBuilder();
		    whereMap.keySet().forEach((key) -> {
		    	if (wheres.length() > 0) {
		    		wheres.append(" AND ");
		    	}
		    	wheres.append("`" + key + "` = ?");
		    });
		    sql.append(" WHERE ");
		    sql.append(wheres.toString());
			
	    	try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
	    		int count = 1;
	    		for (String wValue : whereMap.values()) {
	    			ps.setString(count++, wValue);
	    		}
	    		
	    		ResultSet rs = ps.executeQuery();
	    		while (rs.next() && (amount < 0 || list.size() < amount)) {
	            	list.add(dataFunction.apply(rs));
	            }
	    	}
	    	catch (SQLException e) {
	    		plugin.error("SQL Error!");
				e.printStackTrace();
	        }
		}
		else {
			try (Statement ps = this.con.createStatement()) {
				ResultSet rs = ps.executeQuery(sql.toString());
				while (rs.next() && (amount < 0 || list.size() < amount)) {
		        	list.add(dataFunction.apply(rs));
		        }
			}
			catch (SQLException e) {
				plugin.error("Could not data from the database!");
				e.printStackTrace();
		    }
		}
		list.removeIf(data -> data == null);
		
		return list;
	}

	protected void onTableCreate() {
		
	}
	
	@NotNull
	protected abstract LinkedHashMap<String, String> getColumnsToCreate();
	
	@NotNull
	protected abstract LinkedHashMap<String, String> getColumnsToSave(@NotNull U user);
	
	@NotNull
	protected abstract Function<ResultSet, U> getFunctionToUser();

	public void purge() {
		if (!plugin.cfg().dataPurgeEnabled) return;
		
		int count = 0;
		for (U user : this.getUsers()) {
			long lastOnline = user.getLastOnline();
			
			long diff = System.currentTimeMillis() - lastOnline;
			int days = (int) ((diff / (1000*60*60*24)) % 7);
			
			if (days >= plugin.cfg().dataPurgeDays) {
				this.deleteUser(user.getUUID().toString());
				count++;
			}
		}
		plugin.info("[User Data] Purged " + count + " inactive users.");
	}
	
	public final void execute(@NotNull String sql) {
		this.con = this.getConnection();
		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
    		ps.executeUpdate();
    	}
    	catch (SQLException e) {
    		plugin.error("Could not execute SQL statement: [" + sql + "]");
			e.printStackTrace();
    	}
	}
	
	@NotNull
    public List<@NotNull U> getUsers() {
		return this.getDatas(this.TABLE_USERS, Collections.emptyMap(), this.getFunctionToUser(), -1);
    }
    
	@Nullable
    public U getUser(@NotNull Player player) {
		return this.getUser(player.getUniqueId());
	}
	
	@Nullable
    public U getUser(@NotNull UUID uuid) {
		return this.getUser(uuid.toString(), true);
	}
	
	@Nullable
    public final U getUser(@NotNull String uuid, boolean isId) {
		Map<String, String> whereMap = new HashMap<>();
		if (isId) {
			whereMap.put(COL_USER_UUID, uuid);
		}
		else {
			whereMap.put(COL_USER_NAME, uuid);
		}
    	return this.getData(this.TABLE_USERS, whereMap, this.getFunctionToUser());
    }
    
    public boolean isUserExists(@NotNull String uuid, boolean uid) {
    	return this.getUser(uuid, uid) != null;
    }
    
    public void saveUser(@NotNull U user) {
    	LinkedHashMap<String, String> colMap = new LinkedHashMap<>();
    	colMap.put(COL_USER_NAME, user.getName());
    	colMap.put(COL_USER_LAST_ONLINE, String.valueOf(user.getLastOnline()));
    	this.getColumnsToSave(user).forEach((col, val) -> {
    		colMap.merge(col, val, (oldV, newV) -> newV);
    	});
    	
    	Map<String, String> whereMap = new HashMap<>();
    	whereMap.put(COL_USER_UUID, user.getUUID().toString());
    	
    	this.saveData(this.TABLE_USERS, colMap, whereMap);
    }
    
    public void addUser(@NotNull U user) {
    	if (this.isUserExists(user.getUUID().toString(), true)) return;
    	
    	LinkedHashMap<String, String> colMap = new LinkedHashMap<>();
    	colMap.put(COL_USER_UUID, user.getUUID().toString());
    	colMap.put(COL_USER_NAME, user.getName());
    	colMap.put(COL_USER_LAST_ONLINE, String.valueOf(user.getLastOnline()));
    	this.getColumnsToSave(user).forEach((col, val) -> {
    		colMap.merge(col, val, (oldV, newV) -> newV);
    	});
    	this.addData(this.TABLE_USERS, colMap);
    }
	
    public void deleteUser(@NotNull String uuid) {
    	String sql = "DELETE FROM " + TABLE_USERS + " WHERE `uuid` = '" + uuid + "'";
    	this.execute(sql);
    }
}
