package com.mysite.sbb.entity;

import com.mysite.sbb.QuestionEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter 
@Entity
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 200)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String content;

    @CreatedDate
    private LocalDateTime createDate;

    @LastModifiedDate
    private LocalDateTime modifyDate;

    @PrePersist
    public void prePersist() {
        this.modifyDate = null; // 객체 생성 시 처음에 수정일 null값으로 설정
    }
    @OneToMany(mappedBy = "question", cascade = CascadeType.REMOVE)
    @LazyCollection(LazyCollectionOption.EXTRA) // answerList.size(); 함수가 실행될 때 SELECT COUNT 실행
    // N+1 문제는 발생하지만, 한 페이지에 보여주는 10개의 게시물의 정보를 가져와서 개수를 표기하는 것 보다는 덜 부담
    private List<Answer> answerList = new ArrayList<>();

    @ManyToOne
    private SiteUser author;

    @ManyToMany
    private Set<SiteUser> voters = new LinkedHashSet<>();

    private int view = 0;

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

    public String getCategoryAsString() {
        switch (this.category) {
            case 0:
                return "질문과답변";
            case 1:
                return "자유게시판";
            case 2:
                return "버그및건의";
            default:
                throw new RuntimeException("올바르지 않은 접근입니다.");
        }
    }


}
