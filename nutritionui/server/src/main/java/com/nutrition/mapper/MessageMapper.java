package com.nutrition.mapper;

import com.nutrition.entity.Message;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MessageMapper {
    
    @Insert("INSERT INTO messages (user_id, title, content, message_type, is_read, created_at) VALUES (#{userId}, #{title}, #{content}, #{messageType}, #{isRead}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Message message);
    
    @Select("SELECT * FROM messages WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{limit}")
    List<Message> findByUserId(@Param("userId") Long userId, @Param("limit") int limit);
    
    @Select("SELECT * FROM messages WHERE user_id = #{userId} AND is_read = false")
    List<Message> findUnreadByUserId(Long userId);
    
    @Select("SELECT COUNT(*) FROM messages WHERE user_id = #{userId} AND is_read = false")
    int countUnreadByUserId(Long userId);
    
    @Update("UPDATE messages SET is_read = true WHERE id = #{id}")
    int markAsRead(Long id);
    
    @Update("UPDATE messages SET is_read = true WHERE user_id = #{userId}")
    int markAllAsRead(Long userId);
    
    @Delete("DELETE FROM messages WHERE id = #{id}")
    int delete(Long id);
}

