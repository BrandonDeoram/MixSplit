package com.demo.MixSplit.Split;
import com.demo.MixSplit.DTO.MusicResultDTO;
import com.demo.MixSplit.Utility.ACRHeaders;
import com.demo.MixSplit.Utility.AudioUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.multipart.MultipartFile;


@Service
public class SplitService {
    private final ACRConfig acrConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final int POLLING_INTERVAL_MS = 10000; // 10 seconds

    @Autowired
    public SplitService(ACRConfig acrConfig, RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper) {
        this.acrConfig = acrConfig;
        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = objectMapper;
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

        // Upload file to acrcloud and grab the id
        String id = getId(file, filename);
        try {
            Thread.sleep(20000); // 20 seconds delay
            return getSongs(id);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to wait for processing", e);
        }
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
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dataNode);
            System.out.println("Contents of dataNode:");
            System.out.println(prettyJson);
        } catch (JsonProcessingException e) {
            System.err.println("Error pretty-printing JSON: " + e.getMessage());
        }
        List<MusicResultDTO> musicResults = new ArrayList<>();

        if (dataNode != null && dataNode.isArray()) {

            for (JsonNode songNode : dataNode) {
                List<String> artistNames = new ArrayList<>();
                String id = songNode.path("id").asText();
                int uid = songNode.path("uid").asInt();
                int cid = songNode.path("cid").asInt();
                String name = songNode.path("name").asText();

                JsonNode resultsNode = songNode.path("results");
                JsonNode musicArrayNode = resultsNode.path("music");

                if (musicArrayNode.isArray()) {
                    for (JsonNode musicNode : musicArrayNode) {
                        JsonNode resultNode = musicNode.path("result");
                        JsonNode externalMetadataNode = resultNode.path("external_metadata");
                        JsonNode spotifyNode = externalMetadataNode.path("spotify");
                        JsonNode spotifyArtistsNode = spotifyNode.path("artists");
                        if (!spotifyNode.isMissingNode()) {
                            JsonNode trackNode = spotifyNode.path("track");
                            if (!trackNode.isMissingNode()) {
                                if (spotifyArtistsNode.isArray()) {
                                    artistNames = new ArrayList<>();
                                    for (JsonNode artistNode : spotifyArtistsNode) {
                                        String artistName = artistNode.path("name").asText();
                                        if (!artistName.isEmpty()) {
                                            artistNames.add(artistName);
                                        }
                                    }

                                }
                                else{
                                    System.out.println("not a array");
                                }

                                String spotifyId = trackNode.path("id").asText();
                                String trackName = trackNode.path("name").asText();

                                MusicResultDTO musicResultDTO = new MusicResultDTO();
                                musicResultDTO.setId(id);
                                musicResultDTO.setUid(uid);
                                musicResultDTO.setCid(cid);
                                musicResultDTO.setName(trackName);
                                musicResultDTO.setSpotify(spotifyId);
                                musicResultDTO.setArtistNames(artistNames);
                                musicResults.add(musicResultDTO);
                            }
                        }
                    }
                }
            }
        }
        return musicResults;
    }

    private String getId(MultipartFile file,String filename) throws IOException {

        //Declaring were going to post this song to ACRCloud
        String httpMethod = "POST";
        int container_id = 18449;
        String requestUrl = "https://api-v2.acrcloud.com/api/fs-containers/18449/files";
        HttpHeaders headers = ACRHeaders.createMultipartHeaders(acrConfig.getToken());


        // Create body
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new MultipartFileResource(file));
        body.add("data_type", "audio");
        body.add("name", filename);

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
