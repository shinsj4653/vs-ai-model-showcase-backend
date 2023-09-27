package visang.showcase.aibackend.dto.response.diagnosis;

import lombok.Data;
import visang.showcase.aibackend.dto.response.diagnosis.dashboard.DiffLevelCorrectRate;
import visang.showcase.aibackend.dto.response.diagnosis.dashboard.TopicCorrectRate;
import visang.showcase.aibackend.dto.response.triton.AreaKnowledgeResponse;
import visang.showcase.aibackend.vo.TopicKnowledge;

import java.util.List;

@Data
public class DashboardDto {

    /* 전체 정답률 */
    private Integer total_questions; // 총 문제 수
    private Integer correct_answers; // 정답 수
    private Integer incorrect_answers; // 오답 수

    /* 난이도별 정답률 */
    private List<DiffLevelCorrectRate> difficulty_levels;

    /* 토픽별 정답률 */
    private List<TopicCorrectRate> topic_answer_result;

    /* 영역별 지식 수준 */
    private List<AreaKnowledgeResponse> section_level;

    /* 강/약 지식요인 */
    private List<TopicKnowledge> strong_level;
    private List<TopicKnowledge> weak_level;

    /* 앞으로 배울 토픽 예상 지식 수준 */
    private List<Object> future_topic_level_expectation;

}