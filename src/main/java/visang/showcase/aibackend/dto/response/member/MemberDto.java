package visang.showcase.aibackend.dto.response.member;

import lombok.Builder;
import lombok.Getter;
import visang.showcase.aibackend.util.SubjectCodeTransformer;

@Getter
@Builder
public class MemberDto {
    public String member_no; // 학생 번호
    public Integer tgt_topic; // 타켓 토픽 번호
    public String topic_nm; // 토픽단원명
    public String subject_nm;  // 학년, 학기 정보 (ex 초4-1)

    public static MemberDto of(MemberQueryDto queryDto) {

        SubjectCodeTransformer transformer = new SubjectCodeTransformer();

        return MemberDto.builder()
                .member_no(queryDto.getMember_no())
                .tgt_topic(queryDto.getQ_idx())
                .topic_nm(queryDto.getTopic_nm())
                .subject_nm(transformer.transform(queryDto.getSubject_cd()))
                .build();
    }

}
