package com.cst438.domain;

import jakarta.persistence.*;

import java.sql.Date;

@Entity
public class Assignment {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="assignment_id")
    private int assignmentId;

    @ManyToOne
    @JoinColumn(name = "section_no")
    private Section section;

    @Column(name="title")
    private String title;

    @Column(name="due_date")
    private Date dueDate;

    @Column(name="course_id")
    private String courseId;


    // Constructors, getters, and setters
    public Assignment() {
    }

    public Assignment(String title, Date dueDate, Section section) {
        this.title = title;
        this.dueDate = dueDate;
        this.section = section;
    }

    public int getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }
}