/**
 * Licensed to JumpMind Inc under one or more contributor
 * license agreements.  See the NOTICE file distributed
 * with this work for additional information regarding
 * copyright ownership.  JumpMind Inc licenses this file
 * to you under the GNU General Public License, version 3.0 (GPLv3)
 * (the "License"); you may not use this file except in compliance
 * with the License.
 *
 * You should have received a copy of the GNU General Public License,
 * version 3.0 (GPLv3) along with this library; if not, see
 * <http://www.gnu.org/licenses/>.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jumpmind.db.platform.db2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.jumpmind.db.model.Column;
import org.jumpmind.db.model.Table;
import org.jumpmind.db.platform.DatabaseMetaDataWrapper;
import org.jumpmind.db.platform.IDatabasePlatform;
import org.jumpmind.db.sql.JdbcSqlTemplate;

public class Db2zOsDdlReader extends Db2DdlReader {

    public Db2zOsDdlReader(IDatabasePlatform platform) {
        super(platform);
    }
    
    @Override
    protected void enhanceTableMetaData(Connection connection, DatabaseMetaDataWrapper metaData, Table table) throws SQLException {
        log.debug("about to read additional column data");
        /* DB2 does not return the auto-increment status via the database
         metadata */
        String sql = "SELECT NAME, DEFAULT FROM SYSIBM.SYSCOLUMNS WHERE TBNAME=?";
        if (StringUtils.isNotBlank(metaData.getSchemaPattern())) {
            sql = sql + " AND TBCREATOR=?";
        }

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, table.getName());
            if (StringUtils.isNotBlank(metaData.getSchemaPattern())) {
                pstmt.setString(2, metaData.getSchemaPattern());
            }

            rs = pstmt.executeQuery();
            while (rs.next()) {
                String columnName = rs.getString(1);
                Column column = table.getColumnWithName(columnName);
                if (column != null) {
                    String isIdentity = rs.getString(2);
                    if (isIdentity != null && 
                            (isIdentity.startsWith("I") || isIdentity.startsWith("J"))) {
                        column.setAutoIncrement(true);
                        log.debug("Found identity column {} on {}", columnName, table.getName());
                    }
                }
            }
        } finally {
            JdbcSqlTemplate.close(rs);
            JdbcSqlTemplate.close(pstmt);
        }
        log.debug("done reading additional column data");
        
    }
    

}
