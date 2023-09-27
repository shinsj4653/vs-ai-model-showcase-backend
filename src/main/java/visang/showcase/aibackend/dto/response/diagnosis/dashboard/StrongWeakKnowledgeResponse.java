package visang.showcase.aibackend.dto.response.diagnosis.dashboard;

import lombok.Getter;
import visang.showcase.aibackend.vo.TopicKnowledge;

import java.util.List;

@Getter
public class StrongWeakKnowledgeResponse {

    private List<TopicKnowledge> strong_level;
    private List<TopicKnowledge> weak_level;

    public StrongWeakKnowledgeResponse(List<TopicKnowledge> strongKnowledges, List<TopicKnowledge> weakKnowledges) {
        this.strong_level = strongKnowledges;
        this.weak_level = weakKnowledges;
    }
}