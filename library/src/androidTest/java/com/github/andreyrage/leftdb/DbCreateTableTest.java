package com.github.andreyrage.leftdb;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.test.AndroidTestCase;

import com.github.andreyrage.leftdb.config.RelationshipConfig;
import com.github.andreyrage.leftdb.entities.AllFields;
import com.github.andreyrage.leftdb.entities.AllFieldsNotNull;
import com.github.andreyrage.leftdb.entities.DaoTestEntry;
import com.github.andreyrage.leftdb.entities.WrongIncObject;
import com.github.andreyrage.leftdb.exceptions.IncorrectAutoIncTypeException;
import com.github.andreyrage.leftdb.utils.SerializeUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class DbCreateTableTest extends AndroidTestCase {

    public LeftDBUtils dbUtils;

    @Override
    protected void setUp() throws Exception {
        dbUtils = new DBUtilsCreateTable(getContext(), "testdb.sqlite", 1);
        assertNotNull(dbUtils.db);
    }

    @Override
    protected void tearDown() throws Exception {
        dbUtils.getDbHandler().deleteDataBase();
    }

    public void testCorrectCreateTableQuery() throws Exception {
        String correctQuery = "CREATE TABLE AllFields (" +
                "bigDecimalO TEXT, booleanP INTEGER, " +
                "booleanW INTEGER, calendarO INTEGER, " +
                "dao TEXT, dateO INTEGER, doubleP REAL, " +
                "doubleW REAL, floatP REAL, floatW REAL, " +
                "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                "integerP INTEGER, integerW INTEGER, " +
                "list BLOB, longP INTEGER, " +
                "longW INTEGER, " +
                "serialize BLOB, " +
                "shortP INTEGER, " +
                "shortW INTEGER, " +
                "stringO TEXT " +
                ");";
        String leftDbQuery = dbUtils.createTableSQL(AllFields.class, (Collection<RelationshipConfig>) null);
        assertEquals(correctQuery, leftDbQuery);
    }

    public void testCorrectCreateTableQueryWithNotNullFields() throws Exception {
        String correctQuery = "CREATE TABLE AllFieldsNotNull (" +
                "bigDecimalO TEXT NOT NULL, " +
                "booleanP INTEGER, " +
                "booleanW INTEGER NOT NULL, " +
                "calendarO INTEGER NOT NULL, " +
                "dao TEXT, " +
                "dateO INTEGER NOT NULL, " +
                "doubleP REAL, " +
                "doubleW REAL NOT NULL, " +
                "floatP REAL, " +
                "floatW REAL NOT NULL, " +
                "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                "integerP INTEGER, " +
                "integerW INTEGER NOT NULL, " +
                "list BLOB, " +
                "longP INTEGER, " +
                "longW INTEGER, " +
                "serialize BLOB NOT NULL, " +
                "shortP INTEGER, " +
                "shortW INTEGER NOT NULL, " +
                "stringO TEXT NOT NULL " +
                ");";
        String leftDbQuery = dbUtils.createTableSQL(AllFieldsNotNull.class, (Collection<RelationshipConfig>) null);
        assertEquals(correctQuery, leftDbQuery);
    }

    public void testInsertNullValuesIntoNullableFields() throws Exception {
        dbUtils.db.execSQL(dbUtils.createTableSQL(AllFields.class, (Collection<RelationshipConfig>) null));
        AllFields allFieldsNull = new AllFields(
                1,
                (short) 10,
                null,
                20,
                null,
                (long) 30,
                null,
                0.40f,
                null,
                0.50d,
                null,
                true,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        boolean nullIsInsertedException = false;
        try {
            dbUtils.add(allFieldsNull);
        } catch (SQLiteException e) {
            nullIsInsertedException = true;
        }
        assertFalse(nullIsInsertedException);
    }

    public void testInsertNullValuesIntoNotNullFields() throws Exception {
        dbUtils.db.execSQL(dbUtils.createTableSQL(AllFieldsNotNull.class, (Collection<RelationshipConfig>) null));
        AllFieldsNotNull allFieldsNull = new AllFieldsNotNull(
                1,
                (short) 10,
                null,
                20,
                null,
                (long) 30,
                null,
                0.40f,
                null,
                0.50d,
                null,
                true,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        boolean nullIsInsertedException = false;
        try {
            dbUtils.add(allFieldsNull);
        } catch (Exception e) {
            nullIsInsertedException = true;
        }
        assertTrue(nullIsInsertedException);
    }

    public class DBUtilsCreateTable extends LeftDBUtils {

        public DBUtilsCreateTable(Context context, String name, int version) {
            setDBContext(context, name, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            super.onCreate(db);
        }

        @Override
        protected String serializeObject(Object object) {
            try {
                return Arrays.toString(SerializeUtils.serialize(object));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected <T> T deserializeObject(String string, Class<T> tClass, Type genericType) {
            String[] byteValues = string.substring(1, string.length() - 1).split(",");
            byte[] bytes = new byte[byteValues.length];
            for (int i=0, len=bytes.length; i<len; i++) {
                bytes[i] = Byte.parseByte(byteValues[i].trim());
            }

            try {
                Object o = SerializeUtils.deserialize(bytes);
                if (o != null) {
                    return tClass.cast(o);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
