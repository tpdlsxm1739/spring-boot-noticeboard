package com.mysite.sbb.Configuration;
import com.mysite.sbb.Repository.AnswerRepository;
import com.mysite.sbb.Repository.QuestionRepository;
import com.mysite.sbb.Service.AnswerService;
import com.mysite.sbb.Service.QuestionService;
import com.mysite.sbb.Service.UserService;
import com.mysite.sbb.entity.Answer;
import com.mysite.sbb.entity.Question;
import com.mysite.sbb.entity.SiteUser;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Profile({"dev", "test"})
public class NotProd {
    @Bean
    CommandLineRunner initData(
            PasswordEncoder passwordEncoder,
            QuestionService questionService,
		UserService userService,
            AnswerService answerService,
            QuestionRepository questionRepository,
            AnswerRepository answerRepository
    )
    {
        return new CommandLineRunner() {
            @Override
            @Transactional
            public void run(String... args) throws Exception {
                userService.create("admin","admin@test.com", "1234");

                SiteUser user1 = userService.create("user1", "user1@test.com", passwordEncoder.encode("1234"));
                SiteUser user2 = userService.create("user2", "user2@test.com", passwordEncoder.encode("1234"));
                SiteUser user3 = userService.create("puar12", "r4560798@naver.com", passwordEncoder.encode("1234"));

                List<Question> list = new ArrayList<>();
                for (int i = 1; i <= 300; i++) {
                    Question tmp = new Question();
                    tmp.setSubject(String.format("테스트 데이터입니다:[%03d]", i));
                    tmp.setContent("내용무");
                    tmp.setAuthor(user2);
                    list.add(tmp);
                }
                questionRepository.saveAll(list);
                Question question1 = questionService.create("질문입니닷", "질문이에요!", user2,0);
                Question question2 = questionService.create("질문입니닷22", "질문이에요!22", user2,0);

                Answer answer1 = answerService.create(question1, "답변1", user1);
                Answer answer2 = answerService.create(question1, "답변2", user1);

                List<Answer> answerList = new ArrayList<>();
                for (int i = 1; i <= 300; i++) {
                    Answer tmp = new Answer();
                    tmp.setContent("테스트 답변%d".formatted(i));
                    tmp.setQuestion(question2);
                    tmp.setAuthor(user3);
                    answerList.add(tmp);
                }

                answerRepository.saveAll(answerList);

            }
        };
    }
}
