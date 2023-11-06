package digginger.Viewee.controller;

import digginger.Viewee.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;

import digginger.Viewee.model.GPT;

import java.util.HashMap;
import java.util.Map;

@Controller
public class GPTController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @PostMapping("/gpt/setting")
    public ResponseEntity<?> setting(@RequestBody UserInfoDTO userInfoDTO) {
        GPT reset = new GPT(GPT.GPT3_5, GPT.SYSTEM_ROLE_INTERVIEWER_SETTING, GPT.OPEN_API_KEY);
        reset.clearMsgHistory();

        GPT gpt = new GPT(GPT.GPT3_5, GPT.SYSTEM_ROLE_INTERVIEWER_SETTING, GPT.OPEN_API_KEY);

        String receivedMsg = String.format("이름:'%s', 생년월일:'%s', 학력:'%s', 경력:'%s', 지원 직무:'%s', 자격증:'%s', 자기소개서:'%s'",
                userInfoDTO.getName(),
                userInfoDTO.getBirth(),
                userInfoDTO.getEducation(),
                userInfoDTO.getCareer(),
                userInfoDTO.getSupport_job(),
                userInfoDTO.getCertificate(),
                userInfoDTO.getResume()
        );

        //ApiResponseDTO apiResponseDTO = new ApiResponseDTO();
        String responseMsg = "";


        receivedMsg = receivedMsg.trim();
        responseMsg = gpt.chatToGPT(receivedMsg);

        //apiResponseDTO.addResponseMsg("questions", responseMsg);

        log.debug("receivedMsg: {}", receivedMsg);
        log.debug("responseMsg: {}", responseMsg);

        final String quest_test =
                "#1:당신이 졸업 작품과 인턴 경험을 통해 배운 주요 역량은 무엇이며, 어떻게 이를 이번 포지션에서 활용할 것인가요?\n" +
                "#2:시계와 수저통 제품을 성공적으로 출시한 경험 중에서 특히 도전적이었던 순간은 무엇이었나요? 어떻게 극복했나요?\n" +
                "#3:더 자세히 제품 디자인 및 개발 프로세스에서 어떤 역할을 담당했나요? 프로젝트 팀과의 협업 경험은 어떠했나요?\n" +
                "#4:디자이너로서 어떻게 스스로를 계속 발전시키려고 노력하고 있는지 알려주세요.\n" +
                "#5:디자인 분야에서 무엇이 당신을 가장 열정적으로 만드는 요소인가요?";

        Map<String, String> response = new HashMap<>();
        response.put("questions", quest_test);
        gpt.addMsg("assistant", quest_test);
        //return ResponseEntity.ok(apiResponseDTO.getResponseMsg());
        return ResponseEntity.ok(response);

    }

    @PostMapping("gpt/interview")
    public ResponseEntity<?> interview(@RequestBody interviewDTO interviewDTO){

        GPT gpt = new GPT(GPT.GPT3_5, GPT.QUESTIONS_SET, GPT.OPEN_API_KEY);
        String receivedMsg = String.format("답변:'%s'", interviewDTO.getAnswer());

        ApiResponseDTO apiResponseDTO = new ApiResponseDTO();
        String responseMsg = "";

        receivedMsg = receivedMsg.trim();
        responseMsg = gpt.chatToGPT(receivedMsg);

        apiResponseDTO.addResponseMsg("question", responseMsg);

        log.debug("receivedMsg: {}", receivedMsg);
        log.debug("responseMsg: {}", responseMsg);

        return ResponseEntity.ok(apiResponseDTO.getResponseMsg());

    }

    @PostMapping("gpt/interview/feedback")
    public ResponseEntity<?> feedback(@RequestBody feedbackDTO feedbackDTO){

        GPT gpt = new GPT(GPT.GPT3_5, GPT.FEEDBACK, GPT.OPEN_API_KEY);



        String receivedMsg = String.format("textSentimentAnalysisData : '%s', facialExpressionAnalysisData: '%s',answerFeed : '%s', overrallFeed : '%s'",
                feedbackDTO.getTextSentimentAnalysisData(),
                feedbackDTO.getFacialExpressionAnalysisData(),
                feedbackDTO.getAnswerFeed(),
                feedbackDTO.getOverrallFeed()
        );

        ApiResponseDTO apiResponseDTO = new ApiResponseDTO();
        String responseMsg = "";

        receivedMsg = receivedMsg.trim();
        responseMsg = gpt.chatToGPT(receivedMsg);

        apiResponseDTO.addResponseMsg("feedback", responseMsg);

        log.debug("receivedMsg: {}", receivedMsg);
        log.debug("responseMsg: {}", responseMsg);

        return ResponseEntity.ok(apiResponseDTO.getResponseMsg());
    }

    @PostMapping("gpt/re_Interview")
    public ResponseEntity<?> re_Interview(@RequestBody interviewDTO interviewDTO){

        GPT gpt = new GPT(GPT.GPT3_5, GPT.RE_INTERVIEW, GPT.OPEN_API_KEY);
        String receivedMsg = String.format("답변:'%s'", interviewDTO.getAnswer());

        //ApiResponseDTO apiResponseDTO = new ApiResponseDTO();
        String responseMsg = "";

        receivedMsg = receivedMsg.trim();
        responseMsg = gpt.chatToGPT(receivedMsg);

        //apiResponseDTO.addResponseMsg("question", responseMsg);

        log.debug("receivedMsg: {}", receivedMsg);
        log.debug("responseMsg: {}", responseMsg);

        final String quest_test =
                "#1:당신이 졸업 작품과 인턴 경험을 통해 배운 주요 역량은 무엇이며, 어떻게 이를 이번 포지션에서 활용할 것인가요?\n" +
                "#2:시계와 수저통 제품을 성공적으로 출시한 경험 중에서 특히 도전적이었던 순간은 무엇이었나요? 어떻게 극복했나요?\n" +
                "#3:더 자세히 제품 디자인 및 개발 프로세스에서 어떤 역할을 담당했나요? 프로젝트 팀과의 협업 경험은 어떠했나요?\n" +
                "#4:디자이너로서 어떻게 스스로를 계속 발전시키려고 노력하고 있는지 알려주세요.\n" +
                "#5:디자인 분야에서 무엇이 당신을 가장 열정적으로 만드는 요소인가요?";

        Map<String, String> response = new HashMap<>();
        response.put("questions", quest_test);
        gpt.addMsg("assistant", quest_test);
        return ResponseEntity.ok(response);
    }


    @PostMapping("gpt/interview/feedback/re_answer_feedback_1")
    public ResponseEntity<?> re_answer_feedback_1(@RequestBody feedbackDTO feedbackDTO){

        GPT gpt = new GPT(GPT.GPT3_5, GPT.RE_FEEDBACK_1, GPT.OPEN_API_KEY);

        // 다시 작성된 답변을 받아서 처리하는 로직입니다.
        String receivedMsg = String.format("reAnswer : '%s', textSentimentAnalysisData : '%s', facialExpressionAnalysisData: '%s',answerFeed : '%s', overrallFeed : '%s'",
                feedbackDTO.getReAnswer(),
                feedbackDTO.getTextSentimentAnalysisData(),
                feedbackDTO.getFacialExpressionAnalysisData(),
                feedbackDTO.getAnswerFeed(),
                feedbackDTO.getOverrallFeed()
        );

        ApiResponseDTO apiResponseDTO = new ApiResponseDTO();
        String responseMsg = "";

        receivedMsg = receivedMsg.trim();
        responseMsg = gpt.chatToGPT(receivedMsg);

        apiResponseDTO.addResponseMsg("feedback", responseMsg);

        log.debug("receivedMsg: {}", receivedMsg);
        log.debug("responseMsg: {}", responseMsg);

        return ResponseEntity.ok(apiResponseDTO.getResponseMsg());
    }







    @PostMapping("gpt/interview/feedback/re_answer_feedback_2")
    public ResponseEntity<?> re_answer_feedback_2(@RequestBody feedbackDTO feedbackDTO){

        GPT gpt = new GPT(GPT.GPT3_5, GPT.RE_FEEDBACK_2, GPT.OPEN_API_KEY);

        // 다시 작성된 답변을 받아서 처리하는 로직입니다.
        String receivedMsg = String.format("reAnswer : '%s', textSentimentAnalysisData : '%s', facialExpressionAnalysisData: '%s',answerFeed : '%s', overrallFeed : '%s'",
                feedbackDTO.getReAnswer(),
                feedbackDTO.getTextSentimentAnalysisData(),
                feedbackDTO.getFacialExpressionAnalysisData(),
                feedbackDTO.getAnswerFeed(),
                feedbackDTO.getOverrallFeed()
        );

        ApiResponseDTO apiResponseDTO = new ApiResponseDTO();
        String responseMsg = "";

        receivedMsg = receivedMsg.trim();
        responseMsg = gpt.chatToGPT(receivedMsg);

        apiResponseDTO.addResponseMsg("feedback", responseMsg);

        log.debug("receivedMsg: {}", receivedMsg);
        log.debug("responseMsg: {}", responseMsg);

        return ResponseEntity.ok(apiResponseDTO.getResponseMsg());
    }



    @PostMapping("gpt/interview/feedback/re_answer_feedback_3")
    public ResponseEntity<?> re_answer_feedback_3(@RequestBody feedbackDTO feedbackDTO){

        GPT gpt = new GPT(GPT.GPT3_5, GPT.RE_FEEDBACK_3, GPT.OPEN_API_KEY);

        // 다시 작성된 답변을 받아서 처리하는 로직입니다.
        String receivedMsg = String.format("reAnswer : '%s', textSentimentAnalysisData : '%s', facialExpressionAnalysisData: '%s',answerFeed : '%s', overrallFeed : '%s'",
                feedbackDTO.getReAnswer(),
                feedbackDTO.getTextSentimentAnalysisData(),
                feedbackDTO.getFacialExpressionAnalysisData(),
                feedbackDTO.getAnswerFeed(),
                feedbackDTO.getOverrallFeed()
        );

        ApiResponseDTO apiResponseDTO = new ApiResponseDTO();
        String responseMsg = "";

        receivedMsg = receivedMsg.trim();
        responseMsg = gpt.chatToGPT(receivedMsg);

        apiResponseDTO.addResponseMsg("feedback", responseMsg);

        log.debug("receivedMsg: {}", receivedMsg);
        log.debug("responseMsg: {}", responseMsg);

        return ResponseEntity.ok(apiResponseDTO.getResponseMsg());
    }
    @PostMapping("gpt/interview/feedback/re_answer_feedback_4")
    public ResponseEntity<?> re_answer_feedback_4(@RequestBody feedbackDTO feedbackDTO){

        GPT gpt = new GPT(GPT.GPT3_5, GPT.RE_FEEDBACK_4, GPT.OPEN_API_KEY);

        // 다시 작성된 답변을 받아서 처리하는 로직입니다.
        String receivedMsg = String.format("reAnswer : '%s', textSentimentAnalysisData : '%s', facialExpressionAnalysisData: '%s',answerFeed : '%s', overrallFeed : '%s'",
                feedbackDTO.getReAnswer(),
                feedbackDTO.getTextSentimentAnalysisData(),
                feedbackDTO.getFacialExpressionAnalysisData(),
                feedbackDTO.getAnswerFeed(),
                feedbackDTO.getOverrallFeed()
        );

        ApiResponseDTO apiResponseDTO = new ApiResponseDTO();
        String responseMsg = "";

        receivedMsg = receivedMsg.trim();
        responseMsg = gpt.chatToGPT(receivedMsg);

        apiResponseDTO.addResponseMsg("feedback", responseMsg);

        log.debug("receivedMsg: {}", receivedMsg);
        log.debug("responseMsg: {}", responseMsg);

        return ResponseEntity.ok(apiResponseDTO.getResponseMsg());
    }
    @PostMapping("gpt/interview/feedback/re_answer_feedback_5")
    public ResponseEntity<?> re_answer_feedback_5(@RequestBody feedbackDTO feedbackDTO){

        GPT gpt = new GPT(GPT.GPT3_5, GPT.RE_FEEDBACK_5, GPT.OPEN_API_KEY);

        // 다시 작성된 답변을 받아서 처리하는 로직입니다.
        String receivedMsg = String.format("reAnswer : '%s', textSentimentAnalysisData : '%s', facialExpressionAnalysisData: '%s',answerFeed : '%s', overrallFeed : '%s'",
                feedbackDTO.getReAnswer(),
                feedbackDTO.getTextSentimentAnalysisData(),
                feedbackDTO.getFacialExpressionAnalysisData(),
                feedbackDTO.getAnswerFeed(),
                feedbackDTO.getOverrallFeed()
        );

        ApiResponseDTO apiResponseDTO = new ApiResponseDTO();
        String responseMsg = "";

        receivedMsg = receivedMsg.trim();
        responseMsg = gpt.chatToGPT(receivedMsg);

        apiResponseDTO.addResponseMsg("feedback", responseMsg);

        log.debug("receivedMsg: {}", receivedMsg);
        log.debug("responseMsg: {}", responseMsg);

        return ResponseEntity.ok(apiResponseDTO.getResponseMsg());
    }




    @PostMapping("gpt/re_Interview/feedback")
    public ResponseEntity<?> re_interview_feedback(@RequestBody feedbackDTO feedbackDTO){

        GPT gpt = new GPT(GPT.GPT3_5, GPT.RE_INTERVIEW_FEEDBACK, GPT.OPEN_API_KEY);

        String receivedMsg = String.format("textSentimentAnalysisData : '%s', facialExpressionAnalysisData: '%s',answerFeed : '%s', overrallFeed : '%s'",
                feedbackDTO.getTextSentimentAnalysisData(),
                feedbackDTO.getFacialExpressionAnalysisData(),
                feedbackDTO.getAnswerFeed(),
                feedbackDTO.getOverrallFeed()
        );

        ApiResponseDTO apiResponseDTO = new ApiResponseDTO();
        String responseMsg = "";

        receivedMsg = receivedMsg.trim();
        responseMsg = gpt.chatToGPT(receivedMsg);

        apiResponseDTO.addResponseMsg("feedback", responseMsg);

        log.debug("receivedMsg: {}", receivedMsg);
        log.debug("responseMsg: {}", responseMsg);

        return ResponseEntity.ok(apiResponseDTO.getResponseMsg());
    }
}

