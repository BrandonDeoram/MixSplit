package com.demo.MixSplit.Service;
import com.demo.MixSplit.Config.ACRConfig;
import com.demo.MixSplit.DTO.MusicResultDTO;
import com.demo.MixSplit.Split.ACRResult;
import com.demo.MixSplit.Utility.ACRHeaders;
import com.demo.MixSplit.Utility.AudioUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class SplitService {
    private final ACRConfig acrConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private CompletableFuture<ResponseEntity<String>> pollingFuture;

    private static final int POLLING_INTERVAL_MS = 5000; // 5 seconds
    private static final int MAX_RETRIES = 10; // Maximum number of retries
    private int pollCount = 0;

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    @Autowired
    public SplitService(ACRConfig acrConfig, RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper) {
        this.acrConfig = acrConfig;
        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = objectMapper;
    }
    public  CompletableFuture<List<MusicResultDTO>> uploadAndPoll(MultipartFile file, String filename) throws IOException {
        // Step 1: Upload the file to ACRCloud
        String fileId = getId(file, filename);
        System.out.println(fileId);

        // Step 2: Polling logic with CompletableFuture
        CompletableFuture<List<MusicResultDTO>> futureResult = new CompletableFuture<>();

        // Schedule the polling task at a fixed interval
        scheduler.scheduleAtFixedRate(new PollingTask(futureResult, fileId, MAX_RETRIES), 0, POLLING_INTERVAL_MS, TimeUnit.MILLISECONDS);

        return futureResult; // Will complete once the polling is successful or retries are exhausted
    }

    @Async
    public CompletableFuture<List<MusicResultDTO>> pollProcessingStatus(String fileId) {
        CompletableFuture<List<MusicResultDTO>> futureResult = new CompletableFuture<>();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Call the ACRCloud API and get processing status
                ResponseEntity<String> response = checkProcessingStatus(fileId);

                if (response.getStatusCode() == HttpStatus.OK) {
                    JsonNode resultNode = new ObjectMapper().readTree(response.getBody());
                    JsonNode dataNode = resultNode.get("data");

                    if (dataNode != null && dataNode.isArray() && dataNode.size() > 0) {
                        JsonNode resultsNode = dataNode.get(0).get("results");

                        // If results are available, complete the future with results
                        if (resultsNode != null && !resultsNode.isNull()) {
                            System.out.println("Results are available returning");
                            List<MusicResultDTO> songResults = extractMusicResultDTO(dataNode);
                            System.out.println("printing first");
                            System.out.println(songResults.get(0).toString());
                            futureResult.complete(songResults);
                            scheduler.shutdown();
                        }
                    }
                }
            } catch (Exception e) {
                futureResult.completeExceptionally(e);
                scheduler.shutdown();
            }
        }, 0, 5, TimeUnit.SECONDS);

        return futureResult;
    }
    // PollingTask checks the status of the file processing
    private class PollingTask implements Runnable {
        private final CompletableFuture<List<MusicResultDTO>> futureResult;
        private final String fileId;
        private int retryCount = 0;
        private final int maxRetries;

        PollingTask(CompletableFuture<List<MusicResultDTO>> futureResult, String fileId, int maxRetries) {
            this.futureResult = futureResult;
            this.fileId = fileId;
            this.maxRetries = maxRetries;
        }

        @Override
        public void run() {
            if (retryCount >= maxRetries) {
                futureResult.completeExceptionally(new RuntimeException("Max retries reached, ACRCloud processing not finished."));
                scheduler.shutdown(); // Stop polling
                return;
            }

            try {
                // Check the processing status from ACRCloud API
                ResponseEntity<String> response = checkProcessingStatus(fileId);

                if (response.getStatusCode() == HttpStatus.OK) {
                    // Parse the response JSON to check for 'results'
                    JsonNode resultNode = new ObjectMapper().readTree(response.getBody());

                    // Extract 'data' and check the 'results' field
                    JsonNode dataNode = resultNode.get("data");
                    if (dataNode != null && dataNode.isArray() && dataNode.size() > 0) {
                        JsonNode firstElement = dataNode.get(0); // Get the first element in 'data'
                        JsonNode resultsNode = firstElement.get("results");

                        // Check if 'results' is null (polling continues if it's null)
                        // Stop polling
                        if (resultsNode != null && !resultsNode.isNull()) {
                            List<MusicResultDTO> songResults = extractMusicResultDTO(dataNode);
                            futureResult.complete(songResults); // Complete with song results
                            scheduler.shutdown(); // Stop polling
                            return;
                        }else {
                            System.out.println("still processing");
                        }
                    }
                }

                retryCount++; // Increment retry count for each poll attempt
            } catch (Exception e) {
                futureResult.completeExceptionally(e);
                scheduler.shutdown(); // Stop polling in case of error
            }
        }
    }

    public ResponseEntity<String> checkProcessingStatus(String fileId) {
        String url = String.format("https://api-v2.acrcloud.com/api/fs-containers/18449/files/%s", fileId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(acrConfig.getToken());
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
    }


    public String findSong() {
        String host = acrConfig.getHost();
        String accessKey = acrConfig.getAccessKey();
        String accessSecret = acrConfig.getAccessSecret();

        byte[] byteArray = AudioUtils.audioToBytes("H:/MixSplit/src/music/mewtwo.mp3");


        String url = "https://identify-us-west-2.acrcloud.com/v1/identify";
        String httpMethod = "POST";
        String httpUri = "/v1/identify";
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
        String dataType = "audio"; // or "fingerprint"
        String signatureVersion = "1";


        String stringToSign = httpMethod + "\n" + httpUri + "\n" + accessKey + "\n" + dataType + "\n" + signatureVersion + "\n" + timestamp;
        String signature = AudioUtils.generateSignature(stringToSign, acrConfig.getAccessSecret());
        HttpHeaders headers = ACRHeaders.createHeaders(acrConfig.getToken());
        String audioBase64 = Base64.encodeBase64String(byteArray);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("sample", audioBase64);
        body.add("access_key", accessKey);
        body.add("sample_bytes", String.valueOf(byteArray.length)); // Size of the byte array
        body.add("timestamp", timestamp);
        body.add("signature", signature);
        body.add("data_type", dataType);
        body.add("signature_version", signatureVersion);


        ResponseEntity<String> response;
        try {
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
            response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            System.out.println("finished sending response" + response.getBody() + response.toString());
        } catch (RestClientException e) {
            throw new RuntimeException(e);
        }
        return response.getBody();
    }

    // We also need to store the results of this somewhere in our database which should we attached to our user
    public ResponseEntity<List<MusicResultDTO>> uploadFileACRCloud(MultipartFile file, String filename) throws IOException {

        // Upload file to acrcloud and grab the id of that file
        String id = getId(file, filename);

        try {
            Thread.sleep(20000); // 20 seconds delay
            return getSongs(id);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to wait for processing", e);
        }
    }
    public String getIdSong(MultipartFile file, String filename) throws IOException {
        String id = getId(file, filename);
        System.out.println("ID of song" + id);
        return id;
    }
    public ResponseEntity<List<MusicResultDTO>> getSongs(String fileId) {
        String token = acrConfig.getToken();
        int containerId = 18449;
        String requestUrl = String.format("https://api-v2.acrcloud.com/api/fs-containers/%d/files/%s", containerId, fileId);

        HttpHeaders headers = ACRHeaders.createMultipartHeaders(acrConfig.getToken());
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    requestUrl,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode dataNode = rootNode.get("data");
            return new ResponseEntity<>(extractMusicResultDTO(dataNode), response.getStatusCode());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON response", e);
        }
    }

    private List<MusicResultDTO> extractMusicResultDTO(JsonNode dataNode){
        try {
            if (dataNode != null) {
                String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dataNode);
                System.out.println(prettyJson);
            }
        } catch (JsonProcessingException e) {
            System.err.println("Error pretty-printing JSON: " + e.getMessage());
        }

        List<MusicResultDTO> musicResults = new ArrayList<>();
        if (dataNode == null || !dataNode.isArray()) {
            return musicResults; // Return empty list if input is null or not an array
        }

        for (JsonNode songNode : dataNode) {
            String id = songNode.path("id").asText();
            int uid = songNode.path("uid").asInt();
            int cid = songNode.path("cid").asInt();

            JsonNode musicArrayNode = songNode.path("results").path("music");
            if (!musicArrayNode.isArray()) {
                continue; // Skip if "music" node is not an array
            }

            for (JsonNode musicNode : musicArrayNode) {
                MusicResultDTO musicResultDTO = processMusicNode(id, uid, cid, musicNode);
                if (musicResultDTO != null) {
                    musicResults.add(musicResultDTO);
                }
            }
        }

        return musicResults;
    }
    
    // Helper functions
    private MusicResultDTO processMusicNode(String id, int uid, int cid, JsonNode musicNode){
        JsonNode resultNode = musicNode.path("result");
        JsonNode spotifyNode = resultNode.path("external_metadata").path("spotify");

        if (spotifyNode.isMissingNode()){
            return null;
        }
        JsonNode trackNode = spotifyNode.path("track");
        if (trackNode.isMissingNode()) {
            return null; // Skip if track node is missing
        }

        List<String> artistNames = extractArtistNames(spotifyNode.path("artists"));
        String spotifyId = trackNode.path("id").asText();
        String trackName = trackNode.path("name").asText();

        MusicResultDTO musicResultDTO = new MusicResultDTO(id);
        musicResultDTO.setStartTime(resultNode.path("sample_begin_offset_ms").asText());
        musicResultDTO.setEndTime(resultNode.path("sample_end_time_offset_ms").asText());
        musicResultDTO.setUid(uid);
        musicResultDTO.setCid(cid);
        musicResultDTO.setName(trackName);
        musicResultDTO.setSpotify(spotifyId);
        musicResultDTO.setArtistNames(artistNames);

        return musicResultDTO;
    }
    private List<String> extractArtistNames(JsonNode artistsNode){
        List<String> artistNames = new ArrayList<>();
        if (artistsNode.isArray()) {
            for (JsonNode artistNode : artistsNode) {
                String artistName = artistNode.path("name").asText();
                if (!artistName.isEmpty()) {
                    artistNames.add(artistName);
                }
            }
        }
        return artistNames;
    }

    public String getId(MultipartFile file,String filename) throws IOException {

        //Declaring were going to post this song to ACRCloud
        String httpMethod = "POST";
        int container_id = 18449;
        String requestUrl = "https://api-v2.acrcloud.com/api/fs-containers/18449/files";
        HttpHeaders headers = ACRHeaders.createMultipartHeaders(acrConfig.getToken());


        MultiValueMap<String, Object> body = createRequestBody(file, filename);

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
    private MultiValueMap<String, Object> createRequestBody(MultipartFile file, String filename) throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new MultipartFileResource(file));
        body.add("data_type", "audio");
        body.add("name", filename);
        return body;
    }


    private static class MultipartFileResource extends ByteArrayResource {
        private final String filename;

        public MultipartFileResource(MultipartFile multipartFile) throws IOException {
            super(multipartFile.getBytes());
            this.filename = multipartFile.getOriginalFilename();
        }

        @Override
        public String getFilename() {
            return this.filename;
        }
    }


}
