package cn.sdu.clouddrive.userservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@Service
/**
 * 图片托管服务
 * 负责将用户上传的图片文件上传到指定的图片托管平台
 */
public class ImageHostingService {

    @Autowired
    private RestTemplate restTemplate;

    // 图片托管平台配置
    private static final String API_BASE_URL = "https://picui.cn/api/v1";
    private static final String UPLOAD_URL = API_BASE_URL + "/upload";

    // 这里需要配置你的图片托管平台token
    @Value("${image.hosting.token:}")
    private String hostingToken;

    /**
     * 上传图片到托管平台
     */
    public String uploadImage(MultipartFile file) {
        try {
//            log.info("开始上传图片到托管平台，文件名: {}", file.getOriginalFilename());

            // 创建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Accept", "application/json");

            // 如果有token，添加授权头
            if (hostingToken != null && !hostingToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + hostingToken);
            }

            // 创建文件资源
            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            // 创建请求体
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileResource);
            body.add("permission", 1); // 1=公开

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 发送请求
            ResponseEntity<Map> response = restTemplate.exchange(
                UPLOAD_URL,
                HttpMethod.POST,
                requestEntity,
                Map.class
            );

            // 检查响应状态
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Boolean status = (Boolean) responseBody.get("status");

                if (status != null && status) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    if (data != null) {
                        Map<String, Object> links = (Map<String, Object>) data.get("links");
                        if (links != null) {
                            String imageUrl = (String) links.get("url");
//                            log.info("图片上传成功，URL: {}", imageUrl);
                            return imageUrl;
                        }
                    }
                }

                String message = (String) responseBody.get("message");
//                log.error("图片上传失败，响应: {}", message);
                throw new RuntimeException("图片上传失败: " + message);
            } else {
//                log.error("图片上传失败，HTTP状态码: {}", response.getStatusCode());
                throw new RuntimeException("图片上传失败，HTTP状态码: " + response.getStatusCode());
            }

        } catch (Exception e) {
//            log.error("上传图片到托管平台失败", e);
            throw new RuntimeException("图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 验证图片文件
     */
    public boolean isValidImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // 检查文件大小 (最大10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            return false;
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        return contentType.startsWith("image/") &&
               (contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif") ||
                contentType.equals("image/webp"));
    }
}
