package com.nutrition.mapper;

import com.nutrition.entity.Announcement;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AnnouncementMapper {
    
    @Select("SELECT * FROM announcements ORDER BY is_pinned DESC, publish_time DESC")
    List<Announcement> findAll();
    
    @Select("SELECT * FROM announcements WHERE id = #{id}")
    Announcement findById(Long id);
    
    @Insert("INSERT INTO announcements (title, summary, content, is_pinned, publish_time) " +
            "VALUES (#{title}, #{summary}, #{content}, #{isPinned}, #{publishTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Announcement announcement);
    
    @Update("UPDATE announcements SET title=#{title}, summary=#{summary}, content=#{content}, " +
            "is_pinned=#{isPinned}, publish_time=#{publishTime} WHERE id=#{id}")
    int update(Announcement announcement);
    
    @Delete("DELETE FROM announcements WHERE id=#{id}")
    int deleteById(Long id);
    
    @Update("UPDATE announcements SET is_pinned=#{isPinned} WHERE id=#{id}")
    int updatePinnedStatus(@Param("id") Long id, @Param("isPinned") Boolean isPinned);
}

