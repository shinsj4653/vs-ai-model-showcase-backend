package visang.showcase.aibackend.dto.response.diagnosis;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DashboardDto {

    /* 전체 정답률 */
    private Integer total_questions; // 총 문제 수
    private Integer correct_answers; // 정답 수
    private Integer incorrect_answers; // 오답 수

    /* 난이도별 정답률 */
    private List<Object> difficulty_levels;

    /* 토픽별 정답률 */
    private List<Object> topic_answer_result;

    /* 영역별 지식 수준 */
    private List<Object> section_level;

    /* 강/약 지식요인 */
    private List<Object> strong_level;
    private List<Object> weak_level;

    /* 앞으로 배울 토픽 예상 지식 수준 */
    private List<Object> future_topic_level_expectation;

}
