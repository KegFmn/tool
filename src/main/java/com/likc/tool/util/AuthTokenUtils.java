package com.likc.tool.util;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class AuthTokenUtils {

    public static byte[] readAsBytes(InputStreamCacher cacher){
        int len = cacher.getLength();
        byte[] buffer = new byte[len];
        try(InputStream in = cacher.getInputStream()){
            in.read(buffer, 0, len);
        }catch (IOException e){
            e.printStackTrace();
        }
        return buffer;
    }

    // 根据SecretKey计算签名
    public static String genSignature(String secretKey, byte[] binaryparams){
        HashFunction function = Hashing.hmacSha256(secretKey.getBytes());
        HashCode code = function.hashBytes(binaryparams);
        return code.toString();
    }

    // 校验签名
    public static boolean isValidToken(String secretKey, InputStreamCacher cacher, String signature ) {
        if (StringUtils.isEmpty(secretKey) || StringUtils.isEmpty(signature)) {
            return false;
        }
        String reSignature = genSignature(secretKey,readAsBytes(cacher));
        log.info("secretKey: " + secretKey);
        log.info("content: " + new String(readAsBytes(cacher)));
        log.info("signature: " + signature);
        log.info("reSignature: " + reSignature);
        return signature.equals(reSignature);
    }

    public static class InputStreamCacher {
        private int length;
        private ByteArrayOutputStream byteArrayOutputStream = null;

        public InputStreamCacher(InputStream inputStream) {
            if (inputStream == null)
                return;
            length = 0;
            byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            try {
                while ((len = inputStream.read(buffer)) > -1 ) {
                    byteArrayOutputStream.write(buffer, 0, len);
                    length += len;
                }
                byteArrayOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public InputStream getInputStream() {
            if (byteArrayOutputStream== null)
                return null;
            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        }

        public int getLength() {
            return length;
        }
    }
}