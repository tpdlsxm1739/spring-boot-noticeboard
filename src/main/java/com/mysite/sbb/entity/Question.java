package com.mysite.sbb.entity;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Set;

import com.mysite.sbb.QuestionEnum;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
@Getter
@Setter 
@Entity   //엔티티로 만들기 위해서
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //고유번호를 생성하는 옵션
    private Integer id;

    @Column(length = 200) //칼럼의 길이를 설정할때 사용
    private String subject;

    @Column(columnDefinition = "TEXT") //컬럼의 속성,글자수를 제한할수 없는 경우
    private String content;

    private LocalDateTime createDate;

    @OneToMany(mappedBy = "question", cascade= CascadeType.REMOVE)
    private List<Answer> answerList;

    @ManyToOne
    private SiteUser author;

    @ManyToMany
    Set<SiteUser> voter;

    @Column(columnDefinition = "integer default 0", nullable = false)
    private int view=0;
    public void updateView() {
        this.view++;
    }
    /* 게시판 분류
	0 : 질문답변
	1 : 강좌
	2 : 자유게시판
	 */
    private int category;
    public QuestionEnum getCategoryAsEnum() {
        switch (this.category) {
            case 0:
                return QuestionEnum.QNA;
            case 1:
                return QuestionEnum.FREE;
            case 2:
                return QuestionEnum.BUG;
            default:
                throw new RuntimeException("올바르지 않은 접근입니다.");
        }
    }



    private LocalDateTime modifyDate;
}
