package com.j256.ormlite.android;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseResults;

/**
 * Android implementation of the compiled statement.
 * 
 * @author kevingalligan, graywatson
 */
public class AndroidCompiledStatement implements CompiledStatement {

	private final String sql;
	private final SQLiteDatabase db;
	private final StatementType type;

	private Cursor cursor;
	private final List<Object> args = new ArrayList<Object>();
	private Integer max;

	public AndroidCompiledStatement(String sql, SQLiteDatabase db, StatementType type) {
		this.sql = sql;
		this.db = db;
		this.type = type;
	}

	public int getColumnCount() throws SQLException {
		return getCursor().getColumnCount();
	}

	public String getColumnName(int column) throws SQLException {
		return getCursor().getColumnName(column);
	}

	public DatabaseResults runQuery(ObjectCache objectCache) throws SQLException {
		// this could come from DELETE or UPDATE, just not a SELECT
		if (!type.isOkForQuery()) {
			throw new IllegalArgumentException("Cannot call query on a " + type + " statement");
		}
		return new AndroidDatabaseResults(getCursor(), objectCache);
	}

	public int runUpdate() throws SQLException {
		if (!type.isOkForUpdate()) {
			throw new IllegalArgumentException("Cannot call update on a " + type + " statement");
		}
		String finalSql = null;
		try {
			if (max == null) {
				finalSql = sql;
			} else {
				finalSql = sql + " " + max;
			}
			db.execSQL(finalSql, args.toArray(new Object[args.size()]));
		} catch (android.database.SQLException e) {
			throw SqlExceptionUtil.create("Problems executing Android statement: " + finalSql, e);
		}
		return 1;
	}

	public int runExecute() throws SQLException {
		if (!type.isOkForExecute()) {
			throw new IllegalArgumentException("Cannot call execute on a " + type + " statement");
		}
		try {
			db.execSQL(sql, new Object[0]);
		} catch (android.database.SQLException e) {
			throw SqlExceptionUtil.create("Problems executing Android statement: " + sql, e);
		}
		return 0;
	}

	public void close() throws SQLException {
		if (cursor != null) {
			try {
				cursor.close();
			} catch (android.database.SQLException e) {
				throw SqlExceptionUtil.create("Problems closing Android cursor", e);
			}
		}
	}

	public void setObject(int parameterIndex, Object obj, SqlType sqlType) throws SQLException {
		isInPrep();
		if (obj == null) {
			args.add(parameterIndex, null);
		} else {
			args.add(parameterIndex, obj.toString());
		}
	}

	public void setMaxRows(int max) throws SQLException {
		isInPrep();
		this.max = max;
	}

	/***
	 * This is mostly an internal class but is exposed for those people who need access to the Cursor itself.
	 * 
	 * <p>
	 * NOTE: This is not thread safe. Not sure if we need it, but keep that in mind.
	 * </p>
	 */
	public Cursor getCursor() throws SQLException {
		if (cursor == null) {
			String finalSql = null;
			try {
				if (max == null) {
					finalSql = sql;
				} else {
					finalSql = sql + " " + max;
				}
				cursor = db.rawQuery(finalSql, args.toArray(new String[args.size()]));
				cursor.moveToFirst();
			} catch (android.database.SQLException e) {
				throw SqlExceptionUtil.create("Problems executing Android query: " + finalSql, e);
			}
		}

		return cursor;
	}

	private void isInPrep() throws SQLException {
		if (cursor != null) {
			throw new SQLException("Query already run. Cannot add argument values.");
		}
	}
}
