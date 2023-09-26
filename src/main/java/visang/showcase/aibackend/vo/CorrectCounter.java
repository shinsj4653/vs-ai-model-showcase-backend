package visang.showcase.aibackend.vo;

import lombok.Getter;

@Getter
public class CorrectCounter {
    private Integer correctCount;
    private Integer wrongCount;

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