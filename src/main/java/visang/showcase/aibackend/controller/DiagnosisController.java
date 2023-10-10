package visang.showcase.aibackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import visang.showcase.aibackend.dto.request.diagnosis.DiagnosisDashboardRequest;
import visang.showcase.aibackend.dto.request.token.TokenRequest;
import visang.showcase.aibackend.dto.response.common.ResponseDto;
import visang.showcase.aibackend.dto.response.common.ResponseUtil;
import visang.showcase.aibackend.dto.response.diagnosis.DiagnosisProblemDto;
import visang.showcase.aibackend.service.DiagnosisService;
import visang.showcase.aibackend.util.JwtTokenProvider;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("diagnosis")
public class DiagnosisController {



    private final DiagnosisService diagnosisService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/getProblems")
    public ResponseDto<List<DiagnosisProblemDto>> getProblems(@RequestBody TokenRequest request) {

        // 토큰에 저장된 memberNo 값 추출
        String token = request.getTransaction_token();
        String memberNo = jwtTokenProvider.getMemberNo(token);

        // memberNo 값이 세션에 존재할 경우에만 서비스단 로직 수행
        if (memberNo != null) {
            return ResponseUtil.SUCCESS("진단평가 필요 문항 조회 성공", diagnosisService.getProblems(memberNo));

        } else {
            return ResponseUtil.ERROR("세션에 memberNo가 없습니다.", null);
        }
    }

    @PostMapping("/dashboard")
    public ResponseDto<?> getDashboardResult(@RequestBody DiagnosisDashboardRequest request) {

        // 토큰에 저장된 memberNo 값 추출
        String token = request.getTransaction_token();
        String memberNo = jwtTokenProvider.getMemberNo(token);

        // memberNo 값이 토큰에 존재할 경우에만 서비스단 로직 수행
        if (memberNo != null) {
            return ResponseUtil.SUCCESS("진단평가의 대시보드 결과 조회 성공", diagnosisService.getDashBoardResult(memberNo, request, token));
        } else {
            return ResponseUtil.ERROR("세션에 memberNo가 없습니다.", null);
        }
    }
}