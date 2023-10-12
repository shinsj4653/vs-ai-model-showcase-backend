package visang.showcase.aibackend.controller;

import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.*;
import visang.showcase.aibackend.dto.request.evaluation.EvaluationDashboardRequest;
import visang.showcase.aibackend.dto.request.evaluation.EvaluationNextProbRequest;
import visang.showcase.aibackend.dto.request.token.TokenRequest;
import visang.showcase.aibackend.dto.response.common.ResponseDto;
import visang.showcase.aibackend.dto.response.common.ResponseUtil;
import visang.showcase.aibackend.dto.response.evaluation.EvaluationContinueDto;
import visang.showcase.aibackend.dto.response.evaluation.EvaluationKnowledgeRateDto;
import visang.showcase.aibackend.dto.response.evaluation.EvaluationProblemDto;
import visang.showcase.aibackend.dto.response.evaluation.EvaluationStartDto;
import visang.showcase.aibackend.dto.response.evaluation.dashboard.EvaluationDashboardResult;
import visang.showcase.aibackend.service.EvaluationService;
import visang.showcase.aibackend.util.JwtTokenProvider;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("evaluation")
public class EvaluationController {

    private final EvaluationService evaluationService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/getProblems")
    public ResponseDto<List<EvaluationStartDto>> getProblems(@RequestBody TokenRequest tokenRequest) {
        String token = tokenRequest.getTransaction_token();
        String memberNo = jwtTokenProvider.getMemberNo(token);
        return ResponseUtil.SUCCESS("형성평가 문항 5개 조회 성공", evaluationService.getProblems(memberNo, token));
    }

    @PostMapping("/dashboard")
    public ResponseDto<EvaluationDashboardResult> getEvaluationDashboardResults(@RequestBody EvaluationDashboardRequest request) throws IOException, ParseException {

        String token = request.getTransaction_token();
        String memberNo = jwtTokenProvider.getMemberNo(token);

        EvaluationDashboardResult response = evaluationService.getDashboardResult(memberNo, request, token);
        return ResponseUtil.SUCCESS("형성평가 결과 대시보드 데이터 조회 성공", response);
    }

    @PostMapping("/getNextProblem")
    public ResponseDto<EvaluationContinueDto> getNextKnowledgeRage(@RequestBody EvaluationNextProbRequest request) {

        String token = request.getTransaction_token();
        String memberNo = jwtTokenProvider.getMemberNo(token);

        if (memberNo != null) {
            EvaluationContinueDto nextProblem = evaluationService.getNextProblem(token, memberNo, request);
            return ResponseUtil.SUCCESS("형성평가 문항 결과에 따른 새로운 문항 추천 성공",nextProblem);
        } else{
            return ResponseUtil.FAILURE("memberNo가 토큰에 존재하지 않습니다.", null);
        }
    }
}