package visang.showcase.aibackend.dto.response.diagnosis.dashboard;

import lombok.Getter;

@Getter
public class TopicCorrectRate {
    private String topic_name;
    private Integer correct_answers;
    private Integer incorrect_answers;

    public TopicCorrectRate(String topic_name, int correct_answers, int incorrect_answers) {
        this.topic_name = topic_name;
        this.correct_answers = correct_answers;
        this.incorrect_answers = incorrect_answers;
    }
}