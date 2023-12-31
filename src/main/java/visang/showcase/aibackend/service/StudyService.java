package visang.showcase.aibackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import visang.showcase.aibackend.dto.request.study.StudyResultSaveRequest;
import visang.showcase.aibackend.dto.request.triton.KnowledgeLevelRequest;
import visang.showcase.aibackend.dto.request.triton.KnowledgeReqObject;
import visang.showcase.aibackend.dto.response.diagnosis.DiagnosisProblemDto;
import visang.showcase.aibackend.dto.response.study.RecommendProblemDto;
import visang.showcase.aibackend.dto.response.study.StudyReadyDto;
import visang.showcase.aibackend.dto.response.study.StudyReadyProbDto;
import visang.showcase.aibackend.dto.response.triton.RecommendProbResponse;
import visang.showcase.aibackend.mapper.DiagnosisMapper;
import visang.showcase.aibackend.mapper.StudyMapper;
import visang.showcase.aibackend.mapper.TransactionMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudyService {
    // 트리톤 서버 URL
    private static final String TRITON_SERVER_URL = "http://106.241.14.35:8000";
    // 추천 기능 URI
    private static final String RECOMMEND_URI = "/v2/models/gkt_reco/infer";

    private final DiagnosisMapper diagnosisMapper;
    private final StudyMapper studyMapper;
    private final TransactionMapper transactionMapper;
    private final ObjectMapper objectMapper;

    // 학습준비 이행 가능 여부 판단 기준이 되는 지식 수준
    public static final double THRESHOLD = 4.0;

    public StudyReadyDto isStudyReady(String token) {
        // 타켓토픽의 지식 수준이 기준을 넘으면 학습준비를 할 필요가 없다
        Double tgtTopicKnowledgeRate = transactionMapper.getTgtTopicKnowledgeRate(token);

        if (tgtTopicKnowledgeRate >= THRESHOLD)
            return new StudyReadyDto(false);
        else // 기준을 못 넘겼을 시에는 학습 준비를 해야 한다
            return new StudyReadyDto(true);
    }

    public List<RecommendProblemDto> getStudyReadyProblems(String memberNo, String token) {

        // transaction_data에 저장된 100개의 진단평가 문항 가져오기
        String diagnosisData = transactionMapper.getDiagnosisData(token);
        List<DiagnosisProblemDto> diagnosisResult;

        try{
            diagnosisResult = List.of(objectMapper.readValue(diagnosisData, DiagnosisProblemDto[].class));
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        KnowledgeLevelRequest recommendProbRequest = createTritonRequest(memberNo, diagnosisResult);
        RecommendProbResponse recommendProbResponse = postWithRecommendTriton(recommendProbRequest);

        // 학습준비에 필요한 토픽 배열의 맨 첫 토픽Idx 가져오기
        List<Object> qIdxList = recommendProbResponse.getOutputs()
                .get(1)
                .getData();

        List<RecommendProblemDto> result = new ArrayList<>();

        for (Object qIdx : qIdxList) {
            Integer idx = (Integer) qIdx;
            result.add(studyMapper.getRecommendProblemWithQIdx(idx).get(0));
        }

        // 토픽 Idx에 해당하는 문항 5개 가져오기
        return result;
    }

    public List<StudyReadyProbDto> setStudyReadyProblems(StudyResultSaveRequest request, String transaction_token) {

        // 학습준비 문제 풀이 시퀀스
        List<StudyReadyProbDto> probList = request.getProb_list();

        // 학습준비 데이터 DB에 저장
        try {
            String study_data = objectMapper.writeValueAsString(probList);
            transactionMapper.updateStudyData(transaction_token, study_data);

            return probList;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 트리톤 서버에 전송할 RequestBody 생성
     */
    private KnowledgeLevelRequest createTritonRequest(String memberNo, List<DiagnosisProblemDto> probList) {

        // memberNo에 해당하는 tgt_topic 값 가져오기
        Integer tgtTopic = diagnosisMapper.getTgtTopic(memberNo);


        // 세션에 저장된 진단평가 100문항에서 필요한 정보 사용
        List<Long> q_idx_list = probList.stream()
                .map(m -> Long.valueOf(m.getQ_idx())).collect(Collectors.toList());  // 토픽 리스트

        List<Long> correct_list = probList.stream()
                .map(m -> Long.valueOf(m.getCorrect())).collect(Collectors.toList()); // 정오답 리스트

        List<Long> diff_level_list = probList.stream()
                .map(m -> Long.valueOf(m.getDiff_level())).collect(Collectors.toList()); // 문제 난이도 리스트

        // INPUT__ 객체 생성
        List<KnowledgeReqObject> inputs = new ArrayList<>();
        inputs.add(createRequestObj(0, q_idx_list));
        inputs.add(createRequestObj(1, correct_list));
        inputs.add(createRequestObj(2, diff_level_list));

        // 옵션 값 추가
        inputs.add(createRequestObj(3, List.of(Long.valueOf(tgtTopic), 5L, 1L, 5L, 5L)));

        return new KnowledgeLevelRequest(inputs);
    }

    /**
     * RequestBody의 INPUT__ 요청 객체 생성
     */
    private KnowledgeReqObject createRequestObj(int idx, List<Long> payload) {
        KnowledgeReqObject obj = new KnowledgeReqObject();
        obj.setName("INPUT__" + idx);
        obj.setDatatype("INT64");

        List<Integer> shape = List.of(1, payload.size()); // 배치 사이즈, 문항 수
        obj.setShape(shape);

        List<List<Long>> data = List.of(payload);
        obj.setData(data);

        return obj;
    }

    private RecommendProbResponse postWithRecommendTriton(KnowledgeLevelRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        RecommendProbResponse response = restTemplate.postForObject(TRITON_SERVER_URL + RECOMMEND_URI, request, RecommendProbResponse.class);
        return response;
    }
}
