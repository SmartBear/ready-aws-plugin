package com.smartbear.aws;

import com.eviware.soapui.plugins.PluginAdapter;
import com.eviware.soapui.plugins.PluginConfiguration;
import com.eviware.soapui.support.UISupport;

@PluginConfiguration(groupId = "com.smartbear.plugins",
    name = "${project.name}",
    version = "${project.version}",
    autoDetect = true,
    description = "${project.description}",
    infoUrl = "${project.url}" )
public class PluginConfig extends PluginAdapter {
}