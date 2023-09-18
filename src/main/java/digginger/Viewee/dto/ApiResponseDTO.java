package digginger.Viewee.dto;

import java.util.LinkedHashMap;
import java.util.Map;

public class ApiResponseDTO {

    private Map<String, String> responseMsg;

    public ApiResponseDTO() {
        responseMsg = new LinkedHashMap<>();
    }

    public Map<String, String> getResponseMsg() {
        return responseMsg;
    }

    public void setResponseMsg(Map<String, String> responseMsg) {
        this.responseMsg = responseMsg;
    }

    public void addResponseMsg(String key, String value) {
        responseMsg.put(key, value);
    }
}

