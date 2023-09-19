package com.mysite.sbb.Service;



import com.mysite.sbb.Repository.QuestionRepository;
import com.mysite.sbb.entity.Answer;
import com.mysite.sbb.entity.Question;
import com.mysite.sbb.entity.SiteUser;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.mysite.sbb.DataNotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor

public class QuestionService {

    private final QuestionRepository questionRepository;

    public List<Question> getList(){
        return this.questionRepository.findAll();
    }
    @Transactional
    public Question updateQuestionView(Question question) {
        question.updateView();
        questionRepository.save(question);
        return question;
    }

    public Question getQuestion(Integer id) {
        Optional<Question> question = this.questionRepository.findById(id);
        if (question.isPresent()) {
            return question.get();
        } else {
            throw new DataNotFoundException("question not found");
        }
    }

    public Question create(String subject, String content, SiteUser user,int category) {
        Question q = new Question();
        q.setSubject(subject);
        q.setContent(content);
        q.setCreateDate(LocalDateTime.now());
        q.setAuthor(user);
        q.setCategory(category);
        this.questionRepository.save(q);
        return q;
    }
    public Page<Question> getList(int category, int page, String kw) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page,10,Sort.by(sorts));//
        Specification<Question> spec = search(kw);
        return questionRepository.findAllByKeywordAndType(kw, category, pageable);
    }

    public void modify(Question question ,String subject, String content) {
        question.setSubject(subject);
        question.setContent(content);
        question.setModifyDate(LocalDateTime.now());
        this.questionRepository.save(question);
    }
    public void delete(Question question) {
        this.questionRepository.delete(question);
    }
    public void vote(Question question, SiteUser siteUser) {
        question.getVoters().add(siteUser);
        this.questionRepository.save(question);
    }

    private Specification<Question> search(String kw) {
        return new Specification<>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Predicate toPredicate(Root<Question> q, CriteriaQuery<?> query, CriteriaBuilder cb) {
                query.distinct(true); //중복을 제거
                Join<Question, SiteUser> u1 = q.join("author",JoinType.LEFT);
                Join<Question, Answer> a = q.join("answerList",JoinType.LEFT);
                Join<Answer, SiteUser> u2 = a.join("author", JoinType.LEFT);
                return cb.or(cb.like(q.get("subject"),"%"+kw+"%"),
                        cb.like(q.get("content"),"%"+kw+"%"),
                        cb.like(u1.get("username"),"%"+kw+"%"),
                        cb.like(a.get("content"),"%"+kw+"%"),
                        cb.like(u2.get("username"),"%"+kw+"%"));
            }
        };
    }

    public Long getQuestionCount(SiteUser author) {
        return questionRepository.countByAuthor(author);
    }

    public List<Question> getQuestionTop5LatestByUser(SiteUser author) {
        return questionRepository.findTop5ByAuthorOrderByCreateDateDesc(author);
    }

    // 유저 개인별 질문 모음(질문자)
    public Page<Question> getPersonalQuestionListByQuestionAuthorId(int page, String kw, Long authorId) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts)); //페이지 번호, 개수
        return questionRepository.findAllByKeywordAndAuthorId(kw, authorId, pageable);
    }


    // 유저 개인별 질문 모음(답변자)
    public Page<Question> getPersonalQuestionListByAnswer_AuthorId(int page, String kw, Long answerAuthorId) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts)); //페이지 번호, 개수
        return questionRepository.findAllByKeywordAndAndAnswer_AuthorId(kw, answerAuthorId, pageable);
    }





}
