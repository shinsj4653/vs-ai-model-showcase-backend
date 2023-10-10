package visang.showcase.aibackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import visang.showcase.aibackend.dto.request.evaluation.EvaluationDashboardRequest;
import visang.showcase.aibackend.dto.request.token.TokenRequest;
import visang.showcase.aibackend.dto.response.common.ResponseDto;
import visang.showcase.aibackend.dto.response.common.ResponseUtil;
import visang.showcase.aibackend.dto.response.evaluation.EvaluationProblemDto;
import visang.showcase.aibackend.dto.response.evaluation.dashboard.EvaluationDashboardResult;
import visang.showcase.aibackend.service.EvaluationService;
import visang.showcase.aibackend.util.JwtTokenProvider;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("evaluation")
public class EvaluationController {

    private final EvaluationService evaluationService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/getProblems")
    public ResponseDto<List<EvaluationProblemDto>> getProblems(@RequestBody TokenRequest tokenRequest) {
        String token = tokenRequest.getTransaction_token();
        String memberNo = jwtTokenProvider.getMemberNo(token);

        return ResponseUtil.SUCCESS("형성평가 문항 5개 조회 성공", evaluationService.getProblems(memberNo));
    }

    @PostMapping("/dashboard")
    public ResponseDto<EvaluationDashboardResult> getEvaluationDashboardResults(@RequestBody EvaluationDashboardRequest request){

        String token = request.getTransaction_token();
        String memberNo = jwtTokenProvider.getMemberNo(token);

        EvaluationDashboardResult response = evaluationService.getDashboardResult(memberNo, request, token);
        return ResponseUtil.SUCCESS("형성평가 결과 대시보드 데이터 조회 성공", response);
    }
}