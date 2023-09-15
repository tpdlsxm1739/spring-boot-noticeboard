package com.mysite.sbb.Repository;

import com.mysite.sbb.entity.Answer;
import com.mysite.sbb.entity.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface AnswerRepository extends JpaRepository<Answer, Integer> {

    Long countByAuthor(SiteUser author);

    List<Answer> findTop5ByAuthorOrderByCreateDateDesc(SiteUser user);

    List<Answer> findTop15ByOrderByCreateDateDesc();

}
