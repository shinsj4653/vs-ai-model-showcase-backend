package visang.showcase.aibackend.dto.request.study;

import lombok.Getter;
import visang.showcase.aibackend.dto.response.study.RecommendProblemDto;

import java.util.List;

@Getter
public class RecommendProbRequest {
    private List<RecommendProblemDto> prob_list;
}
