package digginger.Viewee.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;


@Getter @Setter
public class ApiResponseDTO {

    private Map<String, String> responseMsg;

    public ApiResponseDTO() {
        responseMsg = new LinkedHashMap<>();
    }

    public void setResponseMsg(Map<String, String> responseMsg) {
        this.responseMsg = responseMsg;
    }

    public void addResponseMsg(String key, String value) {
        responseMsg.put(key, value);
    }
}

