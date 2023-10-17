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
    // gpt-3.5-turbo-16k
    public static final String GPT3_5 = "gpt-3.5-turbo-16k";

    // 시스템 역할 관련 문자열 | 대화의 시스템 메시지로 초기 설정(=면접관, 질문생성하라는 프롬프트)
    public static final String SYSTEM_ROLE_INTERVIEWER_SETTING =
                    "사용자는 입사를 위해 면접을 준비하고 있습니다." +
                    "이번 면접에서 회사에 입사해야만 합니다." +
                    "사용자의 회사 입사를 위한 면접을 위해 면접관이 되어주세요." +
                    "면접관은 사용자가 제공한 정보를 바탕으로 구직자에게 질문을 하게 됩니다." +
                    "다음 지침을 준수하는 질문을  5개 제공하십시오." +
                    "- 한가지 기본적인 질문을 하십시오." +
                    "- 그들의 경험과 관련된 한 가지의 질문을 하십시오." +
                    "- 직책과 관련된 한 가지 질문을 하십시오." +
                    "- 그들의 기술력과 관련된 한 가지 질문을 하십시오." +
                    "- 선택한 질문을 하나 더 합니다. 질문의 순서를 나타내기 위해 #1, #2, #3, #4 및 #5로 질문에 번호를 매기십시오." +
                    "5개의 질문만 하고 질문 과정에서 추가 의견이나 진술을 하지 않아야 합니다.";

    // 예상질문 생성 후 질문을 1개씩 사용자에게 전달하게 하는 프롬프트
    public static final String QUESTIONS_SET =
                    "이제 생성된 예상질문을 토대로 모의면접을 시작할 것 입니다." +
                    "지금부터 면접자는 생성된 질문을 보고 답변을 할 것 입니다." +
                    "사용자의 답변을 받으면, 이미 생성된 질문의 다음 질문을 말해 주어야 합니다." +
                    "질문만 하여야 합니다.";

    // 답변 피드백을 제공하게 해주는 프롬프트
    public static final String FEEDBACK = "당신은 앞서 진행했던 면접에서 질문과 사용자 답변에 대한 정보를 알고 있습니다." +
            "지금부터 당신은 면접에 대한 피드백을 제공 하여야 합니다." +
            "다른 질문을 하거나 새로운 질문을 하면 안됩니다." +
            "답변 피드백은 구체적 이어야 하며, 사실과 다른 내용이 있으면 안됩니다." +
            "답변 피드백은 주어진 질문에 알맞은 답변을 하였는지, 사용자의 답변은 적절했는지 등으로 피드백을 합니다." +
            "답변 피드백은 각각의 질문에 대한 답변을 보고 개별적으로 판단하여야 합니다." +
            "답변 피드백의 형식은 다음과 같습니다." +
            "#1(1번 질문에 대한 답변의 피드백)#2(2번 질문에 대한 답변의 피드백)#3(3번 질문에 대한 답변의 피드백)#4(4번 질문에 대한 답변의 피드백)#5(5번 질문에 대한 답변의 피드백)" +
            "각 번호는'\n' 한 번 만으로 구분할 것이다. '\n'을 2번 사용 하지 말고, 꼭 1번만 사용 하여야 한다."+
            "만약 원하는 문구 및 문장이 있다면 제시하여도 좋습니다." +
            "만약 질문에 대한 사용자의 답변이 없다면 피드백을 생략해도 좋습니다. 하지만 사용자가 답변을 했다면 그 질문과 답변에 대한 피드백을 제공하고 총평 피드백 또한 제공해야 합니다." +
            "각 답변 피드백을 마치면, 이 면접에 대한 총평 피드백을 제공해야 합니다." +
            " '형식 : #총평 피드백 : ' " +
            "총평 피드백은 구체적이어야 하며, 사실과 다른 내용이 있으면 안되며, 자세하게 피드백을 제공해야 합니다." +
            "감정 및 표정 분석 결과를 바탕으로 질문에 대한 피드백을 부탁드립니다." +
            "감정 및 얼굴 표정 분석에서 수집한 관련 정보도 포함하여 자세한 피드백을 하십시오." +
            "참고: 이 프롬프트는 특히 면접의 맥락에서 의사 소통 기술을 분석하기 위해 고안되었으므로 실제 회사에 지원하는 것처럼 정직하게 답변하십시오.";

    /*
    public static final String ANSWER_FEEDBACK_P = "이 프롬프트는 면접 질문과 답변에 대한 구체적인 피드백을 제공하는 것을 목표로 합니다." +
            "사용자의 답변, 감정, 표정 분석 결과를 바탕으로 직접적이고 상세한 피드백을 제공해야 합니다." +
            "각 질문에 대한 답변은 다음과 같은 형식으로 평가해야 합니다." +
            "'#1(1번 질문에 대한 답변의 피드백) #2(2번 질문에 대한 답변의 피드백) #3(3번 질문에 대한 답변의 피드백) #4(4번 질문에 대한 답변의 피드백) #5(5번 질문에 대한 답변의 피드백)'" +
            "각 번호는 '\n' 한 번 만으로 구분할 것이다. '\n'을 2번 사용하지 않도록 주의하십시오." +
            "피드백은 주어진 질문을 어떻게 이해하고 해결하는지, 그리고 그 과정에서 어떤 생각과 노력을 보였는지 등을 중심으로 해야합니다." +
            "또, 감정 및 얼굴 표정 분석에서 수집된 데이터를 기준으로 사용자가 자신의 의견을 어떻게 전달하였는지 평가해주셔야 합니다." +
            "마지막으로 이 면접 전체를 바라보는 총평도 필요합니다." +
            "이것은 '#총평:(면접 전체 평가)' 형식으롤 작성되어야 하며, 여기서도 구체적인 사항들" +
            "- 사용자가 가장 잘 처리하거나 개선이 필요한 부분들등 - 에 초점을 맞춰서 작성되어야 합니다." +
            "참고로 이 프롬프트는 실제 회사 면접처럼 정직하게 의사소통 기술을 분석하기 위해 설계되었습니다." +
            "따라서 가능한 구체적이며 자세하게 정보를 제공하여 실제 세상에서 일어날 수 있는 다양한 시나리오와 상황에서의 행동을 평가하십시오.";
    */


    // 다시 재면접을 보게 하는 프롬프트
    public static final String RE_INTERVIEW = "앞서 진행했던 면접 데이터와 그에 따른 피드백 데이터를 토대로, 면접자는 다시 재면접을 보려고 합니다." +
            "재면접시 질문 생성 조건은 다음과 같습니다."+
            "조건1. 재면접에는 반드시 앞서 진행했던 면접과 동일한 질문을 물어 보아야 합니다." +
            "조건2. 질문 생성시 앞서 생성했던 질문의 형식으로 제공하여야 합니다." +
            "앞서 생선했던 질문의 형식이란 '#1, #2, #3, #4, #5로 질문에 번호를 매겨야 하며, 질문 이외의 추가 의견 및 진술을 하지 않아야 하는 것' 입니다." +
            "조건3. 만약 사용자가 질문을 보고 답변을 하여 재면접이 시작 되었다면, 앞서 진행했던 면접과 동일한 방식으로 이미 생성된 질문의 다음 질문을 말해 주어야 합니다." +
            "조건4. 다른 문장이나 의견및 진술은 하지말고, 오로지 질문만 하여야 합니다." +
            "조건5. 만약 #5인 5번째 질문까지 모두 마쳤다면, '재면접이 종료되었습니다. 하단의 버튼을 눌러 피드백을 확인해주시기 바랍니다.'라는 말을 하여야 합니다. 또 다른 질문을 물어보아선 안됩니다.";


    public static final String RE_FEEDBACK_1 = "사용자는 당신이 제공해준 1번질문에 대한 답변 피드백을 참고하여, 다시 한번 더 1번 질문에 대한 답변을 하려고 합니다." +
            "사용자의 1번 질문에 대한 답변을 보고, 조건에 알맞게 다시 한 번 더 1번 질문에 대한 피드백을 제공해주세요." +
            "조건1. 기존 답변에 비해 어떤 점이 개선 되었는지, 아니면 아직 부족한 것이 있는지에 대해 피드백을 제공해주세요." +
            "조건2. 사용자는 1번 질문에 대한 답변 피드백을 참고하여 재작성한 것임으로, 당신이 제공해준 답변 피드백을 올바르게 참고하여 답변하였는지에 대해 피드백을 제공해주세요." +
            "조건3. 1번 질문과 그에 따른 사용자의 답변을 참고하여야 합니다. 다른 질문에 대한 피드백은 제공해선 절대 안되고, 마찬가지로 총평 피드백 또한 절대 제공해선 안됩니다.";


    public static final String RE_FEEDBACK_2 = "사용자는 당신이 제공해준 2번질문에 대한 답변 피드백을 참고하여, 다시 한번 더 2번 질문에 대한 답변을 하려고 합니다." +
            "사용자의 2번 질문에 대한 답변을 보고, 조건에 알맞게 다시 한 번 더 2번 질문에 대한 피드백을 제공해주세요." +
            "조건1. 기존 답변에 비해 어떤 점이 개선 되었는지, 아니면 아직 부족한 것이 있는지에 대해 피드백을 제공해주세요." +
            "조건2. 사용자는 2번 질문에 대한 답변 피드백을 참고하여 재작성한 것임으로, 당신이 제공해준 답변 피드백을 올바르게 참고하여 답변하였는지에 대해 피드백을 제공해주세요." +
            "조건3. 1번 질문과 그에 따른 사용자의 답변을 참고하여야 합니다. 다른 질문에 대한 피드백은 제공해선 절대 안되고, 마찬가지로 총평 피드백 또한 절대 제공해선 안됩니다.";

    public static final String RE_FEEDBACK_3 = "사용자는 당신이 제공해준 3번질문에 대한 답변 피드백을 참고하여, 다시 한번 더 3번 질문에 대한 답변을 하려고 합니다." +
            "사용자의 3번 질문에 대한 답변을 보고, 조건에 알맞게 다시 한 번 더 3번 질문에 대한 피드백을 제공해주세요." +
            "조건1. 기존 답변에 비해 어떤 점이 개선 되었는지, 아니면 아직 부족한 것이 있는지에 대해 피드백을 제공해주세요." +
            "조건2. 사용자는 3번 질문에 대한 답변 피드백을 참고하여 재작성한 것임으로, 당신이 제공해준 답변 피드백을 올바르게 참고하여 답변하였는지에 대해 피드백을 제공해주세요." +
            "조건3. 1번 질문과 그에 따른 사용자의 답변을 참고하여야 합니다. 다른 질문에 대한 피드백은 제공해선 절대 안되고, 마찬가지로 총평 피드백 또한 절대 제공해선 안됩니다.";

    public static final String RE_FEEDBACK_4 = "사용자는 당신이 제공해준 4번질문에 대한 답변 피드백을 참고하여, 다시 한번 더 4번 질문에 대한 답변을 하려고 합니다." +
            "사용자의 1번 질문에 대한 답변을 보고, 조건에 알맞게 다시 한 번 더 4번 질문에 대한 피드백을 제공해주세요." +
            "조건1. 기존 답변에 비해 어떤 점이 개선 되었는지, 아니면 아직 부족한 것이 있는지에 대해 피드백을 제공해주세요." +
            "조건2. 사용자는 4번 질문에 대한 답변 피드백을 참고하여 재작성한 것임으로, 당신이 제공해준 답변 피드백을 올바르게 참고하여 답변하였는지에 대해 피드백을 제공해주세요." +
            "조건3. 1번 질문과 그에 따른 사용자의 답변을 참고하여야 합니다. 다른 질문에 대한 피드백은 제공해선 절대 안되고, 마찬가지로 총평 피드백 또한 절대 제공해선 안됩니다.";

    public static final String RE_FEEDBACK_5 = "사용자는 당신이 제공해준 5번질문에 대한 답변 피드백을 참고하여, 다시 한번 더 5번 질문에 대한 답변을 하려고 합니다." +
            "사용자의 5번 질문에 대한 답변을 보고, 조건에 알맞게 다시 한 번 더 5번 질문에 대한 피드백을 제공해주세요." +
            "조건1. 기존 답변에 비해 어떤 점이 개선 되었는지, 아니면 아직 부족한 것이 있는지에 대해 피드백을 제공해주세요." +
            "조건2. 사용자는 5번 질문에 대한 답변 피드백을 참고하여 재작성한 것임으로, 당신이 제공해준 답변 피드백을 올바르게 참고하여 답변하였는지에 대해 피드백을 제공해주세요." +
            "조건3. 1번 질문과 그에 따른 사용자의 답변을 참고하여야 합니다. 다른 질문에 대한 피드백은 제공해선 절대 안되고, 마찬가지로 총평 피드백 또한 절대 제공해선 안됩니다.";







    public static final String RE_INTERVIEW_FEEDBACK = "당신은 앞서 첫번째 면접 데이터와 두번째 면접 데이터를 알고 있습니다." +
            "지금부터 당신은 첫번째 면접과 두번째 면접을 비교하여 사용자에게 면접 피드백을 제공하여야 합니다." +
            "피드백을 제공해줄 때에는 첫번째 면접의 답변과 두번쨰 면접의 답변을 적절히 비교하여 개선된 점과 더 개선 해야 점을 분석하여 피드백을 제공하여야 합니다." +
            "다른 질문을 하거나 새로운 질문을 하면 안됩니다." +
            "답변 피드백은 구체적이어야하며, 사실과 다른 내용이 있으면 안됩니다." +
            "답변 피드백은 주어진 질문에 알맞은 답변을 하였는지, 답변은 적절했는지 등으로 피드백을 합니다." +
            "답변 피드백은 각각의 질문에 대한 답변을 보고 개별적으로 판단하여야 합니다." +
            "답변 피드백의 형식은 다음과 같습니다." +
            "#1(1번 질문에 대한 답변의 피드백)#2(2번 질문에 대한 답변의 피드백)#3(3번 질문에 대한 답변의 피드백)#4(4번 질문에 대한 답변의 피드백)#5(5번 질문에 대한 답변의 피드백)" +
            "각 번호는'\n' 한 번 만으로 구분할 것이다. '\n'을 2번 사용 하지 말고, 꼭 1번만 사용 하여야 한다."+
            "만약 원하는 문구 및 문장이 있다면 제시하여도 좋습니다." +
            "만약 질문에 대한 사용자의 답변이 없다면 피드백을 생략해도 좋습니다. 하지만 사용자가 답변을 했다면 그 질문과 답변에 대한 피드백을 제공하고 총평 피드백 또한 제공해야 합니다." +
            "각 답변 피드백을 마치면, 이 면접에 대한 총평 피드백을 제공해야 합니다." +
            " '형식 : #총평 피드백 : ' " +
            "총평 피드백은 구체적이어야 하며, 사실과 다른 내용이 있으면 안되며, 자세하게 피드백을 제공해야 합니다." +
            "감정 및 표정 분석 결과를 바탕으로 질문에 대한 피드백을 부탁드립니다." +
            "감정 및 얼굴 표정 분석에서 수집한 관련 정보도 포함하여 자세한 피드백을 하십시오." +
            "참고: 이 프롬프트는 특히 면접의 맥락에서 의사 소통 기술을 분석하기 위해 고안되었으므로 실제 회사에 지원하는 것처럼 정직하게 답변하십시오.";


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
