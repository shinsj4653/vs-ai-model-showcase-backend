package visang.showcase.aibackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import visang.showcase.aibackend.dto.response.member.MemberQueryDto;

import java.util.List;

@Mapper
public interface MemberMapper {
    List<MemberQueryDto> getMembers();
}