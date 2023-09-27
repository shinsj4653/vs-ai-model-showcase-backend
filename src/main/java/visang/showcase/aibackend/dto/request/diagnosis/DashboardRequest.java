package visang.showcase.aibackend.dto.request.diagnosis;

import lombok.Getter;
import visang.showcase.aibackend.dto.response.diagnosis.DiagnosisProblemDto;

import java.util.List;

@Getter
public class DashboardRequest {
    private List<DiagnosisProblemDto> prob_list;
}