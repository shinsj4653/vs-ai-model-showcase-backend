package visang.showcase.aibackend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import visang.showcase.aibackend.dto.response.common.ResponseDto;
import visang.showcase.aibackend.dto.response.diagnosis.dashboard.DiagnosisDashboardResultDto;

@RestController
public class DiagnosisDashboardController {
    @GetMapping("/diagnosis/dashboard")
    public ResponseDto<DiagnosisDashboardResultDto> getDiagnosisDashboard() {


        return null;
    }
}