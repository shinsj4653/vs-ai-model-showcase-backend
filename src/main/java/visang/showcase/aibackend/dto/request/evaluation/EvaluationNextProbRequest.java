package visang.showcase.aibackend.dto.request.evaluation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EvaluationNextProbRequest {
    private String transaction_token; // 트랜잭션 토큰
    private Integer q_idx; // 토픽 인덱스
    private Integer diff_level; // 난이도
    private Integer correct; // 정오답 여부 -> 디폴트 값 : 0
    private String prob_no; // 문항 번호
}