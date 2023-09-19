package visang.showcase.aibackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import visang.showcase.aibackend.service.QuestionBankService;

@RestController
@RequiredArgsConstructor
@RequestMapping("questionbank")
public class QuestionBankController {

    private final QuestionBankService questionBankService;

    @GetMapping("example")
    public ResponseEntity<String> getQuestionExample() {
        return ResponseEntity.ok(questionBankService.getQuestionExample());
    }
}
