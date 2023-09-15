package com.mysite.sbb.Service;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.Repository.AnswerRepository;
import com.mysite.sbb.entity.Answer;
import com.mysite.sbb.entity.Question;
import com.mysite.sbb.entity.SiteUser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AnswerService {

    private final AnswerRepository answerRepository;

    @Transactional
    public Answer create(Question question, String content, SiteUser user) {
        Answer answer = new Answer();
        answer.setContent(content);
        answer.setCreateDate(LocalDateTime.now());
        answer.setQuestion(question);
        answer.setAuthor(user);
        this.answerRepository.save(answer);
        return answer;

    }

    public Answer getAnswer(Integer id) {
        Optional<Answer> answer = this.answerRepository.findById(id);
        if (answer.isPresent()) {
            return answer.get();
        } else {
            throw new DataNotFoundException("answer not found");
        }
    }

    @Transactional
    public void modify(Answer answer, String content) {
        answer.setContent(content);
        answer.setModifyDate(LocalDateTime.now());
        this.answerRepository.save(answer);
    }

    @Transactional
    public void delete(Answer answer) {
        this.answerRepository.delete(answer);
    }

    @Transactional
    public void vote(Answer answer, SiteUser siteUser) {
        answer.getVoter().add(siteUser);
        this.answerRepository.save(answer);
    }

    public Long getAnswerCount(SiteUser author) {
        return answerRepository.countByAuthor(author);
    }

    public List<Answer> getAnswerTop5LatestByUser(SiteUser user) {
        return answerRepository.findTop5ByAuthorOrderByCreateDateDesc(user);
    }

    public List<Answer> getAnswerTop15Latest() {
        return answerRepository.findTop15ByOrderByCreateDateDesc();
    }

}
