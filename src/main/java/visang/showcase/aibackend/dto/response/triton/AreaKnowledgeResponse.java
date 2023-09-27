package visang.showcase.aibackend.dto.response.triton;

import lombok.Getter;

@Getter
public class AreaKnowledgeResponse {
    private String category;
    private String knowledgeLevel;
    private String testType;

    public AreaKnowledgeResponse(String category, String knowledgeLevel, String testType) {
        this.category = category;
        this.knowledgeLevel = knowledgeLevel;
        this.testType = testType;
    }
}