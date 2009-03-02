/*
 * SymmetricDS is an open source database synchronization solution.
 *
 * Copyright (C) Keith Naas <knaas@users.sourceforge.net>
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.jumpmind.symmetric.db.h2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jumpmind.symmetric.db.AbstractDbDialect;
import org.jumpmind.symmetric.db.BinaryEncoding;
import org.jumpmind.symmetric.db.IDbDialect;
import org.jumpmind.symmetric.model.Node;
import org.jumpmind.symmetric.model.Trigger;
import org.jumpmind.symmetric.model.TriggerHistory;

public class H2Dialect extends AbstractDbDialect implements IDbDialect {

    static final Log logger = LogFactory.getLog(H2Dialect.class);
    private boolean storesUpperCaseNames = true;
    
    protected void initForSpecificDialect() {
        jdbcTemplate
                .update("CREATE ALIAS IF NOT EXISTS BASE64_ENCODE for \"org.jumpmind.symmetric.db.h2.H2Functions.encodeBase64\"");
    }

    protected boolean doesTriggerExistOnPlatform(String catalogName, String schema, String tableName, String triggerName) {
        boolean exists = jdbcTemplate.queryForInt("select count(*) from INFORMATION_SCHEMA.TRIGGERS WHERE TRIGGER_NAME = ?",
                new Object[] { triggerName }) > 0;
        if (!exists) {
            exists = jdbcTemplate.queryForInt("select count(*) from INFORMATION_SCHEMA.VIEWS WHERE TABLE_NAME = ?",
                    new Object[] { String.format("%s_VIEW", triggerName) }) > 0;
        }
        return exists;
    }

    public void removeTrigger(String schemaName, String triggerName, TriggerHistory hist) {
        try {
            int count = jdbcTemplate.update(String.format("DROP TRIGGER IF EXISTS %s", triggerName));
            if (count > 0) {
                logger.info(String.format("Just dropped trigger %s", triggerName));
            }
            count = jdbcTemplate.update(String.format("DROP VIEW IF EXISTS %s_VIEW", triggerName));
            if (count > 0) {
                logger.info(String.format("Just dropped view %s_VIEW", triggerName));
            }
        } catch (Exception e) {
            logger.warn("Error removing " + triggerName + ": " + e.getMessage());
        }
    }

    /**
     * All the templates have ' escaped because the SQL is inserted into a view.  When returning the raw SQL
     * for use as SQL it needs to be un-escaped.
     */
    @Override
    public String createInitalLoadSqlFor(Node node, Trigger trigger) {
        String sql = super.createInitalLoadSqlFor(node, trigger);
        sql = sql.replace("''", "'");
        return sql;
    }
    
    public void removeTrigger(String catalogName, String schemaName, String triggerName, String tableName,
            TriggerHistory oldHistory) {
        removeTrigger(schemaName, triggerName, oldHistory);        
    }

    @Override
    public boolean isBlobSyncSupported() {
        return true;
    }

    @Override
    public boolean isClobSyncSupported() {
        return true;
    }

    public void disableSyncTriggers(String nodeId) {
        jdbcTemplate.update("set @sync_prevented=1");
        jdbcTemplate.update("set @node_value=?", new Object[] {nodeId});
    }

    public void enableSyncTriggers() {
        jdbcTemplate.update("set @sync_prevented=null");
        jdbcTemplate.update("set @node_value=null");
    }

    public String getSyncTriggersExpression() {
        return " @sync_prevented is null ";
    }

    /**
     * An expression which the java trigger can string replace
     */
    public String getTransactionTriggerExpression(Trigger trigger) {
        return H2Trigger.TX_REPLACEMENT_TOKEN;
    }

    public String getSelectLastInsertIdSql(String sequenceName) {
        return "call IDENTITY()";
    }

    @Override
    public BinaryEncoding getBinaryEncoding() {
        return BinaryEncoding.BASE64;
    }

    public boolean isCharSpacePadded() {
        return false;
    }

    public boolean isCharSpaceTrimmed() {
        return true;
    }

    public boolean isEmptyStringNulled() {
        return false;
    }

    public boolean storesUpperCaseNamesInCatalog() {
        return storesUpperCaseNames;
    }

    public boolean supportsGetGeneratedKeys() {
        return false;
    }    
    
    @Override
    public boolean supportsTransactionId() {
        return true;
    }    

    protected boolean allowsNullForIdentityColumn() {
        return false;
    }

    public void purge() {
    }

    public String getDefaultCatalog() {
        return null;
    }

    public String getDefaultSchema() {
        return null;
    }
    
    public String getInitialLoadTableAlias() {
        return "t.";
    }

}
