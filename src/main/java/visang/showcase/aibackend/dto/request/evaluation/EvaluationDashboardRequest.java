package visang.showcase.aibackend.dto.request.evaluation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import visang.showcase.aibackend.dto.response.evaluation.EvaluationProblemDto;

import java.util.List;

@Getter
@NoArgsConstructor
public class EvaluationDashboardRequest {
    private List<EvaluationProbRequest> prob_list;
}