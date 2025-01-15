package com.likc.tool.util;



import com.likc.tool.common.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.time.Instant;
import java.util.zip.GZIPInputStream;

@Slf4j
public class FileUtils {

    public static File MultipartToFile(MultipartFile multipartFile) {
        File file;
        try {
            String originalFilename = multipartFile.getOriginalFilename();
            if (StringUtils.isBlank(originalFilename)) {
                throw new BizException("文件名为空");
            }

            String[] fileName = originalFilename.split("\\.(?=[^.]*$)", 2);

            file = createTempFile(fileName[0] + "__", "." + fileName[1]);
            //使用MultipartFile的transferTo()方法将文件内容传输到File对象中
            multipartFile.transferTo(file);
        } catch (IOException e) {
            log.error("转换失败: {}", e.getMessage(), e);
            throw new BizException("文件转换失败");
        }
        //返回转换后的File对象
        return file;
    }

    public static File downLoadFile(String url) {
        byte[] imageData = downloadFileAsByteArray(url, null);
        String format = getFileFormat(url);

        try {
            String md5 = DigestUtils.md5DigestAsHex(url.getBytes()) + Instant.now().toEpochMilli();
            File tempFile = createTempFile("download_" + md5, "." + format);
            try (OutputStream out = new FileOutputStream(tempFile)) {
                out.write(imageData);
            }

            return tempFile;
        } catch (IOException e) {
            throw new BizException("字节流写入File异常");
        }
    }

    public static byte[] downloadFileAsByteArray(String fileUrl, String compressionAlgorithm) {
        try {
            URL url = new URL(fileUrl);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;

            if ("GZIP".equals(compressionAlgorithm)) {
                try (InputStream in = new GZIPInputStream(url.openStream())) {
                    while ((bytesRead = in.read(buffer)) != -1) {
                        byteArrayOutputStream.write(buffer, 0, bytesRead);
                    }
                }
            } else {
                try (InputStream in = url.openStream()) {
                    while ((bytesRead = in.read(buffer)) != -1) {
                        byteArrayOutputStream.write(buffer, 0, bytesRead);
                    }
                }
            }

            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new BizException(String.format("下载文件发生异常: %s", e.getMessage()));
        }
    }

    public static String getFileFormat(String fileUrl) {
        String extension;
        try {
            URL url = new URL(fileUrl);
            String path = url.getPath();

            if (StringUtils.isBlank(path)) {
                throw new BizException("文件下载URL有误");
            }

            String[] parts = path.split("/");
            String filename = parts[parts.length - 1];

            int dotIndex = filename.lastIndexOf('.');
            if (dotIndex > -1 && dotIndex < filename.length() - 1) {
                extension = filename.substring(dotIndex + 1);
            } else {
                throw new BizException("无法获取文件格式");
            }
        } catch (Exception e) {
            throw new BizException("无法获取文件格式");
        }

        return extension;
    }

    public static File createTempFile(String prefix, String suffix)  {
        String tmpDirPath = System.getProperty("java.io.tmpdir");
        File tmpDir = new File(tmpDirPath);
        File tempFile;
        try {
            tempFile = File.createTempFile(prefix, suffix, tmpDir);
        } catch (IOException e) {
            log.error("创建临时文件异常", e);
            throw new BizException("创建临时文件异常");
        }

        tempFile.deleteOnExit();
        return tempFile;
    }

    public static void writeCsvFile(File file, String content, String charsetName) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file, false)) {
            fileOutputStream.write(content.getBytes(charsetName));
            fileOutputStream.flush();
        } catch (IOException e) {
            log.error("写入CSV文件失败", e);
            throw new BizException("写入CSV文件失败");
        }
    }

    /**
     * 安全删除临时文件
     *
     * @param file
     * @throws IOException
     */
    public static void deleteFile(File file) {
        if (file.exists() && file.canWrite()) {
            if (!file.delete()) {
                log.error("文件删除失败: {}", file.getAbsolutePath());
                throw new BizException("文件删除失败: " + file.getAbsolutePath());
            }
        } else {
            log.error("文件不存在或不可写: {}", file.getAbsolutePath());
            throw new BizException("文件不存在或不可写: " + file.getAbsolutePath());
        }
    }
}
