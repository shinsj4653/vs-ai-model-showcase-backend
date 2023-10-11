package visang.showcase.aibackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface TransactionMapper {

    // 타겟토픽 지식수준
    void updateTgtTopicKnowledgeRate(@Param("transaction_token") String transaction_token, @Param("tgtTopicKnowledgeRate") Double tgtTopicKnowledgeRate);
    Double getTgtTopicKnowledgeRate(@Param("transaction_token") String transaction_token);

    // 진단평가 데이터
    String getDiagnosisData(@Param("transaction_token") String transaction_token);
    void updateDiagnosisData(@Param("transaction_token") String transaction_token, @Param("diagnosis_data") String diagnosis_data);

    // 학습준비 데이터
    String getStudyData(@Param("transaction_token") String transaction_token);
    void updateStudyData(@Param("transaction_token") String transaction_token, @Param("study_data") String study_data);

    // 형성평가 데이터
    String getEvaluationData(@Param("transaction_token") String transaction_token);
    void updateEvaluationData(@Param("transaction_token") String transaction_token, @Param("evaluation_data") String evaluation_data);


}