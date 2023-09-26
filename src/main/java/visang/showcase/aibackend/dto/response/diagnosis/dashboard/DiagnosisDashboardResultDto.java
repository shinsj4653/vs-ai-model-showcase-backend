package visang.showcase.aibackend.dto.response.diagnosis.dashboard;

import lombok.Getter;

@Getter
public class DiagnosisDashboardResultDto {
    private int total_questions;
    private int correct_answers;
    private int incorrect_answers;
    private DifficultyLevelsDto difficulty_levels;
    private TopicAnswerResultDto topic_answer_result;

    public DiagnosisDashboardResultDto(WholeCorrectRate wholeCorrectRate, DifficultyLevelsDto difficulty_levels,
                                       TopicAnswerResultDto topic_answer_result) {
        this.total_questions = wholeCorrectRate.getTotal_questions();
        this.correct_answers = wholeCorrectRate.getCorrect_answers();
        this.incorrect_answers = wholeCorrectRate.getIncorrect_answers();
        this.difficulty_levels = difficulty_levels;
        this.topic_answer_result = topic_answer_result;
    }
}