package com.demo.MixSplit.Split;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ACRResult {

    @JsonProperty("data")
    private Data data;

    // Getter and Setter
    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Data {

        @JsonProperty("uid")
        private int uid;

        @JsonProperty("cid")
        private int cid;

        @JsonProperty("name")
        private String name;

        @JsonProperty("duration")
        private String duration;

        @JsonProperty("uri")
        private String uri;

        @JsonProperty("data_type")
        private String dataType;

        @JsonProperty("engine")
        private int engine;

        @JsonProperty("count")
        private int count;

        @JsonProperty("state")
        private int state;

        @JsonProperty("updated_at")
        private String updatedAt;

        @JsonProperty("created_at")
        private String createdAt;

        @JsonProperty("id")
        private String id;

        @JsonProperty("total")
        private int total;

        @JsonProperty("results")
        private Object results; // Adjust type if needed

        @JsonProperty("detail")
        private String detail;

        // Getters and Setters
        public int getUid() {
            return uid;
        }

        public void setUid(int uid) {
            this.uid = uid;
        }

        public int getCid() {
            return cid;
        }

        public void setCid(int cid) {
            this.cid = cid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getDataType() {
            return dataType;
        }

        public void setDataType(String dataType) {
            this.dataType = dataType;
        }

        public int getEngine() {
            return engine;
        }

        public void setEngine(int engine) {
            this.engine = engine;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public Object getResults() {
            return results;
        }

        public void setResults(Object results) {
            this.results = results;
        }

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }
    }
}
