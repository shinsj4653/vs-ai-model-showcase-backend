package visang.showcase.aibackend.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import visang.showcase.aibackend.dto.request.evaluation.EvaluationDashboardRequest;
import visang.showcase.aibackend.dto.request.evaluation.EvaluationNextProbRequest;
import visang.showcase.aibackend.dto.request.evaluation.EvaluationProbRequest;
import visang.showcase.aibackend.dto.request.triton.KnowledgeLevelRequest;
import visang.showcase.aibackend.dto.request.triton.KnowledgeReqObject;
import visang.showcase.aibackend.dto.response.diagnosis.DiagnosisProblemDto;
import visang.showcase.aibackend.dto.response.evaluation.EvaluationContinueDto;
import visang.showcase.aibackend.dto.response.evaluation.EvaluationProbSaveDto;
import visang.showcase.aibackend.dto.response.evaluation.EvaluationProblemDto;
import visang.showcase.aibackend.dto.response.evaluation.EvaluationStartDto;
import visang.showcase.aibackend.dto.response.evaluation.dashboard.CheckProbDto;
import visang.showcase.aibackend.dto.response.evaluation.dashboard.EvaluationDashboardResult;
import visang.showcase.aibackend.dto.response.evaluation.dashboard.TopicLevelChangeDto;
import visang.showcase.aibackend.dto.response.study.StudyReadyProbDto;
import visang.showcase.aibackend.dto.response.triton.KnowledgeLevelResponse;
import visang.showcase.aibackend.dto.response.triton.KnowledgeResObject;
import visang.showcase.aibackend.mapper.DiagnosisMapper;
import visang.showcase.aibackend.mapper.EvaluationMapper;
import visang.showcase.aibackend.mapper.TransactionMapper;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLOutput;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class EvaluationService {
    // 트리톤 서버 URL
    private static final String TRITON_SERVER_URL = "http://106.241.14.35:8000";
    // 추론 기능 URI
    private static final String INFERENCE_URI = "/v2/models/gkt_last/infer";
    // 타겟토픽 지식수준의 기준값
    public static final double EVALUATION_THRESHHOLD = 4.0;

    private final EvaluationMapper evaluationMapper;
    private final DiagnosisMapper diagnosisMapper;
    private final TransactionMapper transactionMapper;
    private final ObjectMapper objectMapper;

    /**
     * 연관토픽 구하는 로직
     */
    private List<TopicValue> getRelatedKnowledgeRate(Integer tgtTopicIdx) throws IOException, ParseException {

        JSONParser parser = new JSONParser();

        System.out.println(System.getProperty("user.dir"));

        // json 파일 읽기
        Reader reader = new FileReader(System.getProperty("user.dir") + "/src/main/resources/json/topicmap_v0.json");
        JSONObject jsonObject = (JSONObject) parser.parse(reader);

        reader.close();
        
        // 연관 토픽 맵
        List<List<Double>> map = (List<List<Double>>) jsonObject.get("map");

        // 타켓 토픽에 해당되는 연관도 라인 가져와서 내림차순 정렬
        List<Double> tgtTopicLine = map.get(1);
        List<TopicValue> topicValues = new ArrayList<>();

        for (int i = 0; i < tgtTopicLine.size(); i++) {
            Double value = tgtTopicLine.get(i);
            topicValues.add(new TopicValue(i, value));
        }
        topicValues.sort(Comparator.comparingDouble(TopicValue::getValue).reversed());

        // 상위 5개만 필요
        return topicValues.subList(0, 6);
    }

    // index(토픽idx)와 연관 정도를 담을 객체
    public class TopicValue {
        private int index;
        private double value;

        public TopicValue(int index, double value) {
            this.index = index;
            this.value = value;
        }

        public int getIndex() {
            return index;
        }

        public double getValue() {
            return value;
        }
    }

    /**
     * 형성평가 문항 5개 조회
     * @param memberNo 학생번호
     */
    public List<EvaluationStartDto> getProblems(String memberNo, String token) {

        // 현재 지식수준 가져오기
        Double knowledgeRate = Double.valueOf(String.format("%.2f", transactionMapper.getTgtTopicKnowledgeRate(token)));

        Integer qIdx = diagnosisMapper.getTgtTopic(memberNo);
        return evaluationMapper.getProblems(qIdx).stream()
                .map(item -> {
                    return new EvaluationStartDto(item, token, knowledgeRate);
                })
                .collect(Collectors.toList());
    }

    /**
     * RequestBody의 INPUT__ 요청 객체 생성
     */
    private KnowledgeReqObject createRequestObj(int idx, int probSize, List<Long> payload) {
        KnowledgeReqObject obj = new KnowledgeReqObject();
        obj.setName("INPUT__" + idx);
        obj.setDatatype("INT64");

        List<Integer> shape = List.of(1, probSize); // 배치 사이즈, 문항 수
        obj.setShape(shape);

        List<List<Long>> data = List.of(payload);
        obj.setData(data);

        return obj;
    }

    /**
     * 트리톤 서버에 전송할 RequestBody 생성
     */
    private KnowledgeLevelRequest createTritonRequest(EvaluationDashboardRequest request, String token) {
        // 진단평가 문제 100개 + 학습준비 문제 5개 + 진단평가 문제 5개 누적

        String diagnosisData = transactionMapper.getDiagnosisData(token);
        String studyData = transactionMapper.getStudyData(token);

        List<DiagnosisProblemDto> diagnosisResult;
        List<StudyReadyProbDto> studyReadyResult = null;
        try{
            diagnosisResult = List.of(objectMapper.readValue(diagnosisData, DiagnosisProblemDto[].class));
            
            // 학습준비 데이터가 null이 아닐 경우에만 리스트로 변환해주기
            if (studyData != null)
                studyReadyResult = List.of(objectMapper.readValue(studyData, StudyReadyProbDto[].class));
            
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        List<EvaluationProbRequest> evaluationResult =  request.getProb_list();

        // 토픽리스트 추출
        List<Long> diagnosisQIdxs = diagnosisResult.stream()
                .map(m -> Long.valueOf(m.getQ_idx())).collect(Collectors.toList());
        List<Long> evaluationQIdxs = evaluationResult.stream()
                .map(m -> Long.valueOf(m.getQ_idx())).collect(Collectors.toList());

        // 정오답 리스트 추출
        List<Long> diagnosisCorrects = diagnosisResult.stream()
                .map(m -> Long.valueOf(m.getCorrect())).collect(Collectors.toList());
        List<Long> evaluationCorrects = evaluationResult.stream()
                .map(m -> Long.valueOf(m.getCorrect())).collect(Collectors.toList());

        // 문제 난이도 리스트
        List<Long> diagnosisDiffs = diagnosisResult.stream()
                .map(m -> Long.valueOf(m.getDiff_level())).collect(Collectors.toList());
        List<Long> evaluationDiffs = evaluationResult.stream()
                .map(m -> Long.valueOf(m.getDiff_level())).collect(Collectors.toList());

        // 먼저 진단평가 + 형성평가 데이터 합치기
        List<Long> mergedQIdxs = Stream.of(diagnosisQIdxs, evaluationQIdxs)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        List<Long> mergedCorrects = Stream.of(diagnosisCorrects, evaluationCorrects)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        List<Long> mergedDiffs = Stream.of(diagnosisDiffs, evaluationDiffs)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        // 학습준비를 하고 온 상태라면, 학습준비 데이터도 합쳐주기
        if (studyData != null) {
            List<Long> studyReadyQIdxs = studyReadyResult.stream()
                    .map(m -> Long.valueOf(m.getQ_idx())).collect(Collectors.toList());

            List<Long> studyReadyCorrects = studyReadyResult.stream()
                    .map(m -> Long.valueOf(m.getCorrect())).collect(Collectors.toList());

            List<Long> studyReadyDiffs = studyReadyResult.stream()
                    .map(m -> Long.valueOf(m.getDiff_level())).collect(Collectors.toList());

            mergedQIdxs = Stream.of(mergedQIdxs, studyReadyQIdxs)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            mergedCorrects = Stream.of(mergedCorrects, studyReadyCorrects)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            mergedDiffs = Stream.of(mergedDiffs, studyReadyDiffs)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }

        // INPUT__ 객체 생성
        List<KnowledgeReqObject> inputs = new ArrayList<>();
        inputs.add(createRequestObj(0, mergedQIdxs.size(), mergedQIdxs));
        inputs.add(createRequestObj(1, mergedCorrects.size(), mergedCorrects));
        inputs.add(createRequestObj(2, mergedDiffs.size(), mergedDiffs));

        return new KnowledgeLevelRequest(inputs);
    }

    /**
     * 트리톤 서버에 POST /v2/models/gkt_last/infer 요청
     *
     * @param request RequestBody 데이터
     * @return KnowledgeLevelResponse 반환
     */
    private KnowledgeLevelResponse postWithKnowledgeLevelTriton(KnowledgeLevelRequest request) {

        RestTemplate restTemplate = new RestTemplate();
        KnowledgeLevelResponse responseEntity = restTemplate.postForObject(TRITON_SERVER_URL + INFERENCE_URI, request, KnowledgeLevelResponse.class);
        return responseEntity;
    }

    /**
     * 형성평가 결과 대시보드 데이터 반환
     *
     * @param memberNo 학생번호
     * @param request RequestBody 데이터
     * @return EvaluationDashboardResult 반환
     */
    public EvaluationDashboardResult getDashboardResult(String memberNo, EvaluationDashboardRequest request, String token) throws IOException, ParseException {
        EvaluationDashboardResult result = new EvaluationDashboardResult();

        // 형성평가 문항들을 누적하여 지식수준 재추론
        KnowledgeLevelRequest tritonRequest = createTritonRequest(request, token);
        KnowledgeLevelResponse response = postWithKnowledgeLevelTriton(tritonRequest);

        // 지식수준 추론 결과
        List<Double> knowledgeRates = response.getOutputs()
                .get(0)
                .getData();

        // 타겟토픽명
        String topicName = request.getProb_list()
                .get(0)
                .getTopic_nm();

        // 진단평가 직후의 타겟토픽 지식수준
        Double before = Double.valueOf(String.format("%.2f", transactionMapper.getTgtTopicKnowledgeRate(token)));

        // 형성평가 이후의 타겟토픽 지식수준
        Integer tgtTopic = diagnosisMapper.getTgtTopic(memberNo);
        Double after = Double.valueOf(String.format("%.2f", knowledgeRates.get(tgtTopic)));

        // 타켓 토픽과 연관된 토픽들의 지식수준의 평균값
        List<TopicValue> relatedTop5Topics = getRelatedKnowledgeRate(tgtTopic);
        Double relatedTopicKnowledgeRate = 0.0;

        for (TopicValue topic : relatedTop5Topics) {
            Double rate = knowledgeRates.get(topic.getIndex());
            relatedTopicKnowledgeRate += rate;
            relatedTopicKnowledgeRate /= 5;
        }

        Double relatedRate = Double.valueOf(String.format("%.2f", relatedTopicKnowledgeRate));

        // 타겟 토픽 전후 지식수준
        result.setTopic_level_change(new TopicLevelChangeDto(before, after));

        // 문항인덱스 정보를 추출하기 위한 문항번호 리스트
        List<Integer> prob_nos = request.getProb_list()
                .stream()
                .map((prob) -> prob.getProb_no())
                .collect(Collectors.toList());

        if (after >= EVALUATION_THRESHHOLD && relatedRate >= EVALUATION_THRESHHOLD) { // 실수로 틀린 문항
            // 틀린문제 문항번호 추출
            List<Integer> prob_idxs = request.getProb_list()
                    .stream()
                    .filter((prob) -> prob.getCorrect() == 0) // 오답인 경우
                    .map((prob) -> prob_nos.indexOf(prob.getProb_no()) + 1)
                    .collect(Collectors.toList());
            // 틀린문제가 존재하는 경우
            result.setMistake_prob(new CheckProbDto(prob_idxs, topicName, after, relatedRate));
            // 점검해야 하는 문항정보는 null로 반환
            result.setCheck_prob(new CheckProbDto(new ArrayList<>(), topicName, after, relatedRate));
        } else if (after < EVALUATION_THRESHHOLD && relatedRate < EVALUATION_THRESHHOLD) { // 점검이 필요한 문항
            // 맞은문제 문항번호 추출
            List<Integer> prob_idxs = request.getProb_list()
                    .stream()
                    .filter((prob) -> prob.getCorrect() == 1) // 정답인 경우
                    .map((prob) -> prob_nos.indexOf(prob.getProb_no()) + 1)
                    .collect(Collectors.toList());
            // 맞은문제가 존재하는 경우
            result.setCheck_prob(new CheckProbDto(prob_idxs, topicName, after, relatedRate));
            // 실수로 틀린 문항정보는 null로 반환
            result.setMistake_prob(new CheckProbDto(new ArrayList<>(), topicName, after, relatedRate));
        }

        return result;
    }


    // 지식추론 통신 로직 리팩토링해서 분리 고민 -> 코드 재활용
    // 1문제씩 누적시켜서 저장하기 때문에
    // 형성평가 최종 결과 대시보드 부분도 누적시킨 데이터를 조회하도록 변경해면 될듯
    private KnowledgeLevelRequest createNextKnowledgeRateRequest(String token, EvaluationNextProbRequest request) {
        // 진단평가 문제 100개 + 학습준비 문제 5개 + 형성평가 문제 데이터
        String diagnosisData = transactionMapper.getDiagnosisData(token);
        String studyData = transactionMapper.getStudyData(token);
        String prevEvaluationData = transactionMapper.getEvaluationData(token);

        // 첫 형성평가 문제인 경우 -> 누적 x
        if (prevEvaluationData == null) {
            // 형성평가 데이터 저장
            try {
                List<EvaluationProbSaveDto> data = List.of(new EvaluationProbSaveDto(request.getQ_idx(), request.getDiff_level(), request.getCorrect()));
                String json = objectMapper.writeValueAsString(data);
                transactionMapper.updateEvaluationData(token, json);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

        } else { // 이전 형성평가 문제들에 누적시키기
            try {
                // 이전 데이터 조회 후 누적
                EvaluationProbSaveDto[] prevData = objectMapper.readValue(prevEvaluationData, EvaluationProbSaveDto[].class);
                                                EvaluationProbSaveDto prob = new EvaluationProbSaveDto(request.getQ_idx(), request.getDiff_level(), request.getCorrect());
                List<EvaluationProbSaveDto> data = Arrays.stream(prevData).collect(Collectors.toList());
                data.add(prob);

                // 누적시킨 형성평가 데이터 저장
                String content = objectMapper.writeValueAsString(data);
                transactionMapper.updateEvaluationData(token,content);

            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        List<DiagnosisProblemDto> diagnosisResult;
        List<StudyReadyProbDto> studyReadyResult = null;
        List<EvaluationProbSaveDto> evaluationResult;

        String nextEvaluationData = transactionMapper.getEvaluationData(token);

        try {
            diagnosisResult = List.of(objectMapper.readValue(diagnosisData, DiagnosisProblemDto[].class));

            // 학습준비 데이터가 null이 아닐 경우에만 리스트로 변환해주기
            if (studyData != null)
                studyReadyResult = List.of(objectMapper.readValue(studyData, StudyReadyProbDto[].class));

            evaluationResult = List.of(objectMapper.readValue(nextEvaluationData, EvaluationProbSaveDto[].class));

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // 토픽리스트 추출
        List<Long> diagnosisQIdxs = diagnosisResult.stream()
                .map(m -> Long.valueOf(m.getQ_idx())).collect(Collectors.toList());
        List<Long> evaluationQIdxs = evaluationResult.stream()
                .map(m -> Long.valueOf(m.getQ_idx())).collect(Collectors.toList());

        // 정오답 리스트 추출
        List<Long> diagnosisCorrects = diagnosisResult.stream()
                .map(m -> Long.valueOf(m.getCorrect())).collect(Collectors.toList());
        List<Long> evaluationCorrects = evaluationResult.stream()
                .map(m -> Long.valueOf(m.getCorrect())).collect(Collectors.toList());

        // 문제 난이도 리스트
        List<Long> diagnosisDiffs = diagnosisResult.stream()
                .map(m -> Long.valueOf(m.getDiff_level())).collect(Collectors.toList());
        List<Long> evaluationDiffs = evaluationResult.stream()
                .map(m -> Long.valueOf(m.getDiff_level())).collect(Collectors.toList());

        // 먼저 진단평가 + 형성평가 데이터 합치기
        List<Long> mergedQIdxs = Stream.of(diagnosisQIdxs, evaluationQIdxs)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        List<Long> mergedCorrects = Stream.of(diagnosisCorrects, evaluationCorrects)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        List<Long> mergedDiffs = Stream.of(diagnosisDiffs, evaluationDiffs)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        // 학습준비를 하고 온 상태라면, 학습준비 데이터도 합쳐주기
        if (studyData != null) {
            List<Long> studyReadyQIdxs = studyReadyResult.stream()
                    .map(m -> Long.valueOf(m.getQ_idx())).collect(Collectors.toList());

            List<Long> studyReadyCorrects = studyReadyResult.stream()
                    .map(m -> Long.valueOf(m.getCorrect())).collect(Collectors.toList());

            List<Long> studyReadyDiffs = studyReadyResult.stream()
                    .map(m -> Long.valueOf(m.getDiff_level())).collect(Collectors.toList());

            mergedQIdxs = Stream.of(mergedQIdxs, studyReadyQIdxs)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            mergedCorrects = Stream.of(mergedCorrects, studyReadyCorrects)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            mergedDiffs = Stream.of(mergedDiffs, studyReadyDiffs)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }

        // INPUT__ 객체 생성
        List<KnowledgeReqObject> inputs = new ArrayList<>();
        inputs.add(createRequestObj(0, mergedQIdxs.size(), mergedQIdxs));
        inputs.add(createRequestObj(1, mergedCorrects.size(), mergedCorrects));
        inputs.add(createRequestObj(2, mergedDiffs.size(), mergedDiffs));

        return new KnowledgeLevelRequest(inputs);
    }

    public Map<Integer, List<EvaluationProblemDto>> getProbMapByDiffLevel(List<EvaluationProblemDto> problems) {
        return problems.stream().collect(Collectors.groupingBy(EvaluationProblemDto::getDiff_level));
    }

    public EvaluationProblemDto getNextProblemInfo(String memberNo, EvaluationNextProbRequest request) {

        // tgtTopic 가져오기
        Integer tgtTopic = diagnosisMapper.getTgtTopic(memberNo);

        // 난이도에 따른 문항 정보 Map 생성
        List<EvaluationProblemDto> problems = evaluationMapper.getNextRecommendProblem(tgtTopic);
        Map<Integer, List<EvaluationProblemDto>> probMap = getProbMapByDiffLevel(problems);

        // correct에 따른 난이도 처리 다르게
        Integer correct = request.getCorrect();
        Integer diffLevel = request.getDiff_level();

        List<Integer> diffLevelList = new ArrayList<>(probMap.keySet());
        Collections.sort(diffLevelList); // 오름차순 정렬
        // 기준 난이도 Idx 구하기
        int diffLevelIdx = diffLevelList.indexOf(diffLevel);

        // 난이도 판별 후, 가져올 문제 리스트
        List<EvaluationProblemDto> newProblems;

        // 맞았을 경우
        if (correct == 1) {

            // 해당 타켓 토픽에서 이미 가장 높은 난이도면, 동일한 난이도에서 문제 가져오기
            // 새로운 난이도 있는 경우, 업데이트 된 난이도에서 문제 가져오기
            if (diffLevelIdx + 1 == diffLevelList.size()) {
                newProblems = probMap.get(diffLevelList.get(diffLevelIdx));
            } else{
                newProblems = probMap.get(diffLevelList.get(diffLevelIdx + 1));
            }

        } else { // 틀렸을 경우

            // 해당 타켓 토픽에서 이미 가장 낮은 난이도면, 동일한 난이도에서 문제 가져오기
            // 새로운 난이도 있는 경우, 업데이트 된 난이도에서 문제 가져오기
            if (diffLevelIdx - 1 < 0) {
                newProblems = probMap.get(diffLevelList.get(diffLevelIdx));
            } else {
                newProblems = probMap.get(diffLevelList.get(diffLevelIdx - 1));
            }

        }

        EvaluationProblemDto result = null;

        // 문항 리스트 중, 이전 문제와 중복되지 않는 문제 반환
        for (EvaluationProblemDto newProblem : newProblems) {
            if (Integer.parseInt(newProblem.getProb_no()) > Integer.parseInt(request.getProb_no())) {
                result = newProblem;
                break;
            }

        }

        // 만약 난이도 내에서 마지막 문항일 시, 해당 난이도의 첫 문항을 반환
        if (result == null) {
            result = newProblems.get(0);
        }

        return result;
    }

    public EvaluationContinueDto getNextProblem(String token, String memberNo, EvaluationNextProbRequest request) {

        // 난이도와 o,x 여부에 따른 새로운 문항 추천
        EvaluationProblemDto nextProblem = getNextProblemInfo(memberNo, request);

        // 형성평가 데이터를 1문제씩 누적시킨후 지식추론값 가져오기
        KnowledgeLevelRequest tritonRequest = createNextKnowledgeRateRequest(token, request);
        KnowledgeLevelResponse response = postWithKnowledgeLevelTriton(tritonRequest);
        // 지식수준 추론 결과
        List<Double> knowledgeRates = response.getOutputs()
                .get(0)
                .getData();
        // 바뀐 지식추론값 반환
        Integer tgtTopic = diagnosisMapper.getTgtTopic(memberNo);
        Double newKnowledgeRate = Double.valueOf(String.format("%.2f", knowledgeRates.get(tgtTopic)));

        return new EvaluationContinueDto(token, newKnowledgeRate, nextProblem);
    }
}