package visang.showcase.aibackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import visang.showcase.aibackend.dto.request.evaluation.EvaluationDashboardRequest;
import visang.showcase.aibackend.dto.response.common.ResponseDto;
import visang.showcase.aibackend.dto.response.common.ResponseUtil;
import visang.showcase.aibackend.dto.response.evaluation.EvaluationProblemDto;
import visang.showcase.aibackend.dto.response.evaluation.dashboard.EvaluationDashboardResult;
import visang.showcase.aibackend.service.EvaluationService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("evaluation")
public class EvaluationController {

    private final EvaluationService evaluationService;

    @GetMapping("/getProblems")
    public ResponseDto<List<EvaluationProblemDto>> getProblems(HttpSession session) {
        String memberNo = (String) session.getAttribute("memberNo");
        return ResponseUtil.SUCCESS("형성평가 문항 5개 조회 성공", evaluationService.getProblems(memberNo));
    }

    @PostMapping("/dashboard")
    public ResponseDto<EvaluationDashboardResult> getEvaluationDashboardResults(@RequestBody EvaluationDashboardRequest request,
                                                                                HttpServletRequest httpServletRequest){
        HttpSession session = httpServletRequest.getSession();
        String memberNo = (String) session.getAttribute("memberNo");

        EvaluationDashboardResult response = evaluationService.getDashboardResult(memberNo, request, httpServletRequest);
        return ResponseUtil.SUCCESS("형성평가 결과 대시보드 데이터 조회 성공", response);
    }
}