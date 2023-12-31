package visang.showcase.aibackend.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TopicKnowledge implements Comparable<TopicKnowledge> {

    private Integer qIdx;
    private String topicNm;
    private Double knowledgeRate;

    public TopicKnowledge(Integer qIdx, String topicNm, Double knowledgeRate) {
        this.qIdx = qIdx;
        this.topicNm = topicNm;
        this.knowledgeRate = knowledgeRate;
    }

    public Double getKnowledgeRate() {
        return knowledgeRate;
    }

    @Override
    public int compareTo(TopicKnowledge other) {
        return this.knowledgeRate.compareTo(other.knowledgeRate);
    }
}