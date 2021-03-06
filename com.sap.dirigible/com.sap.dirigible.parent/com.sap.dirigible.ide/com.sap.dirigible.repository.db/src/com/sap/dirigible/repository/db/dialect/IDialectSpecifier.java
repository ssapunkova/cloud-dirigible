/*******************************************************************************
 * Copyright (c) 2014 SAP AG or an SAP affiliate company. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 *******************************************************************************/

package com.sap.dirigible.repository.db.dialect;

import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface IDialectSpecifier {

	public static final String DIALECT_TIMESTAMP = "$TIMESTAMP$"; //$NON-NLS-1$
	public static final String DIALECT_BLOB = "$BLOB$"; //$NON-NLS-1$
	public static final String DIALECT_CURRENT_TIMESTAMP = "$CURRENT_TIMESTAMP$"; //$NON-NLS-1$

	String specify(String sql);

	String getSpecificType(String commonType);

	String createLimitAndOffset(int limit, int offset);

	String createTopAndStart(int limit, int offset);
	
	boolean isSchemaFilterSupported();
	
	String getSchemaFilterScript();
	
	String getAlterAddOpen();
	
	String getAlterAddClose();

	InputStream getBinaryStream(ResultSet resultSet, String columnName) throws SQLException;

}
