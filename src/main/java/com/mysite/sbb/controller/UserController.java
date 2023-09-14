package com.mysite.sbb.controller;


import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.Form.UserPWFindForm;
import com.mysite.sbb.Service.AnswerService;
import com.mysite.sbb.Service.QuestionService;
import com.mysite.sbb.entity.Answer;
import com.mysite.sbb.entity.Question;
import com.mysite.sbb.entity.SiteUser;
import jakarta.validation.Valid;

import com.mysite.sbb.Form.UserCreateForm;
import com.mysite.sbb.Service.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final QuestionService questionService;

    private final AnswerService answerService;

    @GetMapping("/signup")
    public String signup(UserCreateForm userCreateForm) {
        return "signup_form";
    }
    @GetMapping("/login")
    public String login() {
        return "login_form";
    }

    @PostMapping("/signup")
    public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "signup_form";
        }

        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect",
                    "2개의 패스워드가 일치하지 않습니다.");
            return "signup_form";
        }
        try {

            userService.create(userCreateForm.getUsername(),
                    userCreateForm.getEmail(), userCreateForm.getPassword1());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
            return "signup_form";
        } catch (Exception e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", e.getMessage());
            return "signup_form";
        }
        return "redirect:/";
    }

        @GetMapping("/mypage")
        @PreAuthorize("isAuthenticated()")
        public String showmyPage(Model model, Principal principal)
        {
            SiteUser user = userService.getUser(principal.getName());

            if(user == null) {
                throw new DataNotFoundException("사용자를 찾을 수 없습니다.");
            }
            model.addAttribute("user", user);

            Long questionCount = questionService.getQuestionCount(user);
            model.addAttribute("questionCount", questionCount);

            List<Question> questionList = questionService.getQuestionTop5LatestByUser(user);
            model.addAttribute("questionList", questionList);

            Long answerCount = answerService.getAnswerCount(user);
            model.addAttribute("answerCount", answerCount);

            List<Answer> answerList = answerService.getAnswerTop5LatestByUser(user);
            model.addAttribute("answerList", answerList);

            return "my_page";
        }
    @PreAuthorize("isAnonymous()")
    @GetMapping("/pw_find")
    public String showFindPassWord(UserPWFindForm userPWFindForm) {
        return "pw_find";
    }

    @PreAuthorize("isAnonymous()")
    @PostMapping("/pw_find")
    public String findPassWord(Model model, UserPWFindForm userPWFindForm, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if(bindingResult.hasErrors()) {
            return "pw_find";
        }

        SiteUser user = userService.getUser(userPWFindForm.getUsername());

        if(user == null) {
            bindingResult.reject("notFindUser", "일치하는 사용자가 없습니다.");
            return "pw_find";
        }

        if(!user.getEmail().equals(userPWFindForm.getEmail())){
            bindingResult.reject("notCorrectEmail", "등록된 회원 정보와 이메일이 다릅니다.");
            return "pw_find";
        }

        String tempPW = userService.setTemporaryPW(user);

        // 이메일 전송
        // @Async 붙은 메서드는 동일한 클래스에서 호출할 수 없기에 컨트롤러에서 메일 발송 요청
        userService.sendEmail(userPWFindForm.getEmail(), user.getUsername(), tempPW);

        // 로그인 페이지에서 보여줄 성공 메시지를 플래시 애트리뷰트로 추가
        redirectAttributes.addFlashAttribute("successMessage", "임시 비밀번호가 이메일로 전송되었습니다. 이메일 확인 후 로그인 해주세요.");

        return "redirect:/user/login";
    }


    }

