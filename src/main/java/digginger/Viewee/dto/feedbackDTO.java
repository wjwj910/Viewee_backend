package digginger.Viewee.dto;

public class feedbackDTO {

    public String getAnswerFeed() { return answerFeed; }

    public void setAnswerFeed(String answerFeed) { this.answerFeed = answerFeed; }

    public String getOverrallFeed() { return overrallFeed ; }

    public void setOverrallFeed(String overrallFeed) { this.overrallFeed = overrallFeed; }

    private String answerFeed;
    private String overrallFeed;

    public String getTextSentimentAnalysisData() {
        return textSentimentAnalysisData;
    }

    public void setTextSentimentAnalysisData(String textSentimentAnalysisData) {
        this.textSentimentAnalysisData = textSentimentAnalysisData;
    }

    public String getFacialExpressionAnalysisData() {
        return facialExpressionAnalysisData;
    }

    public void setFacialExpressionAnalysisData(String facialExpressionAnalysisData) {
        this.facialExpressionAnalysisData = facialExpressionAnalysisData;
    }

    private String textSentimentAnalysisData;
    private String facialExpressionAnalysisData;}
