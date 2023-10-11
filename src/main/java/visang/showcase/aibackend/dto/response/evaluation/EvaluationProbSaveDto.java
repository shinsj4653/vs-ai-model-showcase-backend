package visang.showcase.aibackend.dto.response.evaluation;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EvaluationProbSaveDto {
    private Integer q_idx; // 토픽 인덱스
    private Integer diff_level; // 난이도
    private Integer correct; // 정오답 여부 -> 디폴트 값 : 0
}