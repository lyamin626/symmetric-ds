/*
 * Licensed to JumpMind Inc under one or more contributor 
 * license agreements.  See the NOTICE file distributed
 * with this work for additional information regarding 
 * copyright ownership.  JumpMind Inc licenses this file
 * to you under the GNU Lesser General Public License (the
 * "License"); you may not use this file except in compliance
 * with the License. 
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, see           
 * <http://www.gnu.org/licenses/>.
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License. 
 */
package org.jumpmind.symmetric.fs.config;

public class Node {

    protected String nodeId;
    protected String groupId;
    protected String syncUrl;
    protected String securityToken;
    
    public Node(String nodeId, String groupId, String syncUrl, String securityToken) {
        this.nodeId = nodeId;
        this.groupId = groupId;
        this.syncUrl = syncUrl;
        this.securityToken = securityToken;
    }
    
    public Node() {     
    }

    public String getNodeId() {
        return nodeId;
    }
    
    public String getGroupId() {
        return groupId;
    }
    
    public String getSyncUrl() {
        return syncUrl;
    }
   
    public String getSecurityToken() {
        return securityToken;
    }
    
}