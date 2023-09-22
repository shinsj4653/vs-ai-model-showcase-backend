package visang.showcase.aibackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import visang.showcase.aibackend.dto.response.member.MemberDto;
import visang.showcase.aibackend.dto.response.member.MemberQueryDto;
import visang.showcase.aibackend.mapper.MemberMapper;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;
    public List<MemberDto> getMembers() {
        List<MemberQueryDto> members = memberMapper.getMembers();
        return members.stream()
                .map(MemberDto::of)
                .collect(Collectors.toList());
    }
}
