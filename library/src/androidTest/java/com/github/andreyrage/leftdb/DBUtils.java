/*
 * Copyright 2017 Andrii Horishnii
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.andreyrage.leftdb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.github.andreyrage.leftdb.entities.AllFields;
import com.github.andreyrage.leftdb.entities.AnnotationId;
import com.github.andreyrage.leftdb.entities.AutoIncId;
import com.github.andreyrage.leftdb.entities.ChildManyCustomName;
import com.github.andreyrage.leftdb.entities.ChildOne;
import com.github.andreyrage.leftdb.entities.ExtendEntity;
import com.github.andreyrage.leftdb.entities.FloatKey;
import com.github.andreyrage.leftdb.entities.FloatKeyChild;
import com.github.andreyrage.leftdb.entities.NotAnnotationId;
import com.github.andreyrage.leftdb.entities.ParentManyArrayCustomName;
import com.github.andreyrage.leftdb.entities.ParentOne;
import com.github.andreyrage.leftdb.entities.PrimaryKeyId;
import com.github.andreyrage.leftdb.entities.SerializableObject;
import com.github.andreyrage.leftdb.entities.StringKey;
import com.github.andreyrage.leftdb.entities.StringKeyChild;
import com.github.andreyrage.leftdb.utils.SerializeUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;

public class DBUtils extends LeftDBUtils {

	public static DBUtils newInstance(Context context, String name, int version) {
		DBUtils dbUtils = new DBUtils();
		dbUtils.setDBContext(context, name, version);
		return dbUtils;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		super.onCreate(db);
		createTable(db, AllFields.class);
		createTable(db, SerializableObject.class);
		createTable(db, ExtendEntity.class);
		createTables(db, Arrays.asList(
				AutoIncId.class,
				ChildManyCustomName.class,
				ChildOne.class,
				ParentManyArrayCustomName.class,
				ParentOne.class,
				AnnotationId.class,
				NotAnnotationId.class,
				PrimaryKeyId.class,
				StringKey.class,
				StringKeyChild.class,
				FloatKey.class,
				FloatKeyChild.class
		));

		createRelationship(db, ParentOne.class);
		createRelationship(db, ParentManyArrayCustomName.class);
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
