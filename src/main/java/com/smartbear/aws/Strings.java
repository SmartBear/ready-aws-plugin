package com.smartbear.aws;

public final class Strings {
    private static final String REFERENCE_TO_ISSUES = " You can search and create issues for this plugin at https://github.com/SmartBear/ready-aws-plugin/issues.";
    private Strings() {
    }

    public static final class PluginInfo {
        public static final String NAME = "AWS API Gateway Plugin";
        public static final String DESCRIPTION = "Adds actions for importing APIs from Amazon Web Service API Gateway";
    }

    public static final class AddApiAction {
        public static final String NAME = "Add API from AWS API Gateway";
        public static final String DESCRIPTION = "Imports APIs from an Amazon Web Service API Gateway";
    }

    public static final class DeployApiAction {
        public static final String NAME = "Deploy API to AWS API Gateway";
        public static final String DESCRIPTION = "Deploy API to an Amazon Web Service API Gateway";
    }

    public static final class NewProjectAction {
        public static final String NAME = "Create Project From AWS API Gateway";
        public static final String DESCRIPTION = "Creates a new project for APIs imported from AWS API Gateway";
        public static final String UNABLE_CREATE_ERROR = "Failed to create the project due to %s exception with \"%s\" message";
        public static final String ACTION_CAPTION = "AWS API Gateway";
    }

    public static final class Error {
        public static final String INVALID_KEY = "Unable to initialize the encryption algorithm.";
        public static final String NO_SUCH_ALGORITHM = "Required encryption algorithm isn't available.";
        public static final String UNSUPPORTED_ENCODING = "Unable to encode the encrypted data to the UTF-8.";
        public static final String MALFORMED_URL = "URL format is invalid: %s";
        public static final String UNABLE_CREATE_CONNECTION = "Unable to open the connection to the specified host: %s";
        public static final String UNAVAILABLE_HOST = "The \"%s\" host is unavailable or invalid.";
        public static final String UNAVAILABLE_DATA = "No data available at the \"%s\" location." + REFERENCE_TO_ISSUES;
        public static final String INVALID_JSON_RESPONSE = "Can't parse JSON of the response of the request to the \"%s\" location." + REFERENCE_TO_ISSUES;
        public static final String UNEXPECTED_RESPONSE_FORMAT = "Unexpected response format of the JSON for the %s." + REFERENCE_TO_ISSUES;
        public static final String UNABLE_CONNECT = "Unable to connect to AWS REST API";
        public static final String UNABLE_SET_REQEST_BODY = "Unable to set request body";
        public static final String INVALID_REGION = "List of the API is empty. Please select the region where were registered your APIs.";

        public static final String ABSENT_ENDPOINT = "The RestService has no endpoints to select";
        public static final String EMPTY_ENDPOINT = "Please select nonempty endpoint";
        public static final String INVALID_ENDPOINT_FORMAT = "Selected endpoint has invalid URL format";
        public static final String EMPTY_DEFAULT_RESPONSE = "Please enter default response";
        public static final String INVALID_DEFAULT_RESPONSE = "Invalid value of the default response, please see available HTTP status codes in the RFC 7231";

    }

    public static final class AccountInfoDialog {
        public static final String PROMPT_API_DIALOG_CAPTION = "Import APIs from AWS API Gateway";
        public static final String PROMPT_API_DIALOG_DESCRIPTION = "Enter an account details";

        public static final String ACCESS_KEY_LABEL = "Access Key";
        public static final String ACCESS_KEY_DESCRIPTION = "Please enter access key ID";
        public static final String SECRET_KEY_LABEL = "Secret Key";
        public static final String SECRET_KEY_DESCRIPTION = "Please enter secret access key";
        public static final String REGION_LABEL = "Region";
        public static final String REGION_DESCRIPTION = "Please enter region";
        public static final String EMPTY_FIELD_WARNING = "Please enter '%s'";
    }

    public static final class SelectApiDialog {
        public static final String PROJECT_LABEL = "Project Name";
        public static final String PROJECT_DESCRIPTION = "Name of the project";
        public static final String EMPTY_PROJECT_WARNING = "Please enter project name";

        public static final String CAPTION = "Select APIs to Import";
        public static final String DESCRIPTION = "Please select APIs you would like to import into the project from the list below";
        public static final String NAME_LABEL = "API Name";
        public static final String DESCRIPTION_LABEL = "API Description";
        public static final String DEFINITION_LABEL = "API Definition";
        public static final String GEN_TEST_SUITE = "Generate Test Suite";
        public static final String GEN_LOAD_TEST = "Generate Load Test";
        public static final String GEN_SECUR_TEST = "Generate Security Test";
        public static final String GEN_VIRT_HOST = "Generate Virtual Host";
        public static final String NOTHING_SELECTED_WARNING = "Please select at least one API to import";
    }

    public static final class DeploymentSettingDialog {
        public static final String NAME = "Deployment setting";
        public static final String DESCRIPTION = "Enable 'Proxy integration' option to configure the AWS service as proxy";
        public static final String PROXY_INTEGRATION_LABEL = "Proxy integration";
        public static final String ENDPOINTS_LABEL = "Endpoints";
        public static final String ENDPOINTS_DESCRIPTION = "Enter service endpoints";
        public static final String DEFAULT_RESPONSE_LABEL = "Default response";
        public static final String DEFAULT_RESPONSE_DESCRIPTION = "Enter default method response";
        public static final String INTEGRATION_OPTIONS = "Integration options";

    }

    public static final class ApiImporter {
        public static final String IMPORT_PROGRESS = "Importing APIs...";
        public static final String IMPORT_ERROR = "Failed to read API description for [%s] - [%s]\n";
        public static final String IMPORT_ERROR_TAIL = "You can search and create issues for this plugin at https://github.com/SmartBear/ready-aws-plugin/issues.";
    }

    public static final class ApiExporter {
        public static final String EXPORT_PROGRESS = "Deploying API...";
        public static final String EXPORT_ERROR = "Failed to deploy API for [%s] - [%s]\n";
        public static final String EXPORT_ERROR_TAIL = ApiImporter.IMPORT_ERROR_TAIL;
    }

    public static final class ApiListLoader {
        public static final String LOAD_PROGRESS = "Reading APIs...";
    }
}
