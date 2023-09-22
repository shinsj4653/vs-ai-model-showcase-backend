package visang.showcase.aibackend.dto.response.member;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberDto {
    public String member_no; // 학생 번호
    public String tgt_topic; // 타켓 토픽 번호
    public String topic_nm; // 토픽단원명
    public String subject_nm;  // 학년, 학기 정보 (ex 초4-1)
}
