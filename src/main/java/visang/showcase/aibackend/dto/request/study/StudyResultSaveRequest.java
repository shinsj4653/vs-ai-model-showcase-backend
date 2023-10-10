package visang.showcase.aibackend.dto.request.study;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import visang.showcase.aibackend.dto.response.study.RecommendProblemDto;
import visang.showcase.aibackend.dto.response.study.StudyReadyProbDto;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyResultSaveRequest {
    private String transaction_token;
    private List<StudyReadyProbDto> prob_list;
}