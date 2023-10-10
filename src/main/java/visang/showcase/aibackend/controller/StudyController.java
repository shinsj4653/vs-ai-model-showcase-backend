package visang.showcase.aibackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import visang.showcase.aibackend.dto.request.study.StudyResultSaveRequest;
import visang.showcase.aibackend.dto.request.token.TokenRequest;
import visang.showcase.aibackend.dto.response.common.ResponseDto;
import visang.showcase.aibackend.dto.response.common.ResponseUtil;
import visang.showcase.aibackend.dto.response.study.RecommendProblemDto;
import visang.showcase.aibackend.dto.response.study.StudyReadyDto;
import visang.showcase.aibackend.dto.response.study.StudyReadyProbDto;
import visang.showcase.aibackend.service.StudyService;
import visang.showcase.aibackend.util.JwtTokenProvider;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("study")
public class StudyController {

    private final StudyService studyService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/isReady")
    public ResponseDto<StudyReadyDto> isStudyReady(@RequestBody TokenRequest request) {
        String token = request.getTransaction_token();
        return ResponseUtil.SUCCESS("학습 준비 완료 여부 반환 성공", studyService.isStudyReady(token));
    }

    @PostMapping("/recommend")
    public ResponseDto<List<RecommendProblemDto>> recommendProb(@RequestBody TokenRequest request) {
        String token = request.getTransaction_token();
        String memberNo = jwtTokenProvider.getMemberNo(token);
        return ResponseUtil.SUCCESS("학습준비에 필요한 문항 조회 성공", studyService.getStudyReadyProblems(memberNo, token));
    }

    @PostMapping("/setResult")
    public ResponseDto<List<StudyReadyProbDto>> setStudyReadyProblems(@RequestBody StudyResultSaveRequest request) {

        String token = request.getTransaction_token();
        String memberNo = jwtTokenProvider.getMemberNo(token);
        if (memberNo != null) {
            return ResponseUtil.SUCCESS("학습준비 문제 풀이 시퀀스 세션에 저장 성공", studyService.setStudyReadyProblems(request, token));
        }
        return ResponseUtil.ERROR("세션에 memberNo가 없습니다.", null);
    }

    //    @GetMapping("/recommend/prob/{prob_no}")
//    public RecommendProblemDto getRecommendProb(@PathVariable String prob_no) {
//        return studyMapper.getRecommendProblemWithProbNo(prob_no);
//    }

    /**
     * 옵션값을 설정하여 트리톤 서버에 전송할 RequestBody 생성
     */
//    private KnowledgeLevelRequest createTritonRequestWithOption(String memberNo, DashboardRequest request) {
//        List<DiagnosisProblemDto> preList = diagnosisMapper.getProblems(memberNo).subList(0, 85);
//
//        // 앞의 85문제 + 학생 진단 후의 15문제 => 총 100 문제
//        List<DiagnosisProblemDto> mergedList = Stream.of(preList, request.getProb_list())
//                .flatMap(Collection::stream)
//                .collect(Collectors.toList());
//
//        List<Integer> q_idx_list = mergedList.stream()
//                .map(m -> m.getQ_idx()).collect(Collectors.toList());  // 토픽 리스트
//
//        List<Integer> correct_list = mergedList.stream()
//                .map(m -> m.getCorrect()).collect(Collectors.toList()); // 정오답 리스트
//
//        List<Integer> diff_level_list = mergedList.stream()
//                .map(m -> m.getDiff_level()).collect(Collectors.toList()); // 문제 난이도 리스트
//
//        // INPUT__ 객체 생성
//        List<KnowledgeReqObject> inputs = new ArrayList<>();
//        inputs.add(createRequestObj(0, q_idx_list));
//        inputs.add(createRequestObj(1, correct_list));
//        inputs.add(createRequestObj(2, diff_level_list));
//        // 옵션 값 추가
//        inputs.add(createRequestObj(3, List.of(279, 5, 1, 5, 5)));
//
//        return new KnowledgeLevelRequest(inputs);
//    }

}