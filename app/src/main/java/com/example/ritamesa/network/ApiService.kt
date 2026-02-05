package com.example.ritamesa.network

import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // ========== AUTHENTICATION ==========
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @POST("auth/logout")
    suspend fun logout(): Response<LogoutResponse>
    
    @GET("me")
    suspend fun getMe(): Response<UserResponse>
    
    // ========== DASHBOARD ==========
    @GET("me/dashboard/summary")
    suspend fun getStudentDashboard(): Response<StudentDashboardResponse>
    
    @GET("me/dashboard/teacher-summary")
    suspend fun getTeacherDashboard(): Response<TeacherDashboardResponse>
    
    @GET("me/homeroom/dashboard")
    suspend fun getHomeroomDashboard(): Response<HomeroomDashboardResponse>
    
    // ========== ATTENDANCE ==========
    @POST("attendance/scan")
    suspend fun scanQR(@Body request: ScanQRRequest): Response<ScanQRResponse>
    
    @GET("me/attendance")
    suspend fun getMyAttendance(
        @Query("month") month: Int?,
        @Query("year") year: Int?
    ): Response<AttendanceHistoryResponse>
    
    @GET("me/attendance/teaching")
    suspend fun getTeacherAttendance(
        @Query("date") date: String?,
        @Query("status") status: String?
    ): Response<TeacherAttendanceResponse>
    
    // ========== FOLLOW-UP ==========
    @GET("me/students/follow-up")
    suspend fun getStudentsFollowUp(
        @Query("search") search: String?
    ): Response<FollowUpResponse>
    
    // ========== NOTIFICATIONS ==========
    @GET("me/notifications")
    suspend fun getNotifications(
        @Query("date") date: String?
    ): Response<NotificationResponse>
    
    // ========== SCHEDULES ==========
    @GET("me/schedules")
    suspend fun getMySchedules(): Response<ScheduleResponse>
    
    // ========== TEACHERS ==========
    @GET("teachers")
    suspend fun getTeachers(
        @Query("search") search: String?
    ): Response<TeachersResponse>
}

// ========== REQUEST DATA CLASSES ==========
data class LoginRequest(
    val email: String,
    val password: String,
    val device_name: String
)

data class ScanQRRequest(
    val qrcode_token: String,
    val latitude: Double?,
    val longitude: Double?
)

// ========== RESPONSE DATA CLASSES ==========
data class LoginResponse(
    val message: String,
    val token: String,
    val user: User
)

data class LogoutResponse(
    val message: String
)

data class UserResponse(
    val id: Int,
    val name: String,
    val email: String,
    val role: String
)

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val is_class_officer: Boolean?,
    val profile: UserProfile?
)

data class UserProfile(
    val nis: String?,
    val nip: String?,
    val class_name: String?,
    val photo_url: String?
)

data class StudentDashboardResponse(
    val date: String,
    val day_name: String,
    val student: StudentInfo,
    val school_hours: SchoolHours,
    val schedule_today: List<ScheduleItem>
)

data class StudentInfo(
    val name: String,
    val class_name: String,
    val nis: String,
    val photo_url: String?,
    val is_class_officer: Boolean
)

data class SchoolHours(
    val start_time: String,
    val end_time: String
)

data class ScheduleItem(
    val id: Int,
    val time_slot: String,
    val subject: String,
    val teacher: String,
    val start_time: String,
    val end_time: String,
    val status: String,
    val status_label: String,
    val check_in_time: String?
)

data class TeacherDashboardResponse(
    val date: String,
    val day_name: String,
    val teacher: TeacherInfo,
    val school_hours: SchoolHours,
    val attendance_summary: AttendanceSummary,
    val schedule_today: List<TeacherScheduleItem>
)

data class TeacherInfo(
    val name: String,
    val nip: String,
    val code: String,
    val photo_url: String?
)

data class AttendanceSummary(
    val present: Int,
    val sick: Int,
    val excused: Int,
    val absent: Int
)

data class TeacherScheduleItem(
    val id: Int,
    val subject: String,
    val class_name: String,
    val time_slot: String,
    val start_time: String,
    val end_time: String
)

data class HomeroomDashboardResponse(
    val date: String,
    val homeroom_class: HomeroomClass,
    val attendance_summary: AttendanceSummary,
    val schedule_today: List<ScheduleItem>
)

data class HomeroomClass(
    val id: Int,
    val name: String,
    val total_students: Int
)

data class ScanQRResponse(
    val message: String,
    val status: String,
    val timestamp: String,
    val schedule: ScheduleInfo?
)

data class ScheduleInfo(
    val subject: String,
    val class_name: String,
    val teacher: String
)

data class AttendanceHistoryResponse(
    val data: List<AttendanceRecord>,
    val summary: AttendanceSummary?
)

data class AttendanceRecord(
    val id: Int,
    val date: String,
    val subject: String,
    val status: String,
    val status_label: String,
    val check_in_time: String?,
    val attachment_url: String?
)

data class TeacherAttendanceResponse(
    val data: List<TeacherAttendanceRecord>,
    val summary: AttendanceSummary?
)

data class TeacherAttendanceRecord(
    val id: Int,
    val subject: String,
    val class_name: String,
    val date: String,
    val time: String,
    val status: String,
    val status_label: String
)

data class FollowUpResponse(
    val data: List<FollowUpStudent>
)

data class FollowUpStudent(
    val id: Int,
    val name: String,
    val nis: String,
    val class_name: String,
    val attendance_summary: StudentAttendanceSummary,
    val badge: Badge,
    val severity_score: Int
)

data class StudentAttendanceSummary(
    val absent: Int,
    val excused: Int,
    val sick: Int
)

data class Badge(
    val type: String,
    val label: String
)

data class NotificationResponse(
    val date: String,
    val notifications: List<Notification>
)

data class Notification(
    val id: Int,
    val type: String,
    val message: String,
    val detail: String,
    val time: String,
    val created_at: String
)

data class ScheduleResponse(
    val data: List<ScheduleItem>
)

data class TeachersResponse(
    val data: List<Teacher>
)

data class Teacher(
    val id: Int,
    val name: String,
    val nip: String,
    val code: String,
    val subject_name: String,
    val photo_url: String?
)
