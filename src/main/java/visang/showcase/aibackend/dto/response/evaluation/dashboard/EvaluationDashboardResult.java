package visang.showcase.aibackend.dto.response.evaluation.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EvaluationDashboardResult {
    private CheckProbDto mistake_prob;
    private CheckProbDto check_prob;
    private TopicLevelChangeDto topic_level_change;
}