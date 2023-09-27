package visang.showcase.aibackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import visang.showcase.aibackend.dto.request.diagnosis.DashboardRequest;
import visang.showcase.aibackend.dto.request.triton.KnowledgeLevelRequest;
import visang.showcase.aibackend.dto.request.triton.KnowledgeReqObject;
import visang.showcase.aibackend.dto.response.diagnosis.DashboardDto;
import visang.showcase.aibackend.dto.response.diagnosis.DiagnosisProblemDto;
import visang.showcase.aibackend.dto.response.triton.KnowledgeLevelResponse;
import visang.showcase.aibackend.dto.response.diagnosis.DiagnosisResultDto;
import visang.showcase.aibackend.dto.response.diagnosis.DiagnosisResultQueryDto;
import visang.showcase.aibackend.dto.response.diagnosis.dashboard.DiffLevelCorrectRate;
import visang.showcase.aibackend.dto.response.diagnosis.dashboard.TopicCorrectRate;
import visang.showcase.aibackend.dto.response.diagnosis.dashboard.WholeCorrectRate;
import visang.showcase.aibackend.dto.response.triton.KnowledgeResObject;
import visang.showcase.aibackend.mapper.DiagnosisMapper;
import visang.showcase.aibackend.vo.CorrectCounter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiagnosisService {
    private static final String CORRECT_ANSWER_KEY = "yes";
    private static final String WRONG_ANSWER_KEY = "no";

    private final DiagnosisMapper diagnosisMapper;

    public List<DiagnosisProblemDto> getProblems(String memberNo) {

        // 끝부분 15개의 문항만 클라이언트로 반환
        return diagnosisMapper.getProblems(memberNo).subList(85 , 100);
    }

    public List<Integer> getDashBoardResult(String memberNo, DashboardRequest request) {

        DashboardDto result = new DashboardDto();

        // 트리톤 서버 요청에 필요한 값 -> q_idx, correct, diff_level
        KnowledgeLevelRequest tritonRequest = new KnowledgeLevelRequest();

        List<DiagnosisProblemDto> preList = diagnosisMapper.getProblems(memberNo).subList(0, 85);
        
        // 앞의 85문제 + 학생 진단 후의 15문제 => 총 100 문제
        List<DiagnosisProblemDto> mergedList = Stream.of(preList, request.getProb_list())
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList());

        List<Integer> q_idx_list = mergedList.stream()
                .map(m -> m.getQ_idx()).collect(Collectors.toList());  // 토픽 리스트

        List<Integer> correct_list = mergedList.stream()
                .map(m -> m.getCorrect()).collect(Collectors.toList()); // 정오답 리스트

        List<Integer> diff_level_list = mergedList.stream()
                .map(m -> m.getDiff_level()).collect(Collectors.toList()); // 문제 난이도 리스트

        List<KnowledgeReqObject> inputs = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            KnowledgeReqObject obj = new KnowledgeReqObject();
            obj.setName("INPUT__" + i);
            obj.setDatatype("INT64");
            List<Integer> shape = new ArrayList<>();
            shape.add(1); // 배치 사이즈
            shape.add(100); // 문항 수
            obj.setShape(shape);

            List<List<Integer>> data = new ArrayList<>();

            if (i == 0)
                data.add(q_idx_list);


            if (i == 1)
                data.add(correct_list);


            if (i == 2)
                data.add(diff_level_list);

            obj.setData(data);
            inputs.add(obj);
        }

        tritonRequest.setInputs(inputs);

        // RestTemplate을 사용하여 트리톤 지식상태 추론 서버의 응답 값 반환
        KnowledgeLevelResponse response = postWithKnowledgeLevelTriton(tritonRequest);

        // 지식수준 추론 결과
        List<Double> knowledgeRates = response.getOutputs().get(0).getData();

        // 영역 셋 (중복제거 o)
        Set<String> categories = mergedList.stream()
                .map(m -> m.getCateg_nm()).collect(Collectors.toSet());// 카테고리 명 셋
        
        // TODO
        // DiagnosisMapper의 getQIdxWithCategory를 통해 영역 셋을 순환하면서 해당하는 q_idx 목록 조회
        // 조회 후, 지식수준 추론 결과에서 각 카테고리에 해당하는 q_idx들을 활용하여 지식수준 값을 가져와서 평균값 계산
        // 해당 평균값들을 영역별로 구분하여 반환

        // 영역 셋 순환
        diagnosisMapper.getQIdxWithCategory(카테고리 이름);
    }


    public KnowledgeLevelResponse postWithKnowledgeLevelTriton(KnowledgeLevelRequest request) {

        RestTemplate restTemplate = new RestTemplate();
        KnowledgeLevelResponse responseEntity = restTemplate.postForObject("http://10.214.2.33:8000" + "/v2/models/gkt/infer", request, KnowledgeLevelResponse.class);

        return responseEntity;
    }

    /**
     * 전체 정답률 계산
     *
     * @param resultRequest 진단평가 결과 데이터
     * @return WholeCorrectRate 반환
     */
//    public WholeCorrectRate calculateWholeCorrectRate(DiagnosisResultRequest resultRequest) {
//        Map<String, Integer> correctRecords = new HashMap<>();
//
//        for (DiagnosisProblemDto prob : resultRequest.getProb_list()) {
//            int correct = prob.getCorrect();
//            if (correct == 0) { // 오답 count
//                correctRecords.put(WRONG_ANSWER_KEY, correctRecords.getOrDefault(WRONG_ANSWER_KEY, 0) + 1);
//            } else { // 정답 count
//                correctRecords.put(CORRECT_ANSWER_KEY, correctRecords.getOrDefault(CORRECT_ANSWER_KEY, 0) + 1);
//            }
//        }
//
//        int total = resultRequest.getProb_list().size();
//        int correct = correctRecords.getOrDefault(CORRECT_ANSWER_KEY, 0);
//        int wrong = correctRecords.getOrDefault(WRONG_ANSWER_KEY, 0);
//
//        return new WholeCorrectRate(total, correct, wrong);
//    }
//
//    /**
//     * 난이도별 정답률 계산
//     *
//     * @param resultRequest 진단평가 결과 데이터
//     * @return List<DiffLevelCorrectRate> 반환
//     */
//    public List<DiffLevelCorrectRate> calculateDiffLevelCorrectRates(DiagnosisResultRequest resultRequest) {
//        Map<Integer, CorrectCounter> diffLevelRecords = new HashMap<>();
//
//        for (DiagnosisProblemDto prob : resultRequest.getProb_list()) {
//            int diff_level = prob.getDiff_level();
//            int correct = prob.getCorrect();
//
//            diffLevelRecords.putIfAbsent(diff_level, new CorrectCounter());
//
//            if (correct == 0) { // 오답 count
//                diffLevelRecords.get(diff_level).wrongCountUp();
//            } else { // 정답 count
//                diffLevelRecords.get(diff_level).correctCountUp();
//            }
//        }
//
//        return diffLevelRecords.entrySet()
//                .stream()
//                .map(entry -> {
//                    int diff_level = entry.getKey();
//                    CorrectCounter counter = entry.getValue();
//                    return new DiffLevelCorrectRate(diff_level, counter.getCorrectCount(), counter.getWrongCount());
//                })
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * 토픽별 정답률 계산
//     *
//     * @param resultRequest 진단평가 결과 데이터
//     * @return List<TopicCorrectRate> 반환
//     */
//    public List<TopicCorrectRate> calculateTopicCorrectRates(DiagnosisResultRequest resultRequest) {
//        Map<Integer, CorrectCounter> topicRecords = new HashMap<>();
//        Map<Integer, String> topicNames = new HashMap<>();
//
//        for (DiagnosisProblemDto prob : resultRequest.getProb_list()) {
//            int q_idx = prob.getQ_idx();
//            int correct = prob.getCorrect();
//            String topic_nm = prob.getTopic_nm();
//
//            // q_idx와 topic_nm 매핑
//            topicNames.putIfAbsent(q_idx, topic_nm);
//            topicRecords.putIfAbsent(q_idx, new CorrectCounter());
//
//            if (correct == 0) { // 오답 count
//                topicRecords.get(q_idx).wrongCountUp();
//            } else { // 정답 count
//                topicRecords.get(q_idx).correctCountUp();
//            }
//        }
//
//        return topicRecords.entrySet()
//                .stream()
//                .map(entry -> {
//                    int q_idx = entry.getKey();
//                    CorrectCounter counter = entry.getValue();
//                    return new TopicCorrectRate(topicNames.get(q_idx), counter.getCorrectCount(), counter.getWrongCount());
//                })
//                .collect(Collectors.toList());
//    }
}