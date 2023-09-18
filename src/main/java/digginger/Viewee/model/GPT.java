package digginger.Viewee.model;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GPT {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${openai.apikey}")
    public static String OPEN_API_KEY;

    // GPT API의 END_POINT
    private static final String API_END_POINT = "https://api.openai.com/v1/chat/completions";
    // GPT 모델
    public static final String GPT3_5 = "gpt-3.5-turbo";
    // 시스템 역할 관련 문자열 | 대화의 시스템 메시지로 초기 설정(=면접관, 질문생성하라는 프롬프트)
    public static final String SYSTEM_ROLE_INTERVIEWER_SETTING =
            "저는 입사를 위해 면접을 준비하고 있습니다." +
            "이번 면접에서  회사에 입사해야만 합니다." +
            "저의 회사 입사를 위한 면접을 위해 면접관이 되어주세요." +
            "면접관은 구직자가 제공한 정보를 바탕으로 구직자에게 질문을 하게 됩니다." +
            "다음 지침을 준수하는 질문을  5개 제공하십시오." +
            "- 한가지 기본적인 질문을 하십시오." +
            "- 그들의 경험과 관련된 한 가지의 질문을 하십시오." +
            "- 직책과 관련된 한 가지 질문을 하십시오." +
            "- 그들의 기술력과 관련된 한 가지 질문을 하십시오." +
            "- 선택한 질문을 하나 더 합니다. 질문의 순서를 나타내기 위해 #1, #2, #3, #4 및 #5로 질문에 번호를 매기십시오." +
            "5개의 질문만 하고 질문 과정에서 추가 의견이나 진술을 하지 않아야 합니다.";

    // 예상질문 생성 후 질문을 1개씩 사용자에게 전달하게 하는 프롬프트
    public static final String QUESTIONS_SET =
            "생성된 예상질문을 토대로 모의면접을 시작할 것입니다." +
            "지금부터 면접자는 생성된 예상질문을 보고 답변을 할 것 입니다." +
            "사용자의 답변을 받으면, 생성된 예상질문의 다음 질문을 말해주어야 합니다." +
            "질문만 하여야 합니다.";

    // 답변 피드백을 제공하게 해주는 프롬프트
    public static final String ANSWER_FEEDBACK = "당신은 앞서 진행했던 면접에서 질문과 사용자 답변에 대한 정보를 알고 있습니다." +
            "답변 피드백은 구체적이어야하며, 사실과 다른 내용이 있으면 안됩니다." +
            "답변 피드백은 주어진 질문에 알맞은 답변을 하였는지, 답변은 적절했는지 등으로 피드백을 합니다." +
            "만약 원하는 문구 및 문장이 있다면 제시하여도 좋습니다." +
            "감정 및 표정 분석 결과를 바탕으로 질문에 대한 피드백을 부탁드립니다.(질문+ 답+ 분석결과)" +
            "감정 및 얼굴 표정 분석에서 수집한 관련 정보도 포함하여 자세한 피드백을 하십시오." +
            "참고: 이 프롬프트는 특히 면접의 맥락에서 의사 소통 기술을 분석하기 위해 고안되었으므로 실제 회사에 지원하는 것처럼 정직하게 답변하십시오.";


    // 총평 피드백을 제공하게 해주는 프롬프트
    public static final String OVERRALL_FEEDBACK = "앞서 진행했던 면접에서 질문과 사용자 답변에 대한 정보를 알고 있습니다." +
            "당신은 사용자의 답변을 보고, 면접에 대한 총평 피드백을 무조건적으로 제공하여야 합니다. 피드백을 제공하지 않으면 안됩니다." +
            "총평 피드백은 지금까지 진행해 왔던 면접을 토대로한 총평 피드백입니다." +
            "총평 피드백은 구체적이어야하며, 사실과 다른 내용이 있으면 안됩니다." +
            "만약 원하는 문구 및 문장이 있다면 제시하여도 좋습니다." +
            "형식대로 말하여야하며 형식은 다음과 같습니다." +
            "형식 = '총평:..., 피드백:1. 2. 3. ...'";



    private String selectedModel;
    private URL url;
    public static JSONArray msgHistory = new JSONArray();

    public GPT(String selectedModel, String systemRole, @Value("${openai.apikey}")String OPEN_API_KEY) {
        this.OPEN_API_KEY = OPEN_API_KEY;
        this.selectedModel = selectedModel;
        try {
            this.url = new URL(API_END_POINT);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.addMsg("system", systemRole);

    }

    // 대화 메시지(JSON) 생성, msgHistory 에 데이터 추가
    public JSONObject addMsg(String role, String msg) {
        JSONObject msgJsonObject = new JSONObject();
        msgJsonObject.put("role", role);
        msgJsonObject.put("content", msg);
        this.msgHistory.add(msgJsonObject);
        log.debug("MsgHistory: {}", msgHistory);
        return msgJsonObject;
    }

    //  gpt 객체에 대한 HttpURLConnection 객체 생성, 반환
    public HttpURLConnection getConnection() {
        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "application/json; utf-8");
            httpURLConnection.setRequestProperty("Authorization", "Bearer " + OPEN_API_KEY);
            httpURLConnection.setDoOutput(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return httpURLConnection;
    }

    // msgHistory 활용하여 API 요청에 필요한 JOSN Payload 생성
    @SuppressWarnings("unchecked")
    private String buildApiParam() {

        JSONObject apiParam = new JSONObject();
        apiParam.put("messages", msgHistory);
        apiParam.put("model", this.selectedModel);

        return apiParam.toJSONString();

    }

    // API 요청 전송, 결과 데이터 JSON 문자열로 반환
    public String sendMsgToAssistant(String aipParam) throws Exception {
        HttpURLConnection httpURLConnection = this.getConnection();
        try (OutputStream os = httpURLConnection.getOutputStream()) {
            byte[] inputBytes = aipParam.getBytes("utf-8");
            os.write(inputBytes, 0, inputBytes.length);
        }

        int resCode = httpURLConnection.getResponseCode();
        log.debug("resCode:" + resCode);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "utf-8"))) {
            StringBuilder res = new StringBuilder();
            String resLine = null;

            while ((resLine = br.readLine()) != null) {
                res.append(resLine.trim());
            }

            return (res.toString());
        }
    }

    // API에서 받은 JSON 데이터 content 필드 추출 후 반환
    @SuppressWarnings({ "rawtypes"})
    public static String extractAssitantMsg(String respJsonStr) {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject) parser.parse(respJsonStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        List choices = (List)jsonObject.get("choices");
        Map firstChoice = (Map)choices.get(0);
        Map msgMap = (Map)firstChoice.get("message");
        String content = (String)msgMap.get("content");
        return content;
    }

    // 사용자의 메시지를 받아 API와 통신, 대답 반환
    public String chatToGPT(String userMsg) {
        this.addMsg("user", userMsg);
        String aipParam = this.buildApiParam();
        log.debug("aipParam : \n " + aipParam);
        try {
            String assistantRespJson = this.sendMsgToAssistant(aipParam);
            String assistantMsg = extractAssitantMsg(assistantRespJson);
            this.addMsg("assistant", assistantMsg);
            return assistantMsg;
        } catch (Exception e) {
            return "API_ERROR";
        }
    }

    public void clearMsgHistory() {
        this.msgHistory = new JSONArray();
    }

}