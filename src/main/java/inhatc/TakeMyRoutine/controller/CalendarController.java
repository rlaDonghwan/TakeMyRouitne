package inhatc.TakeMyRoutine.controller;

import inhatc.TakeMyRoutine.domain.Todo;
import inhatc.TakeMyRoutine.domain.User;
import inhatc.TakeMyRoutine.dto.TodoRequest;
import inhatc.TakeMyRoutine.service.TodoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@CrossOrigin(origins = "http://127.0.0.1:8080")
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/home")
public class CalendarController {


    private final TodoService todoService;

    @GetMapping("/calendar")
    public String calendarPage(Model model, HttpSession session) {
        model.addAttribute("loginType", "home");
        model.addAttribute("pageName", "calendar");

        Long userId = (Long) session.getAttribute("userId");

        // 사용자 정보가 없으면 로그인 페이지로 리다이렉트
        if (userId == null) {
            return "redirect:/home/login";
        }

        // 사용자 정보가 있으면 Todo 페이지로 이동
        model.addAttribute("todoRequest", new TodoRequest(userId));


        return "calendar";
    }

    @GetMapping("/calendar/events")
    @ResponseBody
    public List<Map<String, Object>> getEvents(@RequestParam String start, @RequestParam String end, HttpSession session) {
        // 세션에서 userId 가져오기
        Long sessionUserId = (Long) session.getAttribute("userId");

        // 세션이 없거나 userId가 없으면 빈 리스트 반환
        if (sessionUserId == null) {
            return Collections.emptyList();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        ZonedDateTime startDateUtc = ZonedDateTime.parse(start).withZoneSameInstant(ZoneId.of("UTC"));
        ZonedDateTime endDateUtc = ZonedDateTime.parse(end).withZoneSameInstant(ZoneId.of("UTC"));

        ZonedDateTime startDateKST = startDateUtc.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
        ZonedDateTime endDateKST = endDateUtc.withZoneSameInstant(ZoneId.of("Asia/Seoul"));

        // userId를 이용하여 해당 유저의 이벤트만 가져오기
        List<Todo> todos = todoService.getTodosByUserIdAndTime(sessionUserId, startDateKST.toLocalDateTime(), endDateKST.toLocalDateTime());

        return todos.stream()
                .map(todo -> {
                    Map<String, Object> event = new HashMap<>();
                    event.put("id", todo.getId());
                    event.put("title", todo.getTitle());
                    event.put("start", todo.getStartTime().format(formatter));
                    event.put("end", todo.getStartTime().plusHours(1).format(formatter)); // Adjust end time as needed
                    event.put("allDay", false); // Adjust as needed
                    return event;
                })
                .collect(Collectors.toList());
    }


    @PostMapping("/calendar/addEvent")
    @ResponseBody
    public String addEvent(@RequestParam String title,
                           @RequestParam String date,
                           @RequestParam String time,
                           @RequestParam String content,
                           @RequestParam String place,
                           HttpSession session) {
        // Retrieve user ID from the session
        Long userId = (Long) session.getAttribute("userId");

        // 'time' 파라미터를 LocalDateTime으로 파싱
        LocalDateTime dateTime = LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        // 여기서 받은 값들과 user ID를 이용해서 이벤트를 추가하는 로직을 작성
        // 서비스나 레포지토리 클래스를 통해 데이터베이스에 저장하는 방식을 사용할 수 있어

        // 예시: TodoService 클래스를 사용하여 Todo를 생성하는 메소드 호출
        todoService.addEvent(userId, title, dateTime, content, place);

        return "Event added successfully!";
    }

    @PutMapping("/calendar/updateEvent")
    public ResponseEntity<String> updateEvent(@RequestBody Map<String, Object> requestBody, HttpSession session) {
        try {

            // requestBody.get("todoId")에서 반환되는 값의 타입이 String이라고 가정합니다.
            String todoIdString = (String) requestBody.get("todoId");
            Long todoId = Long.valueOf(todoIdString);


            String title = (String) requestBody.get("title");
            String dateStr = (String) requestBody.get("date");
            LocalDateTime updatedDateTime = LocalDateTime.parse(dateStr);

            // 세션에서 userId 가져오기
            Long userId = (Long) session.getAttribute("userId");

            // TodoService를 통해 이벤트 업데이트
            todoService.updateEvent(todoId, title, updatedDateTime, userId);

            return new ResponseEntity<>("Event updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to update event", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/calendar/deleteEvent")
    public ResponseEntity<String> deleteEvent(@RequestParam Long todoId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        try {
            todoService.deleteEvent(todoId, userId);
            return new ResponseEntity<>("Event deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting event: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

