package visang.showcase.aibackend.dto.response.diagnosis.dashboard;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ExpectedTopicResponse {
    private String topicName;
    private Double knowledgeRate;

    public ExpectedTopicResponse(String topicName, Double knowledgeRate) {
        this.topicName = topicName;
        this.knowledgeRate = knowledgeRate;
    }
}