package com.smartbear.aws;

import com.eviware.soapui.plugins.PluginAdapter;
import com.eviware.soapui.plugins.PluginConfiguration;
import com.eviware.soapui.support.UISupport;

@PluginConfiguration(groupId = "com.smartbear.plugins", name = Strings.PluginInfo.NAME, version = "1.0",
        autoDetect = true, description = Strings.PluginInfo.DESCRIPTION, infoUrl = "" )
public class PluginConfig extends PluginAdapter {
        public PluginConfig(){
                super();

                UISupport.addResourceClassLoader(getClass().getClassLoader());
        }
}