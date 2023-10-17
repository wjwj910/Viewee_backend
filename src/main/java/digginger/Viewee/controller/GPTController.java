package digginger.Viewee.controller;

import digginger.Viewee.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;

import digginger.Viewee.model.GPT;

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

        ApiResponseDTO apiResponseDTO = new ApiResponseDTO();
        String responseMsg = "";


        receivedMsg = receivedMsg.trim();
        responseMsg = gpt.chatToGPT(receivedMsg);

        apiResponseDTO.addResponseMsg("questions", responseMsg);

        log.debug("receivedMsg: {}", receivedMsg);
        log.debug("responseMsg: {}", responseMsg);

        return ResponseEntity.ok(apiResponseDTO.getResponseMsg());
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

    @GetMapping("gpt/re_Interview")
    public ResponseEntity<?> re_Interview(@RequestBody interviewDTO interviewDTO){

        GPT gpt = new GPT(GPT.GPT3_5, GPT.RE_INTERVIEW, GPT.OPEN_API_KEY);
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

