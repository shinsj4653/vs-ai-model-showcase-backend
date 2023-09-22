package visang.showcase.aibackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import visang.showcase.aibackend.service.HomeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("home")
public class HomeController {

    private final HomeService questionBankService;

    @GetMapping("test")
    public ResponseEntity<String> getQuestionExample() {
        return ResponseEntity.ok(questionBankService.getQuestionExample());
    }
}
