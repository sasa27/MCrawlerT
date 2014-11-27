package com.j256.ormlite.android;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;

import android.database.Cursor;

import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.db.SqliteAndroidDatabaseType;
import com.j256.ormlite.support.DatabaseResults;

/**
 * Android implementation of our results object.
 * 
 * @author kevingalligan, graywatson
 */
public class AndroidDatabaseResults implements DatabaseResults {

	private final Cursor cursor;
	private final ObjectCache objectCache;
	private static final DatabaseType databaseType = new SqliteAndroidDatabaseType();

	public AndroidDatabaseResults(Cursor cursor, ObjectCache objectCache) {
		this.cursor = cursor;
		this.objectCache = objectCache;
	}

	/**
	 * Constructor that allows you to inject a cursor that has already been configured with first-call set to false.
	 * 
	 * @deprecated The firstCall is no longer needed since the library now calls first() and next on its own.
	 */
	@Deprecated
	public AndroidDatabaseResults(Cursor cursor, boolean firstCall, ObjectCache objectCache) {
		this(cursor, objectCache);
	}

	public int getColumnCount() {
		return cursor.getColumnCount();
	}

	public boolean first() {
		return cursor.moveToFirst();
	}

	public boolean next() {
		return cursor.moveToNext();
	}

	public boolean previous() {
		return cursor.moveToPrevious();
	}

	public boolean moveRelative(int offset) {
		return cursor.move(offset);
	}

	public int findColumn(String columnName) throws SQLException {
		int index = cursor.getColumnIndex(columnName);
		if (index < 0) {
			/*
			 * Hack here. It turns out that if we've asked for '*' then the field foo is in the cursor as foo. But if we
			 * ask for a particular field list, which escapes the field names, with DISTINCT the fiend names are in the
			 * cursor with the escaping. Ugly!!
			 */
			StringBuilder sb = new StringBuilder(columnName.length() + 4);
			databaseType.appendEscapedEntityName(sb, columnName);
			index = cursor.getColumnIndex(sb.toString());
			if (index < 0) {
				throw new SQLException("Unknown field '" + columnName + "' from the Android sqlite cursor");
			}
		}
		return index;
	}

	public String getString(int columnIndex) {
		return cursor.getString(columnIndex);
	}

	public boolean getBoolean(int columnIndex) {
		if (cursor.isNull(columnIndex) || cursor.getShort(columnIndex) == 0) {
			return false;
		} else {
			return true;
		}
	}

	public char getChar(int columnIndex) throws SQLException {
		String string = cursor.getString(columnIndex);
		if (string == null || string.length() == 0) {
			return 0;
		} else if (string.length() == 1) {
			return string.charAt(0);
		} else {
			throw new SQLException("More than 1 character stored in database column: " + columnIndex);
		}
	}

	public byte getByte(int columnIndex) {
		return (byte) getShort(columnIndex);
	}

	public byte[] getBytes(int columnIndex) {
		return cursor.getBlob(columnIndex);
	}

	public short getShort(int columnIndex) {
		return cursor.getShort(columnIndex);
	}

	public int getInt(int columnIndex) {
		return cursor.getInt(columnIndex);
	}

	public long getLong(int columnIndex) {
		return cursor.getLong(columnIndex);
	}

	public float getFloat(int columnIndex) {
		return cursor.getFloat(columnIndex);
	}

	public double getDouble(int columnIndex) {
		return cursor.getDouble(columnIndex);
	}

	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		throw new SQLException("Android does not support timestamp.  Use JAVA_DATE_LONG or JAVA_DATE_STRING types");
	}

	public InputStream getBlobStream(int columnIndex) {
		return new ByteArrayInputStream(cursor.getBlob(columnIndex));
	}

	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		throw new SQLException("Android does not support BigDecimal type.  Use BIG_DECIMAL or BIG_DECIMAL_STRING types");
	}

	public boolean wasNull(int columnIndex) {
		return cursor.isNull(columnIndex);
	}

	public ObjectCache getObjectCache() {
		return objectCache;
	}

	/***
	 * Returns the underlying Android cursor object. This should not be used unless you know what you are doing.
	 */
	public Cursor getRawCursor() {
		return cursor;
	}
}
