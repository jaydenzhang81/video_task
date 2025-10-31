package com.videotask.util;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * 视频文件下载工具类
 */
@Component
public class VideoDownloadUtil {

    private final RestTemplate restTemplate;

    public VideoDownloadUtil() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * 从URL下载视频文件并转换为MultipartFile
     * @param videoUrl 视频URL
     * @return MultipartFile对象
     */
    public MultipartFile downloadVideoFile(String videoUrl) {
        try {
            // 获取文件名
            String fileName = getFileNameFromUrl(videoUrl);
            
            // 下载文件内容
            byte[] fileContent = downloadFileContent(videoUrl);
            
            // 获取文件类型
            String contentType = getContentType(videoUrl);
            
            return createMultipartFile(fileName, contentType, fileContent);
            
        } catch (Exception e) {
            System.err.println("下载视频文件失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 从URL获取文件名
     */
    private String getFileNameFromUrl(String url) {
        try {
            String fileName = new File(new URL(url).getPath()).getName();
            if (fileName.isEmpty() || !fileName.contains(".")) {
                fileName = "video.mp4"; // 默认文件名
            }
            return fileName;
        } catch (Exception e) {
            return "video.mp4";
        }
    }

    /**
     * 下载文件内容
     */
    private byte[] downloadFileContent(String url) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<byte[]> response = restTemplate.exchange(
            url, HttpMethod.GET, entity, byte[].class
        );
        
        return response.getBody();
    }

    /**
     * 获取文件类型
     */
    private String getContentType(String url) {
        try {
            URLConnection connection = new URL(url).openConnection();
            String contentType = connection.getContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "video/mp4"; // 默认类型
            }
            return contentType;
        } catch (Exception e) {
            return "video/mp4";
        }
    }

    /**
     * 创建MultipartFile对象
     */
    private MultipartFile createMultipartFile(String fileName, String contentType, byte[] content) {
        return new MultipartFile() {
            @Override
            public String getName() {
                return "video";
            }

            @Override
            public String getOriginalFilename() {
                return fileName;
            }

            @Override
            public String getContentType() {
                return contentType;
            }

            @Override
            public boolean isEmpty() {
                return content == null || content.length == 0;
            }

            @Override
            public long getSize() {
                return content != null ? content.length : 0;
            }

            @Override
            public byte[] getBytes() throws IOException {
                return content;
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(content);
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                if (content != null) {
                    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(dest)) {
                        fos.write(content);
                    }
                }
            }
        };
    }

    /**
     * 检查URL是否可访问
     */
    public boolean isUrlAccessible(String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.HEAD, entity, String.class
            );
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取文件大小
     */
    public long getFileSize(String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.HEAD, entity, String.class
            );
            
            String contentLength = response.getHeaders().getFirst("Content-Length");
            if (contentLength != null) {
                return Long.parseLong(contentLength);
            }
            
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
