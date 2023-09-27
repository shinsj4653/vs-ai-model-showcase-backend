package visang.showcase.aibackend.dto.response.diagnosis;

import lombok.Getter;

@Getter
public class DiagnosisResultQueryDto {
    private Integer prob_solve_idx; // 문제 풀이 문항 인덱스
    private String subsection_nm; // 소단원 한글명
    private String topic_nm; // 토픽 한글명
}
