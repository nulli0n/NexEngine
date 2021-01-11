package su.nexmedia.engine.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.core.Version;

public class Reflex {

	private static final NexEngine ENGINE;
	
	static {
		ENGINE = NexEngine.get();
	}
	
	@Nullable
	public static Class<?> getClass(@NotNull String path, @NotNull String name) {
		return getClass(path + "." + name);
	}
	
	@Nullable
	public static Class<?> getInnerClass(@NotNull String path, @NotNull String name) {
		return getClass(path + "$" + name);
	}
	
	@Nullable
	private static Class<?> getClass(@NotNull String path) {
		try {
			return Class.forName(path);
		} 
		catch (ClassNotFoundException e) {
			ENGINE.error("[Reflex] Class not found: " + path);
			e.printStackTrace();
			return null;
		}
	}
	
	@Nullable
	public static Constructor<?> getConstructor(@NotNull Class<?> clazz, Class<?>... types) {
		try {
			Constructor<?> con = clazz.getDeclaredConstructor(types);
			con.setAccessible(true);
			return con;
		}
		catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Nullable
	public static Object invokeConstructor(@NotNull Constructor<?> con, Object... obj) {
		try {
			return con.newInstance(obj);
		} 
		catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
		return obj;
	}
	
	@Nullable
	public static Class<?> getNMSClass(@NotNull String name) {
		return getClass("net.minecraft.server." + Version.CURRENT.name().toLowerCase(), name);
	}
	
	@Nullable
    public static Field getField(@NotNull Class<?> clazz, @NotNull String fieldName) {
    	try {
    		return clazz.getDeclaredField(fieldName);
        } 
    	catch (NoSuchFieldException e) {
    		Class<?> superClass = clazz.getSuperclass();
    		if (superClass == null) {
    			return null;
    		}
    		return getField(superClass, fieldName);
    	}
    }
    
	@Nullable
    public static Object getFieldValue(@NotNull Object from, @NotNull String fieldName) {
    	Field f;
		try {
			Class<?> clazz;
			if (from instanceof Class) {
				clazz = (Class<?>) from;
			}
			else {
				clazz = from.getClass();
			}
			
			f = getField(clazz, fieldName);
			if (f == null) {
				return null;
			}
			f.setAccessible(true);
			return f.get(from);
		} 
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
    	
    	return null;
    }
    
    public static boolean setFieldValue(@NotNull Object of, @NotNull String fieldName, @Nullable Object value) {
    	Field f;
		try {
			Class<?> clazz;
			boolean isStatic = of instanceof Class;
			if (isStatic) {
				clazz = (Class<?>) of;
			}
			else {
				clazz = of.getClass();
			}
			f = getField(clazz, fieldName);
			if (f == null) {
				return false;
			}
			
			f.setAccessible(true);
			if (isStatic) {
				f.set(null, value);
			}
			else {
				f.set(of, value);
			}
			return true;
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return false;
    }
    
    @Nullable
    public static Method getMethod(@NotNull Class<?> clazz, @NotNull String fieldName, @NotNull Class<?>... o) {
    	try {
    		return clazz.getDeclaredMethod(fieldName, o);
        } 
    	catch (NoSuchMethodException e) {
    		Class<?> superClass = clazz.getSuperclass();
    		if (superClass == null) {
    			return null;
    		}
    		else {
    			return getMethod(superClass, fieldName);
    		}
    	}
    }
    
    @Nullable
    public static Object invokeMethod(@NotNull Method m, @Nullable Object by, @Nullable Object... param) {
    	m.setAccessible(true);
    	try {
			return m.invoke(by, param);
		}
    	catch (IllegalAccessException e) {
			e.printStackTrace();
		}
    	catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
    	catch (InvocationTargetException e) {
			e.printStackTrace();
		}
    	return null;
    }
}
