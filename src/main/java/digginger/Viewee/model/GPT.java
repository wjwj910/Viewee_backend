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
    public static final String FEEDBACK = "당신은 앞서 진행했던 면접에서 5개의 질문과 그 질문에 대한 사용자의 답변 5개를 알고 있습니다." +
            "지금부터 당신은 면접에 대한 피드백을 제공해야 합니다." +
            "다음 조건에 알맞는 피드백을 제공하세요." +
            "조건1. 다른 질문을 하거나 새로운 질문을 하면 안됩니다." +
            "조건2. 답변 피드백은 구체적 이어야 하며, 사실과 다른 내용이 있으면 안됩니다." +
            "조건3. 답변 피드백은 주어진 질문에 알맞은 답변을 하였는지, 사용자의 답변은 적절했는지 등으로 피드백을 합니다." +
            "조건4. 답변 피드백은 각각의 질문에 대한 답변을 보고 개별적으로 판단하여야 합니다." +
            "조건5. 답변 피드백에 텍스트 감정 분석 데이터 및 표정 감정 분석 데이터를 참고 및 활용하여 피드백을 제공해주세요." +
            "조건6. 다음과 같은 형식을 지켜 피드백을 대답하시오." +
            "형식 = '#1피드백:\n#2피드백:\n#3피드백:\n#4피드백:\n#5피드백:' " +
            "띄어쓰기와 줄바꿈문자는 형식에 맞춰서 해야 합니다." +
            "각 번호는'\n' 한 번 만으로 구분할 것이다. '\n'을 2번 사용 하지 말고, 꼭 1번만 사용 하여야 한다."+
            "조건6. 꼭 형식에 맞춰서 답변해야 함으로 예시를 보여드리겠습니다." +
            "예시 = '#1피드백: 당신은 졸업 작품과 인턴 경험을 통해 정말 집요할 정도의 시장조사와 타겟팅 능력을 배웠다는 것을 언급하였습니다. 이러한 역량을 이번 제품 디자이너 포지션에서 활용할 것이라고 말씀하셨는데, 이는 좋은 입사 동기와 연결된 것으로 보입니다.\\n#2피드백: 당신은 시계와 수저통 제품을 성공적으로 출시한 경험 중에서 모든 순간이 도전이었으며, 걱정이 많은 성향을 가진 사람으로써 모든 의사결정을 직접 해야하는 상황에서 두려움이 있었다고 말씀하셨습니다. 하지만 많은 응원과 도움을 받아 극복하였다는 사실이 인상적입니다.\\n#3피드백: 당신은 제품 디자인 및 개발 프로세스에서 조금 특수한 경우로서 전체적인 프로세스를 담당한 경험이 있다고 말씀하셨습니다. 이는 팀원들과의 협업 경험을 통해 동료들의 업무 분담과 협조를 보조하는 역할을 수행하였을 것으로 추측됩니다.\\n#4피드백: 당신은 디자이너로서 스스로를 계속 발전시키기 위해 다양한 분야에 관심을 가지고 있고, 세상을 경험하고 있으며 그 경험을 디자인에 적용하려고 노력하고 있다고 말씀하셨습니다. 이는 디자이너로서 지속적인 성장과 발전을 추구하는 태도를 보여줍니다.\\n#5피드백: 당신은 디자인 분야에서 결과물에 대한 긍정적인 반응이 가장 열정적으로 만든다고 말씀하셨습니다. 이는 자신의 작품이 사람들에게 긍정적인 영향을 미치고 소비자들의 만족을 도출한다는 것에 대한 열망을 보여줍니다. 이는 디자이너로서의 열정을 잘 보여주는 성향입니다.\\n\\n#총평 피드백: 당신은 면접에서 여러 질문에 대해 친절하고 명확한 답변을 제공하였습니다. 당신은 주어진 질문에 맞춰 각각의 답변을 제공하였고, 자신의 경험과 역량을 잘 어필하였습니다. 또한, 답변의 구체성과 사실에 기반한 피드백을 제공하여 면접 질문에 대해 충분한 이해와 준비를 한 것으로 보입니다. 당신은 디자인 분야에서 열정적이고 성장을 추구하는 자세를 가지고 있으며, 이러한 모습이 제품 디자이너로서의 잠재력을 보여줍니다. 면접에서의 성과를 바탕으로 당신은 이 직무에 대한 잠재력을 가지고 있으며, 기업과 함께 성공을 이룰 수 있을 것입니다.'" +
            "주어진 예시는 형식에 맞춰서 답변해야 함으로 보여준 예시임으로 이 예시를 그대로 답변해서는 안되며 참고용으로만 확인하세요." +
            "조건7. 원하는 문구 및 문장이 있다면 제시하여야 한다." +
            "조건8. 만약 질문에 대한 사용자의 답변이 없다면 피드백을 생략해야 한다. 하지만 사용자가 답변을 했다면 반드시 그 질문과 답변에 대한 피드백을 제공하고 총평 피드백 또한 제공해야 합니다." +
            "조건9. 5번째 답변 피드백을 마치면, 이 면접에 대한 총평 피드백을 또한 같이 제공해야 합니다. 총평 피드백 제공하는 형식은 다음과 같습니다. 형식= '#총평 피드백:' " +
            "조건10. 총평 피드백은 구체적이어야 하며, 사실과 다른 내용이 있으면 안되며, 자세하게 피드백을 제공해야 합니다. 최소  4문장 이상으로 피드백을 제공하세요." +
            "조건11. 총평 피드백에 텍스트 감정 분석 데이터 및 표정 감정 분석 데이터를 참고 및 활용하여 피드백을 자세히 제공하세요.";

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
            "조건1. 다음과 같은 형식으로 대답하시오. 형식 = '#1번 질문의 피드백: '" +
            "조건2. 기존 답변에 비해 어떤 점이 개선 되었는지, 아니면 아직 부족한 것이 있는지에 대해 피드백을 제공해주세요." +
            "조건2. 사용자는 1번 질문에 대한 답변 피드백을 참고하여 재작성한 것임으로, 당신이 제공해준 답변 피드백을 올바르게 참고하여 답변하였는지에 대해 피드백을 제공해주세요." +
            "조건3. 이것은 1번 질문에 따른 사용자의 수정된 답변임으로 다른 질문에 대한 피드백을 제공해선 안됩니다." +
            "조건4. 이것은 1번 질문에 따른 사용자의 수정된 답변임으로 총평피드백을 제공해선 안됩니다. 조건1에서 말한 형식 = '#1번 질문의 피드백: '에 맞춰 피드백을 제공해주세요." +
            "조건5. 피드백을 제공시 피드백외의 다른 추가적인 멘트는 할 수 없고, 위 조건에 알맞게 피드백만 제공하여야 합니다.";

    public static final String RE_FEEDBACK_2 =  "사용자는 당신이 제공해준 2번질문에 대한 답변 피드백을 참고하여, 다시 한번 더 2번 질문에 대한 답변을 하려고 합니다." +
            "사용자의 2번 질문에 대한 답변을 보고, 조건에 알맞게 다시 한 번 더 2번 질문에 대한 피드백을 제공해주세요." +
            "조건1. 다음과 같은 형식으로 대답하시오. 형식 = '#2번 질문의 피드백: '" +
            "조건2. 기존 답변에 비해 어떤 점이 개선 되었는지, 아니면 아직 부족한 것이 있는지에 대해 피드백을 제공해주세요." +
            "조건2. 사용자는 2번 질문에 대한 답변 피드백을 참고하여 재작성한 것임으로, 당신이 제공해준 답변 피드백을 올바르게 참고하여 답변하였는지에 대해 피드백을 제공해주세요." +
            "조건3. 이것은 2번 질문에 따른 사용자의 수정된 답변임으로 다른 질문에 대한 피드백을 제공해선 안됩니다." +
            "조건4. 이것은 2번 질문에 따른 사용자의 수정된 답변임으로 총평피드백을 제공해선 안됩니다. 조건1에서 말한 형식 = '#2번 질문의 피드백: '에 맞춰 피드백을 제공해주세요." +
            "조건5. 피드백을 제공시 피드백외의 다른 추가적인 멘트는 할 수 없고, 위 조건에 알맞게 피드백만 제공하여야 합니다.";

    public static final String RE_FEEDBACK_3 =  "사용자는 당신이 제공해준 3번질문에 대한 답변 피드백을 참고하여, 다시 한번 더 3번 질문에 대한 답변을 하려고 합니다." +
            "사용자의 3번 질문에 대한 답변을 보고, 조건에 알맞게 다시 한 번 더 3번 질문에 대한 피드백을 제공해주세요." +
            "조건1. 다음과 같은 형식으로 대답하시오. 형식 = '#3번 질문의 피드백: '" +
            "조건2. 기존 답변에 비해 어떤 점이 개선 되었는지, 아니면 아직 부족한 것이 있는지에 대해 피드백을 제공해주세요." +
            "조건2. 사용자는 3번 질문에 대한 답변 피드백을 참고하여 재작성한 것임으로, 당신이 제공해준 답변 피드백을 올바르게 참고하여 답변하였는지에 대해 피드백을 제공해주세요." +
            "조건3. 이것은 3번 질문에 따른 사용자의 수정된 답변임으로 다른 질문에 대한 피드백을 제공해선 안됩니다." +
            "조건4. 이것은 3번 질문에 따른 사용자의 수정된 답변임으로 총평피드백을 제공해선 안됩니다. 조건1에서 말한 형식 = '#3번 질문의 피드백: '에 맞춰 피드백을 제공해주세요." +
            "조건5. 피드백을 제공시 피드백외의 다른 추가적인 멘트는 할 수 없고, 위 조건에 알맞게 피드백만 제공하여야 합니다.";

    public static final String RE_FEEDBACK_4 =  "사용자는 당신이 제공해준 4번질문에 대한 답변 피드백을 참고하여, 다시 한번 더 4번 질문에 대한 답변을 하려고 합니다." +
            "사용자의 4번 질문에 대한 답변을 보고, 조건에 알맞게 다시 한 번 더 4번 질문에 대한 피드백을 제공해주세요." +
            "조건1. 다음과 같은 형식으로 대답하시오. 형식 = '#4번 질문의 피드백: '" +
            "조건2. 기존 답변에 비해 어떤 점이 개선 되었는지, 아니면 아직 부족한 것이 있는지에 대해 피드백을 제공해주세요." +
            "조건2. 사용자는 4번 질문에 대한 답변 피드백을 참고하여 재작성한 것임으로, 당신이 제공해준 답변 피드백을 올바르게 참고하여 답변하였는지에 대해 피드백을 제공해주세요." +
            "조건3. 이것은 4번 질문에 따른 사용자의 수정된 답변임으로 다른 질문에 대한 피드백을 제공해선 안됩니다." +
            "조건4. 이것은 4번 질문에 따른 사용자의 수정된 답변임으로 총평피드백을 제공해선 안됩니다. 조건1에서 말한 형식 = '#4번 질문의 피드백: '에 맞춰 피드백을 제공해주세요." +
            "조건5. 피드백을 제공시 피드백외의 다른 추가적인 멘트는 할 수 없고, 위 조건에 알맞게 피드백만 제공하여야 합니다.";

    public static final String RE_FEEDBACK_5 =  "사용자는 당신이 제공해준 1번질문에 대한 답변 피드백을 참고하여, 다시 한번 더 5번 질문에 대한 답변을 하려고 합니다." +
            "사용자의 5번 질문에 대한 답변을 보고, 조건에 알맞게 다시 한 번 더 5번 질문에 대한 피드백을 제공해주세요." +
            "조건1. 다음과 같은 형식으로 대답하시오. 형식 = '#5번 질문의 피드백: '" +
            "조건2. 기존 답변에 비해 어떤 점이 개선 되었는지, 아니면 아직 부족한 것이 있는지에 대해 피드백을 제공해주세요." +
            "조건2. 사용자는 5번 질문에 대한 답변 피드백을 참고하여 재작성한 것임으로, 당신이 제공해준 답변 피드백을 올바르게 참고하여 답변하였는지에 대해 피드백을 제공해주세요." +
            "조건3. 이것은 5번 질문에 따른 사용자의 수정된 답변임으로 다른 질문에 대한 피드백을 제공해선 안됩니다." +
            "조건4. 이것은 5번 질문에 따른 사용자의 수정된 답변임으로 총평피드백을 제공해선 안됩니다. 조건1에서 말한 형식 = '#5번 질문의 피드백: '에 맞춰 피드백을 제공해주세요." +
            "조건5. 피드백을 제공시 피드백외의 다른 추가적인 멘트는 할 수 없고, 위 조건에 알맞게 피드백만 제공하여야 합니다.";


    public static final String RE_INTERVIEW_FEEDBACK = "당신은 앞서 첫번째 면접 데이터와 두번째 면접 데이터를 알고 있습니다." +
            "지금부터 당신은 첫번째 면접과 두번째 면접을 비교하여 사용자에게 면접 피드백을 제공하여야 합니다." +
            "사용자는 두번째 면접의 답변에서, 첫번째 면접에서의 답변에 몇가지 문장을 추가해서 답변 할 것입니다." +
            "피드백을 제공해줄 때에는 첫번째 면접의 답변과 두번째 면접의 답변을 적절히 비교하여 개선된 점과 더 개선 해야 점을 분석하여 피드백을 제공하여야 합니다. 조건에 알맞게 피드백을 제공하세요." +
            "조건1. 다른 질문을 하거나 새로운 질문을 하면 안됩니다." +
            "조건2. 답변 피드백은 구체적이어야하며, 사실과 다른 내용이 있으면 안됩니다." +
            "조건3. 답변 피드백은 주어진 질문에 알맞은 답변을 하였는지, 답변은 적절했는지 등으로 피드백을 합니다." +
            "조건4. 답변 피드백은 각각의 질문에 대한 답변을 보고 개별적으로 판단하여야 합니다." +
            "조건5. 답변 피드백에 텍스트 감정 분석 데이터 및 표정 감정 분석 데이터를 참고 및 활용하여 피드백을 제공해주세요." +
            "조건6. 다음과 같은 형식을 지켜 피드백을 대답하시오." +
            "형식 = '#1피드백\n#2피드백\n#3피드백\n#4피드백\n#5피드백\n'" +
            "띄어쓰기와 줄바꿈문자는 형식에 맞춰서 해야 합니다." +
            "각 번호는'\n' 한 번 만으로 구분할 것이다. '\n'을 2번 사용 하지 말고, 꼭 1번만 사용 하여야 한다."+
            "조건6. 꼭 형식에 맞춰서 답변해야 함으로 예시를 보여드리겠습니다." +
            "예시 = '#1피드백: 당신은 졸업 작품과 인턴 경험을 통해 정말 집요할 정도의 시장조사와 타겟팅 능력을 배웠다는 것을 언급하였습니다. 이러한 역량을 이번 제품 디자이너 포지션에서 활용할 것이라고 말씀하셨는데, 이는 좋은 입사 동기와 연결된 것으로 보입니다.\\n#2피드백: 당신은 시계와 수저통 제품을 성공적으로 출시한 경험 중에서 모든 순간이 도전이었으며, 걱정이 많은 성향을 가진 사람으로써 모든 의사결정을 직접 해야하는 상황에서 두려움이 있었다고 말씀하셨습니다. 하지만 많은 응원과 도움을 받아 극복하였다는 사실이 인상적입니다.\\n#3피드백: 당신은 제품 디자인 및 개발 프로세스에서 조금 특수한 경우로서 전체적인 프로세스를 담당한 경험이 있다고 말씀하셨습니다. 이는 팀원들과의 협업 경험을 통해 동료들의 업무 분담과 협조를 보조하는 역할을 수행하였을 것으로 추측됩니다.\\n#4피드백: 당신은 디자이너로서 스스로를 계속 발전시키기 위해 다양한 분야에 관심을 가지고 있고, 세상을 경험하고 있으며 그 경험을 디자인에 적용하려고 노력하고 있다고 말씀하셨습니다. 이는 디자이너로서 지속적인 성장과 발전을 추구하는 태도를 보여줍니다.\\n#5피드백: 당신은 디자인 분야에서 결과물에 대한 긍정적인 반응이 가장 열정적으로 만든다고 말씀하셨습니다. 이는 자신의 작품이 사람들에게 긍정적인 영향을 미치고 소비자들의 만족을 도출한다는 것에 대한 열망을 보여줍니다. 이는 디자이너로서의 열정을 잘 보여주는 성향입니다.\\n\\n#총평 피드백: 당신은 면접에서 여러 질문에 대해 친절하고 명확한 답변을 제공하였습니다. 당신은 주어진 질문에 맞춰 각각의 답변을 제공하였고, 자신의 경험과 역량을 잘 어필하였습니다. 또한, 답변의 구체성과 사실에 기반한 피드백을 제공하여 면접 질문에 대해 충분한 이해와 준비를 한 것으로 보입니다. 당신은 디자인 분야에서 열정적이고 성장을 추구하는 자세를 가지고 있으며, 이러한 모습이 제품 디자이너로서의 잠재력을 보여줍니다. 면접에서의 성과를 바탕으로 당신은 이 직무에 대한 잠재력을 가지고 있으며, 기업과 함께 성공을 이룰 수 있을 것입니다.'" +
            "조건7. 만약 원하는 문구 및 문장이 있다면 제시하여도 좋습니다." +
            "조건8. 만약 질문에 대한 사용자의 답변이 없다면 피드백을 생략해도 좋습니다. 하지만 사용자가 답변을 했다면 그 질문과 답변에 대한 피드백을 제공하고 총평 피드백 또한 제공해야 합니다." +
            "조건9. 각 답변 피드백을 마치면, 이 면접에 대한 총평 피드백을 제공해야 합니다." +
            "조건10. 형식 = '#총평 피드백:' " +
            "조건11. 총평 피드백은 구체적이어야 하며, 사실과 다른 내용이 있으면 안되며, 자세하게 피드백을 제공해야 합니다. 최소 4문장 이상으로 피드백을 제공해야 한다." +
            "조건12. 총평 피드백에 텍스트 감정 분석 데이터 및 표정 감정 분석 데이터를 참고 및 활용하여 피드백을 자세히 제공하세요.";


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
