package visang.showcase.aibackend.vo;

import lombok.Getter;

@Getter
public class CorrectCounter {
    Integer correctCount;
    Integer wrongCount;

    public CorrectCounter() {
        correctCount = 0;
        wrongCount = 0;
    }

    public void correctCountUp() {
        this.correctCount++;
    }

    public void wrongCountUp() {
        this.wrongCount++;
    }
}