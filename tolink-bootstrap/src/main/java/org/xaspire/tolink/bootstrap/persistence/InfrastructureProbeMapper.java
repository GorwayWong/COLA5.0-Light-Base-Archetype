package org.xaspire.tolink.bootstrap.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface InfrastructureProbeMapper {

    @Select("SELECT 1")
    int selectOne();
}
