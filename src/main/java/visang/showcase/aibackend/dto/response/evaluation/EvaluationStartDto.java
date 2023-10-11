package visang.showcase.aibackend.dto.response.evaluation;

import lombok.*;

import java.util.List;

@Data
public class EvaluationStartDto {
    private String transaction_token;
    private Double knowledgeRate;
    private List<EvaluationProblemDto> prob_list;
}