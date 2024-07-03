package com.cst438.domain;

import jakarta.persistence.*;

@Entity
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private int enrollmentId;

    // Grade attribute for student's grade in the course
    private String grade;

    // ManyToOne relationship with User entity (student)
    @ManyToOne
    @JoinColumn(name = "student_email")
    private User student;

    // ManyToOne relationship with Section entity (course section)
    @ManyToOne
    @JoinColumn(name = "section_id")
    private Section section;

    public Enrollment() {
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }
}
