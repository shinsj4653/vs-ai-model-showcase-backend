package visang.showcase.aibackend.dto.response.evaluation;

import lombok.Data;

import java.util.List;

@Data
public class EvaluationContinueDto {
    private final String transaction_token;
    private final Double knowledgeRate;
    private final EvaluationProblemDto prob_info;
}