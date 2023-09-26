package visang.showcase.aibackend.dto.response.diagnosis.dashboard;

import lombok.Getter;

@Getter
public class DiffLevelCorrectRate {
    private int level;
    private int correct_answers;
    private int incorrect_answers;

    public DiffLevelCorrectRate(int level, int correct_answers, int incorrect_answers) {
        this.level = level;
        this.correct_answers = correct_answers;
        this.incorrect_answers = incorrect_answers;
    }
}