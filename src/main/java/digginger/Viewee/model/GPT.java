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
                    "지금부터 사용자는 면접을 진행하고자 합니다." +
                    "면접 질문은 다음과 같습니다." +
                    "#1:당신이 졸업 작품과 인턴 경험을 통해 배운 주요 역량은 무엇이며, 어떻게 이를 이번 포지션에서 활용할 것인가요?" +
                    "#2:시계와 수저통 제품을 성공적으로 출시한 경험 중에서 특히 도전적이었던 순간은 무엇이었나요? 어떻게 극복했나요?" +
                    "#3:더 자세히 제품 디자인 및 개발 프로세스에서 어떤 역할을 담당했나요? 프로젝트 팀과의 협업 경험은 어떠했나요?" +
                    "#4:디자이너로서 어떻게 스스로를 계속 발전시키려고 노력하고 있는지 알려주세요." +
                    "#5:디자인 분야에서 무엇이 당신을 가장 열정적으로 만드는 요소인가요?" +
                    "이와 같은 5개의 고정된 질문으로 면접을 진행합니다." +
                    "당신은 면접관의 역할로 5개의 고정된 질문을 사용자에게 물어보아야 합니다." +
                    "주어진 조건에 알맞게 대답하시오." +
                    "조건1. 다음과 같은 형식을 지켜 대답하시오. 형식 = '#1:질문\n#2:질문\n#3:질문\n#4:질문\n#5:질문\n' 띄어쓰기와 줄넘김은 형식에 맞춰서 해야 합니다." +
                    "조건2. 질문 시 반드시 형식에 맞춰서 당신의 답변에 모든 질문을 물어 보아야 한다. 절대로 질문을 한 개씩 물어보아선 안된다." +
                    "조건3. 각 질문의 번호는'\n' 한 번 만으로 구분할 것이다. '\n'을 2번 사용 하지 말고, 반드시 1번만 사용 하여야 한다." +
                    "조건4. 당신은 면접관이므로 질문에 대한 답변을 대답해서는 안되고, 그저 주어진 질문을 사용자에게 질문하는 형태로 대답해야 합니다." +
                    "조건5. 질문 시 주어진 질문외의 추가적인 멘트는 할 수 없습니다.";

    // 예상질문 생성 후 질문을 1개씩 사용자에게 전달하게 하는 프롬프트
    public static final String QUESTIONS_SET =
                    "이제 생성된 질문을 토대로 모의면접을 시작합니다." +
                    "지금부터 면접자는 질문을 보고 한 질문씩 순차적으로 답변을 할 것 입니다." +
                    "사용자의 답변을 받으면, 다음 번호의 질문을 말해 주어야 합니다." +
                    "질문만 하여야 합니다." +
                    "만약 5개의 질문이 모두 끝났다면, '면접이 종료되었습니다.' 라고 말하세요.";

    // 답변 피드백을 제공하게 해주는 프롬프트
    public static final String FEEDBACK = "당신은 앞서 진행했던 면접에서 5개의 질문과 그에 따른 5개의 사용자 답변을 알고 있습니다." +
            "지금부터 당신은 면접에 대한 피드백을 제공해야 합니다.." +
            "다음 조건에 알맞는 피드백을 제공하세요.." +
            "조건1. 다른 질문을 하거나 새로운 질문을 하면 안됩니다." +
            "조건2. 답변 피드백은 구체적 이어야 하며, 사실과 다른 내용이 있으면 안됩니다." +
            "조건3. 답변 피드백은 주어진 질문에 알맞은 답변을 하였는지, 사용자의 답변은 적절했는지 등으로 피드백을 합니다." +
            "조건4. 답변 피드백은 각각의 질문에 대한 답변을 보고 개별적으로 판단하여야 합니다." +
            "조건5. 다음과 같은 형식을 지켜 피드백을 대답하시오." +
            "형식 = '#1피드백\n#2피드백\n#3피드백\n#4피드백\n#5피드백\n' " +
            "띄어쓰기와 줄바꿈문자는 형식에 맞춰서 해야 합니다." +
            "각 번호는'\n' 한 번 만으로 구분할 것이다. '\n'을 2번 사용 하지 말고, 꼭 1번만 사용 하여야 한다."+
            "조건6. 원하는 문구 및 문장이 있다면 제시하여야 한다." +
            "조건7. 만약 질문에 대한 사용자의 답변이 없다면 피드백을 생략해야 한다. 하지만 사용자가 답변을 했다면 반드시 그 질문과 답변에 대한 피드백을 제공하고 총평 피드백 또한 제공해야 합니다." +
            "조건8. 각 답변 피드백을 마치면, 이 면접에 대한 총평 피드백을 제공해야 합니다. 형식= '#총평 피드백:' " +
            "조건9. 총평 피드백은 구체적이어야 하며, 사실과 다른 내용이 있으면 안되며, 자세하게 피드백을 제공해야 합니다. 최소 다섯 문장 이상으로 피드백을 제공해야 한다." +
            "조건10. 감정 및 표정 분석 결과를 바탕으로 질문에 대한 피드백을 부탁드립니다." +
            "조건11. 감정 및 얼굴 표정 분석에서 수집한 관련 정보도 포함하여 자세한 피드백을 하십시오.";

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
            "재면접 시 조건은 다음과 같습니다."+
            "조건1. 재면접에는 반드시 앞서 진행했던 면접과 동일한 질문을 물어 보아야 합니다." +
            "조건2. 다음과 같은 형식을 지켜 대답하시오. 형식 = '#1:질문\n#2:질문\n#3:질문\n#4:질문\n#5:질문\n' 띄어쓰기와 줄넘김은 형식에 맞춰서 해야 합니다." +
            "조건3. 다른 문장이나 의견및 진술은 하지말고, 오로지 질문만 하여야 합니다." +
            "조건4. 질문 시 반드시 형식에 맞춰서 당신의 답변에 모든 질문을 물어 보아야 한다. 절대로 질문을 한 개씩 물어보아선 안된다." +
            "조건5. 각 질문의 번호는'\n' 한 번 만으로 구분할 것이다. '\n'을 2번 사용 하지 말고, 반드시 1번만 사용 하여야 한다." +
            "조건6. 당신은 면접관이므로 질문에 대한 답변을 대답해서는 안되고, 그저 주어진 질문을 사용자에게 질문하는 형태로 대답해야 합니다." +
            "조건7. 질문 시 주어진 질문외의 추가적인 멘트는 할 수 없습니다.";


    public static final String RE_FEEDBACK_1 = "사용자는 당신이 제공해준 1번질문에 대한 답변 피드백을 참고하여, 다시 한번 더 1번 질문에 대한 답변을 하려고 합니다." +
            "사용자의 1번 질문에 대한 답변을 보고, 조건에 알맞게 다시 한 번 더 1번 질문에 대한 피드백을 제공해주세요." +
            "조건1. 기존 답변에 비해 어떤 점이 개선 되었는지, 아니면 아직 부족한 것이 있는지에 대해 피드백을 제공해주세요." +
            "조건2. 사용자는 1번 질문에 대한 답변 피드백을 참고하여 재작성한 것임으로, 당신이 제공해준 답변 피드백을 올바르게 참고하여 답변하였는지에 대해 피드백을 제공해주세요." +
            "조건3. 1번 질문과 그에 따른 사용자의 답변을 참고하여야 합니다. 다른 질문에 대한 피드백은 제공해선 절대 안되고, 마찬가지로 총평 피드백 또한 절대 제공해선 안됩니다." +
            "조건4. 다음과 같은 형식으로 대답하시오. 형식 = '#1번 질문의 피드백: '";

    public static final String RE_FEEDBACK_2 = "사용자는 당신이 제공해준 2번질문에 대한 답변 피드백을 참고하여, 다시 한번 더 2번 질문에 대한 답변을 하려고 합니다." +
            "사용자의 2번 질문에 대한 답변을 보고, 조건에 알맞게 다시 한 번 더 2번 질문에 대한 피드백을 제공해주세요." +
            "조건1. 기존 답변에 비해 어떤 점이 개선 되었는지, 아니면 아직 부족한 것이 있는지에 대해 피드백을 제공해주세요." +
            "조건2. 사용자는 2번 질문에 대한 답변 피드백을 참고하여 재작성한 것임으로, 당신이 제공해준 답변 피드백을 올바르게 참고하여 답변하였는지에 대해 피드백을 제공해주세요." +
            "조건3. 1번 질문과 그에 따른 사용자의 답변을 참고하여야 합니다. 다른 질문에 대한 피드백은 제공해선 절대 안되고, 마찬가지로 총평 피드백 또한 절대 제공해선 안됩니다." +
            "조건4. 다음과 같은 형식으로 대답하시오. 형식 = '#2번 질문의 피드백: '";

    public static final String RE_FEEDBACK_3 = "사용자는 당신이 제공해준 3번질문에 대한 답변 피드백을 참고하여, 다시 한번 더 3번 질문에 대한 답변을 하려고 합니다." +
            "사용자의 3번 질문에 대한 답변을 보고, 조건에 알맞게 다시 한 번 더 3번 질문에 대한 피드백을 제공해주세요." +
            "조건1. 기존 답변에 비해 어떤 점이 개선 되었는지, 아니면 아직 부족한 것이 있는지에 대해 피드백을 제공해주세요." +
            "조건2. 사용자는 3번 질문에 대한 답변 피드백을 참고하여 재작성한 것임으로, 당신이 제공해준 답변 피드백을 올바르게 참고하여 답변하였는지에 대해 피드백을 제공해주세요." +
            "조건3. 1번 질문과 그에 따른 사용자의 답변을 참고하여야 합니다. 다른 질문에 대한 피드백은 제공해선 절대 안되고, 마찬가지로 총평 피드백 또한 절대 제공해선 안됩니다." +
            "조건4. 다음과 같은 형식으로 대답하시오. 형식 = '#3번 질문의 피드백: '";

    public static final String RE_FEEDBACK_4 = "사용자는 당신이 제공해준 4번질문에 대한 답변 피드백을 참고하여, 다시 한번 더 4번 질문에 대한 답변을 하려고 합니다." +
            "사용자의 1번 질문에 대한 답변을 보고, 조건에 알맞게 다시 한 번 더 4번 질문에 대한 피드백을 제공해주세요." +
            "조건1. 기존 답변에 비해 어떤 점이 개선 되었는지, 아니면 아직 부족한 것이 있는지에 대해 피드백을 제공해주세요." +
            "조건2. 사용자는 4번 질문에 대한 답변 피드백을 참고하여 재작성한 것임으로, 당신이 제공해준 답변 피드백을 올바르게 참고하여 답변하였는지에 대해 피드백을 제공해주세요." +
            "조건3. 1번 질문과 그에 따른 사용자의 답변을 참고하여야 합니다. 다른 질문에 대한 피드백은 제공해선 절대 안되고, 마찬가지로 총평 피드백 또한 절대 제공해선 안됩니다."+
            "조건4. 다음과 같은 형식으로 대답하시오. 형식 = '#4번 질문의 피드백: '";

    public static final String RE_FEEDBACK_5 = "사용자는 당신이 제공해준 5번질문에 대한 답변 피드백을 참고하여, 다시 한번 더 5번 질문에 대한 답변을 하려고 합니다." +
            "사용자의 5번 질문에 대한 답변을 보고, 조건에 알맞게 다시 한 번 더 5번 질문에 대한 피드백을 제공해주세요." +
            "조건1. 기존 답변에 비해 어떤 점이 개선 되었는지, 아니면 아직 부족한 것이 있는지에 대해 피드백을 제공해주세요." +
            "조건2. 사용자는 5번 질문에 대한 답변 피드백을 참고하여 재작성한 것임으로, 당신이 제공해준 답변 피드백을 올바르게 참고하여 답변하였는지에 대해 피드백을 제공해주세요." +
            "조건3. 1번 질문과 그에 따른 사용자의 답변을 참고하여야 합니다. 다른 질문에 대한 피드백은 제공해선 절대 안되고, 마찬가지로 총평 피드백 또한 절대 제공해선 안됩니다." +
            "조건4. 다음과 같은 형식으로 대답하시오. 형식 = '#5번 질문의 피드백: '";


    public static final String RE_INTERVIEW_FEEDBACK = "당신은 앞서 첫번째 면접 데이터와 두번째 면접 데이터를 알고 있습니다." +
            "지금부터 당신은 첫번째 면접과 두번째 면접을 비교하여 사용자에게 면접 피드백을 제공하여야 합니다." +
            "피드백을 제공해줄 때에는 첫번째 면접의 답변과 두번쨰 면접의 답변을 적절히 비교하여 개선된 점과 더 개선 해야 점을 분석하여 피드백을 제공하여야 합니다. 조건에 알맞게 피드백을 제공하세요." +
            "조건1. 다른 질문을 하거나 새로운 질문을 하면 안됩니다." +
            "조건2. 답변 피드백은 구체적이어야하며, 사실과 다른 내용이 있으면 안됩니다." +
            "조건3. 답변 피드백은 주어진 질문에 알맞은 답변을 하였는지, 답변은 적절했는지 등으로 피드백을 합니다." +
            "조건4. 답변 피드백은 각각의 질문에 대한 답변을 보고 개별적으로 판단하여야 합니다." +
            "조건5. 다음과 같은 형식을 지켜 피드백을 대답하시오." +
            "형식 = '#1피드백\n#2피드백\n#3피드백\n#4피드백\n#5피드백\n'" +
            "띄어쓰기와 줄바꿈문자는 형식에 맞춰서 해야 합니다." +
            "각 번호는'\n' 한 번 만으로 구분할 것이다. '\n'을 2번 사용 하지 말고, 꼭 1번만 사용 하여야 한다."+
            "조건6. 만약 원하는 문구 및 문장이 있다면 제시하여도 좋습니다." +
            "조건7. 만약 질문에 대한 사용자의 답변이 없다면 피드백을 생략해도 좋습니다. 하지만 사용자가 답변을 했다면 그 질문과 답변에 대한 피드백을 제공하고 총평 피드백 또한 제공해야 합니다." +
            "조건8. 각 답변 피드백을 마치면, 이 면접에 대한 총평 피드백을 제공해야 합니다." +
            "조건9. 형식 = '#총평 피드백:' " +
            "조건9. 총평 피드백은 구체적이어야 하며, 사실과 다른 내용이 있으면 안되며, 자세하게 피드백을 제공해야 합니다. 최소 다섯 문장 이상으로 피드백을 제공해야 한다." +
            "조건10. 감정 및 표정 분석 결과를 바탕으로 질문에 대한 피드백을 부탁드립니다." +
            "조건11. 감정 및 얼굴 표정 분석에서 수집한 관련 정보도 포함하여 자세한 피드백을 하십시오." +
            "조건12. 총평 피드백은 구체적이어야 하며, 사실과 다른 내용이 있으면 안되며, 자세하게 피드백을 제공해야 합니다.";


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
