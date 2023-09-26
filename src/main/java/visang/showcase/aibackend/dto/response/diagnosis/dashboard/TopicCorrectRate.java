package visang.showcase.aibackend.dto.response.diagnosis.dashboard;

import lombok.Getter;

@Getter
public class TopicCorrectRate {
    private String topic_name;
    private int correct_answers;
    private int incorrect_answers;
}