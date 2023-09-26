package visang.showcase.aibackend.dto.response.diagnosis.dashboard;

import lombok.Getter;

@Getter
public class WholeCorrectRate {
    private int total_questions;
    private int correct_answers;
    private int incorrect_answers;

    public WholeCorrectRate(int total_questions, int correct_answers, int incorrect_answers) {
        this.total_questions = total_questions;
        this.correct_answers = correct_answers;
        this.incorrect_answers = incorrect_answers;
    }
}