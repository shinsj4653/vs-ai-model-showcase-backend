package visang.showcase.aibackend.service;

import lombok.Getter;

@Getter
class CorrectCounter {
    int correctCount;
    int wrongCount;

    CorrectCounter() {
        correctCount = 0;
        wrongCount = 0;
    }

    void correctCountUp() {
        this.correctCount++;
    }

    void wrongCountUp() {
        this.wrongCount++;
    }
}