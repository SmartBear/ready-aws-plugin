package com.smartbear.aws;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.submit.filters.AbstractRequestFilter;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.plugins.auto.PluginRequestFilter;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.smartbear.aws.amazon.ApiExecutionSignatureBuilder;
import com.smartbear.aws.amazon.SignatureBuilder;
import com.smartbear.rapisupport.RapiLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

@PluginRequestFilter(protocol = "https")
public class AwsIamAuthRequestFilter extends AbstractRequestFilter {
    public static final String DATE_HEADER = "X-Amz-Date";
    public static final String AUTH_HEADER = "Authorization";
    public static final String CREDENTIAL_PROPERTY_TMPL = "aws-credential-%s";

    @Override
    public void filterRestRequest(SubmitContext context, RestRequestInterface request) {
        setAwsHeaders(request);

        super.filterRestRequest(context, request);
    }

    @Override
    public void afterRestRequest(SubmitContext context, RestRequestInterface request) {
        RestMethod method = request.getRestMethod();
        if (method.hasProperty(DATE_HEADER) && method.hasProperty(AUTH_HEADER)) {
            StringToStringsMap headers = request.getRequestHeaders();
            headers.remove(DATE_HEADER);
            headers.remove(AUTH_HEADER);
            request.setRequestHeaders(headers);
        }
        super.afterRestRequest(context, request);
    }

    private void setAwsHeaders(RestRequestInterface request) {
        RestMethod method = request.getRestMethod();
        if (!method.hasProperty(DATE_HEADER) || !method.hasProperty(AUTH_HEADER)) {
            return;
        }

        SignatureBuilder signatureBuilder = createSignatureBuilder(request);
        if (signatureBuilder == null) {
            return;
        }

        try {
            String httpMethod = request.getMethod().toString();
            String uri = request.getPath();
            String query = getQueryString(request);
            String authHeader = signatureBuilder.buildAuthHeader(httpMethod, uri, query, "");

            StringToStringsMap headers = request.getRequestHeaders();
            headers.remove(DATE_HEADER);
            headers.put(DATE_HEADER, signatureBuilder.getAmzDate());
            headers.remove(AUTH_HEADER);
            headers.put(AUTH_HEADER, authHeader);
            request.setRequestHeaders(headers);
        } catch (ApplicationException ex) {
            RapiLogger.logError(ex, "Error during set the AWS authorization header");
        }
    }

    private String getQueryString(RestRequestInterface request) {
        List<String> result = new LinkedList<>();
        for (String key: request.getParams().keySet()) {
            RestParamProperty param = request.getParams().getProperty(key);
            if (param.getStyle() != RestParamsPropertyHolder.ParameterStyle.QUERY) {
                continue;
            }

            String value = param.getValue();
            if (StringUtils.isNullOrEmpty(value)) {
                continue;
            }

            result.add(String.format("%s=%s", key, value));
        }
        return StringUtils.join(result.toArray(new String[] {}), "&");
    }


    private SignatureBuilder createSignatureBuilder(RestRequestInterface request) {
        String host = getHost(request);
        if (StringUtils.isNullOrEmpty(host)) {
            return null;
        }

        RestMethod method = request.getRestMethod();
        WsdlProject project = method.getProject();
        String serviceName = method.getInterface().getName();
        String credentialProperty = String.format(CREDENTIAL_PROPERTY_TMPL, serviceName.replaceAll("\\s", "-"));
        if (!project.hasProperty(credentialProperty)) {
            return null;
        }
        String credential = project.getProperty(credentialProperty).getValue();
        if (StringUtils.isNullOrEmpty(credential)) {
            return null;
        }
        String[] parts = credential.split("###");
        if (parts.length != 3) {
            return null;
        }

        String accessKey = parts[0];
        String secretKey = parts[1];
        String region = parts[2];
        return new ApiExecutionSignatureBuilder(accessKey, secretKey, region, host);
    }

    private String getHost(RestRequestInterface request) {
        if (!request.hasEndpoint()) {
            return "";
        }
        try {
            URL url = new URL(request.getEndpoint());
            return url.getHost();
        } catch (MalformedURLException ex) {
            return "";
        }
    }
}
