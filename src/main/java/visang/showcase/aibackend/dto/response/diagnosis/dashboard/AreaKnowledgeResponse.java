package visang.showcase.aibackend.dto.response.diagnosis.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class AreaKnowledgeResponse {
    private String category;
    private Double knowledgeLevel;
    private String testType;

    public AreaKnowledgeResponse(String category, Double knowledgeLevel, String testType) {
        this.category = category;
        this.knowledgeLevel = knowledgeLevel;
        this.testType = testType;
    }
}