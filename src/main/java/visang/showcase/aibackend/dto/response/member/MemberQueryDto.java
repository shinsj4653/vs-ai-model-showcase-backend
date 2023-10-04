package visang.showcase.aibackend.dto.response.member;

import lombok.Getter;

@Getter
public class MemberQueryDto {
    private String member_no;
    private Integer q_idx;
    private String topic_nm;
    private String subject_cd;
}
