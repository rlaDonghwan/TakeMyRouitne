package inhatc.TakeMyRoutine.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TodoGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "todoGroup", cascade = CascadeType.ALL)
    private List<Todo> todos;

    private String groupName; // 그룹의 이름

    private String category; // 그룹의 카테고리

}
