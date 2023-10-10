package visang.showcase.aibackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface TransactionMapper {

    // 진단평가 데이터
//    void insertDiagnosisData(@Param("transaction_token") String transaction_token, @Param("diagnosis_data") String diagnosis_data);
    void updateDiagnosisData(@Param("transaction_token") String transaction_token);


    // 학습준비 데이터
    void updateStudyData(@Param("transaction_token") String transaction_token, @Param("study_data") String study_data);

}