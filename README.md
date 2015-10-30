# AWS API Gateway Plugin

===================

A plugin for Ready! API that allows you to import/export APIs directly from/to an AWS API Gateway Service.

Installation
------------

Install the plugin via the integrated Plugin Repository available via the Plugin Manager in Ready! API 1.6 and newer

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

Once installed there will have two ways to import an API from an AWS API Gateway Service:

* Via the "Add API from AWS API Gateway" option on the Project menu in the "Projects" tab
* Via the "AWS API Gateway" option in the "Create project from..." drop-down when creating a new project

In both cases you will be prompted for:
* input the Access Key and Secret Key to authenticate in the AWS API Gateway service
* select the region where you want to get APIs

Once a valid credentials and region have been specified you will be presented with optional project name, a list of available APIs and
import options. Now you can easily:

* send ad-hoc requests to the API to explore its functionality
* create functional tests of the API which you can further use to create Load Tests, Security Tests and API Monitors
(in the SoapUI NG module)
* create a load tests of the API (in the LoadUI NG module)
* create a security tests of the API (in the Secure module)
* create a virtualized version of the API for sandboxing/simulation purposes (in the ServiceV module).

Export
------

Another possibility is to export the selected REST service to the AWS API Gateway Service.

Follow these steps to perform export:
* select “Deploy API to AWS API Gateway” in the context menu for the selected REST service
* input the Access Key and Secret Key to authenticate in the AWS API Gateway service and select the region where you want to create API
* specify deployment options. If Proxy integration will select then each method will be configured with Integration of the HTTP Proxy type