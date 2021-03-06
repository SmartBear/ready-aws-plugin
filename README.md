# Amazon API Gateway Plugin

===================

A plugin for ReadyAPI that allows you to import/export APIs directly from/to an Amazon API Gateway Service.

Installation
------------

Install the plugin via the integrated Plugin Repository available via the Plugin Manager in ReadyAPI 1.6 and newer

Build it yourself
-----------------

You can build the plugin by oneself by cloning this repository locally - make sure you have java and maven 3.X correctly 
installed - and run 

```mvn clean install assembly:single```

in the project folder. The plugin dist.jar will be created in the target folder and can be installed via the 
Plugin Managers' "Load from File" action.

Usage
-----

Import
------

Once installed, this plugin provides two ways to import an API from an Amazon API Gateway Service:

* Via the "Add API from AWS API Gateway" option on the Project menu in the "Projects" tab
* Via the "AWS API Gateway" option in the "Create project from..." drop-down when creating a new project

In both cases you will be prompted for:
* the Access Key and Secret Key to authorize in the AWS API Gateway service
* the region where you want to get APIs

After you specify valid credentials and a region, you will be presented with optional project name, a list of available APIs and import options. Now you can easily:

* send ad-hoc requests to the API to explore its functionality
* create functional tests of the API which you can further use to create Load Tests, Security Tests and API Monitors
(in the SoapUI module)
* create a load test of the API (in the LoadUI module)
* create a security test of the API (in the Secure module)
* create a virtualized version of the API for sandboxing/simulation purposes (in the ServiceV module).

Export
------

Another possibility is to export the selected REST service to the Amazon API Gateway Service.

Follow these steps to perform export:
* select “Deploy API to AWS API Gateway” in the context menu for the selected REST service
* input the Access Key and Secret Key to authorize in the AWS API Gateway service and select the region where you want to create API
* specify deployment options. If you select Proxy integration, each method will be configured with Integration of the HTTP Proxy type
