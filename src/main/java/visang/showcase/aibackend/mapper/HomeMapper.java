package visang.showcase.aibackend.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HomeMapper {
    String getTopicName();
}
