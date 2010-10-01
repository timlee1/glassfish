/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admingui.common.util;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.glassfish.admingui.common.handlers.RestApiHandlers;


/**
 *
 * @author anilam
 */
public class AppUtil {

    public static List<String> getSnifferListOfModule(String appName, String moduleName){
        Map subMap = RestApiHandlers.restRequest(
            GuiUtil.getSessionValue("REST_URL")+"/applications/application/" + appName + "/module/" + moduleName + "/engine", null, "GET", null);
        final Map dataMap = (Map) subMap.get("data");
        List sniffersList = new ArrayList();
        if (dataMap != null){
            final Map extraProperties = (Map)(dataMap).get("extraProperties");
            if (extraProperties != null){
                final Map<String, Object> childResourcesMap = (Map) extraProperties.get("childResources");
                if (childResourcesMap != null){
                    //List<String> sniffers =  new ArrayList( childResourcesMap.keySet());
                    for (String oneSniffer: childResourcesMap.keySet()){
                        if (sniffersHide.contains(oneSniffer) )
                            continue;
                        sniffersList.add(oneSniffer);
                    }
                    Collections.sort(sniffersList);
                    return sniffersList;
                }
            }
        }
        return sniffersList;
    }

    public static boolean isApplicationEnabled(String appName,  String target){
        String prefix = (String) GuiUtil.getSessionValue("REST_URL");
        List clusters = TargetUtil.getClusters();
        List standalone = TargetUtil.getStandaloneInstances();
        standalone.add("server");
        Map attrs = null;
        String endpoint="";
        if (clusters.contains(target)){
            endpoint = prefix + "/clusters/cluster/" + target + "/application-ref/" + appName;
            attrs = RestApiHandlers.getAttributesMap(prefix + endpoint);
        }else{
            endpoint = prefix+"/servers/server/" + target + "/application-ref/" + appName;
            attrs = RestApiHandlers.getAttributesMap(endpoint);
        }
        return Boolean.parseBoolean((String) attrs.get("enabled"));
    }

    static public Map getWsEndpointMap(String appName, String moduleName, List snifferList){
        Map wsAppMap = new HashMap();
        try{
            String encodedAppName = URLEncoder.encode(appName, "UTF-8");
            String encodedModuleName = URLEncoder.encode(moduleName, "UTF-8");
            String prefix = GuiUtil.getSessionValue("REST_URL") + "/applications/application/";
            if (snifferList.contains("webservices")){
                Map wsAttrMap = new HashMap();
                wsAttrMap.put("applicationname", encodedAppName);
                wsAttrMap.put("modulename", encodedModuleName);
                Map wsMap = RestApiHandlers.restRequest(prefix+"list-webservices", wsAttrMap, "GET", null);
                Map extraProps = (Map)((Map)wsMap.get("data")).get("extraProperties");
                if (extraProps != null){
                    wsAppMap = (Map) extraProps.get(appName);
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return wsAppMap;
    }

    static public Map getEndpointDetails(Map wsEndpointMap, String moduleName, String componentName){
        if (wsEndpointMap == null){
            return null;
        }
        Map modMap = (Map) wsEndpointMap.get(moduleName);
        if (modMap == null){
            return null;
        }
        return (Map) modMap.get(componentName);
    }

    static final public List sniffersHide = new ArrayList();
    static {
        sniffersHide.add("security");
    }
}

