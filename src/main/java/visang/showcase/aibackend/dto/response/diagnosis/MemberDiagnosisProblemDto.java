package visang.showcase.aibackend.dto.response.diagnosis;

import lombok.Getter;

@Getter
public class MemberDiagnosisProblemDto {
    private String prob_no; // 문항 번호
    private Integer prob_seq_no; // 문항 시퀀스 번호
    private String topic_nm; // 토픽 한글명
    private String chapter_nm;  // 대단원 한글명
    private String section_nm; // 중단원 한글명
    private String subsection_nm; // 소단원 한글명
    private Integer correct; // 문항 정오답 여부
}
