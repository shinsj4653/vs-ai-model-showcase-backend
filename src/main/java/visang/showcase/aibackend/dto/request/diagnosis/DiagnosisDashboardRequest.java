package visang.showcase.aibackend.dto.request.diagnosis;

import lombok.Getter;
import lombok.NoArgsConstructor;
import visang.showcase.aibackend.dto.response.diagnosis.DiagnosisProblemDto;

import java.util.List;

@Getter
@NoArgsConstructor
public class DiagnosisDashboardRequest {
    private String transaction_token;
    private List<DiagnosisProblemDto> prob_list;
}