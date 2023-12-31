package visang.showcase.aibackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import visang.showcase.aibackend.dto.response.member.MemberQueryDto;

import java.util.List;

@Mapper
public interface MemberMapper {
    List<MemberQueryDto> getMembers();

    String saveToken(@Param("transaction_token") String transaction_token);

    Integer getMemberInfo(@Param("memberNo") String memberNo);
}
