package visang.showcase.aibackend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import visang.showcase.aibackend.dto.request.diagnosis.DashboardRequest;
import visang.showcase.aibackend.dto.request.triton.KnowledgeLevelRequest;
import visang.showcase.aibackend.dto.request.triton.KnowledgeReqObject;
import visang.showcase.aibackend.dto.response.diagnosis.DiagnosisProblemDto;
import visang.showcase.aibackend.mapper.DiagnosisMapper;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequiredArgsConstructor
public class RecommendController {

    private final DiagnosisMapper diagnosisMapper;

    /**
     * RequestBody의 INPUT__ 요청 객체 생성
     */
    private KnowledgeReqObject createRequestObj(int idx, List<Integer> payload) {
        KnowledgeReqObject obj = new KnowledgeReqObject();
        obj.setName("INPUT__" + idx);
        obj.setDatatype("INT64");

        List<Integer> shape = List.of(1, payload.size()); // 배치 사이즈, 문항 수
        obj.setShape(shape);

        List<List<Integer>> data = List.of(payload);
        obj.setData(data);

        return obj;
    }

    /**
     * 트리톤 서버에 전송할 RequestBody 생성
     */
    private KnowledgeLevelRequest createTritonRequest(String memberNo, DashboardRequest request) {
        List<DiagnosisProblemDto> preList = diagnosisMapper.getProblems(memberNo).subList(0, 85);

        System.out.println("size:" + request.getProb_list().size());
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

        // INPUT__ 객체 생성
        List<KnowledgeReqObject> inputs = new ArrayList<>();
        inputs.add(createRequestObj(0, q_idx_list));
        inputs.add(createRequestObj(1, correct_list));
        inputs.add(createRequestObj(2, diff_level_list));
        // 옵션 값 추가
        inputs.add(createRequestObj(3, List.of(279, 5, 1, 5, 5)));

        return new KnowledgeLevelRequest(inputs);
    }

    private RecommendProbResponse postWithRecommendTriton(KnowledgeLevelRequest request) throws JsonProcessingException {

        // ObjectMapper를 설정합니다.
        ObjectMapper objectMapper = new ObjectMapper();

        // BigDecimal 모듈을 설정합니다.
        SimpleModule module = new SimpleModule();
        module.addDeserializer(BigDecimal.class, new NumberDeserializers.BigDecimalDeserializer());
        objectMapper.registerModule(module);

        // JSON 데이터를 역직렬화합니다.

        RestTemplate restTemplate = new RestTemplate();
        RecommendProbResponse response = restTemplate.postForObject("http://10.214.2.33:8000" + "/v2/models/gkt_reco/infer", request, RecommendProbResponse.class);
        return response;
    }

    @PostMapping("/recommend")
    public RecommendProbResponse recommendProb(HttpSession session, @RequestBody DashboardRequest request) throws JsonProcessingException {
        String memberNo = (String) session.getAttribute("memberNo");
        System.out.println(request.getProb_list().size());
        KnowledgeLevelRequest recommendProbRequest = createTritonRequest(memberNo, request);
        return postWithRecommendTriton(recommendProbRequest);
    }
}