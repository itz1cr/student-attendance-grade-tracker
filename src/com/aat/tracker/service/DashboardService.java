package com.aat.tracker.service;

import com.aat.tracker.dao.AttendanceDAO;
import com.aat.tracker.dao.CourseDAO;
import com.aat.tracker.dao.EnrollmentDAO;
import com.aat.tracker.dao.GradeDAO;
import com.aat.tracker.dao.StudentDAO;
import com.aat.tracker.model.AttendanceStat;
import com.aat.tracker.model.DashboardStats;
import com.aat.tracker.model.Student;
import com.aat.tracker.util.Statistics;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregates the figures shown on the dashboard, reusing the generic
 * {@link Statistics} helpers (Week 8: collections + generics).
 */
public class DashboardService {

    private final StudentDAO studentDao = new StudentDAO();
    private final CourseDAO courseDao = new CourseDAO();
    private final EnrollmentDAO enrollmentDao = new EnrollmentDAO();
    private final AttendanceDAO attendanceDao = new AttendanceDAO();
    private final GradeDAO gradeDao = new GradeDAO();
    private final AttendanceService attendanceService = new AttendanceService();
    private final GradeService gradeService = new GradeService();

    public DashboardStats getStats() {
        List<AttendanceStat> allStats = attendanceService.getAllStatistics();
        List<Double> attendancePercentages = new ArrayList<>();
        int belowThreshold = 0;
        for (AttendanceStat stat : allStats) {
            if (stat.totalClasses() > 0) {
                attendancePercentages.add(stat.attendancePercentage());
                if (stat.attendancePercentage() < AttendanceService.MINIMUM_ATTENDANCE_PERCENT) {
                    belowThreshold++;
                }
            }
        }

        List<Double> gpas = new ArrayList<>();
        for (Student student : studentDao.findAll()) {
            double gpa = gradeService.getGpa(student.getStudentId());
            if (gpa > 0) {
                gpas.add(gpa);
            }
        }

        return new DashboardStats(
                studentDao.countAll(),
                courseDao.countAll(),
                enrollmentDao.countAll(),
                attendanceDao.countAll(),
                gradeDao.countAll(),
                Statistics.mean(attendancePercentages),
                belowThreshold,
                Statistics.mean(gpas));
    }
}
