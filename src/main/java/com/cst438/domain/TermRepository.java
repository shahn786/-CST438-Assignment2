package com.cst438.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TermRepository extends CrudRepository<Term, Integer> {

    @Query("select t from Term t where t.year = :year and t.semester = :semester")
    Term findByYearAndSemester(@Param("year") int year, @Param("semester") String semester);

    @Query("select s from Section s order by s.term.termId desc")
    List<Term> findAllByOrderByTermIdDesc();
}
