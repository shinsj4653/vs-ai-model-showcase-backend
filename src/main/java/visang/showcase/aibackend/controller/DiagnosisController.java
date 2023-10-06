package visang.showcase.aibackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import visang.showcase.aibackend.dto.request.diagnosis.DiagnosisDashboardRequest;
import visang.showcase.aibackend.dto.request.token.TokenRequest;
import visang.showcase.aibackend.dto.response.common.ResponseDto;
import visang.showcase.aibackend.dto.response.common.ResponseUtil;
import visang.showcase.aibackend.dto.response.diagnosis.DiagnosisProblemDto;
import visang.showcase.aibackend.service.DiagnosisService;
import visang.showcase.aibackend.util.JwtTokenProvider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("diagnosis")
public class DiagnosisController {



    private final DiagnosisService diagnosisService;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/getProblems")
    public ResponseDto<List<DiagnosisProblemDto>> getProblems(@RequestBody TokenRequest request) {

        // 토큰에 저장된 memberNo 값 추출
        String token = request.getTransaction_token();
        String memberNo = tokenProvider.getMemberNo(token);
        // return ResponseUtil.SUCCESS("타켓 학생의 문제 풀이 시퀀스, 응답 시퀀스 조회 성공 ", diagnosisService.jsonTest(token));
        // memberNo 값이 세션에 존재할 경우에만 서비스단 로직 수행
        if (memberNo != null) {
            return ResponseUtil.SUCCESS("진단평가 필요 문항 조회 성공", diagnosisService.getProblems(token));

        } else {
            return ResponseUtil.ERROR("세션에 memberNo가 없습니다.", null);
        }
    }

    @PostMapping("/dashboard")
    public ResponseDto<?> getDashboardResult(@RequestBody DiagnosisDashboardRequest request,  HttpServletRequest httpServletRequest) {

        // 토큰에 저장된 memberNo 값 추출
        String token = request.getTransaction_token();
        String memberNo = tokenProvider.getMemberNo(token);

        // memberNo 값이 세션에 존재할 경우에만 서비스단 로직 수행
        if (memberNo != null) {
            return ResponseUtil.SUCCESS("진단평가의 대시보드 결과 조회 성공", diagnosisService.getDashBoardResult(memberNo, request, httpServletRequest));
        } else {
            return ResponseUtil.ERROR("세션에 memberNo가 없습니다.", null);
        }
    }
}