package com.nutrition.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AnnouncementReadMapper {

    @Insert("INSERT INTO announcement_reads (announcement_id, user_id) VALUES (#{announcementId}, #{userId}) ON DUPLICATE KEY UPDATE read_at=CURRENT_TIMESTAMP")
    int markRead(@Param("announcementId") Long announcementId, @Param("userId") Long userId);

    @Select("SELECT announcement_id FROM announcement_reads WHERE user_id=#{userId}")
    List<Long> listReadIds(@Param("userId") Long userId);
}
