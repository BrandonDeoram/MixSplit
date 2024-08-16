package com.demo.MixSplit.Split;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.sound.sampled.UnsupportedAudioFileException;

@Service
public class SplitService {
    @Autowired
    private ACRConfig acrConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final int POLLING_INTERVAL_MS = 10000; // 10 seconds

    public SplitService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    private String encodeBase64(byte[] bstr) {
        Base64 base64 = new Base64();
        return new String(base64.encode(bstr));
    }

    private String encryptByHMACSHA1(byte[] data, byte[] key) {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(key, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(data);
            return encodeBase64(rawHmac);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    public String findSong() {
        String host = acrConfig.getHost();
        String accessKey = acrConfig.getAccessKey();
        String accessSecret = acrConfig.getAccessSecret();

        byte[] byteArray = audioToBytes("H:/MixSplit/src/music/mewtwo.mp3");
//        System.out.println(Arrays.toString(byteArray));

        String url = "https://identify-us-west-2.acrcloud.com/v1/identify";
        String httpMethod = "POST";
        String httpUri = "/v1/identify";
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
        String dataType = "audio"; // or "fingerprint"
        String signatureVersion = "1";

        try{
            String stringToSign = httpMethod + "\n" + httpUri + "\n" + accessKey + "\n" + dataType + "\n" + signatureVersion + "\n" + timestamp;

            Mac sha1Hmac = Mac.getInstance("HmacSHA1");
            SecretKeySpec secretKeySpec = new SecretKeySpec(accessSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
            sha1Hmac.init(secretKeySpec);

            byte[] hash = sha1Hmac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            String signature = Base64.encodeBase64String(hash);
            System.out.println("fnishied signature");

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/x-www-form-urlencoded");

            String audioBase64 = Base64.encodeBase64String(byteArray);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("sample", audioBase64);
            body.add("access_key", accessKey);
            body.add("sample_bytes", String.valueOf(byteArray.length)); // Size of the byte array
            body.add("timestamp", timestamp);
            body.add("signature", signature);
            body.add("data_type", dataType);
            body.add("signature_version", signatureVersion);

            HttpEntity<MultiValueMap<String,String>> entity = new HttpEntity<>(body,headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST,entity,String.class);
            System.out.println("fnishied sending response" + response.getBody() + response.toString());
            return response.getBody();
        }catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error generating signature: " + e.getMessage());
        }
    }
    public byte[] audioToBytes (String absPath){
        Path path = Paths.get(absPath);
        try {
            byte[] audioBytes = Files.readAllBytes(path);
            return audioBytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<JsonNode> uploadFileACRCloud(){

        // Upload file to acrcloud and grab the id
        String id = getId();
        try {
            Thread.sleep(20000); // 20 seconds delay
            return getSongs(id);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to wait for processing", e);
        }
    }

    public ResponseEntity<JsonNode> getSongs(String fileId) {
        RestTemplate restTemplate = new RestTemplate();
        String token = acrConfig.getToken();

        // Declaring where we're going to get this song from ACRCloud
        int containerId = 18449;
        String requestUrl = String.format("https://api-v2.acrcloud.com/api/fs-containers/%d/files/%s", containerId, fileId);

        // Initializing headers
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.setBearerAuth(token);

        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    requestUrl,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            // Convert response body to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode responseBody = objectMapper.readTree(response.getBody());
            System.out.println(responseBody.textValue());
            return new ResponseEntity<>(responseBody, response.getStatusCode());

        } catch (HttpServerErrorException | JsonProcessingException e) {
            // Log and return error details
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    private String getId(){
        RestTemplate restTemplate = new RestTemplate();
        String token = acrConfig.getToken();
        //importing our music
        File path = new File("H:/MixSplit/src/music/DrakeMix.mp3");
        FileSystemResource song = new FileSystemResource(path);

        //Declaring were going to post this song to ACRCloud
        String httpMethod = "POST";
        int container_id = 18449;
        String requestUrl = "https://api-v2.acrcloud.com/api/fs-containers/18449/files";


        //Intializing headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(token);

        // Create body
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", song);
        body.add("data_type", "audio");
        body.add("name", "Drake Mix Test 1");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body,headers);

        // Making the request and returning json
        try {
            ResponseEntity<ACRResult> response = restTemplate.exchange(requestUrl, HttpMethod.POST, requestEntity, ACRResult.class);
            String id = "";
            ACRResult apiResponse = response.getBody();
            if (response.getStatusCode().is2xxSuccessful()){
                // Wait 20 seconds and call function to get all the songs
                ACRResult.Data data = apiResponse.getData();
                if (data!=null){
                    id = data.getId();
                    System.out.println(id);
                }
            }
            return id;

        } catch (HttpServerErrorException e) {
            // Log and return error details
            e.printStackTrace();
            return "";
        }
    }
    public ResponseEntity<JsonNode> getSplit(){
        RestTemplate restTemplate = new RestTemplate();
        String token = acrConfig.getToken();
        //importing our music
        File path = new File("H:/MixSplit/src/music/DrakeMix.mp3");
        FileSystemResource song = new FileSystemResource(path);

        //Declaring were going to post this song to ACRCloud
        String httpMethod = "POST";
        int container_id = 18449;
        String requestUrl = "https://api-v2.acrcloud.com/api/fs-containers/18449/files";


        //Intializing headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(token);

        // Create body
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", song);
        body.add("data_type", "audio");
        body.add("name", "Drake Mix Test 1");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body,headers);

        // Making the request and returning json
        try {
            ResponseEntity<ACRResult> response = restTemplate.exchange(requestUrl, HttpMethod.POST, requestEntity, ACRResult.class);
            String id = "";
            ACRResult apiResponse = response.getBody();
            if (response.getStatusCode().is2xxSuccessful()){
                // Wait 20 seconds and call function to get all the songs
                ACRResult.Data data = apiResponse.getData();
                if (data!=null){
                    id = data.getId();
                    System.out.println(id);
                }
                Thread.sleep(20000);
                getSongs(id);
            }
            return new ResponseEntity<>(response.getStatusCode());

        } catch (HttpServerErrorException e) {
            // Log and return error details
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (HttpClientErrorException e) {
            // Log and return client error details
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            // Log any other exceptions
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }





    // 1. Download soundcloud mix locally for now.

    // 2. Split the song using ACR Cloud & get Timestamp + Song Name & Artist

    // 3. Create a new playlist on the user soundcloud profile

    // 4. Search for each individual song on soundcloud and take the top results and add to playlist




}
