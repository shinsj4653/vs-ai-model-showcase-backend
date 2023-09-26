package visang.showcase.aibackend.dto.response.diagnosis.dashboard;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DiffLevelCorrectRate {
    private Integer level;
    private Integer correct_answers;
    private Integer incorrect_answers;

    public DiffLevelCorrectRate(int level, int correct_answers, int incorrect_answers) {
        this.level = level;
        this.correct_answers = correct_answers;
        this.incorrect_answers = incorrect_answers;
    }
}