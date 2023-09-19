package com.mysite.sbb.controller;

import com.mysite.sbb.Form.AnswerForm;
import com.mysite.sbb.Form.QuestionForm;
import com.mysite.sbb.QuestionEnum;
import com.mysite.sbb.Service.AnswerService;
import com.mysite.sbb.Service.QuestionService;
import com.mysite.sbb.Service.UserService;
import com.mysite.sbb.entity.Answer;
import com.mysite.sbb.entity.Question;
import com.mysite.sbb.entity.SiteUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.LocalTime;

@RequestMapping("/question")
@RequiredArgsConstructor
@Controller
public class QuestionController {
    LocalDateTime todayEndTime = LocalDate.now().atTime(LocalTime.MAX); // 현재 하루의 종료 시간, 2022-08-20T23:59:59.9999999
    LocalDateTime currentTime = LocalDateTime.now(); // 현재 시간, 2022-08-20T19:39:10.936
    long todayEndSecond = todayEndTime.toEpochSecond(ZoneOffset.UTC); // 하루 종료 시간을 시간초로 변환
    long currentSecond = currentTime.toEpochSecond(ZoneOffset.UTC); // 현재 시간을 시간초로 변환
    long remainingTime = todayEndSecond - currentSecond; // 하루 종료까지 남은 시간
    private final QuestionService questionService;
    private final UserService userService;
    private final AnswerService answerService;
    @GetMapping("/list/{type}")
    public String list(Model model, @PathVariable String type,
                       @RequestParam(value = "page", defaultValue = "0") int page
            , @RequestParam(value = "kw", defaultValue = "") String kw)  {
        int category = switch (type) {
            case "qna" -> QuestionEnum.QNA.getStatus();
            case "free" -> QuestionEnum.FREE.getStatus();
            case "bug" -> QuestionEnum.BUG.getStatus();
            default -> throw new RuntimeException("올바르지 않은 접근입니다.");
        };

        model.addAttribute("boardName", category);
        Page<Question> paging = questionService.getList(category, page, kw);
        model.addAttribute("paging", paging);
        return "question_list";
    }


    @GetMapping("/")
    public String root() {
        return "redirect:/question/list";
    }

    @GetMapping("/increase")
    @ResponseBody
    public String increaseHit(Integer questionId, @RequestParam(required = false) Boolean isVisited) {
        Question question = questionService.getQuestion(questionId);

        // 방문한 적이 없을때만 조회수 증가
        if (isVisited != null && !isVisited) {
            questionService.updateQuestionView(question);
        }
            return Integer.toString(question.getView());

    }

    @GetMapping(value = "/detail/{id}")
    public String detail(Model model, @PathVariable Integer id, AnswerForm answerForm,
                         @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "") String sort) {
        Question question = questionService.getQuestion(id);
        Page<Answer> paging = answerService.getAnswerPage(question, page, sort);
        model.addAttribute("question", question);
        model.addAttribute("paging", paging);
        return "question_detail";
    }



    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create/{type}")
    public String showCreate(@PathVariable String type, QuestionForm questionForm, Model model) {
        switch (type) {
            case "qna" -> model.addAttribute("boardName", "질문과답변 작성");
            case "free" -> model.addAttribute("boardName", "자유게시판 작성");
            case "bug" -> model.addAttribute("boardName", "버그및건의 작성");
            default -> throw new RuntimeException("올바르지 않은 접근입니다.");
        }
        return "question_form";

        }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create/{type}")
    public String questionCreate(@Valid QuestionForm questionForm, @PathVariable String type,
                                 BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            return "question_form";
        }

        int category = switch (type) {
            case "qna" -> QuestionEnum.QNA.getStatus();
            case "free" -> QuestionEnum.FREE.getStatus();
            case "bug" -> QuestionEnum.BUG.getStatus();
            default -> throw new RuntimeException("올바르지 않은 접근입니다.");
        };

        SiteUser siteUser = userService.getUser(principal.getName());
        questionService.create(questionForm.getSubject(), questionForm.getContent(), siteUser,category);
        return "redirect:question/list/%s".formatted(type);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String questionModify(QuestionForm questionForm, @PathVariable("id") Integer id, Principal principal,
                                 Model model) {
        Question question = questionService.getQuestion(id);
        if(!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }

        switch (question.getCategoryAsEnum()) {
            case QNA -> model.addAttribute("boardName", "질문과답변 수정");
            case FREE -> model.addAttribute("boardName", "자유게시판 수정");
            case BUG -> model.addAttribute("boardName", "버그및건의 수정");
            default -> throw new RuntimeException("올바르지 않은 접근입니다.");
        }
        questionForm.setSubject(question.getSubject());
        questionForm.setContent(question.getContent());
        return "question_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String questionModify(@Valid QuestionForm questionForm, BindingResult bindingResult,
                                 Principal principal, @PathVariable("id") Integer id) {
        if (bindingResult.hasErrors()) {
            return "question_form";
        }
        Question question = this.questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        this.questionService.modify(question, questionForm.getSubject(), questionForm.getContent());
        return String.format("redirect:/question/detail/%s", id);
    }


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String questionDelete(Principal principal, @PathVariable("id") Integer id) {
        Question question = this.questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
        }
        this.questionService.delete(question);
        return "redirect:/";
    }
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/list/byQuestion/{id}")
    public String personalListByQuestionUserId(Model model, @PathVariable Long id,
                                               @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "") String kw,
                                               Principal principal)  {

        SiteUser siteUser = userService.getUser(principal.getName());

        if(siteUser.getId() != id) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "조회 권한이 없습니다.");
        }

        Page<Question> paging = questionService.getPersonalQuestionListByQuestionAuthorId(page, kw, id);
        model.addAttribute("user", siteUser);
        model.addAttribute("paging", paging);
        // 동일한 템플릿 사용 -> 총 답변수로 표기하기 위함
        model.addAttribute("type", "총 질문수");
        return "personal_list";
    }
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/list/byAnswer/{id}")
    public String personalListByAnswerUserId(Model model, @PathVariable Long id,
                                             @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "") String kw, Principal principal) {
        SiteUser siteUser = userService.getUser(principal.getName());

        if (siteUser.getId() != id) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "조회 권한이 없습니다.");
        }

        // 총 답변 수 View에 나타내기 위함
        Long answerCount = answerService.getAnswerCount(siteUser);
        model.addAttribute("answerCount", answerCount);

        Page<Question> paging = questionService.getPersonalQuestionListByAnswer_AuthorId(page, kw, id);
        model.addAttribute("user", siteUser);
        model.addAttribute("paging", paging);
        // 동일한 템플릿 사용 -> 총 답변수로 표기하기 위함
        model.addAttribute("type", "총 답변수");
        return "personal_list";
    }


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String questionVote(Principal principal, @PathVariable("id") Integer id) {
        Question question = this.questionService.getQuestion(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());
        this.questionService.vote(question, siteUser);
        return String.format("redirect:/question/detail/%s", id);
    }





}