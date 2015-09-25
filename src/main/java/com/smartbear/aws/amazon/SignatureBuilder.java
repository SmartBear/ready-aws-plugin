package com.smartbear.aws.amazon;

import com.smartbear.aws.ApplicationException;
import com.smartbear.aws.Strings;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public final class SignatureBuilder {
    private final String region;
    private final String service;
    private final String signedHeaders;
    private final String algorithm;
    private final String host;
    private final String dateStamp;
    private final String amzDate;

    private final String accessKey;
    private final String secretKey;

    private final String credentialScope;

    public SignatureBuilder(String accessKey, String secretKey, String region) {
        this.region = region;
        this.service = "apigateway";
        this.signedHeaders = "host;x-amz-date";
        this.algorithm = "AWS4-HMAC-SHA256";
        this.host = String.format("apigateway.%s.amazonaws.com", region);

        final Date date = new Date();
        this.amzDate = this.fmtDate(date, "yyyyMMdd'T'HHmmss'Z'");
        this.dateStamp = this.fmtDate(date, "yyyyMMdd");

        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.credentialScope = String.format("%s/%s/%s/aws4_request", this.dateStamp, this.region, this.service);
    }

    public String getAmzDate() {
        return this.amzDate;
    }

    public String getHost() {
        return this.host;
    }

    public String buildAuthHeader(String method, String uri, String query, String body) throws ApplicationException {
        try {
            byte[] signingKeyBytes = this.getSignatureKey();

            String payloadHash = sha256(body);

            String canonicalUri = uri;
            String canonicalQuerystring = query;
            String canonicalHeaders = String.format("host:%s\nx-amz-date:%s\n", host, amzDate);

            String canonicalRequest = String.format("%s\n%s\n%s\n%s\n%s\n%s",
                    method,
                    canonicalUri,
                    canonicalQuerystring,
                    canonicalHeaders,
                    signedHeaders,
                    payloadHash);
            String stringToSign = String.format("%s\n%s\n%s\n%s",
                    algorithm,
                    amzDate,
                    credentialScope,
                    sha256(canonicalRequest));

            String signature = convertToHex(HmacSHA256(stringToSign, signingKeyBytes));

            return fmtAuthorizationHeader(signature);
        } catch (InvalidKeyException e) {
            throw new ApplicationException(Strings.Error.INVALID_KEY, e);
        } catch (NoSuchAlgorithmException e) {
            throw new ApplicationException(Strings.Error.NO_SUCH_ALGORITHM, e);
        } catch (UnsupportedEncodingException e) {
            throw new ApplicationException(Strings.Error.UNSUPPORTED_ENCODING, e);
        }
    }

    private String fmtDate(Date date, String format) {
        final TimeZone utc = TimeZone.getTimeZone("UTC");
        final SimpleDateFormat fmt = new SimpleDateFormat(format);
        fmt.setTimeZone(utc);
        return fmt.format(date);
    }

    private String fmtAuthorizationHeader(String signature) {
        return String.format("%s Credential=%s/%s, SignedHeaders=%s, Signature=%s", algorithm, accessKey, credentialScope, signedHeaders, signature);
    }

    private byte[] getSignatureKey() throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException  {
        final byte[] kSecret = ("AWS4" + secretKey).getBytes("UTF8");
        final byte[] kDate    = HmacSHA256(dateStamp, kSecret);
        final byte[] kRegion  = HmacSHA256(region, kDate);
        final byte[] kService = HmacSHA256(service, kRegion);
        final byte[] kSigning = HmacSHA256("aws4_request", kService);
        return kSigning;
    }

    private static byte[] HmacSHA256(String data, byte[] key) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        final String algorithm="HmacSHA256";
        final Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(key, algorithm));
        return mac.doFinal(data.getBytes("UTF8"));
    }

    static String sha256(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA-256");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

    static String convertToHex(byte[] value) {
        return org.apache.commons.codec.binary.Hex.encodeHexString(value);
    }
}
