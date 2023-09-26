package visang.showcase.aibackend.dto.request.diagnosis;

import lombok.Getter;
import visang.showcase.aibackend.dto.response.diagnosis.DiagnosisProblemDto;

import java.util.List;

@Getter
public class DiagnosisResultRequest {
    private List<DiagnosisProblemDto> prob_list;

//    private class DiagnosisProblem {
//        private Integer prob_solve_idx; // 문제풀이 응답 번호
//        private Integer diff_level; // 문제 난이도
//        private Integer correct; // 문항 정오답 여부
//    }

}