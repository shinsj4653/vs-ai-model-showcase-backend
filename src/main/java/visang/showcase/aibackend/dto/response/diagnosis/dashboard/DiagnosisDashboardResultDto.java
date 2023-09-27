package visang.showcase.aibackend.dto.response.diagnosis.dashboard;

import lombok.Getter;

import java.util.List;

// 최종결과는 Map<String, Object> 형태로 반환할 예정
// (deprecated)
@Getter
public class DiagnosisDashboardResultDto {
    private Integer total_questions;
    private Integer correct_answers;
    private Integer incorrect_answers;
    private List<DiffLevelCorrectRate> difficulty_levels;
    private List<TopicCorrectRate> topic_answer_result;

    public DiagnosisDashboardResultDto(WholeCorrectRate wholeCorrectRate, List<DiffLevelCorrectRate> difficulty_levels,
                                       List<TopicCorrectRate> topic_answer_result) {
        this.total_questions = wholeCorrectRate.getTotal_questions();
        this.correct_answers = wholeCorrectRate.getCorrect_answers();
        this.incorrect_answers = wholeCorrectRate.getIncorrect_answers();
        this.difficulty_levels = difficulty_levels;
        this.topic_answer_result = topic_answer_result;
    }
}