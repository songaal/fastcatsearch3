package org.fastcatsearch.ir.setting;

import java.util.Map;

import org.fastcatsearch.ir.common.SettingException;

public interface Settings {

	public Settings getComponentSettings(Class<?> component);

	public Settings getByPrefix(String prefix);

	public Map<String, String> getAsMap();

	public String get(String setting);

	public String get(String setting, String defaultValue);

	public float getAsFloat(String setting, float defaultValue) throws SettingException;

	public double getAsDouble(String setting, double defaultValue) throws SettingException;

	public int getAsInt(String setting, int defaultValue) throws SettingException;

	public long getAsLong(String setting, long defaultValue) throws SettingException;

	public boolean getAsBoolean(String setting, boolean defaultValue) throws SettingException;

	public long getAsBytesSize(String setting, long defaultValue) throws SettingException;

	public String[] getAsArray(String settingPrefix) throws SettingException;
}
