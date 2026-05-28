package com.nutrition.mapper;

import com.nutrition.entity.Reminder;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ReminderMapper {
    
    @Insert("INSERT INTO reminders (user_id, reminder_type, reminder_time, days_of_week, is_enabled, created_at) " +
            "VALUES (#{userId}, #{reminderType}, #{reminderTime}, #{daysOfWeek}, #{isEnabled}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Reminder reminder);
    
    @Select("SELECT * FROM reminders WHERE user_id = #{userId}")
    List<Reminder> findByUserId(Long userId);
    
    @Select("SELECT * FROM reminders WHERE user_id = #{userId} AND reminder_type = #{reminderType}")
    List<Reminder> findByUserIdAndType(@Param("userId") Long userId, @Param("reminderType") String reminderType);
    
    @Select("SELECT * FROM reminders WHERE id = #{id}")
    Reminder findById(Long id);
    
    @Select("SELECT * FROM reminders WHERE is_enabled = true")
    List<Reminder> findAllEnabled();
    
    @Update("UPDATE reminders SET reminder_time = #{reminderTime}, days_of_week = #{daysOfWeek}, is_enabled = #{isEnabled} WHERE id = #{id}")
    int update(Reminder reminder);
    
    @Delete("DELETE FROM reminders WHERE id = #{id}")
    int delete(Long id);
}

