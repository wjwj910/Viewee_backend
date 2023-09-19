package digginger.Viewee.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class feedbackDTO {

    private String answerFeed;
    private String overrallFeed;
    private String textSentimentAnalysisData;
    private String facialExpressionAnalysisData;
}
