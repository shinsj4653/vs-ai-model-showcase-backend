package visang.showcase.aibackend.dto.response.diagnosis.dashboard;

import lombok.Getter;

import java.util.List;

@Getter
public class TopicAnswerResultDto {
    private List<TopicCorrectRate> topic_answer_result;

    public TopicAnswerResultDto(List<TopicCorrectRate> topic_answer_result) {
        this.topic_answer_result = topic_answer_result;
    }
}